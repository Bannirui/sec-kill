package com.example.sec.kill.boot;

import com.example.sec.kill.constant.RedisKey;
import com.example.sec.kill.constant.RedisKeyPrefix;
import com.example.sec.kill.dal.pojo.SecKill;
import com.example.sec.kill.middlewares.mq.MQConsumer;
import com.example.sec.kill.service.SecKillService;
import com.example.sec.kill.singleton.MyRuntimeSchema;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: dingrui
 * @Date: Create in 2020/5/5
 * @Description: 项目初始化工作
 */
@Slf4j
@Component
public class SecKillApplicationInit implements CommandLineRunner {

    @Resource
    private JedisPool jedisPool;

    @Resource
    private SecKillService secKillService;

    @Resource
    private MQConsumer mqConsumer;

    @Override
    public void run(String... args) throws Exception {
        initRedis();
    }

    /**
     * 预热秒杀数据到redis
     */
    private void initRedis() {
        Jedis jedis = jedisPool.getResource();

        // 清空redis缓存
        jedis.flushDB();

        List<SecKill> secKillList = secKillService.list(null);
        if (secKillList == null || secKillList.size()<1) {
            log.info("[*] FatalError secKill_list_data is empty");
            return;
        }

        // 遍历存入redis
        secKillList.forEach(secKill -> {
            jedis.sadd(RedisKey.SECKILL_ID_SET, secKill.getId() + "");

            String inventoryKey = RedisKeyPrefix.SECKILL_INVENTORY + secKill.getId();
            jedis.set(inventoryKey, String.valueOf(secKill.getInventory()));

            String secKillGoodsKey = RedisKeyPrefix.SECKILL_GOODS + secKill.getId();
            byte[] goodsBytes = ProtostuffIOUtil.toByteArray(secKill, MyRuntimeSchema.getInstance().getGoodsRuntimeSchema(),
                    LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
            jedis.set(secKillGoodsKey.getBytes(), goodsBytes);
        });

        jedis.close();
        log.info("[+] Redis缓存数据初始化完毕");
    }
}

