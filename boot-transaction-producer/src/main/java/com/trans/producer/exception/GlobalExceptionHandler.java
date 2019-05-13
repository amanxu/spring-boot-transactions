package com.trans.producer.exception;

import com.trans.producer.enums.ErrorCodeEnum;
import com.trans.producer.pojo.Result;
import com.trans.producer.utils.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description:
 * @author: xiaoxu.nie
 * @date: 2018-08-19 16:00
 */
@Slf4j
@ControllerAdvice(basePackages = {"com.trans"})
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Result defaultExceptionHandler(Exception exception) {

        return ResultUtil.error(ErrorCodeEnum.BIZ_ERR.getCode(), exception.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = BusinessException.class)
    public Result businessExceptionHandler(BusinessException ex) {

        return ResultUtil.error(ex.getCode(), ex.getMessage());
    }
}
