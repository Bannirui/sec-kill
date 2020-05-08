package com.example.sec.kill.bean;

import com.rabbitmq.client.Address;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: dingrui
 * @Date: Create in 2020/5/6
 * @Description: RabbitMQ集群配置
 */
@Data
@Builder
@Accessors
public class MQConfigBean implements Serializable {
    private static final long serialVersionUID = 1455424667366570298L;

    private List<Address> addressList;
    private String username;
    private String password;
    private boolean publisherConfirms;
    private String virtualHost;
    private String queue;
}
