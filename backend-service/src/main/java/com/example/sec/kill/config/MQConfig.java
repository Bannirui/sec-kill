package com.example.sec.kill.config;

import com.example.sec.kill.bean.MQConfigBean;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @Author: dingrui
 * @Date: Create in 2020/5/6
 * @Description:
 */
@Slf4j
@Configuration
public class MQConfig {
    /**
     * RabbitMQ集群配置
     */
    @Value("${rabbitmq.address-list}")
    private String addressList;

    @Value("${rabbitmq.username}")
    private String username;

    @Value("${rabbitmq.password}")
    private String password;

    @Value("${rabbitmq.publisher-confirms}")
    private boolean publisherConfirms;

    @Value("${rabbitmq.virtual-host}")
    private String virtualHost;

    @Value("${rabbitmq.queue}")
    private String queue;

    @Bean
    public MQConfigBean mqConfigBean() {
        // 参数校验
        if (StringUtils.isEmpty(addressList)) {
            throw new InvalidPropertyException(MQConfigBean.class, "addressList", "rabbitmq.address-list is Empty");
        }

        // 配置文件中逗号分隔符 localhost:5672,192.168.101.11:5672
        String[] addressStrArr = addressList.split(",");
        // 存储RabbitMQ Address
        List<Address> addressList = new LinkedList<>();

        for (String addressStr : addressStrArr) {
            // 分割出ip与端口号 按照索引位取出ip 端口号
            String[] strArr = addressStr.split(":");
            Address address = new Address(strArr[0], Integer.parseInt(strArr[1]));
            addressList.add(address);
        }

        // 实例化RabbitMQ配置类
        return MQConfigBean.builder()
                .addressList(addressList)
                .username(username)
                .publisherConfirms(publisherConfirms)
                .virtualHost(virtualHost)
                .queue(queue)
                .build();
    }

    @Bean("mqConnectionSecKill")
    public Connection mqConnectionSecKill(@Autowired MQConfigBean mqConfigBean) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        // 用户名
        factory.setUsername(username);
        // 密码
        factory.setPassword(password);
        // 虚拟主机路径（相当于数据库名）
        factory.setVirtualHost(virtualHost);
        // 返回连接
        return factory.newConnection(mqConfigBean.getAddressList());
    }

    @Bean("mqConnectionReceive")
    public Connection mqConnectionReceive(@Autowired MQConfigBean mqConfigBean) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        //用户名
        factory.setUsername(username);
        //密码
        factory.setPassword(password);
        //虚拟主机路径（相当于数据库名）
        factory.setVirtualHost(virtualHost);
        //返回连接
        return factory.newConnection(mqConfigBean.getAddressList());
    }
}
