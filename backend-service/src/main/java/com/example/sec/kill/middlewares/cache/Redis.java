package com.example.sec.kill.middlewares.cache;

import com.example.sec.kill.constant.RedisKey;
import com.example.sec.kill.constant.RedisKeyPrefix;
import com.example.sec.kill.dal.pojo.SecKill;
import com.example.sec.kill.singleton.MyRuntimeSchema;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @Author: dingrui
 * @Date: Create in 2020/5/7
 * @Description:
 */
@Slf4j
@Repository
public class Redis {
    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;

    private RuntimeSchema<SecKill> schema = MyRuntimeSchema.getInstance().getGoodsRuntimeSchema();

    public SecKill getSecKill(Long id) {
        // redis操作逻辑
        try {
            try (Jedis jedis = jedisPool.getResource()) {
                String key = RedisKeyPrefix.SECKILL_GOODS + id;
                byte[] bytes = jedis.get(key.getBytes());
                // 缓存中获取到bytes
                if (bytes != null) {
                    //空对象
                    SecKill secKill = schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes, secKill, schema);
                    // secKill 被反序列化
                    return secKill;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public String putSecKill(SecKill secKill) {
        // set Object(SecKill) -> 序列化 -> byte[]
        try {
            try (Jedis jedis = jedisPool.getResource()) {
                String key = RedisKeyPrefix.SECKILL_GOODS + secKill.getId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(secKill, schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                return jedis.set(key.getBytes(), bytes);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 从缓存中获取所有的实时商品数据(包括实时库存量)
     *
     * @return
     */
    public List<SecKill> getAllGoods() {
        List<SecKill> result = new ArrayList<>();
        Jedis jedis = jedisPool.getResource();
        Set<String> idSet = jedis.smembers(RedisKey.SECKILL_ID_SET);
        if (idSet != null || idSet.size() > 0) {
            for (String secKillId : idSet) {
                String goodsKey = RedisKeyPrefix.SECKILL_GOODS + secKillId;
                byte[] bytes = jedis.get(goodsKey.getBytes());
                if (bytes != null) {
                    SecKill secKill = schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes, secKill, schema);

                    try {
                        // goodsKey获取到的库存量是初始值，并不是当前值，所有需要从RedisKeyPrefix.SECKILL_INVENTORY+seckillID
                        // 获取到的库存，再设置到结果中去
                        String inventoryStr = jedis.get(RedisKeyPrefix.SECKILL_INVENTORY + secKillId);
                        if (!StringUtils.isEmpty(inventoryStr)) {
                            secKill.setInventory(Integer.valueOf(inventoryStr));
                        }
                    } catch (NumberFormatException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                    result.add(secKill);
                }
            }
        }
        jedis.close();
        return result;
    }

    public void setAllGoods(List<SecKill> list) {
        Jedis jedis = jedisPool.getResource();
        if (list == null || list.size() < 1) {
            log.info("[-] FatalError secKill_list_data is empty");
            return;
        }

        jedis.del(RedisKey.SECKILL_ID_SET);

        for (SecKill seckill : list) {
            jedis.sadd(RedisKey.SECKILL_ID_SET, seckill.getId() + "");

            String secKillGoodsKey = RedisKeyPrefix.SECKILL_GOODS + seckill.getId();
            byte[] goodsBytes = ProtostuffIOUtil.toByteArray(seckill, MyRuntimeSchema.getInstance().getGoodsRuntimeSchema(),
                    LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
            jedis.set(secKillGoodsKey.getBytes(), goodsBytes);
        }
        jedis.close();
        log.info("[+] 数据库Goods数据同步到Redis完毕！");
    }
}
