package com.example.sec.kill.controller;

import com.example.sec.kill.dal.dto.ExposerDto;
import com.example.sec.kill.dal.dto.SecKillExecutionDto;
import com.example.sec.kill.dal.pojo.SecKill;
import com.example.sec.kill.enums.SecKillStateEnum;
import com.example.sec.kill.exception.SecKillException;
import com.example.sec.kill.service.SecKillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: dingrui
 * @Date: Create in 2020/5/5
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("sec-kill")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class SecKillController {

    private final SecKillService seckillService;

    /**
     * 获取列表页 按照create_time降序
     * @return
     */
    @GetMapping("/list")
    public List<SecKill> list() {
        return seckillService.getSecKillList();
    }

    /**
     * 根据secKillId获取秒杀详情
     * @param secKillId
     * @return
     */
    @GetMapping("/detail/{secKillId}")
    public SecKill detail(@PathVariable(value = "secKillId", required = true) Long secKillId) {
        return seckillService.getById(secKillId);
    }

    /**
     * 根据secKillId暴露秒杀地址
     * @param secKillId
     * @return
     */
    @GetMapping("/exposer/{secKillId}")
    public ExposerDto exposer(@PathVariable(value = "secKillId", required = true) Long secKillId) {
        return seckillService.exportSecKillUrl(secKillId);
    }

    /**
     * 根据secKillId phone md5 进行抢购
     * @param secKillId
     * @param phone
     * @param md5
     * @return
     */
    @GetMapping("/execution/{secKillId}/{phone}/{md5}")
    public SecKillExecutionDto execution(@PathVariable(value = "secKillId", required = true) Long secKillId,@PathVariable(value = "phone", required = true) Long phone, @PathVariable(value = "md5", required = true) String md5) {
        try {
            SecKillExecutionDto secKillExecutionDto = seckillService.executeSecKill(secKillId, phone, md5);
            return secKillExecutionDto;
        } catch (SecKillException e) {
            // 请求限流
            SecKillExecutionDto secKillExecutionDto = new SecKillExecutionDto(secKillId, e.getSecKillStateEnum());
            return secKillExecutionDto;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            SecKillExecutionDto secKillExecutionDto = new SecKillExecutionDto(secKillId, SecKillStateEnum.INNER_ERROR);
            return secKillExecutionDto;
        }
    }

    /**
     * @param id
     * @param phone
     * @return 返回代码的含义0： 排队中; 1: 秒杀成功; 2： 秒杀失败
     * String boughtKey = RedisKeyPrefix.BOUGHT_USERS + seckillId
     * 还有一个redisKey存放已经入队列了的userPhone，   ENQUEUED_USER
     * 进队列的时候sadd ENQUEUED_USER , 消费成功的时候，sdel ENQUEUED_USER
     * 查询这个isGrab接口的时候，先查sismembles boughtKey, true则表明秒杀成功.
     * 否则，ismembles ENQUEUED_USER, 如果在队列中，说明排队中， 如果不在，说明秒杀失败
     */
    @GetMapping("/isGrab/{id}/{phone}")
    public String isGrab(@PathVariable(name = "id", required = true) Long id, @PathVariable(name = "phone", required = true) Long phone) {
        int isGrab = seckillService.isGrab(id, phone);
        return isGrab + "";
    }
}
