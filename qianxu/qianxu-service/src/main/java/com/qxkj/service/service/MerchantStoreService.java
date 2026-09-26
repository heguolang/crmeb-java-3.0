package com.qxkj.service.service;

import com.qxkj.common.model.merchant.MerchantStoreVerifyRecord;
import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.common.model.system.SystemStore;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.MerchantStoreRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.MerchantStoreNearVo;
import com.qxkj.common.response.StoreOrderVerificationConfirmResponse;

import java.util.List;

/**
 * 门店系统 Service
 */
public interface MerchantStoreService {

    // ==================== 后台 ====================

    /** 门店分页列表 */
    CommonPage<SystemStore> adminPage(String keywords, Integer status, PageParamRequest pageRequest);

    /** 门店详情 */
    SystemStore detail(Integer id);

    /** 新增门店 */
    Boolean saveStore(MerchantStoreRequest request);

    /** 编辑门店 */
    Boolean updateStore(MerchantStoreRequest request);

    /** 启用/禁用门店 */
    Boolean updateShow(Integer id, Boolean isShow);

    /** 删除门店（软删） */
    Boolean deleteStore(Integer id);

    /** 核销记录分页（后台） */
    CommonPage<MerchantStoreVerifyRecord> verifyRecords(Integer storeId, String orderNo, PageParamRequest pageRequest);

    // ==================== 用户端 ====================

    /** 我的门店（负责人身份 + 门店信息 + 履约数据概览），非负责人返回 isLeader=false */
    java.util.HashMap<String, Object> myStore(Integer uid);

    /** 附近/可服务门店列表（按距离计算，含半径判定） */
    List<MerchantStoreNearVo> nearby(String latitude, String longitude, Integer productId);

    /** 门店负责人：按核销码预览待核销订单 */
    StoreOrderVerificationConfirmResponse previewVerifyOrder(String vCode, Integer uid);

    /** 门店负责人：按核销码核销订单（自提/门店核销） */
    Boolean verifyOrderByCode(String vCode, Integer uid);

    /** 核销记录分页（门店负责人端） */
    CommonPage<MerchantStoreVerifyRecord> myVerifyRecords(Integer uid, PageParamRequest pageRequest);

    // ==================== 公共 ====================

    /** 写入核销记录（供后台核销/负责人核销共用） */
    void writeVerifyRecord(StoreOrder order, Integer verifyUid, String verifyName, int source);
}
