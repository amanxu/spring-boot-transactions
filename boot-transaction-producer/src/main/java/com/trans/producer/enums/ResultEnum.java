package com.trans.producer.enums;

import lombok.Getter;

/**
 * @description:
 * @author: xiaoxu.nie
 * @date: 2018-09-08 10:20
 */
@Getter
public enum ResultEnum {

    SUCCESS(0, "成功"),
    FAIL(1, "失败");

    private Integer code;
    private String msg;

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
