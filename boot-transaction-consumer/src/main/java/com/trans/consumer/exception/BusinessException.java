package com.trans.consumer.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @description:
 * @author: xiaoxu.nie
 * @date: 2018-08-19 23:42
 */
@Setter
@Getter
public class BusinessException extends RuntimeException {

    private Integer code;

    public BusinessException(String msg) {
        super(msg);
    }

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

}
