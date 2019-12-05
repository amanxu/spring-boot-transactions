##### 基于rocketMQ的分布式柔性事务
    -- 对于实时性要求不高的业务采用分布式柔性事务，其基本原理就是：生产者可以100%保证业务执行成功，
    RocketMQ中的消息100%可以信赖以
第三方平台手机充值流程为例，生产者流程简介如下：
```
 1.生产者首先向MQ发送UNKNOW事务状态的事务消息，事务消息包含要执行的业务的详细信息（如：手机号、充值金额、充值时间等），
   UNKNOW状态的消息对RocketMQ不可见
   
 2.如果MQ消息发送失败，则结束当前流程，告知客户端操作失败；如果MQ消息发送成功，则执行TransactionListener实现类中的本地业务逻辑：
   2.1 将业务信息和MQ发送成功返回的事务信息（事务ID、消息ID等）保存到MYSQL数据库，并记录状态为UNKNOW状态 
   2.2 执行本地业务（手机充值中支付流程完成，即用户方扣款成功），更新2.1步骤中的MYSQL数据库中事务日志状态为执行成功，
   同时在消息监听实现类中返回事务状态COMMIT_MESSAGE，如果本地业务执行失败则返回事务状态ROLLBACK_MESSAGE，当前消息废弃
   当MQ服务端对应的事务消息状态为COMMIT_MESSAGE时，MQ消费者可以正常消费当前消息
 
 3.当2.1或者2.2步骤中向MQ服务端发送失败时，即MQ服务端的未收到MQ客户端的事务消息状态更新时，MQ服务端会定时对当前消息进行事务回查，
 TransactionListener接口中的checkLocalTransaction方法的实现中会根据MQ服务端发送到客户端的消息的ID查询MYSQL中事务日志，判断
 当前事务消息的状态，如果状态为业务执行失败则返回MQ服务端状态ROLLBACK_MESSAGE，当前MQ事务消息废除，如果事务日志状态为业务执行成功，
 则返回MQ服务端状态COMMIT_MESSAGE，当前事务消息可以正常被消费者消费
```
#### 生产者Java代码实现如下：
```java
@Slf4j
@Service
public class TransactionListenerImpl implements TransactionListener {

    @Autowired
    private ITransMsgStateRecordService transMsgStateRecordService;

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {

        // 1. 记录初始状态事务消息
        TransMsgStateRecord msgStateRecord = convertMqMsgToRecord(msg);
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
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
        transMsgStateRecordService.updateTransMsg(msgStateRecord);
        return LocalTransactionState.COMMIT_MESSAGE;
    }

    /**
     * <p>RocketMQ 服务端检测到消息状态超时时，主动向producer发起查询事务状态请求，
     * 根据生产者返回事务状态决定MQ服务中的消息是rollback还是commit</p>
     * 该方法是防止producer发送给mq服务的消息丢失，提供给mq服务回查消息状态使用
     *
     * @param msg
     * @return
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        String transactionId = msg.getTransactionId();
        TransMsgStateRecord msgStateRecord = transMsgStateRecordService.findMsgStateByTransId(transactionId);
        if (msgStateRecord == null) {
            return LocalTransactionState.UNKNOW;
        }
        Integer status = msgStateRecord.getTransState();
        if (null == status) {
            return LocalTransactionState.UNKNOW;
        }
        switch (status) {
            case 0:
                return LocalTransactionState.UNKNOW;
            case 1:
                return LocalTransactionState.COMMIT_MESSAGE;
            case 2:
                return LocalTransactionState.ROLLBACK_MESSAGE;
            default:
                return LocalTransactionState.COMMIT_MESSAGE;
        }
    }

    /**
     * 将mq事务消息转为事务消息记录
     *
     * @param msg
     * @return
     */
    private TransMsgStateRecord convertMqMsgToRecord(Message msg) {
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
````
消费者流程简介如下：
````
  1.消费者接受到需要消费的MQ消息后，首先判断当前消息是否被消费过，防止重复消费，然后将被消费的消息写入MQYSQL数据库
  2.然后执行业务逻辑（给手机号增加余额），如果业务执行成功，更行本地MQYSQL中事务日志的状态，同时返回MQ服务端状态SUCCESS
  如果业务执行失败，更新本地事务消息日志状态执行失败，同时返回MQ服务端状态为SUSPEND_CURRENT_QUEUE_A_MOMENT，该消息可以持续被消费
````
#### 消费者Java代码实现如下：
````java
@Slf4j
@Service
public class ConsumerTranMsgListener implements MessageListenerOrderly {

    @Autowired
    private ITransMsgConsumeRecordService transMsgConsumeRecordService;

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> messageExtList, ConsumeOrderlyContext context) {
        context.setAutoCommit(true);

        TransMsgConsumeRecord consumeRecord = null;
        try {
            // 消费端接收订阅的消息并消费
            for (MessageExt messageExt : messageExtList) {
                // TODO 是否需要Redis锁定当前消息
                log.info("Customer Received Msg TranId:{}; Content: {}; ", messageExt.getTransactionId(), new String(messageExt.getBody()));
                // 获取消息ID，根据事务ID查询当前消息是否被消费过，避免重复消费
                TransMsgConsumeRecord msgRecord = transMsgConsumeRecordService.findMsgRecordByMsgId(messageExt.getMsgId());
                // 判断事务消费日志是否存在，同时判断事务消息的消费状态，如果消费成功则丢弃当前消息
                if (msgRecord != null && ConsumeTranStepEnum.SUCCESS.getCode().equals(msgRecord.getTransState())) {
                    return ConsumeOrderlyStatus.SUCCESS;
                }
                // 解析当前消息
                consumeRecord = convertMsgToRecord(messageExt);
                // TODO 1.判断当前消息中业务数据(比如订单号等)是否已执行,如果已执行丢弃当前消息

                // 如果当前消息和当前消息体内容都未被重复消费则执行正常逻辑
                transMsgConsumeRecordService.createMsgRecord(consumeRecord);

                //TODO 2.消费端模拟业务处理

                // 本地事务执行成功，更新事务消息消费结果
                consumeRecord.setTransState(ConsumeTranStepEnum.SUCCESS.getCode());
                transMsgConsumeRecordService.updateMsgRecord(consumeRecord);
            }
        } catch (Exception e) {
            log.error("{} Exception:{}", Thread.currentThread().getName(), e);
            consumeRecord.setTransState(ConsumeTranStepEnum.SUSPEND_CURRENT_QUEUE_A_MOMENT.getCode());
            transMsgConsumeRecordService.updateMsgRecord(consumeRecord);
            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
        }
        return ConsumeOrderlyStatus.SUCCESS;
    }

    /**
     * 转换事务消息为事务消息日志
     *
     * @param msg
     * @return
     */
    private static TransMsgConsumeRecord convertMsgToRecord(MessageExt msg) {
        TransMsgConsumeRecord consumeRecord = new TransMsgConsumeRecord();
        consumeRecord.setTransId(msg.getTransactionId());
        consumeRecord.setMsgBody(new String(msg.getBody()));
        consumeRecord.setMsgId(msg.getMsgId());
        consumeRecord.setQueueId(msg.getQueueId());
        consumeRecord.setBornTime(new Date(msg.getBornTimestamp()));
        consumeRecord.setReconsumeTimes(msg.getReconsumeTimes());
        consumeRecord.setStoreTime(new Date(msg.getStoreTimestamp()));
        consumeRecord.setSysFlag(msg.getSysFlag());
        consumeRecord.setFlag(msg.getFlag());
        consumeRecord.setTopic(msg.getTopic());
        consumeRecord.setMsgKeys(msg.getKeys());

        consumeRecord.setMsgUniqKey(msg.getProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX));
        consumeRecord.setMsgTags(msg.getProperty(MessageConst.PROPERTY_TAGS));
        consumeRecord.setMsgGroup(msg.getProperty(MessageConst.PROPERTY_PRODUCER_GROUP));

        String tranMsg = msg.getProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED);
        consumeRecord.setMsgIsTran(StringUtils.isBlank(tranMsg) ? false : Boolean.parseBoolean(tranMsg));

        String msgWait = msg.getProperty(MessageConst.PROPERTY_WAIT_STORE_MSG_OK);
        consumeRecord.setMsgIsWait(StringUtils.isBlank(msgWait) ? false : Boolean.parseBoolean(msgWait));

        String realQueueId = msg.getProperty(MessageConst.PROPERTY_REAL_QUEUE_ID);
        consumeRecord.setMsgRealQueueId(StringUtils.isBlank(realQueueId) ? null : Integer.parseInt(realQueueId));

        String transCheckTimes = msg.getProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES);
        consumeRecord.setMsgTranCheckTimes(StringUtils.isBlank(transCheckTimes) ? null : Integer.parseInt(transCheckTimes));
        consumeRecord.setTransState(ConsumeTranStepEnum.NOT_CONSUME.getCode());
        consumeRecord.setCreateTime(new Date());
        return consumeRecord;
    }
}
````