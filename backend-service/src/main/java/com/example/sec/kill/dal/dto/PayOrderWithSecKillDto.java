package com.example.sec.kill.dal.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author: dingrui
 * @Date: Create in 2020/5/7
 * @Description:
 */
@Data
@Accessors(chain = true)
@Builder
public class PayOrderWithSecKillDto {
    /**
     * 秒杀商品id
     */
    private Long secKillId;

    /**
     * 用户手机号
     */
    private Long userPhone;

    /**
     * 状态标示:-1:无效 0:成功 1:已付款 2:已发货
     */
    private Integer state;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 库存数量
     */
    private Integer inventory;

    /**
     * 秒杀开启时间
     */
    private Date startTime;

    /**
     * 秒杀结束时间
     */
    private Date endTime;

    /**
     * 创建时间
     */
    private Date createTime;

}
