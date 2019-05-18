package com.trans.producer.service.impl;

import com.trans.producer.config.MsgConfigProperties;
import com.trans.producer.model.TransMsgStateRecord;
import com.trans.producer.service.ITransMsgService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.UUID;

/**
 * @description:
 * @author: xiaoxu.nie
 * @date: 2018-12-25 17:39
 */
@Slf4j
@Service
public class TransMsgServiceImpl implements ITransMsgService {

    @Autowired
    private TransactionMQProducer transactionMQProducer;

    @Autowired
    private MsgConfigProperties msgConfigProperties;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void producerTransMsg(String transMsg) {
        long timeStamp = System.currentTimeMillis();
        String tags = "ZM-MQ:" + timeStamp;
        String keys = "ZM-MQ-KEY:" + timeStamp;
        String msg = "ZM-TRANS-MSG:" + timeStamp + ":" + transMsg;
        try {
            Message message = new Message(msgConfigProperties.getTransTopic(), tags, keys,
                    msg.getBytes(RemotingHelper.DEFAULT_CHARSET));
            // 向MQ发送消息
            SendResult sendResult = transactionMQProducer.sendMessageInTransaction(message, null);

            log.info("RocketMQ Send Msg Result:{}", sendResult);
        } catch (MQClientException | UnsupportedEncodingException e) {
            log.error("producerTransMsg:{}", e);
        }
    }

    @Override
    public void templateTransMsg(String msg) {
        TransMsgStateRecord transMsgStateRecord = new TransMsgStateRecord();
        transMsgStateRecord.setBizType(1);
        transMsgStateRecord.setCreateTime(new Date());
        transMsgStateRecord.setFlag(1);
        transMsgStateRecord.setMsgBody("amanxu-xiaoxu.nie");
        transMsgStateRecord.setMsgGroup("Trans-Msg-Topic");
        transMsgStateRecord.setMsgUniqKey(UUID.randomUUID().toString());
        transMsgStateRecord.setTransState(2);
        if (log.isDebugEnabled()) {
            log.debug("Debug transMsgStateRecord:{}", transMsgStateRecord);
        }
        log.debug("Debug transMsgStateRecord:{}", transMsgStateRecord);
        log.info("Info transMsgStateRecord:{}", transMsgStateRecord);
        log.warn("Warn transMsgStateRecord:{}", transMsgStateRecord);
        log.error("Error transMsgStateRecord:{}", transMsgStateRecord);
    }
}
