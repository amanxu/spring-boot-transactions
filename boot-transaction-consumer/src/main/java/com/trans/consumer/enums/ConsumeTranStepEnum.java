package com.trans.consumer.enums;

import lombok.Getter;

import java.io.Serializable;

/**
 * @description:
 * @author: xiaoxu.nie
 * @date: 2018-08-29 11:10
 */
@Getter
public enum ConsumeTranStepEnum implements Serializable {
    NOT_CONSUME(0, "事务消息未消费"),
    SUCCESS(1, "事务消息消费成功"),
    SUSPEND_CURRENT_QUEUE_A_MOMENT(2, "挂起");
    private Integer code;
    private String msg;

    ConsumeTranStepEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsgByCode(Integer code) {
        for (ConsumeTranStepEnum errorCodeEnum : ConsumeTranStepEnum.values()) {
            if (errorCodeEnum.code.equals(code)) {
                return errorCodeEnum.getMsg();
            }
        }
        return null;
    }
}
