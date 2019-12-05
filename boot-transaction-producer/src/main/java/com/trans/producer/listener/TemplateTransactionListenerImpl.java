package com.trans.producer.listener;

import com.trans.producer.enums.ProducerTranStepEnum;
import com.trans.producer.model.TransMsgStateRecord;
import com.trans.producer.service.ITransMsgStateRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @description: 新版本中接口，逻辑实现稍有不同，该类暂未做完，后续会更新
 * @author: xiaoxu.nie
 * @date: 2019/5/28 10:59
 */
@Slf4j
@Service
@RocketMQTransactionListener(txProducerGroup = "Trans-Msg-Topic")
public class TemplateTransactionListenerImpl implements RocketMQLocalTransactionListener {

    @Autowired
    private ITransMsgStateRecordService transMsgStateRecordService;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        // 1. 记录初始状态事务消息
        TransMsgStateRecord msgStateRecord = new TransMsgStateRecord();
        msgStateRecord.setMsgBody(msg.getPayload().toString());
        transMsgStateRecordService.createTransMsg(msgStateRecord);
        try {
            // TODO 2.执行本地业务代码后续补充
            Thread.sleep(100);
            // 3.业务执行成功：保存业务执行日志，此处模拟业务执行成功，保存日志是防止发送本地事务执行结果消息失败时MSQ服务向客户端发起查询做判断
            msgStateRecord.setTransState(ProducerTranStepEnum.COMMIT_MESSAGE.getCode());
        } catch (Exception e) {
            log.error("BIZ Executor Exception:{}", e);
            // 4.业务执行失败，记录消息事务状态
            msgStateRecord.setTransState(ProducerTranStepEnum.ROLLBACK_MESSAGE.getCode());
            transMsgStateRecordService.updateTransMsg(msgStateRecord);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
        transMsgStateRecordService.updateTransMsg(msgStateRecord);
        return RocketMQLocalTransactionState.COMMIT;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        String transactionId = msg.getHeaders().getId().toString();
        TransMsgStateRecord msgStateRecord = transMsgStateRecordService.findMsgStateByTransId(transactionId);
        if (msgStateRecord == null) {
            return RocketMQLocalTransactionState.UNKNOWN;
        }
        Integer status = msgStateRecord.getTransState();
        if (null == status) {
            return RocketMQLocalTransactionState.UNKNOWN;
        }
        switch (status) {
            case 0:
                return RocketMQLocalTransactionState.UNKNOWN;
            case 1:
                return RocketMQLocalTransactionState.COMMIT;
            case 2:
                return RocketMQLocalTransactionState.ROLLBACK;
            default:
                return RocketMQLocalTransactionState.COMMIT;
        }
    }

    /**
     * 将mq事务消息转为事务消息记录
     *
     * @param msg
     * @return
     */
    private TransMsgStateRecord convertMqMsgToRecord(org.apache.rocketmq.common.message.Message msg) {
        TransMsgStateRecord msgStateRecord = new TransMsgStateRecord();
        msgStateRecord.setTopic(msg.getTopic());
        msgStateRecord.setFlag(msg.getFlag());
        msgStateRecord.setMsgBody(new String(msg.getBody()));
        msgStateRecord.setMsgKeys(msg.getKeys());
        msgStateRecord.setMsgTags(msg.getTags());
        msgStateRecord.setTransId(msg.getTransactionId());
        msgStateRecord.setMsgUniqKey(msg.getProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX));
        msgStateRecord.setMsgGroup(msg.getProperty(MessageConst.PROPERTY_PRODUCER_GROUP));
        String tranMsg = msg.getProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED);
        msgStateRecord.setMsgIsTran(StringUtils.isBlank(tranMsg) ? false : Boolean.parseBoolean(tranMsg));
        String msgWait = msg.getProperty(MessageConst.PROPERTY_WAIT_STORE_MSG_OK);
        msgStateRecord.setMsgIsWait(StringUtils.isBlank(msgWait) ? false : Boolean.parseBoolean(msgWait));
        msgStateRecord.setCreateTime(new Date());
        // 默认是预发送状态
        msgStateRecord.setTransState(ProducerTranStepEnum.UN_KNOW.getCode());
        return msgStateRecord;
    }

}
