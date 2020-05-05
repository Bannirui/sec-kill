package com.example.sec.kill.dal.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 秒杀库存表
 * </p>
 *
 * @author dingrui
 * @since 2020-05-05
 */
@Data
@Accessors
public class Seckill implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 商品库存id
     */
    @TableId(value = "seckill_id", type = IdType.AUTO)
    private Long seckillId;

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

    private Long version;

    @Override
    public String toString() {
        return "Seckill{" +
        "seckillId=" + seckillId +
        ", name=" + name +
        ", inventory=" + inventory +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", createTime=" + createTime +
        ", version=" + version +
        "}";
    }
}
