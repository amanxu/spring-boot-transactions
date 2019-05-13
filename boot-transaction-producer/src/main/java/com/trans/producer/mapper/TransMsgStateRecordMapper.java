package com.trans.producer.mapper;

import com.trans.producer.model.TransMsgStateRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TransMsgStateRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TransMsgStateRecord record);

    int insertSelective(TransMsgStateRecord record);

    TransMsgStateRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TransMsgStateRecord record);

    int updateByPrimaryKey(TransMsgStateRecord record);

    /**
     * 根据事务ID查询事务消息日志
     *
     * @param transId
     * @return
     */
    TransMsgStateRecord findRecordByTransId(@Param("transId") String transId);

    /**
     * 根据消息的keys查询消息的日志记录
     *
     * @param msgKeys
     * @return
     */
    TransMsgStateRecord findRecordByMsgKeys(@Param("msgKeys") String msgKeys);
}