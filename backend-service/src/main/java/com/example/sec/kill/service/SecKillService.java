package com.example.sec.kill.service;

import com.example.sec.kill.dal.dto.ExposerDto;
import com.example.sec.kill.dal.dto.SecKillExecutionDto;
import com.example.sec.kill.dal.pojo.SecKill;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sec.kill.exception.SecKillException;

import java.util.List;

/**
 * <p>
 * 秒杀库存表 服务类
 * </p>
 *
 * @author dingrui
 * @since 2020-05-05
 */
public interface SecKillService extends IService<SecKill> {

    /**
     * 查询所有秒杀记录 按照create_time降序
     * @return
     */
    List<SecKill> getSecKillList();

    /**
     * 根据secKillId暴露秒杀地址
     * @param secKillId
     * @return
     */
    ExposerDto exportSecKillUrl(Long secKillId);

    /**
     * 根据secKillId phone md5 进行抢购
     * @param secKillId
     * @param phone
     * @param md5
     * @return
     */
    SecKillExecutionDto executeSecKill(Long secKillId, Long phone, String md5);

    /**
     * 在redis中进行真正的秒杀操作
     * @param secKillId
     * @param phone
     * @throws SecKillException
     */
    void handleInRedis(Long secKillId, Long phone) throws SecKillException;

    /**
     * 更新库存
     * @param secKillId
     * @param phone
     * @return
     * @throws SecKillException
     */
    SecKillExecutionDto updateInventory(Long secKillId, Long phone) throws SecKillException;

    /**
     * 抢购结果
     * @param secKillId
     * @param phone
     * @return 0： 排队中; 1: 秒杀成功; 2： 秒杀失败
     */
    public int isGrab(Long secKillId, Long phone);
}
