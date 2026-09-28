package com.qxkj.admin.controller;

import com.qxkj.common.model.dividend.DividendDetail;
import com.qxkj.common.model.dividend.DividendLevel;
import com.qxkj.common.model.dividend.DividendRecord;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.DividendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 后台 —— 全球分红
 */
@Slf4j
@RestController
@RequestMapping("api/admin/dividend")
@Api(tags = "后台——全球分红")
public class DividendController {

    @Autowired
    private DividendService dividendService;

    /**
     * 分红设置 + 各等级分红比例
     */
    @PreAuthorize("hasAuthority('admin:dividend:info')")
    @ApiOperation(value = "全球分红设置与等级比例")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> info() {
        return CommonResult.success(dividendService.getConfig());
    }

    /**
     * 保存分红设置
     */
    @PreAuthorize("hasAuthority('admin:dividend:info')")
    @ApiOperation(value = "保存全球分红设置")
    @RequestMapping(value = "/config", method = RequestMethod.POST)
    public CommonResult<String> saveConfig(@RequestBody Map<String, Object> body) {
        Boolean status = Boolean.TRUE.equals(body.get("status"))
                || "1".equals(String.valueOf(body.get("status")))
                || "true".equalsIgnoreCase(String.valueOf(body.get("status")));
        Integer cycle = body.get("cycle") == null ? null : Integer.valueOf(String.valueOf(body.get("cycle")));
        Integer base = body.get("base") == null ? null : Integer.valueOf(String.valueOf(body.get("base")));
        if (dividendService.saveConfig(status, cycle, base)) {
            return CommonResult.success("保存成功");
        }
        return CommonResult.failed("保存失败");
    }

    /**
     * 保存分红级别
     */
    @PreAuthorize("hasAuthority('admin:dividend:info')")
    @ApiOperation(value = "保存分红级别")
    @RequestMapping(value = "/level/save", method = RequestMethod.POST)
    public CommonResult<String> saveLevel(@RequestBody DividendLevel level) {
        if (dividendService.saveLevel(level)) {
            return CommonResult.success("保存成功");
        }
        return CommonResult.failed("保存失败");
    }

    /**
     * 修改分红级别
     */
    @PreAuthorize("hasAuthority('admin:dividend:info')")
    @ApiOperation(value = "修改分红级别")
    @RequestMapping(value = "/level/update", method = RequestMethod.POST)
    public CommonResult<String> updateLevel(@RequestBody DividendLevel level) {
        if (dividendService.updateLevel(level)) {
            return CommonResult.success("修改成功");
        }
        return CommonResult.failed("修改失败");
    }

    /**
     * 删除分红级别（软删）
     */
    @PreAuthorize("hasAuthority('admin:dividend:info')")
    @ApiOperation(value = "删除分红级别")
    @RequestMapping(value = "/level/delete", method = RequestMethod.GET)
    public CommonResult<String> deleteLevel(@RequestParam Integer id) {
        if (dividendService.deleteLevel(id)) {
            return CommonResult.success("删除成功");
        }
        return CommonResult.failed("删除失败");
    }

    /**
     * 结算记录分页
     */
    @PreAuthorize("hasAuthority('admin:dividend:info')")
    @ApiOperation(value = "全球分红结算记录")
    @RequestMapping(value = "/record/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<DividendRecord>> recordList(@Validated PageParamRequest pageParamRequest) {
        CommonPage<DividendRecord> commonPage = CommonPage.restPage(dividendService.recordList(pageParamRequest));
        return CommonResult.success(commonPage);
    }

    /**
     * 发放明细分页
     */
    @PreAuthorize("hasAuthority('admin:dividend:info')")
    @ApiOperation(value = "全球分红发放明细")
    @RequestMapping(value = "/detail/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<DividendDetail>> detailList(@RequestParam Integer recordId, @Validated PageParamRequest pageParamRequest) {
        CommonPage<DividendDetail> commonPage = CommonPage.restPage(dividendService.detailList(recordId, pageParamRequest));
        return CommonResult.success(commonPage);
    }

    /**
     * 手动结算上一周期（幂等，重复触发返回未结算）
     */
    @PreAuthorize("hasAuthority('admin:dividend:info')")
    @ApiOperation(value = "手动结算上一周期")
    @RequestMapping(value = "/run", method = RequestMethod.POST)
    public CommonResult<String> run(@RequestParam Integer cycleType) {
        Integer recordId = dividendService.settleLastPeriod(cycleType, "[手动触发]");
        if (recordId != null && recordId > 0) {
            return CommonResult.success("结算完成，记录ID=" + recordId);
        }
        return CommonResult.success("本期已结算过，未重复发放");
    }
}
