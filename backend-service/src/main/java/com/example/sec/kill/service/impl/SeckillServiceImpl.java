package com.example.sec.kill.service.impl;

import com.example.sec.kill.dal.pojo.Seckill;
import com.example.sec.kill.dal.dao.SeckillMapper;
import com.example.sec.kill.service.SeckillService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀库存表 服务实现类
 * </p>
 *
 * @author dingrui
 * @since 2020-05-05
 */
@Service
public class SeckillServiceImpl extends ServiceImpl<SeckillMapper, Seckill> implements SeckillService {

}
