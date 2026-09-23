package com.zbkj.front.controller;

import com.alibaba.fastjson.JSONObject;
import com.zbkj.common.model.product.StoreProductGroup;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.StoreProductGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 移动端商品分组（分组落地页）
 */
@Slf4j
@RestController
@RequestMapping("api/front/product/group")
@Api(tags = "商品分组")
public class ProductGroupController {

    @Autowired
    private StoreProductGroupService storeProductGroupService;

    /**
     * 分组落地页基础信息。
     * 返回 theme_id（该分组的装修页），H5 据此再取装修数据（theme_info/home?theme_id=X）
     * 与分组商品（theme/product?group_ids=X）。
     */
    @ApiOperation(value = "分组落地页信息（含装修页ID）")
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public CommonResult<JSONObject> detail(@PathVariable(value = "id") Integer id) {
        StoreProductGroup group = storeProductGroupService.getEnabledById(id);
        if (group == null) {
            return CommonResult.failed("分组不存在或已停用");
        }
        JSONObject result = new JSONObject(true);
        result.put("id", group.getId());
        result.put("name", group.getName());
        result.put("theme_id", group.getThemeId() == null ? 0 : group.getThemeId());
        result.put("layout", group.getLayout());
        result.put("style", group.getStyle());
        result.put("badge", group.getBadge());
        result.put("title_multi", Boolean.TRUE.equals(group.getTitleMulti()) ? 1 : 0);
        result.put("min_buy", group.getMinBuy() == null ? 1 : group.getMinBuy());
        result.put("limit_one", Boolean.TRUE.equals(group.getLimitOne()) ? 1 : 0);
        return CommonResult.success(result);
    }
}
