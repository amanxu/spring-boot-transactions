package com.trans.consumer.enums;

import lombok.Getter;

import java.io.Serializable;

/**
 * @description:
 * @author: xiaoxu.nie
 * @date: 2018-08-29 11:10
 */
@Getter
public enum ErrorCodeEnum implements Serializable {

    TRANS_MSG_STATE_ADD_ERR(600001, "新增事务消息状态日志失败"),
    TRANS_MSG_STATE_EXISTS_ERR(600002, "事务消息状态日志不存在"),
    TRANS_MSG_STATE_UPDATE_ERR(600003, "更新事务消息状态日志失败"),
    TRANS_MSG_CONSUME_LOG_ADD_ERR(600006, "新增消费事务消息日志失败"),
    TRANS_MSG_CONSUME_LOG_UPDATE_ERR(600007, "事务消息日志更新失败"),
    ROCKET_MQ_SEND_FAIL(900005, "RocketMq消息发送失败"),
    TRANS_ROLLBACK_ERR(900006, "事务回滚");

    private Integer code;
    private String msg;

    ErrorCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsgByCode(Integer code) {
        for (ErrorCodeEnum errorCodeEnum : ErrorCodeEnum.values()) {
            if (errorCodeEnum.code.equals(code)) {
                return errorCodeEnum.getMsg();
            }
        }
        return null;
    }
}
