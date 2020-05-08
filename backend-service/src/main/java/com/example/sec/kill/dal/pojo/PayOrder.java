package com.example.sec.kill.dal.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 秒杀成功明细表
 * </p>
 *
 * @author dingrui
 * @since 2020-05-05
 */
@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("pay_order")
public class PayOrder implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 秒杀商品id
     */
    @TableField(value = "sec_kill_id")
    private Long secKillId;

    /**
     * 用户手机号
     */
    @TableField(value = "user_phone")
    private Long userPhone;

    /**
     * 状态标示:-1:无效 0:成功 1:已付款 2:已发货
     */
    private Integer state;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

}
