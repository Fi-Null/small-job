package com.small.job.admin.service;

import com.small.job.admin.model.SmallJobInfo;
import com.small.job.core.biz.model.ReturnT;

import java.util.Date;
import java.util.Map;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/30/19 2:41 PM
 */
public interface SmallJobService {
    /**
     * page list
     *
     * @param start
     * @param length
     * @param jobGroup
     * @param jobDesc
     * @param executorHandler
     * @param author
     * @return
     */
    public Map<String, Object> pageList(int start, int length, int jobGroup, int triggerStatus, String jobDesc, String executorHandler, String author);

    /**
     * add job
     *
     * @param jobInfo
     * @return
     */
    public ReturnT<String> add(SmallJobInfo jobInfo);

    /**
     * update job
     *
     * @param jobInfo
     * @return
     */
    public ReturnT<String> update(SmallJobInfo jobInfo);

    /**
     * remove job
     * *
     *
     * @param id
     * @return
     */
    public ReturnT<String> remove(int id);

    /**
     * start job
     *
     * @param id
     * @return
     */
    public ReturnT<String> start(int id);

    /**
     * stop job
     *
     * @param id
     * @return
     */
    public ReturnT<String> stop(int id);

    /**
     * dashboard info
     *
     * @return
     */
    public Map<String, Object> dashboardInfo();

    /**
     * chart info
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public ReturnT<Map<String, Object>> chartInfo(Date startDate, Date endDate);

}
