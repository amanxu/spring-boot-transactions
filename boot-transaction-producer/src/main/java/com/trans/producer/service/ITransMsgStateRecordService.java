package com.trans.producer.service;


import com.trans.producer.model.TransMsgStateRecord;

/**
 * @description:
 * @author: xiaoxu.nie
 * @date: 2018-12-22 20:06
 */
public interface ITransMsgStateRecordService {

    /**
     * 新增事务消息日志
     *
     * @param msgStateRecord
     */
    void createTransMsg(TransMsgStateRecord msgStateRecord);

    /**
     * 更新事务消息日志
     *
     * @param msgStateRecord
     */
    void updateTransMsg(TransMsgStateRecord msgStateRecord);

    /**
     * 根据事务ID查询事务日志记录
     *
     * @param transId
     * @return
     */
    TransMsgStateRecord findMsgStateByTransId(String transId);

    /**
     * 根据消息ID查询事务消息记录
     *
     * @param msgKeys
     * @return
     */
    TransMsgStateRecord findMsgStateByMsgKeys(String msgKeys);
}
