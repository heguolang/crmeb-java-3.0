package com.zbkj.admin.controller;

import com.zbkj.common.model.system.DistributorLevel;
import com.zbkj.common.request.DistributorLevelRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.DistributorLevelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分销商等级 前端控制器
 * 独立于会员等级的分销等级体系，含返佣比例与升级条件配置
 */
@Slf4j
@RestController
@RequestMapping("api/admin/distributor/level")
@Api(tags = "分销 -- 分销商等级")
public class DistributorLevelController {

    @Autowired
    private DistributorLevelService distributorLevelService;

    /**
     * 分销商等级列表
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:brokerage:list')")
    @ApiOperation(value = "分销商等级列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<List<DistributorLevel>> getList() {
        return CommonResult.success(distributorLevelService.getList());
    }

    /**
     * 分销商等级详情
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:brokerage:list')")
    @ApiOperation(value = "分销商等级详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    public CommonResult<DistributorLevel> info(@PathVariable(value = "id") Integer id) {
        return CommonResult.success(distributorLevelService.getLevelInfo(id));
    }

    /**
     * 新增分销商等级
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:brokerage:update')")
    @ApiOperation(value = "新增分销商等级")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated DistributorLevelRequest request) {
        if (distributorLevelService.saveLevel(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 更新分销商等级
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:brokerage:update')")
    @ApiOperation(value = "更新分销商等级")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult<String> update(@PathVariable(value = "id") Integer id,
                                       @RequestBody @Validated DistributorLevelRequest request) {
        if (distributorLevelService.updateLevel(id, request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 删除分销商等级
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:brokerage:delete')")
    @ApiOperation(value = "删除分销商等级")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public CommonResult<String> delete(@PathVariable(value = "id") Integer id) {
        if (distributorLevelService.deleteLevel(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 启用/隐藏分销商等级
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:brokerage:update')")
    @ApiOperation(value = "启用/隐藏分销商等级")
    @RequestMapping(value = "/use/{id}/{isShow}", method = RequestMethod.POST)
    public CommonResult<String> use(@PathVariable(value = "id") Integer id,
                                    @PathVariable(value = "isShow") Boolean isShow) {
        if (distributorLevelService.updateShow(id, isShow)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 重算指定用户的分销商等级统计并重新判定等级（修复历史数据用）
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:brokerage:update')")
    @ApiOperation(value = "重算用户分销商等级")
    @RequestMapping(value = "/recalc/{uid}", method = RequestMethod.POST)
    public CommonResult<String> recalc(@PathVariable(value = "uid") Integer uid) {
        if (distributorLevelService.recalcUser(uid)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }
}
