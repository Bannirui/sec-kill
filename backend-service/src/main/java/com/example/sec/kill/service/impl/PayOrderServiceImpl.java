package com.example.sec.kill.service.impl;

import com.example.sec.kill.dal.pojo.PayOrder;
import com.example.sec.kill.dal.dao.PayOrderMapper;
import com.example.sec.kill.service.PayOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀成功明细表 服务实现类
 * </p>
 *
 * @author dingrui
 * @since 2020-05-05
 */
@Service
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements PayOrderService {

}
