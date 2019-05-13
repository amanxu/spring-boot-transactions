package com.trans.producer.service.impl;

import com.trans.producer.enums.ErrorCodeEnum;
import com.trans.producer.exception.BusinessException;
import com.trans.producer.mapper.TransMsgStateRecordMapper;
import com.trans.producer.model.TransMsgStateRecord;
import com.trans.producer.service.ITransMsgStateRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description:
 * @author: xiaoxu.nie
 * @date: 2018-12-22 20:14
 */
@Service
public class TransMsgStateRecordServiceImpl implements ITransMsgStateRecordService {

    @Resource
    private TransMsgStateRecordMapper transMsgStateRecordMapper;

    @Override
    public void createTransMsg(TransMsgStateRecord msgStateRecord) {
        int result = transMsgStateRecordMapper.insertSelective(msgStateRecord);
        if (result <= 0) {
            throw new BusinessException(ErrorCodeEnum.TRANS_MSG_STATE_ADD_ERR.getCode(), ErrorCodeEnum.TRANS_MSG_STATE_ADD_ERR.getMsg());
        }
    }

    @Override
    public void updateTransMsg(TransMsgStateRecord msgStateRecord) {
        int result = transMsgStateRecordMapper.updateByPrimaryKeySelective(msgStateRecord);
        if (result <= 0) {
            throw new BusinessException(ErrorCodeEnum.TRANS_MSG_STATE_UPDATE_ERR.getCode(), ErrorCodeEnum.TRANS_MSG_STATE_UPDATE_ERR.getMsg());
        }
    }

    @Override
    public TransMsgStateRecord findMsgStateByTransId(String transId) {
        return transMsgStateRecordMapper.findRecordByTransId(transId);
    }

    @Override
    public TransMsgStateRecord findMsgStateByMsgKeys(String msgKeys) {
        return transMsgStateRecordMapper.findRecordByMsgKeys(msgKeys);
    }
}
