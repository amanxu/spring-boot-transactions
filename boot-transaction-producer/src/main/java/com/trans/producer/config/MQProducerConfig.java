package com.trans.producer.config;

import com.trans.producer.listener.TransactionListenerImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @description:
 * @author: xiaoxu.nie
 * @date: 2019/5/13 15:53
 */
@Slf4j
@Configuration
public class MQProducerConfig {

    @Autowired
    private MsgConfigProperties msgConfigProperties;

    @Autowired
    private TransactionListenerImpl transactionListener;

    /**
     * Spring中注册一个生产者，在需要发送事务消息的地方注入该Bean执行发送
     *
     * @return
     * @throws MQClientException
     */
    @Bean
    public TransactionMQProducer transactionMQProducer() {

        TransactionMQProducer producer = new TransactionMQProducer(msgConfigProperties.getTransGroup());

        // 自定义线程池
        ExecutorService executorService = new ThreadPoolExecutor(2, 5, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("Client-Transaction-Msg-Check-Thread");
                return thread;
            }
        });
        producer.setExecutorService(executorService);
        // 本地事务执行和事务状态监听器
        producer.setTransactionListener(transactionListener);
        // RocketMq 服务地址
        producer.setNamesrvAddr(msgConfigProperties.getNameServer());
        try {
            producer.start();
        } catch (MQClientException e) {
            log.error("transactionMQProducer MQClientException:{}", e);
        }
        return producer;
    }
}
