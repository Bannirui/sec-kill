package com.example.sec.kill.middlewares.mq;

import com.alibaba.fastjson.JSON;
import com.example.sec.kill.bean.MQConfigBean;
import com.example.sec.kill.constant.RedisKey;
import com.example.sec.kill.dal.dto.SecKillMsgBody;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Author: dingrui
 * @Date: Create in 2020/5/6
 * @Description:
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class MQProducer {
    private final MQChannelManager mqChannelManager;

    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;

    private final MQConfigBean mqConfigBean;

    /**
     * 生产者 发送秒杀消息体
     * @param body
     */
    public void send(SecKillMsgBody body) {
        // 对象转json
        String msg = JSON.toJSONString(body);

        // 获取当前线程使用的RabbitMQ通道
        Channel channel = mqChannelManager.getSendChannel();
        try {
            log.info("[+] [mqSend] '{}'", msg);
            channel.confirmSelect();

            channel.basicPublish("",
                    mqConfigBean.getQueue(),
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean sendAcked = false;
        try {
            sendAcked = channel.waitForConfirms(100);
        } catch (InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }

        log.info("[+] sendAcked={}", sendAcked);
        if (sendAcked) {
            Jedis jedis = jedisPool.getResource();
            jedis.sadd(RedisKey.QUEUE_PRE_SECKILL, body.getSecKillId() + "@" + body.getPhone());
            jedis.close();
        } else {
            log.info("[+] mqSend_NACKED, NOW_RETRY >>> ");
            try {
                channel.basicPublish("",
                        mqConfigBean.getQueue(),
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        msg.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
