package com.trans.producer.pojo;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @param <T>
 * @author niexx
 */
@Setter
@Getter
@ToString
public class Result<T> implements Serializable {

    /**
     * 结果码
     */
    private Integer code;

    /**
     * 结果描述
     */
    private String msg;

    /**
     * 数据域
     */
    private T data;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
