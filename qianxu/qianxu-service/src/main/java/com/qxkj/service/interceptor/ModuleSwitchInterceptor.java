package com.qxkj.service.interceptor;

import com.qxkj.service.service.SystemConfigService;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 业务模块开关拦截器
 * 按隐藏面板的模块开关拦截对应接口；关闭时返回 404（不暴露接口存在）。
 */
public class ModuleSwitchInterceptor implements HandlerInterceptor {

    private final SystemConfigService systemConfigService;

    /** URI 前缀 -> 开关配置键（按声明顺序匹配） */
    private static final Map<String, String> RULES = new LinkedHashMap<>();

    static {
        RULES.put("/api/admin/stock", "sys_switch_stock");
        RULES.put("/api/front/stock", "sys_switch_stock");
        RULES.put("/api/admin/merchantStore", "sys_switch_store");
        RULES.put("/api/front/merchantStore", "sys_switch_store");
        RULES.put("/api/admin/agent", "sys_switch_daili");
        RULES.put("/api/front/agent", "sys_switch_daili");
        RULES.put("/api/front/spread", "sys_switch_spread");
        RULES.put("/api/front/user/spread", "sys_switch_spread");
        RULES.put("/api/admin/retail", "sys_switch_spread");
        // 营销模块开关
        RULES.put("/api/admin/marketing/coupon", "sys_switch_coupon");
        RULES.put("/api/front/coupons", "sys_switch_coupon");
        RULES.put("/api/admin/user/integral", "sys_switch_integral");
        RULES.put("/api/admin/store/seckill", "sys_switch_seckill");
        RULES.put("/api/front/seckill", "sys_switch_seckill");
        RULES.put("/api/admin/store/bargain", "sys_switch_bargain");
        RULES.put("/api/front/bargain", "sys_switch_bargain");
        RULES.put("/api/admin/store/combination", "sys_switch_combination");
        RULES.put("/api/front/combination", "sys_switch_combination");
    }

    public ModuleSwitchInterceptor(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String key = null;
        for (Map.Entry<String, String> entry : RULES.entrySet()) {
            if (uri.startsWith(entry.getKey())) {
                key = entry.getValue();
                break;
            }
        }
        if (key == null) {
            return true;
        }
        if ("1".equals(systemConfigService.getValueByKey(key))) {
            return true;
        }
        // 模块已关闭：直接 404，不暴露接口
        response.setStatus(404);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":404,\"message\":\"Not Found\"}");
        return false;
    }
}
