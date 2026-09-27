package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.pingou.PingouLevel;

import java.util.List;

/**
 * 拼购等级设置（全局默认分享奖励阶梯）服务
 */
public interface PingouLevelService extends IService<PingouLevel> {

    /**
     * 等级列表（按 sort 降序、id 升序）
     */
    List<PingouLevel> getLevelList();

    /**
     * 保存等级
     */
    boolean saveLevel(PingouLevel level);

    /**
     * 修改等级
     */
    boolean updateLevel(PingouLevel level);

    /**
     * 删除等级（软删）
     */
    boolean deleteLevel(Integer id);
}
