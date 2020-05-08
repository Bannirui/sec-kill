package com.example.sec.kill.dal.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Author: dingrui
 * @Date: Create in 2020/5/6
 * @Description:
 */
@Data
@Accessors(chain = true)
@Builder
public class SecKillMsgBody implements Serializable {
    private static final long serialVersionUID = -6796684324942737283L;

    private Long secKillId;
    private Long phone;
}
