package com.trans.consumer.service;


import com.trans.consumer.model.TransMsgConsumeRecord;

/**
 * @description:
 * @author: xiaoxu.nie
 * @date: 2018-12-23 22:36
 */
public interface ITransMsgConsumeRecordService {

    /**
     * 创建事务消息消费记录
     *
     * @param consumeRecord
     */
    void createMsgRecord(TransMsgConsumeRecord consumeRecord);

    /**
     * 更新事务消息消费记录
     *
     * @param consumeRecord
     */
    void updateMsgRecord(TransMsgConsumeRecord consumeRecord);

    /**
     * 根据事务ID查询消费事务记录
     *
     * @param transId
     * @return
     */
    TransMsgConsumeRecord findMsgRecordByTransId(String transId);

    /**
     * 根据消息ID查询事务消息消费记录
     *
     * @param msgId
     * @return
     */
    TransMsgConsumeRecord findMsgRecordByMsgId(String msgId);
}
