package com.qxkj.front.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.constants.CouponConstants;
import com.qxkj.common.constants.RegularConstants;
import com.qxkj.common.constants.SmsConstants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.request.IosBindingPhoneRequest;
import com.qxkj.common.request.IosLoginRequest;
import com.qxkj.common.request.WxBindingPhoneRequest;
import com.qxkj.common.response.LoginResponse;
import com.qxkj.common.token.FrontTokenComponent;
import com.qxkj.common.utils.CommonUtil;
import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.common.utils.RedisUtil;
import com.qxkj.common.model.coupon.StoreCoupon;
import com.qxkj.common.model.coupon.StoreCouponUser;
import com.qxkj.common.model.user.User;
import com.qxkj.common.model.user.UserToken;
import com.qxkj.common.request.RegisterThirdUserRequest;
import com.qxkj.front.service.IosService;
import com.qxkj.service.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;

/**
 *
 *  +----------------------------------------------------------------------
 *  | 黔序商城 [ 黔序科技，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
 *  +----------------------------------------------------------------------
 *  | Author: 贵州黔序科技有限公司
 *  +----------------------------------------------------------------------
 */
@Service
public class IosServiceImpl implements IosService {

    private static final Logger logger = LoggerFactory.getLogger(IosServiceImpl.class);

    @Autowired
    private UserTokenService userTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private StoreCouponService storeCouponService;

    @Autowired
    private StoreCouponUserService storeCouponUserService;

    @Autowired
    private FrontTokenComponent tokenComponent;

    /**
     * ios登录
     * @param loginRequest 登录请求对象
     */
    @Override
    public LoginResponse login(IosLoginRequest loginRequest) {
        /*注释部分为ios identityToken校验
        try {
            Map<String, String> map = new HashMap<String, String>();
            //验证identityToken
            if (!AppleUtil.verify(loginRequest.getIdentityToken())) {
                throw new QianxuException("Ios授权验证失败");
            }
            //对identityToken解码
            JSONObject json = AppleUtil.parserIdentityToken(loginRequest.getIdentityToken());
            if (json == null) {
                throw new QianxuException("Ios授权解码失败");
            }
        } catch (Exception e) {
            logger.error("app iosLogin error:" + e.getMessage(), e);
        }*/
        // 检测是否存在
        logger.info("ios ================ 登录 请求参数：loginRequest = " + loginRequest);
        UserToken userToken = userTokenService.getByOpenidAndType(loginRequest.getOpenId(), Constants.THIRD_LOGIN_TOKEN_TYPE_IOS);
        LoginResponse loginResponse = new LoginResponse();
        if (ObjectUtil.isNotNull(userToken)) {// 已存在，正常登录
            User user = userService.getById(userToken.getUid());
            if (!user.getStatus()) {
                throw new QianxuException("当前账户已禁用，请联系管理员！");
            }

            // 记录最后一次登录时间
            user.setLastLoginTime(QianxuDateUtil.nowDateTime());
            user.setUpdateTime(DateUtil.date());
            Boolean execute = transactionTemplate.execute(e -> {
                userService.updateById(user);
                return Boolean.TRUE;
            });
            if (!execute) {
                logger.error(StrUtil.format("App记录用户最后一次登陆时间失败，uid={}", user.getUid()));
            }
            try {
                String token = tokenComponent.createToken(user);
                loginResponse.setToken(token);
            } catch (Exception e) {
                logger.error(StrUtil.format("App登录生成token失败，uid={}", user.getUid()));
                e.printStackTrace();
            }
            loginResponse.setType("login");
            loginResponse.setUid(user.getUid());
            loginResponse.setNikeName(user.getNickname());
            loginResponse.setPhone(user.getPhone());
            return loginResponse;
        }
        // 没有用户Ios直接创建新用户
        User user = new User();
        String randomString = RandomUtil.randomString(11);
        user.setPhone("");
        user.setAccount(randomString);
        user.setSpreadUid(0);
        user.setSpreadTime(QianxuDateUtil.nowDateTime());
        user.setPwd("123");
        user.setUserType("ios");
        user.setNickname(CommonUtil.createNickName(randomString));
        user.setAvatar(systemConfigService.getValueByKey(Constants.USER_DEFAULT_AVATAR_CONFIG_KEY));
        Boolean execute = transactionTemplate.execute(e -> {
            userService.save(user);
            // 赠送新人券
            giveNewPeopleCoupon(user.getUid());
            userTokenService.bind(loginRequest.getOpenId(), Constants.THIRD_LOGIN_TOKEN_TYPE_IOS, user.getUid());
            return Boolean.TRUE;
        });
        if (!execute) {
            logger.error("App用户注册生成失败，nickName = " + user.getNickname());
        }
        try {
            String token = tokenComponent.createToken(user);
            loginResponse.setToken(token);
        } catch (Exception e) {
            logger.error(StrUtil.format("App绑定手机号，自动登录生成token失败，uid={}", user.getUid()));
            e.printStackTrace();
        }
        loginResponse.setType("login");
        loginResponse.setUid(user.getUid());
        loginResponse.setNikeName(user.getNickname());
        loginResponse.setPhone(user.getPhone());
        return loginResponse;
    }

    /**
     * IOS绑定手机号
     * @param request 请求参数
     * @return 登录信息
     */
    @Override
    public LoginResponse registerBindingPhone(WxBindingPhoneRequest request) {
        logger.info("ios ================ 绑定手机号 请求参数：request = " + request);
        checkBindingPhone(request);

        // 进入创建用户绑定手机号流程
        Object o = redisUtil.get(request.getKey());
        if (ObjectUtil.isNull(o)) {
            throw new QianxuException("用户缓存已过期，请清除缓存重新登录");
        }
        RegisterThirdUserRequest registerThirdUserRequest = JSONObject.parseObject(o.toString(), RegisterThirdUserRequest.class);

        boolean isNew = true;

        User user = userService.getByPhone(request.getPhone());
        if (ObjectUtil.isNull(user)) {
            user.setPhone(request.getPhone());
            user.setAccount(request.getPhone());
            user.setSpreadUid(0);
            user.setSpreadTime(QianxuDateUtil.nowDateTime());
            user.setPwd(CommonUtil.createPwd(request.getPhone()));
            user.setUserType(registerThirdUserRequest.getType());
            user.setNickname(registerThirdUserRequest.getNickName());
            user.setAvatar(systemConfigService.getValueByKey(Constants.USER_DEFAULT_AVATAR_CONFIG_KEY));
        } else {// 已有账户，关联到之前得账户即可
            // 查询是否用对应得token
            int type = Constants.THIRD_LOGIN_TOKEN_TYPE_IOS;

            UserToken userToken = userTokenService.getTokenByUserId(user.getUid(), type);
            if (ObjectUtil.isNotNull(userToken)) {
                throw new QianxuException("该手机号已被注册");
            }
            isNew = false;
        }

        boolean finalIsNew = isNew;
        Boolean execute = transactionTemplate.execute(e -> {
            if (finalIsNew) {// 新用户
                // 赠送新人券
                giveNewPeopleCoupon(user.getUid());
            }
            userTokenService.bind(registerThirdUserRequest.getOpenId(), Constants.THIRD_LOGIN_TOKEN_TYPE_IOS, user.getUid());
            return Boolean.TRUE;
        });
        if (!execute) {
            logger.error("App用户注册生成失败，nickName = " + registerThirdUserRequest.getNickName());
        }
        LoginResponse loginResponse = new LoginResponse();
        try {
            String token = tokenComponent.createToken(user);
            loginResponse.setToken(token);
        } catch (Exception e) {
            logger.error(StrUtil.format("App绑定手机号，自动登录生成token失败，uid={}", user.getUid()));
            e.printStackTrace();
        }
        loginResponse.setType("login");
        return loginResponse;
    }

    /**
     * ios绑定手机号（登录后）
     * @param request 请求对象
     * @return 是否绑定
     */
    @Override
    public Boolean bindingPhone(IosBindingPhoneRequest request) {
        logger.info("ios ================ 绑定手机号 请求参数：request = " + request);
        if (StrUtil.isBlank(request.getCaptcha())) {
            throw new QianxuException("验证码不能为空");
        }
        boolean matchPhone = ReUtil.isMatch(RegularConstants.PHONE_TWO, request.getPhone());
        if (!matchPhone) {
            throw new QianxuException("手机号格式错误，请输入正确得手机号");
        }
        boolean match = ReUtil.isMatch(RegularConstants.VALIDATE_CODE_NUM_SIX, request.getCaptcha());
        if (!match) {
            throw new QianxuException("验证码格式错误，验证码必须为6位数字");
        }
        checkValidateCode(request.getPhone(), request.getCaptcha());

        User tempUser = userService.getByPhone(request.getPhone());
        if (ObjectUtil.isNotNull(tempUser)) {
            throw new QianxuException("手机号已注册");
        }

        // 可以绑定
        Integer userId = userService.getUserIdException();
        User user = userService.getById(userId);
        user.setPhone(request.getPhone());
        user.setPwd(CommonUtil.createPwd(request.getPhone()));
        user.setAccount(request.getPhone());
        user.setUpdateTime(DateUtil.date());
        return userService.updateById(user);
    }

    /**
     * 绑定手机号数据校验
     */
    private void checkBindingPhone(WxBindingPhoneRequest request) {
        if (!request.getType().equals("ios")) {
            throw new QianxuException("未知的用户类型");
        }
        if (StrUtil.isBlank(request.getCaptcha())) {
            throw new QianxuException("验证码不能为空");
        }
        boolean matchPhone = ReUtil.isMatch(RegularConstants.PHONE_TWO, request.getPhone());
        if (!matchPhone) {
            throw new QianxuException("手机号格式错误，请输入正确得手机号");
        }
        // 公众号用户校验验证码
        boolean match = ReUtil.isMatch(RegularConstants.VALIDATE_CODE_NUM_SIX, request.getCaptcha());
        if (!match) {
            throw new QianxuException("验证码格式错误，验证码必须为6位数字");
        }
        checkValidateCode(request.getPhone(), request.getCaptcha());
    }

    /**
     * 检测手机验证码
     * @param phone 手机号
     * @param code 验证码
     */
    private void checkValidateCode(String phone, String code) {
        Object validateCode = redisUtil.get(SmsConstants.SMS_VALIDATE_PHONE + phone);
        if (validateCode == null) {
            throw new QianxuException("验证码已过期");
        }
        if (!validateCode.toString().equals(code)) {
            throw new QianxuException("验证码错误");
        }
        //删除验证码
        redisUtil.delete(SmsConstants.SMS_VALIDATE_PHONE + phone);
    }

    /**
     * 赠送新人券
     * @param uid 用户uid
     */
    private void giveNewPeopleCoupon(Integer uid) {
        // 查询是否有新人注册赠送优惠券
        List<StoreCouponUser> couponUserList = CollUtil.newArrayList();
        List<StoreCoupon> couponList = storeCouponService.findRegisterList();
        if (CollUtil.isNotEmpty(couponList)) {
            couponList.forEach(storeCoupon -> {
                //是否有固定的使用时间
                if (!storeCoupon.getIsFixedTime()) {
                    String endTime = QianxuDateUtil.addDay(QianxuDateUtil.nowDate(Constants.DATE_FORMAT), storeCoupon.getDay(), Constants.DATE_FORMAT);
                    storeCoupon.setUseEndTime(QianxuDateUtil.strToDate(endTime, Constants.DATE_FORMAT));
                    storeCoupon.setUseStartTime(QianxuDateUtil.nowDateTimeReturnDate(Constants.DATE_FORMAT));
                }

                StoreCouponUser storeCouponUser = new StoreCouponUser();
                storeCouponUser.setCouponId(storeCoupon.getId());
                storeCouponUser.setName(storeCoupon.getName());
                storeCouponUser.setMoney(storeCoupon.getMoney());
                storeCouponUser.setMinPrice(storeCoupon.getMinPrice());
                storeCouponUser.setUseType(storeCoupon.getUseType());
                if (storeCoupon.getIsFixedTime()) {// 使用固定时间
                    storeCouponUser.setStartTime(storeCoupon.getUseStartTime());
                    storeCouponUser.setEndTime(storeCoupon.getUseEndTime());
                } else {// 没有固定使用时间
                    Date nowDate = QianxuDateUtil.nowDateTime();
                    storeCouponUser.setStartTime(nowDate);
                    DateTime dateTime = cn.hutool.core.date.DateUtil.offsetDay(nowDate, storeCoupon.getDay());
                    storeCouponUser.setEndTime(dateTime);
                }
                storeCouponUser.setType("register");
                if (storeCoupon.getUseType() > 1) {
                    storeCouponUser.setPrimaryKey(storeCoupon.getPrimaryKey());
                }
                storeCouponUser.setType(CouponConstants.STORE_COUPON_USER_TYPE_REGISTER);
                couponUserList.add(storeCouponUser);
            });
        }

        // 赠送客户优惠券
        if (CollUtil.isNotEmpty(couponUserList)) {
            couponUserList.forEach(couponUser -> couponUser.setUid(uid));
            storeCouponUserService.saveBatch(couponUserList);
            couponList.forEach(coupon -> storeCouponService.deduction(coupon.getId(), 1, coupon.getIsLimited()));
        }
    }
}
