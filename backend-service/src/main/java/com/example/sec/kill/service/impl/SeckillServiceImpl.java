package com.example.sec.kill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sec.kill.dal.dao.SeckillMapper;
import com.example.sec.kill.dal.dto.ExposerDto;
import com.example.sec.kill.dal.pojo.Seckill;
import com.example.sec.kill.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 秒杀库存表 服务实现类
 * </p>
 *
 * @author dingrui
 * @since 2020-05-05
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Transactional
public class SeckillServiceImpl extends ServiceImpl<SeckillMapper, Seckill> implements SeckillService {

    private final SeckillMapper seckillMapper;

    /**
     * md5盐值字符串,用于混淆MD5
     */
    @Value("${sec-kill.salt}")
    private final String salt;
    /**
     * 查询所有秒杀记录 按照create_time降序
     *
     * @return
     */
    @Override
    public List<Seckill> getSecKillList() {
        // 优先从redis缓存中读取 todo
        // 缓存中没有则从数据库查询
        // 查询结果再回写redis todo
        List<Seckill> seckills = seckillMapper.selectList(
                new LambdaQueryWrapper<Seckill>()
                        .orderByDesc(Seckill::getCreateTime)
        );
        return seckills;
    }

    /**
     * 根据seckillId暴露秒杀地址
     * 秒杀开启输出秒杀接口地址
     * 否则输出系统时间和秒杀时间
     * @param secKillId
     * @return
     */
    @Override
    public ExposerDto exportSeckillUrl(Long secKillId) {
        // 查询redis todo
        // 查询数据库
        Seckill seckill = seckillMapper.selectById(secKillId);
        // 判空 如果存在回写redis 如果为空返回系统时间
        if (seckill == null) {
            // 返回系统时间
            return new ExposerDto(false, secKillId);
        }
        // 回写redis todo

        // 判断秒杀是否开始 秒杀还没开始或者已经结束返回系统时间
        // 系统当前时间
        Date currentTime = new Date();
        if (currentTime.getTime() < seckill.getStartTime().getTime() || currentTime.getTime() > seckill.getEndTime().getTime()) {
            return new ExposerDto(false, secKillId, currentTime.getTime(), seckill.getStartTime().getTime(), seckill.getEndTime().getTime());
        }

        // md5
        String md5 = getMD5(secKillId);
        return new ExposerDto(true, md5, secKillId);
    }

    private String getMD5(long secKillId) {
        String base = secKillId + "/" + salt;
        return DigestUtils.md5DigestAsHex(base.getBytes());
    }

}
