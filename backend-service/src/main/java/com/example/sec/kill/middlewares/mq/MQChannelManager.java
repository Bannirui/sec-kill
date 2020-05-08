package com.example.sec.kill.middlewares.mq;

import com.example.sec.kill.bean.MQConfigBean;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @Author: dingrui
 * @Date: Create in 2020/5/6
 * @Description: 管理当前线程使用的Rabbitmq通道 使用了ThreadLocal
 */
@Slf4j
@Component
public class MQChannelManager {

    // RabbitMQ生产者connection
    @Resource
    private Connection mqConnectionSecKill;

    @Resource
    private MQConfigBean mqConfigBean;

    private ThreadLocal<Channel> localSendChannel = new ThreadLocal<Channel>() {
        @Override
        public Channel initialValue() {
            try {
                Channel channelInst = mqConnectionSecKill.createChannel();
                channelInst.queueDeclare(mqConfigBean.getQueue(), true, false, false, null);
                return channelInst;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    };

    /**
     * 获取当前线程使用的RabbitMQ通道
     * @return
     */
    public Channel getSendChannel() {
        log.info("[+] Send_CurThread.id={}--->", Thread.currentThread().getId());
        // channel为空则声明队列返回 否则直接返回
        Channel channel = localSendChannel.get();
        if (channel == null) {
            try {
                channel = mqConnectionSecKill.createChannel();
                channel.queueDeclare(mqConfigBean.getQueue(), true, false, false, null);
                localSendChannel.set(channel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return channel;
    }
}
