package com.example.sec.kill.dal.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("sec_kill")
public class SecKill implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 商品库存id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
    @TableField(value = "start_time")
    private Date startTime;

    /**
     * 秒杀结束时间
     */
    @TableField(value = "end_time")
    private Date endTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 版本号用于数据表实现乐观锁
     */
    private Long version;

}
