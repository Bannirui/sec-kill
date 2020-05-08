package com.example.sec.kill.dal.dto;


import com.example.sec.kill.dal.pojo.PayOrder;
import com.example.sec.kill.enums.SecKillStateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 封装秒杀执行后结果
 * @author dingrui
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecKillExecutionDto<T> implements Serializable {

    private static final long serialVersionUID = -8394458698404675141L;

    private long secKillId;

    /**
     * 秒杀执行结果状态
     */
    private Integer state;

    /**
     * 状态表示
     */
    private String stateInfo;

    /**
     * 秒杀成功对象
     * 可能为PayOrder也有可能为PayOrderWithSecKillDto
     */
    private T data;

    public SecKillExecutionDto(long secKillId, SecKillStateEnum stateEnum, T data) {
        this.secKillId = secKillId;
        this.state = stateEnum.getState();
        this.stateInfo = stateEnum.getStateInfo();
        this.data = data;
    }

    public SecKillExecutionDto(long secKillId, SecKillStateEnum statEnum) {
        this.secKillId = secKillId;
        this.state = statEnum.getState();
        this.stateInfo = statEnum.getStateInfo();
    }

    @Override
    public String toString() {
        return "SecKillExecution{" +
                "secKillId=" + secKillId +
                ", state=" + state +
                ", stateInfo='" + stateInfo + '\'' +
                ", data=" + data +
                '}';
    }

}
