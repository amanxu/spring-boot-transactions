package com.trans.producer.enums;

import lombok.Getter;

import java.io.Serializable;

/**
 * @description:
 * @author: xiaoxu.nie
 * @date: 2018-08-29 11:10
 */
@Getter
public enum ProducerTranStepEnum implements Serializable {
    UN_KNOW(0, "Prepare发送"),
    COMMIT_MESSAGE(1, "Commit事务提交"),
    ROLLBACK_MESSAGE(2, "RollBack事务回滚");
    private Integer code;
    private String msg;

    ProducerTranStepEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsgByCode(Integer code) {
        for (ProducerTranStepEnum errorCodeEnum : ProducerTranStepEnum.values()) {
            if (errorCodeEnum.code.equals(code)) {
                return errorCodeEnum.getMsg();
            }
        }
        return null;
    }
}
