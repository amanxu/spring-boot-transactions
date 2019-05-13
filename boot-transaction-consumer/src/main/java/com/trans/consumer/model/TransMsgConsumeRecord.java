package com.trans.consumer.model;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xiaoxu.nie
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TransMsgConsumeRecord implements Serializable {

    private Long id;

    private String topic;

    private Integer flag;

    private String msgBody;

    private String msgId;

    private Date bornTime;

    private Date storeTime;

    private Integer reconsumeTimes;

    private Integer queueId;

    private Integer sysFlag;

    private String transId;

    private Integer transState;

    private String msgKeys;

    private Boolean msgIsTran;

    private Integer msgTranCheckTimes;

    private String msgUniqKey;

    private Boolean msgIsWait;

    private String msgGroup;

    private String msgTags;

    private Integer msgRealQueueId;

    private Integer bizType;

    private Date createTime;

    private Date updateTime;
}