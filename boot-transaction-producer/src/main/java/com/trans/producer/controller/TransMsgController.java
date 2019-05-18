package com.trans.producer.controller;

import com.trans.producer.pojo.Result;
import com.trans.producer.service.ITransMsgService;
import com.trans.producer.utils.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: xiaoxu.nie
 * @date: 2018-12-24 13:28
 */
@Api(description = "RocketMQ事务消息Bean模式")
@Slf4j
@RestController
@RequestMapping("/transMsg")
public class TransMsgController {

    @Autowired
    private ITransMsgService transMsgService;

    @ApiOperation(value = "生产数据")
    @GetMapping(value = "/producer")
    public Result transMsgProducer(@RequestParam("msg") String msg) {
        transMsgService.producerTransMsg(msg);
        return ResultUtil.success();
    }

    @ApiOperation(value = "生产数据|RocketMQ工具类")
    @GetMapping(value = "/producerTransMsg")
    public Result mqTransMsgProducer(@RequestParam("msg") String msg) {
        transMsgService.templateTransMsg(msg);
        return ResultUtil.success();
    }

}
