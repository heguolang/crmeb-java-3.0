package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.stock.StockAgent;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 订货系统-订货代理 Dao
 */
public interface StockAgentDao extends BaseMapper<StockAgent> {

    /**
     * CAS 更新层级：只有当前层级仍是期望值才更新成功，防止并发审核交叉写覆盖。
     *
     * @return 影响行数，0 表示已被其他并发流程改过
     */
    @Update("UPDATE eb_stock_agent SET level_id = #{newLevelId} WHERE id = #{id} AND level_id = #{oldLevelId}")
    int updateLevelCas(@Param("id") Integer id,
                       @Param("newLevelId") Integer newLevelId,
                       @Param("oldLevelId") Integer oldLevelId);
}
