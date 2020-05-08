package com.example.sec.kill.exception;


import com.example.sec.kill.enums.SecKillStateEnum;

/**
 * 秒杀相关业务异常
 * @author dingrui
 */
public class SecKillException extends RuntimeException {

    private SecKillStateEnum seKillStateEnum;

    public SecKillException(SecKillStateEnum seckillStateEnum) {
        this.seKillStateEnum = seKillStateEnum;
    }

    public SecKillException(String message) {
        super(message);
    }

    public SecKillException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecKillStateEnum getSecKillStateEnum() {
        return seKillStateEnum;
    }

    public void setSecKillStateEnum(SecKillStateEnum seckillStateEnum) {
        this.seKillStateEnum = seKillStateEnum;
    }
}
