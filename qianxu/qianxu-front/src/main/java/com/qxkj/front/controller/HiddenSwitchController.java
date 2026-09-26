package com.qxkj.front.controller;

import com.qxkj.service.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 模块开关下发（H5 按此隐藏对应功能入口）
 * 只暴露开关布尔值，匿名可访问。
 */
@Api(tags = "模块开关")
@RestController
@RequestMapping("api/front/hidden")
public class HiddenSwitchController {

    @Autowired
    private SystemConfigService systemConfigService;

    @ApiOperation(value = "模块开关状态")
    @RequestMapping(value = "/switches", method = RequestMethod.GET)
    public Map<String, Object> switches() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("teamReward", "1".equals(systemConfigService.getValueByKey("sys_switch_team_reward")));
        result.put("stock", "1".equals(systemConfigService.getValueByKey("sys_switch_stock")));
        result.put("store", "1".equals(systemConfigService.getValueByKey("sys_switch_store")));
        result.put("daili", "1".equals(systemConfigService.getValueByKey("sys_switch_daili")));
        result.put("spread", "1".equals(systemConfigService.getValueByKey("sys_switch_spread")));
        return result;
    }
}
