package com.trans.producer.service;

/**
 * @description:
 * @author: xiaoxu.nie
 * @date: 2018-12-25 17:38
 */
public interface ITransMsgService {

    /**
     * 生产者生成事务消息
     *
     * @param transMsg
     */
    void producerTransMsg(String transMsg);

    /**
     * 发送事务消息
     *
     * @param transMsg
     */
    void templateTransMsg(String transMsg);

}
