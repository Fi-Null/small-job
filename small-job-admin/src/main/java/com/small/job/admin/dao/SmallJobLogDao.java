package com.small.job.admin.dao;

import com.small.job.admin.model.SmallJobLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/30/19 1:58 PM
 */
@Mapper
public interface SmallJobLogDao {

    // exist jobId not use jobGroup, not exist use jobGroup
    public List<SmallJobLog> pageList(@Param("offset") int offset,
                                      @Param("pagesize") int pagesize,
                                      @Param("jobGroup") int jobGroup,
                                      @Param("jobId") int jobId,
                                      @Param("triggerTimeStart") Date triggerTimeStart,
                                      @Param("triggerTimeEnd") Date triggerTimeEnd,
                                      @Param("logStatus") int logStatus);

    public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("jobGroup") int jobGroup,
                             @Param("jobId") int jobId,
                             @Param("triggerTimeStart") Date triggerTimeStart,
                             @Param("triggerTimeEnd") Date triggerTimeEnd,
                             @Param("logStatus") int logStatus);

    public SmallJobLog load(@Param("id") long id);

    public long save(SmallJobLog SmallJobLog);

    public int updateTriggerInfo(SmallJobLog SmallJobLog);

    public int updateHandleInfo(SmallJobLog SmallJobLog);

    public int delete(@Param("jobId") int jobId);

    public Map<String, Object> findLogReport(@Param("from") Date from,
                                             @Param("to") Date to);

    public List<Long> findClearLogIds(@Param("jobGroup") int jobGroup,
                                      @Param("jobId") int jobId,
                                      @Param("clearBeforeTime") Date clearBeforeTime,
                                      @Param("clearBeforeNum") int clearBeforeNum,
                                      @Param("pagesize") int pagesize);

    public int clearLog(@Param("logIds") List<Long> logIds);

    public List<Long> findFailJobLogIds(@Param("pagesize") int pagesize);

    public int updateAlarmStatus(@Param("logId") long logId,
                                 @Param("oldAlarmStatus") int oldAlarmStatus,
                                 @Param("newAlarmStatus") int newAlarmStatus);
}
