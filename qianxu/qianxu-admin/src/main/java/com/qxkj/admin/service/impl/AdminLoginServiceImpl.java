package com.qxkj.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.qxkj.admin.filter.TokenComponent;
import com.qxkj.admin.service.AdminLoginService;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.constants.SysConfigConstants;
import com.qxkj.common.constants.SysGroupDataConstants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.log.AdminLoginLog;
import com.qxkj.common.model.system.SystemAdmin;
import com.qxkj.common.model.system.SystemMenu;
import com.qxkj.common.model.system.SystemPermissions;
import com.qxkj.common.request.LoginAdminUpdatePasswordRequest;
import com.qxkj.common.request.LoginAdminUpdateRequest;
import com.qxkj.common.request.SystemAdminLoginRequest;
import com.qxkj.common.response.MenusResponse;
import com.qxkj.common.response.SystemAdminResponse;
import com.qxkj.common.response.SystemGroupDataAdminLoginBannerResponse;
import com.qxkj.common.response.SystemLoginResponse;
import com.qxkj.common.result.CommonResultCode;
import com.qxkj.common.utils.ClientInfoUtil;
import com.qxkj.common.utils.QianxuUtil;
import com.qxkj.common.utils.RedisUtil;
import com.qxkj.common.utils.RequestUtil;
import com.qxkj.common.utils.SecurityUtil;
import com.qxkj.common.vo.LoginUserVo;
import com.qxkj.common.vo.MenuTree;
import com.qxkj.service.service.*;
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
    private AdminLoginLogService adminLoginLogService;

    /**
     * PC登录
     */
    @Override
    public SystemLoginResponse login(SystemAdminLoginRequest systemAdminLoginRequest, String ip) {
        // 图形验证码校验：受「后台登录数字验证码」开关控制（未配置/值为1=开启）
        // 开关关闭时跳过校验，登录页也不再展示验证码输入项
        String captchaSwitch = adminLoginCaptchaSwitch();
        if (StrUtil.isNotBlank(captchaSwitch) && "0".equals(captchaSwitch)) {
            logger.info("后台登录验证码开关已关闭，跳过验证码校验，account = {}", systemAdminLoginRequest.getAccount());
        } else {
            String captchaKey = systemAdminLoginRequest.getCaptchaKey();
            String captchaCode = systemAdminLoginRequest.getCaptchaCode();
            if (StrUtil.isBlank(captchaKey) || StrUtil.isBlank(captchaCode)) {
                recordLoginLog(systemAdminLoginRequest.getAccount(), null, ip, 0, "未输入验证码");
                throw new QianxuException("请输入验证码");
            }
            String captchaRedisKey = StrUtil.format(Constants.ADMIN_IMAGE_CAPTCHA_KEY, captchaKey);
            if (!redisUtil.exists(captchaRedisKey)) {
                recordLoginLog(systemAdminLoginRequest.getAccount(), null, ip, 0, "验证码已过期");
                throw new QianxuException("验证码已过期，请重新获取");
            }
            String redisCode = redisUtil.get(captchaRedisKey);
            // 无论成功失败均一次性失效，防止重放
            redisUtil.delete(captchaRedisKey);
            if (!captchaCode.trim().equalsIgnoreCase(redisCode)) {
                recordLoginLog(systemAdminLoginRequest.getAccount(), null, ip, 0, "验证码错误");
                throw new QianxuException("验证码错误");
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
                throw new QianxuException("用户不存在或密码错误");
            }
            recordLoginLog(systemAdminLoginRequest.getAccount(), null, ip, 0, e.getMessage());
            throw new QianxuException(e.getMessage());
        }catch (QianxuException e){
            accountErrorNumAdd(systemAdminLoginRequest.getAccount());
            recordLoginLog(systemAdminLoginRequest.getAccount(), null, ip, 0, "账号或密码不正确");
            throw new QianxuException("账号或密码不正确");
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
     * 后台登录数字验证码开关：返回 "1" 开启 / "0" 关闭。
     * 配置缺失或读取异常时一律返回 "1"（保持原有「登录必须验证码」的行为，避免误关造成裸奔）。
     */
    private String adminLoginCaptchaSwitch() {
        try {
            String value = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_ADMIN_LOGIN_CAPTCHA_SWITCH);
            return "0".equals(value) ? "0" : "1";
        } catch (Exception e) {
            logger.error("读取后台登录验证码开关失败，按开启处理，{}", e.getMessage());
            return "1";
        }
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
        // 后台登录数字验证码开关，登录页据此决定是否展示验证码输入项
        map.put("captchaSwitch", adminLoginCaptchaSwitch());
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
        String encryptPassword = QianxuUtil.encryptPassword(request.getOldPassword(), systemAdmin.getAccount());
        if (!systemAdmin.getPwd().equals(encryptPassword)) {
            throw new QianxuException(CommonResultCode.VALIDATE_FAILED, "原密码不正确");
        }
        SystemAdmin newAdmin = new SystemAdmin();
        newAdmin.setId(admin.getId());
        String pwd = QianxuUtil.encryptPassword(request.getPassword(), admin.getAccount());
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
