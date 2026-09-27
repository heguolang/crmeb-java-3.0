package com.qxkj.admin.controller;

import com.qxkj.common.model.pingou.PingouLevel;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.PingouLevelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台 —— 拼购等级设置（全局默认分享奖励阶梯）
 * 活动未单独设置分享奖励阶梯时，结算按本等级比例兜底。
 */
@Slf4j
@RestController
@RequestMapping("api/admin/pingou/level")
@Api(tags = "后台——拼购——等级设置")
public class PingouLevelController {

    @Autowired
    private PingouLevelService pingouLevelService;

    /**
     * 等级列表（全量，按 sort 降序）
     */
    @PreAuthorize("hasAuthority('admin:pingou:level:list')")
    @ApiOperation(value = "拼购等级列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<List<PingouLevel>> getList() {
        return CommonResult.success(pingouLevelService.getLevelList());
    }

    /**
     * 新增等级
     */
    @PreAuthorize("hasAuthority('admin:pingou:level:save')")
    @ApiOperation(value = "新增拼购等级")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody PingouLevel level) {
        if (pingouLevelService.saveLevel(level)) {
            return CommonResult.success("新增拼购等级成功");
        }
        return CommonResult.failed("新增拼购等级失败");
    }

    /**
     * 修改等级
     */
    @PreAuthorize("hasAuthority('admin:pingou:level:update')")
    @ApiOperation(value = "修改拼购等级")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestBody PingouLevel level) {
        if (pingouLevelService.updateLevel(level)) {
            return CommonResult.success("修改拼购等级成功");
        }
        return CommonResult.failed("修改拼购等级失败");
    }

    /**
     * 删除等级
     */
    @PreAuthorize("hasAuthority('admin:pingou:level:delete')")
    @ApiOperation(value = "删除拼购等级")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public CommonResult<String> delete(@RequestParam(value = "id") Integer id) {
        if (pingouLevelService.deleteLevel(id)) {
            return CommonResult.success("删除成功");
        }
        return CommonResult.failed("删除失败");
    }
}
