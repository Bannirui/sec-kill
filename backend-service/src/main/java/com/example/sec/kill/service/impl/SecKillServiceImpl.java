package com.example.sec.kill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sec.kill.constant.RedisKey;
import com.example.sec.kill.constant.RedisKeyPrefix;
import com.example.sec.kill.dal.dao.PayOrderMapper;
import com.example.sec.kill.dal.dao.SecKillMapper;
import com.example.sec.kill.dal.dto.ExposerDto;
import com.example.sec.kill.dal.dto.PayOrderWithSecKillDto;
import com.example.sec.kill.dal.dto.SecKillExecutionDto;
import com.example.sec.kill.dal.dto.SecKillMsgBody;
import com.example.sec.kill.dal.pojo.PayOrder;
import com.example.sec.kill.dal.pojo.SecKill;
import com.example.sec.kill.enums.SecKillStateEnum;
import com.example.sec.kill.exception.SecKillException;
import com.example.sec.kill.middlewares.cache.Redis;
import com.example.sec.kill.middlewares.mq.MQProducer;
import com.example.sec.kill.service.AccessLimitService;
import com.example.sec.kill.service.SecKillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
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
public class SecKillServiceImpl extends ServiceImpl<SecKillMapper, SecKill> implements SecKillService {

    private final AccessLimitService accessLimitService;

    private final MQProducer mqProducer;

    private final PayOrderMapper payOrderMapper;

    private final SecKillMapper seckillMapper;

    private final Redis redis;

    /**
     * md5盐值字符串,用于混淆MD5
     */
    @Value("${sec-kill.salt}")
    private String salt;

    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;

    /**
     * 查询所有秒杀记录 按照create_time降序
     *
     * @return
     */
    @Override
    public List<SecKill> getSecKillList() {
        // 优先从redis缓存中读取
        List<SecKill> allGoods = redis.getAllGoods();

        // 缓存中没有则从数据库查询
        if (CollectionUtils.isEmpty(allGoods)) {
            allGoods = seckillMapper.selectList(
                    new LambdaQueryWrapper<SecKill>()
                            .orderByDesc(SecKill::getCreateTime)
            );

            // 查询结果再回写redis
            redis.setAllGoods(allGoods);
        }

        // 响应结果
        return allGoods;
    }

    /**
     * 根据seckillId暴露秒杀地址
     * 秒杀开启输出秒杀接口地址
     * 否则输出系统时间和秒杀时间
     *
     * @param secKillId
     * @return
     */
    @Override
    public ExposerDto exportSecKillUrl(Long secKillId) {
        // 查询redis
        SecKill secKill = redis.getSecKill(secKillId);

        // 如果redis查询结果为空则查询数据库
        if (secKill == null) {
            // 查询数据库
            secKill = seckillMapper.selectById(secKillId);

            // 判空 如果存在回写redis 如果为空返回系统时间
            if (secKill == null) {
                // 返回系统时间
                return new ExposerDto(false, secKillId);
            }

            // 回写redis
            redis.putSecKill(secKill);
        }

        // 判断秒杀是否开始 秒杀还没开始或者已经结束返回系统时间
        // 系统当前时间
        Date currentTime = new Date();
        if (currentTime.getTime() < secKill.getStartTime().getTime() || currentTime.getTime() > secKill.getEndTime().getTime()) {
            return new ExposerDto(false, secKillId, currentTime.getTime(), secKill.getStartTime().getTime(), secKill.getEndTime().getTime());
        }

        // md5
        String md5 = getMd5(secKillId);
        return new ExposerDto(true, md5, secKillId);
    }

    @Override
    public SecKillExecutionDto executeSecKill(Long secKillId, Long phone, String md5) {
        // 判断是否被限流 若被限流则抛出异常 否则进行秒杀
        if (accessLimitService.tryAcquireSecKill()) {
            // 请求未被限流 进行秒杀
            return handleSecKillAsync(secKillId, phone, md5);
        } else {
            // 请求异常 抛出异常
            log.info("[-] ACCESS_LIMITED secKillId:{} phone:{}", secKillId, phone);
            throw new SecKillException(SecKillStateEnum.ACCESS_LIMIT);
        }
    }

    private String getMd5(long secKillId) {
        String base = secKillId + "/" + salt;
        return DigestUtils.md5DigestAsHex(base.getBytes());
    }

    /**
     * 执行秒杀的逻辑 先在redis里处理 然后发送mq 最后再减库存到数据库
     *
     * @param secKillId
     * @param phone
     * @param md5
     * @return
     */
    private SecKillExecutionDto<PayOrder> handleSecKillAsync(Long secKillId, Long phone, String md5) {
        // 认证md5 如果md5为空或者被篡改 抛出异常
        if (null == md5 || !md5.equals(getMd5(secKillId))) {
            log.info("[-] SECKILL_DATA_REWRITE secKillId:{} phone:{}", secKillId, phone);
            throw new SecKillException(SecKillStateEnum.DATA_REWRITE);
        }

        Jedis jedis = jedisPool.getResource();
        // redis key
        String inventoryKey = RedisKeyPrefix.SECKILL_INVENTORY + secKillId;
        // redis key
        String boughtKey = RedisKeyPrefix.BOUGHT_USERS + secKillId;

        // redis获取value
        String inventoryStr = jedis.get(inventoryKey);
        // 转换值
        int inventory = Integer.parseInt(inventoryStr);

        // 判断redis中获取的库存值 如果小于等于0 抛出抢购秒杀异常
        if (inventory <= 0) {
            jedis.close();
            log.info("[-] SECKILLSOLD_OUT secKillId:{} phone:{}", secKillId, phone);
            throw new SecKillException(SecKillStateEnum.SOLD_OUT);
        }

        // 判断是否重复秒杀
        // 如果phone是集合boughtKey的元素返回1 如果不是boughtKey的元素或者boughtKey不存在则返回0
        if (jedis.sismember(boughtKey, String.valueOf(phone))) {
            // 重复秒杀
            jedis.close();
            log.info("[-] SECKILL_REPEATED secKillId:{} phone:{}", secKillId, phone);
            throw new SecKillException(SecKillStateEnum.REPEAT_KILL);
        } else {
            jedis.close();

            // 进入待秒杀队列 进行后续串行操作
            // secKillId和phone消息体发送消息队列
            SecKillMsgBody secKillMsgBody = SecKillMsgBody.builder().secKillId(secKillId).phone(phone).build();
            mqProducer.send(secKillMsgBody);

            // 消息已经发送mq 立即响应客户端 秒杀已经成功
            PayOrder payOrder = new PayOrder();
            payOrder.setSecKillId(secKillId).setUserPhone(phone).setState(SecKillStateEnum.ENQUEUE_PRE_SECKILL.getState());
            log.info("[+] ENQUEUE_PRE_SECKILL secKillId:{} phone:{}", secKillId, phone);
            return new SecKillExecutionDto<>(secKillId, SecKillStateEnum.ENQUEUE_PRE_SECKILL, payOrder);
        }


    }

    /**
     * 在redis中进行真正的秒杀操作
     *
     * @param secKillId
     * @param phone
     * @throws SecKillException
     */
    @Override
    public void handleInRedis(Long secKillId, Long phone) throws SecKillException {
        Jedis jedis = jedisPool.getResource();

        // redis 库存 key值
        String inventoryKey = RedisKeyPrefix.SECKILL_INVENTORY + secKillId;
        String boughtKey = RedisKeyPrefix.BOUGHT_USERS + secKillId;

        // 根据redis key值获取库存value
        String inventoryStr = jedis.get(inventoryKey);
        // 库存 字符串值转整型
        int inventory = Integer.parseInt(inventoryKey);

        // 对库存判断 小于等于0抛出异常 抢购售罄
        if (inventory <= 0) {
            log.info("[*] handleInRedis SECKILLSOLD_OUT secKillId:{} phone:{}", secKillId, phone);
            throw new SecKillException(SecKillStateEnum.SOLD_OUT);
        }

        // 判断该手机号是否已经下单过 避免重复抢购下单
        if (jedis.sismember(boughtKey, String.valueOf(phone))) {
            log.info("[*] handleInRedis SECKILL_REPEATED secKillId:{} phone:{}", secKillId, phone);
            throw new SecKillException(SecKillStateEnum.REPEAT_KILL);
        }

        jedis.decr(inventoryKey);
        jedis.sadd(boughtKey, String.valueOf(phone));
        log.info("[+] handleInRedis_done");
    }

    /**
     * 更新库存
     * 先插入秒杀记录 再减库存
     *
     * @param secKillId
     * @param phone
     * @return
     * @throws SecKillException
     */
    @Override
    public SecKillExecutionDto updateInventory(Long secKillId, Long phone) throws SecKillException {
        // 当前时间
        Date currentTime = new Date();

        // 执行秒杀逻辑：减库存+记录购买行为
        try {
            // 插入秒杀记录，记录购买行为
            PayOrder payOrder = PayOrder.builder().secKillId(secKillId).userPhone(phone).createTime(currentTime).build();
            // 表中设计关联主键 secKillId和userPhone 一旦记录已经存在再插入则会失败 插入受影响行数小于等于0表明记录重复
            int insertEffectedRows = payOrderMapper.insert(payOrder);

            // 判断是否重复秒杀
            if (insertEffectedRows <= 0) {
                // 重复秒杀
                log.info("[+] secKill REPEATED secKillId:{} phone:{}", secKillId, phone);
                throw new SecKillException(SecKillStateEnum.REPEAT_KILL);
            } else {
                // 秒杀订单已经插入数据库 开始减库存
                SecKill currentSecKill = seckillMapper.selectById(secKillId);
                // 是否过期 默认值 无效
                boolean validTime = false;

                if (currentSecKill != null) {
                    // 当前时间的时间戳
                    long currentTimeStamp = currentTime.getTime();
                    if (currentTimeStamp > currentSecKill.getStartTime().getTime() && currentTimeStamp < currentSecKill.getEndTime().getTime() && currentSecKill.getInventory() > 0 && currentSecKill.getVersion() > -1) {
                        // 当前时间在秒杀开始与结束时间之间表示时间秒杀时间有效 秒杀库存充足 version字段大于-1
                        validTime = true;
                    }
                }

                // 根据validTime判断秒杀是否有效
                if (validTime) {
                    // 秒杀有效
                    // 不采用synchronized对代码块加锁 利用数据表乐观锁id+version只更新一条记录
                    Long version = currentSecKill.getVersion();
                    int updateEffectedRows = seckillMapper.reduceInventory(secKillId, version);

                    // 更新影响的表行数判断是否减库存更新表成功
                    if (updateEffectedRows <= 0) {
                        // 更新失败 没有更新到记录 秒杀结束 rollback
                        log.info("[+] secKill_DATABASE_CONCURRENCY_ERROR secKillId:{} phone:{}", secKillId, phone);
                        throw new SecKillException(SecKillStateEnum.DB_CONCURRENCY_ERROR);
                    } else {
                        // 秒杀成功 commit
                        PayOrderWithSecKillDto payOrderWithSecKillDto = payOrderMapper.queryPayOrderByIdAndPhoneWithSecKill(secKillId, phone);
                        log.info("[+] secKill SUCCESS secKillId:{} phone:{}", secKillId, phone);
                        return new SecKillExecutionDto<>(secKillId, SecKillStateEnum.SUCCESS, payOrderWithSecKillDto);
                    }
                } else {
                    // 秒杀无效 结束
                    log.info("[+] secKill_END secKillId:{} userPhone:{}", secKillId, phone);
                    throw new SecKillException(SecKillStateEnum.END);
                }
            }
        } catch (SecKillException secKillException) {
            throw secKillException;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // 所有编译期异常 转化为运行期异常
            throw new SecKillException(SecKillStateEnum.INNER_ERROR);
        }
    }

    /**
     * 抢购结果
     *
     * @param secKillId
     * @param phone
     * @return 0： 排队中; 1: 秒杀成功; 2： 秒杀失败
     */
    @Override
    public int isGrab(Long secKillId, Long phone) {
        // 返回结果
        int result = 0;

        Jedis jedis = jedisPool.getResource();

        try {
            String boughtKey = RedisKeyPrefix.BOUGHT_USERS + secKillId;
            result = jedis.sismember(boughtKey, String.valueOf(phone)) ? 1 : 0;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result = 0;
        }

        if (result == 0) {
            if (!jedis.sismember(RedisKey.QUEUE_PRE_SECKILL, secKillId + "@" + phone)) {
                result = 2;
            }
        }

        return result;
    }

}
