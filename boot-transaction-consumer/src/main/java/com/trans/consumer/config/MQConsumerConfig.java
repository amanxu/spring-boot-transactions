package com.trans.consumer.config;

import com.trans.consumer.listener.ConsumerTranMsgListener;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: xiaoxu.nie
 * @date: 2019/5/13 16:18
 */
@Configuration
public class MQConsumerConfig {

    @Autowired
    private MsgConfigProperties msgConfigProperties;

    @Autowired
    private ConsumerTranMsgListener consumerTranMsgListener;


    /**
     * Spring中注册一个消费者,设置监听器监听指定主题的消息
     *
     * @return
     * @throws MQClientException
     */
    @Bean
    public DefaultMQPushConsumer defaultMQPushConsumer() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(msgConfigProperties.getTransGroup());
        consumer.setNamesrvAddr(msgConfigProperties.getNameServer());

        //设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费；如果非第一次启动，那么按照上次消费的位置继续消费
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.subscribe(msgConfigProperties.getTransTopic(), "*");

        consumer.registerMessageListener(consumerTranMsgListener);
        consumer.start();
        return consumer;
    }
}
