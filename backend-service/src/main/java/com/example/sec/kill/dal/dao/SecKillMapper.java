package com.example.sec.kill.dal.dao;

import com.example.sec.kill.dal.pojo.SecKill;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 秒杀库存表 Mapper 接口
 * </p>
 *
 * @author dingrui
 * @since 2020-05-05
 */
public interface SecKillMapper extends BaseMapper<SecKill> {
    /**
     * 减库存
     * 利用数据表乐观锁实现
     * @param id
     * @param version
     * @return
     */
    int reduceInventory(Long id, Long version);
}
