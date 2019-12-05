package com.trans.producer.service.impl;

import com.alibaba.fastjson.JSON;
import com.trans.producer.config.MsgConfigProperties;
import com.trans.producer.model.TransMsgStateRecord;
import com.trans.producer.service.ITransMsgService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    /*@Autowired
    private TransactionMQProducer transactionMQProducer;*/

    @Autowired
    private MsgConfigProperties msgConfigProperties;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void producerTransMsg(String transMsg) {
        /*long timeStamp = System.currentTimeMillis();
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
        }*/
    }

    @Override
    public void templateTransMsg(String transMsg) {

        try {
            long timeStamp = System.currentTimeMillis();
            String txGroupProducer = "Trans-Msg-Topic";
            String tags = "ZM-MQ:" + timeStamp;
            String keys = "ZM-MQ-KEYS:" + timeStamp;
            String msg = "ZM-TRANS-MSG:" + timeStamp + ":" + transMsg;

            TransMsgStateRecord transMsgStateRecord = new TransMsgStateRecord();
            transMsgStateRecord.setBizType(1);
            transMsgStateRecord.setCreateTime(new Date());
            transMsgStateRecord.setFlag(1);
            transMsgStateRecord.setMsgBody("amanxu-xiaoxu.nie");
            transMsgStateRecord.setMsgGroup(txGroupProducer);
            transMsgStateRecord.setMsgUniqKey(UUID.randomUUID().toString());
            transMsgStateRecord.setTransState(2);
            if (log.isDebugEnabled()) {
                log.debug("Debug transMsgStateRecord:{}", transMsgStateRecord);
            }
            log.debug("Debug transMsgStateRecord:{}", transMsgStateRecord);
            log.info("Info transMsgStateRecord:{}", transMsgStateRecord);
            log.warn("Warn transMsgStateRecord:{}", transMsgStateRecord);
            log.error("Error transMsgStateRecord:{}", transMsgStateRecord);
            String destination = new StringBuilder(txGroupProducer).append(":").append(tags).toString();
            org.springframework.messaging.Message<String> stringMessage = MessageBuilder.withPayload(transMsg).build();
            TransactionSendResult transactionSendResult = rocketMQTemplate.sendMessageInTransaction(txGroupProducer, destination, stringMessage, null);
            log.info("transactionSendResult:{}", JSON.toJSONString(transactionSendResult));
        } catch (MessagingException e) {
            log.info("MessagingException:{}", e);
        }
    }
}
