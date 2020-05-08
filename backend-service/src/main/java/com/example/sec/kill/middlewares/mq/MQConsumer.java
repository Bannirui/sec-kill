package com.example.sec.kill.middlewares.mq;

import com.alibaba.fastjson.JSON;
import com.example.sec.kill.bean.MQConfigBean;
import com.example.sec.kill.constant.RedisKey;
import com.example.sec.kill.dal.dto.SecKillMsgBody;
import com.example.sec.kill.enums.AckAction;
import com.example.sec.kill.enums.SecKillStateEnum;
import com.example.sec.kill.exception.SecKillException;
import com.example.sec.kill.service.SecKillService;
import com.rabbitmq.client.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @Author: dingrui
 * @Date: Create in 2020/5/7
 * @Description:
 */
@Slf4j
@Component
public class MQConsumer {
    @Resource
    private MQConfigBean mqConfigBean;

    @Resource
    private SecKillService secKillService;

    private Connection mqConnectionReceive;

    @Resource
    private JedisPool jedisPool;

    public void receive() {
        Channel channel = null;
        try {
            channel = mqConnectionReceive.createChannel();
            channel.queueDeclare(mqConfigBean.getQueue(), true, false, false, null);
            channel.basicQos(0, 1, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MyDefaultConsumer myDefaultConsumer = new MyDefaultConsumer(channel);

        try {
            channel.basicConsume(mqConfigBean.getQueue(), false, myDefaultConsumer);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private class MyDefaultConsumer extends DefaultConsumer {

        private Channel channel;

        /**
         * Constructs a new instance and records its association to the passed-in channel.
         *
         * @param channel the channel to which this consumer is attached
         */
        public MyDefaultConsumer(Channel channel) {
            super(channel);
            this.channel = channel;
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            long threadId = Thread.currentThread().getId();
            log.info("[+] receive_threadId={}", threadId);

            String msg = new String(body, StandardCharsets.UTF_8);
            log.info("[+] [mqReceive] '{}'", msg);

            // 字符串消息转消息体
            SecKillMsgBody msgBody = JSON.parseObject(msg, SecKillMsgBody.class);

            AckAction ackAction = AckAction.ACCEPT;

            try {
                secKillService.handleInRedis(msgBody.getSecKillId(), msgBody.getPhone());
                ackAction = AckAction.ACCEPT;
            } catch (SecKillException e) {
                if (e.getSecKillStateEnum() == SecKillStateEnum.SOLD_OUT || e.getSecKillStateEnum() == SecKillStateEnum.REPEAT_KILL) {
                    // 商品已经售罄 或者该phone用户已经秒杀过
                    ackAction = AckAction.THROW;
                } else {
                    // 其他异常
                    log.error(e.getMessage(), e);
                    log.error("[-] NACK error_requeue");
                    ackAction = AckAction.RETRY;
                }
            } finally {
                log.info("[+] processIt");
                switch (ackAction) {
                    case ACCEPT:
                        try {
                            log.info("[+] ACK");
                            channel.basicAck(envelope.getDeliveryTag(), false);
                        } catch (IOException e) {
                            log.error("[+] basicAck_throws_IOException");
                            log.error(e.getMessage(), e);
                            throw e;
                        }

                        Jedis jedisAccept = jedisPool.getResource();
                        jedisAccept.srem(RedisKey.QUEUE_PRE_SECKILL, msgBody.getSecKillId() + "@" + msgBody.getPhone());
                        jedisAccept.close();
                        break;

                    case THROW:
                        log.info("[+] LET_MQ_ACK REASON:SecKillStateEnum.SOLD_OUT, SecKillStateEnum.REPEAT_KILL");
                        channel.basicAck(envelope.getDeliveryTag(), false);
                        Jedis jedisThrow = jedisPool.getResource();
                        jedisThrow.srem(RedisKey.QUEUE_PRE_SECKILL, msgBody.getSecKillId() + "@" + msgBody.getPhone());
                        jedisThrow.close();
                        break;

                    case RETRY:
                        log.info("[+] NACK error_requeue");
                        channel.basicNack(envelope.getDeliveryTag(), false, true);
                        break;
                }

            }
        }
    }
}
