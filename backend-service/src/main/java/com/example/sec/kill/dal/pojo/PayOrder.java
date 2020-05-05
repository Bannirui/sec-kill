package com.example.sec.kill.dal.pojo;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 秒杀成功明细表
 * </p>
 *
 * @author dingrui
 * @since 2020-05-05
 */
public class PayOrder implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 秒杀商品id
     */
    private Long seckillId;

    /**
     * 用户手机号
     */
    private Long userPhone;

    /**
     * 状态标示:-1:无效 0:成功 1:已付款 2:已发货
     */
    private Integer state;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


    public Long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(Long seckillId) {
        this.seckillId = seckillId;
    }

    public Long getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(Long userPhone) {
        this.userPhone = userPhone;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "PayOrder{" +
        "seckillId=" + seckillId +
        ", userPhone=" + userPhone +
        ", state=" + state +
        ", createTime=" + createTime +
        "}";
    }
}
