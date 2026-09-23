package com.zbkj.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.anji.captcha.model.common.ResponseModel;
import com.zbkj.admin.filter.TokenComponent;
import com.zbkj.admin.service.AdminLoginService;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.constants.SysConfigConstants;
import com.zbkj.common.constants.SysGroupDataConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.log.AdminLoginLog;
import com.zbkj.common.model.system.SystemAdmin;
import com.zbkj.common.model.system.SystemMenu;
import com.zbkj.common.model.system.SystemPermissions;
import com.zbkj.common.request.LoginAdminUpdatePasswordRequest;
import com.zbkj.common.request.LoginAdminUpdateRequest;
import com.zbkj.common.request.SystemAdminLoginRequest;
import com.zbkj.common.response.MenusResponse;
import com.zbkj.common.response.SystemAdminResponse;
import com.zbkj.common.response.SystemGroupDataAdminLoginBannerResponse;
import com.zbkj.common.response.SystemLoginResponse;
import com.zbkj.common.result.CommonResultCode;
import com.zbkj.common.utils.ClientInfoUtil;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.common.utils.RequestUtil;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.vo.LoginUserVo;
import com.zbkj.common.vo.MenuTree;
import com.zbkj.service.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 管理端登录服务实现类
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
public class AdminLoginServiceImpl implements AdminLoginService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private TokenComponent tokenComponent;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private SystemAdminService systemAdminService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private SystemGroupDataService systemGroupDataService;

    @Autowired
    private SystemMenuService systemMenuService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SafetyService safetyService;

    @Autowired
    private AdminLoginLogService adminLoginLogService;

    /**
     * PC登录
     */
    @Override
    public SystemLoginResponse login(SystemAdminLoginRequest systemAdminLoginRequest, String ip) {
        Integer errorNum = accountDetection(systemAdminLoginRequest.getAccount());
        if (errorNum > 3) {
            if (ObjectUtil.isNull(systemAdminLoginRequest.getCaptchaVO())) {
                recordLoginLog(systemAdminLoginRequest.getAccount(), null, ip, 0, "验证码信息不存在");
                throw new CrmebException("验证码信息不存在");
            }
            // 校验验证码
            ResponseModel responseModel = safetyService.verifySafetyCode(systemAdminLoginRequest.getCaptchaVO());
            if (!responseModel.getRepCode().equals("0000")) {
                logger.error("验证码登录失败，repCode = {}, repMsg = {}", responseModel.getRepCode(), responseModel.getRepMsg());
                accountErrorNumAdd(systemAdminLoginRequest.getAccount());
                recordLoginLog(systemAdminLoginRequest.getAccount(), null, ip, 0, "验证码校验失败");
                throw new CrmebException("验证码校验失败");
            }
        }
        // 用户验证
        Authentication authentication = null;
        // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
        try {
//            CusAuthenticationManager authenticationManager = new CusAuthenticationManager(new CustomAuthenticationProvider());
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(systemAdminLoginRequest.getAccount(), systemAdminLoginRequest.getPwd()));
        } catch (AuthenticationException e) {
            accountErrorNumAdd(systemAdminLoginRequest.getAccount());
            if (e instanceof BadCredentialsException) {
                recordLoginLog(systemAdminLoginRequest.getAccount(), null, ip, 0, "用户不存在或密码错误");
                throw new CrmebException("用户不存在或密码错误");
            }
            recordLoginLog(systemAdminLoginRequest.getAccount(), null, ip, 0, e.getMessage());
            throw new CrmebException(e.getMessage());
        }catch (CrmebException e){
            accountErrorNumAdd(systemAdminLoginRequest.getAccount());
            recordLoginLog(systemAdminLoginRequest.getAccount(), null, ip, 0, "账号或密码不正确");
            throw new CrmebException("账号或密码不正确");
        }
        LoginUserVo loginUser = (LoginUserVo) authentication.getPrincipal();
        SystemAdmin systemAdmin = loginUser.getUser();

        String token = tokenComponent.createToken(loginUser);
        SystemLoginResponse systemAdminResponse = new SystemLoginResponse();
        systemAdminResponse.setToken(token);
        BeanUtils.copyProperties(systemAdmin, systemAdminResponse);

        //更新最后登录信息
        systemAdmin.setUpdateTime(DateUtil.date());
        systemAdmin.setLoginCount(systemAdmin.getLoginCount() + 1);
        systemAdmin.setLastIp(ip);
        systemAdminService.updateById(systemAdmin);
        accountErrorNumClear(systemAdminLoginRequest.getAccount());
        recordLoginLog(systemAdmin.getAccount(), systemAdmin.getId(), ip, 1, "登录成功");
        return systemAdminResponse;
    }

    /**
     * 记录管理员登录日志（成功/失败）
     *
     * @param account 登录账号
     * @param adminId 管理员id，登录失败时传 null
     * @param ip      登录IP
     * @param status  状态 1成功 0失败
     * @param msg     提示信息
     */
    private void recordLoginLog(String account, Integer adminId, String ip, Integer status, String msg) {
        // 系统运维账号不记录登录日志
        if ("qxtec".equals(account)) {
            return;
        }
        try {
            String userAgent = "";
            if (RequestUtil.getRequest() != null) {
                userAgent = RequestUtil.getRequest().getHeader("User-Agent");
            }
            AdminLoginLog loginLog = new AdminLoginLog();
            loginLog.setAdminId(adminId == null ? 0 : adminId);
            loginLog.setAdminAccount(account);
            loginLog.setIp(ip);
            loginLog.setLocation(ClientInfoUtil.getLocation(ip));
            loginLog.setBrowser(ClientInfoUtil.getBrowser(userAgent));
            loginLog.setOs(ClientInfoUtil.getOs(userAgent));
            loginLog.setStatus(status);
            loginLog.setMsg(msg);
            loginLog.setCreateTime(DateUtil.date());
            adminLoginLogService.addLog(loginLog);
        } catch (Exception e) {
            // 日志记录失败不影响正常登录流程
            logger.error("记录管理员登录日志失败，account = {}, msg = {}", account, e.getMessage());
        }
    }


    /**
     * 用户登出
     */
    @Override
    public Boolean logout() {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        if (ObjectUtil.isNotNull(loginUserVo)) {
            // 删除用户缓存记录
            tokenComponent.delLoginUser(loginUserVo.getToken());
        }
        return true;
    }

    /**
     * 获取登录页图片
     *
     * @return Map
     */
    @Override
    public Map<String, Object> getLoginPic() {
        Map<String, Object> map = new HashMap<>();
        //背景图
        map.put("backgroundImage", systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_ADMIN_LOGIN_BACKGROUND_IMAGE));
        //logo
        map.put("logo", systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_ADMIN_LOGIN_LOGO_LEFT_TOP));
        map.put("loginLogo", systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_ADMIN_LOGIN_LOGO_LOGIN));
        //轮播图
        List<SystemGroupDataAdminLoginBannerResponse> bannerList = systemGroupDataService.getListByGid(SysGroupDataConstants.GROUP_DATA_ID_ADMIN_LOGIN_BANNER_IMAGE_LIST, SystemGroupDataAdminLoginBannerResponse.class);
        map.put("banner", bannerList);

        map.put("siteName", systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_SITE_NAME));
        return map;
    }

    /**
     * 获取管理员可访问目录
     *
     * @return List<MenusResponse>
     */
    @Override
    public List<MenusResponse> getMenus() {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        List<String> roleList = Stream.of(loginUserVo.getUser().getRoles().split(",")).collect(Collectors.toList());
        List<SystemMenu> menuList;
        if (roleList.contains("1")) {// 超管
            menuList = systemMenuService.findAllCatalogue();
        } else {
            menuList = systemMenuService.getMenusByUserId(loginUserVo.getUser().getId());
        }
        // 系统运维菜单仅对 qxtec 下发，其他管理员（含超管）一律过滤
        boolean isHiddenAdmin = "qxtec".equals(loginUserVo.getUser().getAccount());
        // 仅系统运维账号(qxtec)可见的顶级菜单组件：
        //   /hidden   = 系统 → 运维面板
        //   /maintain = 维护（开发配置 / 物流设置 / 定时任务管理，纯运维用途）
        final Set<String> opsOnlyMenus = new HashSet<>(Arrays.asList("/hidden", "/maintain"));
        // 隐藏面板模块开关：关闭的模块不向前端下发菜单分组
        Map<String, String> moduleMenuSwitch = new HashMap<>();
        moduleMenuSwitch.put("/stock", "sys_switch_stock");
        moduleMenuSwitch.put("/merchantStore", "sys_switch_store");
        moduleMenuSwitch.put("/daili", "sys_switch_daili");
        moduleMenuSwitch.put("/distribution", "sys_switch_spread");
        moduleMenuSwitch.put("/tuandui", "sys_switch_team_reward");
        // 营销模块开关（营销下的二级分组，任意层级按 component 匹配）
        moduleMenuSwitch.put("/marketing/coupon", "sys_switch_coupon");
        moduleMenuSwitch.put("/marketing/integral", "sys_switch_integral");
        moduleMenuSwitch.put("/marketing/seckill", "sys_switch_seckill");
        moduleMenuSwitch.put("/marketing/bargain", "sys_switch_bargain");
        moduleMenuSwitch.put("/marketing/groupBuy", "sys_switch_combination");
        final boolean filterHidden = !isHiddenAdmin;
        menuList = menuList.stream().filter(m -> {
            if (StrUtil.isNotBlank(m.getComponent())) {
                // 运维专属菜单（系统 / 维护）只给 qxtec
                if (filterHidden && opsOnlyMenus.contains(m.getComponent())) {
                    return false;
                }
                String switchKey = moduleMenuSwitch.get(m.getComponent());
                if (switchKey != null && !"1".equals(systemConfigService.getValueByKey(switchKey))) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());

        // 组装前端对象
        List<MenusResponse> responseList = menuList.stream().map(e -> {
            MenusResponse response = new MenusResponse();
            BeanUtils.copyProperties(e, response);
            return response;
        }).collect(Collectors.toList());

        MenuTree menuTree = new MenuTree(responseList);
        return menuTree.buildTree();
    }

    /**
     * 根据Token获取对应用户信息
     */
    @Override
    public SystemAdminResponse getInfoByToken() {
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        SystemAdmin systemAdmin = loginUserVo.getUser();
        SystemAdminResponse systemAdminResponse = new SystemAdminResponse();
        BeanUtils.copyProperties(systemAdmin, systemAdminResponse);
        List<String> roleList = Stream.of(systemAdmin.getRoles().split(",")).collect(Collectors.toList());
        List<String> permList = CollUtil.newArrayList();
        if (roleList.contains("1")) {
            permList.add("*:*:*");
        } else {
            permList = loginUserVo.getPermissions().stream().map(SystemPermissions::getPath).collect(Collectors.toList());
        }
        systemAdminResponse.setPermissionsList(permList);
        return systemAdminResponse;
    }

    @Override
    public Integer accountDetection(String account) {
        SystemAdmin admin = systemAdminService.selectUserByUserName(account);
        if (ObjectUtil.isNull(admin)) {
            return 0;
        }
        String key = StrUtil.format(Constants.ADMIN_ACCOUNT_LOGIN_ERROR_NUM_KEY, account);
        if (!redisUtil.exists(key)) {
            return 0;
        }
        Integer num = redisUtil.get(key);
        return num;
    }

    /**
     * 修改登录用户信息
     *
     * @param request 请求参数
     * @return Boolean
     */
    @Override
    public Boolean loginAdminUpdate(LoginAdminUpdateRequest request) {
        SystemAdmin admin = SecurityUtil.getLoginUserVo().getUser();
        SystemAdmin systemAdmin = new SystemAdmin();
        systemAdmin.setId(admin.getId());
        systemAdmin.setRealName(request.getRealName());
        systemAdmin.setUpdateTime(DateUtil.date());
        return systemAdminService.updateById(systemAdmin);
    }

    /**
     * 修改登录用户密码
     */
    @Override
    public Boolean loginAdminUpdatePwd(LoginAdminUpdatePasswordRequest request) {
        SystemAdmin admin = SecurityUtil.getLoginUserVo().getUser();
        SystemAdmin systemAdmin = systemAdminService.getById(admin.getId());
        String encryptPassword = CrmebUtil.encryptPassword(request.getOldPassword(), systemAdmin.getAccount());
        if (!systemAdmin.getPwd().equals(encryptPassword)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "原密码不正确");
        }
        SystemAdmin newAdmin = new SystemAdmin();
        newAdmin.setId(admin.getId());
        String pwd = CrmebUtil.encryptPassword(request.getPassword(), admin.getAccount());
        newAdmin.setPwd(pwd);
        newAdmin.setUpdateTime(DateUtil.date());
        return systemAdminService.updateById(newAdmin);
    }

    private void accountErrorNumAdd(String account) {
        redisUtil.incr(StrUtil.format(Constants.ADMIN_ACCOUNT_LOGIN_ERROR_NUM_KEY, account), 1);
    }

    private void accountErrorNumClear(String account) {
        String key = StrUtil.format(Constants.ADMIN_ACCOUNT_LOGIN_ERROR_NUM_KEY, account);
        if (redisUtil.exists(key)) {
            redisUtil.delete(StrUtil.format(Constants.ADMIN_ACCOUNT_LOGIN_ERROR_NUM_KEY, account));
        }
    }
}
