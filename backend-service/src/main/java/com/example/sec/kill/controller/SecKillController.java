package com.example.sec.kill.controller;

import com.example.sec.kill.dal.dto.ExposerDto;
import com.example.sec.kill.dal.pojo.Seckill;
import com.example.sec.kill.service.SeckillService;
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

    private final SeckillService seckillService;

    /**
     * 获取列表页 按照create_time降序
     * @return
     */
    @GetMapping("/list")
    public List<Seckill> list() {
        return seckillService.getSecKillList();
    }

    /**
     * 根据secKillId获取秒杀详情
     * @param secKillId
     * @return
     */
    @GetMapping("/detail/{secKillId}")
    public Seckill detail(@PathVariable(value = "secKillId", required = true) Long secKillId) {
        return seckillService.getById(secKillId);
    }

    /**
     * 根据secKillId暴露秒杀地址
     * @param secKillId
     * @return
     */
    @GetMapping("/exposer/{secKillId}")
    public ExposerDto exposer(@PathVariable(value = "secKillId", required = true) Long secKillId) {
        return seckillService.exportSeckillUrl(secKillId);
    }
}
