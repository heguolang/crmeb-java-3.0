package com.zbkj.front.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.constants.SmsConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.user.User;
import com.zbkj.common.request.LoginMobileRequest;
import com.zbkj.common.request.LoginRequest;
import com.zbkj.common.response.LoginConfigResponse;
import com.zbkj.common.response.LoginResponse;
import com.zbkj.common.token.FrontTokenComponent;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.front.service.LoginService;
import com.zbkj.service.service.SystemConfigService;
import com.zbkj.service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * 移动端登录服务类
 * +----------------------------------------------------------------------
 * | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2024 https://www.crmeb.com All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 * +----------------------------------------------------------------------
 * | Author: CRMEB Team <admin@crmeb.com>
 * +----------------------------------------------------------------------
 */
@Service
public class LoginServiceImpl implements LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private FrontTokenComponent tokenComponent;
    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 账号密码登录
     *
     * @return LoginResponse
     */
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userService.getByPhone(loginRequest.getPhone());
        if (ObjectUtil.isNull(user)) {
            throw new CrmebException("此账号未注册");
        }
        if (!user.getStatus()) {
            throw new CrmebException("此账号被禁用");
        }

        // 校验密码
        String password = CrmebUtil.encryptPassword(loginRequest.getPassword(), loginRequest.getPhone());
        if (!user.getPwd().equals(password)) {
            throw new CrmebException("密码错误");
        }

        LoginResponse loginResponse = new LoginResponse();
        String token = tokenComponent.createToken(user);
        loginResponse.setToken(token);

        //绑定推广关系
        if (loginRequest.getSpreadPid() > 0) {
            bindSpread(user, loginRequest.getSpreadPid());
        }

        // 记录最后一次登录时间
        user.setLastLoginTime(CrmebDateUtil.nowDateTime());
        user.setUpdateTime(DateUtil.date());
        userService.updateById(user);

        loginResponse.setUid(user.getUid());
        loginResponse.setNikeName(user.getNickname());
        loginResponse.setPhone(user.getPhone());
        return loginResponse;
    }

    /**
     * 手机号密码注册（不依赖短信验证码）
     * 说明：沿用 registerPhone 的注册流程（新人优惠券、默认等级、推广关系），
     * 仅把密码由默认手机号改为用户自定义密码。
     *
     * @param loginRequest 手机号、密码、推广人
     * @return LoginResponse 注册成功直接返回登录态
     */
    @Override
    public LoginResponse register(LoginRequest loginRequest) {
        String phone = loginRequest.getPhone();
        String password = loginRequest.getPassword();
        if (StrUtil.isBlank(password)) {
            throw new CrmebException("密码不能为空");
        }

        // 手机号已注册则不允许重复注册
        User existUser = userService.getByPhone(phone);
        if (ObjectUtil.isNotNull(existUser)) {
            throw new CrmebException("该手机号已注册，请直接登录");
        }

        Integer spreadPid = Optional.ofNullable(loginRequest.getSpreadPid()).orElse(0);
        if (spreadPid > 0) {
            User spreadUser = userService.getById(spreadPid);
            if (ObjectUtil.isNull(spreadUser) || !Boolean.TRUE.equals(spreadUser.getStatus())) {
                throw new CrmebException("推荐人ID不存在或已禁用");
            }
            // 预检绑定条件，避免注册成功却静默未绑定
            User probe = new User();
            probe.setUid(null);
            probe.setSpreadUid(0);
            if (!userService.checkBingSpread(probe, spreadPid, "new")) {
                String isOpen = systemConfigService.getValueByKey(Constants.CONFIG_KEY_STORE_BROKERAGE_IS_OPEN);
                if (StrUtil.isBlank(isOpen) || "0".equals(isOpen)) {
                    throw new CrmebException("分销功能未开启，无法绑定推荐人，请先在后台开启分销");
                }
                String brokerageModel = systemConfigService.getValueByKey(Constants.CONFIG_KEY_STORE_BROKERAGE_MODEL);
                if (!"2".equals(brokerageModel) && !Boolean.TRUE.equals(spreadUser.getIsPromoter())) {
                    throw new CrmebException("推荐人不是推广员，当前为指定分销模式，无法绑定");
                }
                throw new CrmebException("推荐人绑定失败，请检查推荐人ID或分销设置");
            }
        }

        // 复用注册主流程创建用户（含新人券/默认等级/推广关系）
        User user = userService.registerPhone(phone, spreadPid);
        if (ObjectUtil.isNull(user)) {
            throw new CrmebException("注册失败，请稍后重试");
        }

        // 覆盖为自定义密码（registerPhone 默认用的是手机号加密）
        user.setPwd(CrmebUtil.encryptPassword(password, phone));
        user.setUpdateTime(DateUtil.date());
        boolean update = userService.updateById(user);
        if (!update) {
            logger.error("注册设置密码失败, uid = " + user.getUid());
            throw new CrmebException("注册失败，请稍后重试");
        }

        // 直接返回登录态，前端注册后免登录进入
        LoginResponse loginResponse = new LoginResponse();
        String token = tokenComponent.createToken(user);
        loginResponse.setToken(token);
        loginResponse.setUid(user.getUid());
        loginResponse.setNikeName(user.getNickname());
        loginResponse.setPhone(user.getPhone());
        return loginResponse;
    }

    /**
     * 手机号验证码登录
     *
     * @param loginRequest 登录请求信息
     * @return LoginResponse
     */
    @Override
    public LoginResponse phoneLogin(LoginMobileRequest loginRequest) {
        //检测验证码
        checkValidateCode(loginRequest.getPhone(), loginRequest.getCaptcha());
        Integer spreadPid = Optional.ofNullable(loginRequest.getSpreadPid()).orElse(0);
        //查询手机号信息
        User user = userService.getByPhone(loginRequest.getPhone());
        if (ObjectUtil.isNull(user)) {// 此用户不存在，走新用户注册流程
            user = userService.registerPhone(loginRequest.getPhone(), spreadPid);
        } else {
            if (!user.getStatus()) {
                throw new CrmebException("当前账户已禁用，请联系管理员！");
            }
            if (user.getSpreadUid().equals(0) && spreadPid > 0) {
                // 绑定推广关系
                bindSpread(user, spreadPid);
            }
            // 记录最后一次登录时间
            user.setLastLoginTime(CrmebDateUtil.nowDateTime());
            user.setUpdateTime(DateUtil.date());
            boolean b = userService.updateById(user);
            if (!b) {
                logger.error("用户登录时，记录最后一次登录时间出错,uid = " + user.getUid());
            }
        }

        //生成token
        LoginResponse loginResponse = new LoginResponse();
        String token = tokenComponent.createToken(user);
        loginResponse.setToken(token);
        loginResponse.setUid(user.getUid());
        loginResponse.setNikeName(user.getNickname());
        loginResponse.setPhone(user.getPhone());
        return loginResponse;
    }

    /**
     * 检测手机验证码
     *
     * @param phone 手机号
     * @param code  验证码
     */
    private void checkValidateCode(String phone, String code) {
        Object validateCode = redisUtil.get(SmsConstants.SMS_VALIDATE_PHONE + phone);
        if (ObjectUtil.isNull(validateCode)) {
            throw new CrmebException("验证码已过期");
        }
        if (!validateCode.toString().equals(code)) {
            throw new CrmebException("验证码错误");
        }
        //删除验证码
        redisUtil.delete(SmsConstants.SMS_VALIDATE_PHONE + phone);
    }

    /**
     * 绑定分销关系
     *
     * @param user      User 用户user类
     * @param spreadUid Integer 推广人id
     * @return Boolean
     * 1.判断分销功能是否启用
     * 2.判断分销模式
     * 3.根据不同的分销模式校验
     * 4.指定分销，只有分销员才可以分销，需要spreadUid是推广员才可以绑定
     * 5.满额分销，同上
     * 6.人人分销，可以直接绑定
     */
    @Override
    public Boolean bindSpread(User user, Integer spreadUid) {
        Boolean checkBingSpread = userService.checkBingSpread(user, spreadUid, "old");
        if (!checkBingSpread) return false;

        user.setSpreadUid(spreadUid);
        user.setSpreadTime(CrmebDateUtil.nowDateTime());
        user.setUpdateTime(DateUtil.date());

        Boolean execute = transactionTemplate.execute(e -> {
            userService.updateById(user);
            userService.updateSpreadCountByUid(spreadUid, "add");
            return Boolean.TRUE;
        });
        if (!execute) {
            logger.error(StrUtil.format("绑定推广人时出错，userUid = {}, spreadUid = {}", user.getUid(), spreadUid));
        }
        return execute;
    }

    /**
     * 推出登录
     *
     * @param request HttpServletRequest
     */
    @Override
    public void loginOut(HttpServletRequest request) {
        tokenComponent.logout(request);
    }

    /**
     * 校验token是否有效
     *
     * @return true 有效， false 无效
     */
    @Override
    public Boolean tokenIsExist() {
        Integer userId = userService.getUserId();
        return userId > 0;
    }

    /**
     * 获取登录配置
     */
    @Override
    public LoginConfigResponse getLoginConfig() {
        String routinePhoneVerification = systemConfigService.getValueByKey(Constants.WECHAT_ROUTINE_PHONE_VERIFICATION);
        String publicLoginType = systemConfigService.getValueByKey(Constants.WECHAT_PUBLIC_LOGIN_TYPE);
        String mobileLoginLogo = systemConfigService.getValueByKey(Constants.CONFIG_KEY_MOBILE_LOGIN_LOGO);
        String siteName = systemConfigService.getValueByKey(Constants.CONFIG_KEY_SITE_NAME);
        LoginConfigResponse response = new LoginConfigResponse();
        response.setPublicLoginType(publicLoginType);
        response.setRoutinePhoneVerification(routinePhoneVerification);
        response.setMobileLoginLogo(mobileLoginLogo);
        response.setSiteName(siteName);
        return response;
    }
}
