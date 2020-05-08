package com.example.sec.kill.dal.dao;

import com.example.sec.kill.dal.dto.PayOrderWithSecKillDto;
import com.example.sec.kill.dal.pojo.PayOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 秒杀成功明细表 Mapper 接口
 * </p>
 *
 * @author dingrui
 * @since 2020-05-05
 */
public interface PayOrderMapper extends BaseMapper<PayOrder> {

    /**
     * 根据秒杀id与手机号查询秒杀关联订单信息
     * @param secKillId
     * @param userPhone
     * @return
     */
    PayOrderWithSecKillDto queryPayOrderByIdAndPhoneWithSecKill(Long secKillId, Long userPhone);
}
