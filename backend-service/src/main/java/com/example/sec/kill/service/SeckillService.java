package com.example.sec.kill.service;

import com.example.sec.kill.dal.dto.ExposerDto;
import com.example.sec.kill.dal.pojo.Seckill;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 秒杀库存表 服务类
 * </p>
 *
 * @author dingrui
 * @since 2020-05-05
 */
public interface SeckillService extends IService<Seckill> {

    /**
     * 查询所有秒杀记录 按照create_time降序
     * @return
     */
    List<Seckill> getSecKillList();

    ExposerDto exportSeckillUrl(Long secKillId);
}
