package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.dividend.DividendDetail;
import com.qxkj.common.model.dividend.DividendLevel;
import com.qxkj.common.model.dividend.DividendRecord;

import java.util.List;
import java.util.Map;

/**
 * 全球分红服务：
 * 分红级别独立设置（比例自定），升级条件复用所绑定的分销商等级，
 * 达到该分销商等级条件（grade 大于等于该等级）的会员参与该级别比例的分红均分。
 */
public interface DividendService extends IService<DividendRecord> {

    /**
     * 分红设置 + 各等级分红比例
     */
    Map<String, Object> getConfig();

    /**
     * 保存分红设置
     *
     * @param status 开关 true=开
     * @param cycle  周期类型 1=日 2=周 3=月
     * @param base   分红基数 1=订单总业绩 2=订单利润
     */
    Boolean saveConfig(Boolean status, Integer cycle, Integer base);

    /**
     * 分红级别列表（含绑定的分销商等级名称/权重）
     */
    List<DividendLevel> levelList();

    /**
     * 保存分红级别
     */
    Boolean saveLevel(DividendLevel level);

    /**
     * 修改分红级别
     */
    Boolean updateLevel(DividendLevel level);

    /**
     * 删除分红级别（软删）
     */
    Boolean deleteLevel(Integer id);

    /**
     * 分红结算记录分页
     */
    com.github.pagehelper.PageInfo<DividendRecord> recordList(com.qxkj.common.request.PageParamRequest pageParamRequest);

    /**
     * 某期发放明细分页
     */
    com.github.pagehelper.PageInfo<DividendDetail> detailList(Integer recordId, com.qxkj.common.request.PageParamRequest pageParamRequest);

    /**
     * 定时检查：命中周期结算时间点（日00:01/周一00:01/1号01:00）时结算上一周期
     */
    void dividendCheck();

    /**
     * 手动结算上一周期（幂等，重复触发会被期号唯一键拦截）
     */
    Integer settleLastPeriod(Integer cycleType, String mark);
}
