package com.small.job.admin.dao;

import com.small.job.admin.model.SmallJobInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SmallJobInfoDao {

    public List<SmallJobInfo> pageList(@Param("offset") int offset,
                                       @Param("pagesize") int pagesize,
                                       @Param("jobGroup") int jobGroup,
                                       @Param("triggerStatus") int triggerStatus,
                                       @Param("jobDesc") String jobDesc,
                                       @Param("executorHandler") String executorHandler,
                                       @Param("author") String author);
    public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("jobGroup") int jobGroup,
                             @Param("triggerStatus") int triggerStatus,
                             @Param("jobDesc") String jobDesc,
                             @Param("executorHandler") String executorHandler,
                             @Param("author") String author);

    public int save(SmallJobInfo info);

    public SmallJobInfo loadById(@Param("id") int id);

    public int update(SmallJobInfo SmallJobInfo);

    public int delete(@Param("id") long id);

    public List<SmallJobInfo> getJobsByGroup(@Param("jobGroup") int jobGroup);

    public int findAllCount();

    public List<SmallJobInfo> scheduleJobQuery(@Param("maxNextTime") long maxNextTime, @Param("pagesize") int pagesize );

    public int scheduleUpdate(SmallJobInfo SmallJobInfo);

}
