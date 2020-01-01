package com.small.job.admin.service.impl;

import com.small.job.admin.core.cron.CronExpression;
import com.small.job.admin.core.route.ExecutorRouteStrategyEnum;
import com.small.job.admin.core.thread.JobScheduleHelper;
import com.small.job.admin.dao.SmallJobGroupDao;
import com.small.job.admin.dao.SmallJobInfoDao;
import com.small.job.admin.dao.SmallJobLogDao;
import com.small.job.admin.model.SmallJobGroup;
import com.small.job.admin.model.SmallJobInfo;
import com.small.job.admin.service.SmallJobService;
import com.small.job.core.biz.model.ReturnT;
import com.small.job.core.enums.ExecutorBlockStrategyEnum;
import com.small.job.core.enums.GlueTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/30/19 2:42 PM
 */
@Service
public class SmallJobServiceImpl implements SmallJobService {

    private static Logger logger = LoggerFactory.getLogger(SmallJobServiceImpl.class);

    @Resource
    private SmallJobGroupDao smallJobGroupDao;
    @Resource
    private SmallJobInfoDao smallJobInfoDao;
    @Resource
    public SmallJobLogDao smallJobLogDao;

    @Override
    public Map<String, Object> pageList(int start, int length, int jobGroup, int triggerStatus, String jobDesc, String executorHandler, String author) {

        // page list
        List<SmallJobInfo> list = smallJobInfoDao.pageList(start, length, jobGroup, triggerStatus, jobDesc, executorHandler, author);
        int list_count = smallJobInfoDao.pageListCount(start, length, jobGroup, triggerStatus, jobDesc, executorHandler, author);

        // package result
        Map<String, Object> maps = new HashMap<>();
        maps.put("recordsTotal", list_count);        // 总记录数
        maps.put("recordsFiltered", list_count);    // 过滤后的总记录数
        maps.put("data", list);                    // 分页列表
        return maps;
    }

    @Override
    public ReturnT<String> add(SmallJobInfo jobInfo) {
        // valid
        SmallJobGroup group = smallJobGroupDao.load(jobInfo.getJobGroup());
        if (group == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("system_please_choose" + "jobinfo_field_jobgroup"));
        }
        if (!CronExpression.isValidExpression(jobInfo.getJobCron())) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "jobinfo_field_cron_unvalid");
        }
        if (jobInfo.getJobDesc() == null || jobInfo.getJobDesc().trim().length() == 0) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "system_please_input" + "jobinfo_field_jobdesc");
        }
        if (jobInfo.getAuthor() == null || jobInfo.getAuthor().trim().length() == 0) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "system_please_input" + "jobinfo_field_author");
        }
        if (ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null) == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "jobinfo_field_executorRouteStrategy" + "system_unvalid");
        }
        if (ExecutorBlockStrategyEnum.match(jobInfo.getExecutorBlockStrategy(), null) == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "jobinfo_field_executorBlockStrategy" + "system_unvalid");
        }
        if (GlueTypeEnum.match(jobInfo.getGlueType()) == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "jobinfo_field_gluetype" + "system_unvalid");
        }
        if (GlueTypeEnum.BEAN == GlueTypeEnum.match(jobInfo.getGlueType()) && (jobInfo.getExecutorHandler() == null || jobInfo.getExecutorHandler().trim().length() == 0)) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "system_please_input" + "JobHandler");
        }

        // add in db
        jobInfo.setAddTime(new Date());
        jobInfo.setUpdateTime(new Date());
        jobInfo.setGlueUpdatetime(new Date());
        smallJobInfoDao.save(jobInfo);
        if (jobInfo.getId() < 1) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "jobinfo_field_add" + "system_fail");
        }

        return new ReturnT<>(String.valueOf(jobInfo.getId()));
    }

    @Override
    public ReturnT<String> update(SmallJobInfo jobInfo) {
        // valid
        if (!CronExpression.isValidExpression(jobInfo.getJobCron())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "jobinfo_field_cron_unvalid");
        }
        if (jobInfo.getJobDesc() == null || jobInfo.getJobDesc().trim().length() == 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "system_please_input" + "jobinfo_field_jobdesc");
        }
        if (jobInfo.getAuthor() == null || jobInfo.getAuthor().trim().length() == 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "system_please_input" + "jobinfo_field_author");
        }
        if (ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null) == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "jobinfo_field_executorRouteStrategy" + "system_unvalid");
        }
        if (ExecutorBlockStrategyEnum.match(jobInfo.getExecutorBlockStrategy(), null) == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "jobinfo_field_executorBlockStrategy" + "system_unvalid");
        }

        // group valid
        SmallJobGroup jobGroup = smallJobGroupDao.load(jobInfo.getJobGroup());
        if (jobGroup == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "jobinfo_field_jobgroup" + "system_unvalid");
        }

        // stage job info
        SmallJobInfo exists_jobInfo = smallJobInfoDao.loadById(jobInfo.getId());
        if (exists_jobInfo == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "jobinfo_field_id" + "system_not_found");
        }

        // next trigger time (5s后生效，避开预读周期)
        long nextTriggerTime = exists_jobInfo.getTriggerNextTime();
        if (exists_jobInfo.getTriggerStatus() == 1 && !jobInfo.getJobCron().equals(exists_jobInfo.getJobCron())) {
            try {
                Date nextValidTime = new CronExpression(jobInfo.getJobCron()).getNextValidTimeAfter(new Date(System.currentTimeMillis() + JobScheduleHelper.PRE_READ_MS));
                if (nextValidTime == null) {
                    return new ReturnT<String>(ReturnT.FAIL_CODE, "jobinfo_field_cron_never_fire");
                }
                nextTriggerTime = nextValidTime.getTime();
            } catch (ParseException e) {
                logger.error(e.getMessage(), e);
                return new ReturnT<String>(ReturnT.FAIL_CODE, "jobinfo_field_cron_unvalid" + " | " + e.getMessage());
            }
        }

        exists_jobInfo.setJobGroup(jobInfo.getJobGroup());
        exists_jobInfo.setJobCron(jobInfo.getJobCron());
        exists_jobInfo.setJobDesc(jobInfo.getJobDesc());
        exists_jobInfo.setAuthor(jobInfo.getAuthor());
        exists_jobInfo.setAlarmEmail(jobInfo.getAlarmEmail());
        exists_jobInfo.setExecutorRouteStrategy(jobInfo.getExecutorRouteStrategy());
        exists_jobInfo.setExecutorHandler(jobInfo.getExecutorHandler());
        exists_jobInfo.setExecutorParam(jobInfo.getExecutorParam());
        exists_jobInfo.setExecutorBlockStrategy(jobInfo.getExecutorBlockStrategy());
        exists_jobInfo.setExecutorTimeout(jobInfo.getExecutorTimeout());
        exists_jobInfo.setExecutorFailRetryCount(jobInfo.getExecutorFailRetryCount());
        exists_jobInfo.setTriggerNextTime(nextTriggerTime);

        exists_jobInfo.setUpdateTime(new Date());
        smallJobInfoDao.update(exists_jobInfo);


        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> remove(int id) {
        SmallJobInfo smallJobInfo = smallJobInfoDao.loadById(id);
        if (smallJobInfo == null) {
            return ReturnT.SUCCESS;
        }

        smallJobInfoDao.delete(id);
        smallJobInfoDao.delete(id);
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> start(int id) {
        SmallJobInfo smallJobInfo = smallJobInfoDao.loadById(id);

        // next trigger time (5s后生效，避开预读周期)
        long nextTriggerTime = 0;
        try {
            Date nextValidTime = new CronExpression(smallJobInfo.getJobCron()).getNextValidTimeAfter(new Date(System.currentTimeMillis() + JobScheduleHelper.PRE_READ_MS));
            if (nextValidTime == null) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "jobinfo_field_cron_never_fire");
            }
            nextTriggerTime = nextValidTime.getTime();
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
            return new ReturnT<String>(ReturnT.FAIL_CODE, "jobinfo_field_cron_unvalid" + " | "+ e.getMessage());
        }

        smallJobInfo.setTriggerStatus(1);
        smallJobInfo.setTriggerLastTime(0);
        smallJobInfo.setTriggerNextTime(nextTriggerTime);

        smallJobInfo.setUpdateTime(new Date());
        smallJobInfoDao.update(smallJobInfo);
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> stop(int id) {
        SmallJobInfo smallJobInfo = smallJobInfoDao.loadById(id);

        smallJobInfo.setTriggerStatus(0);
        smallJobInfo.setTriggerLastTime(0);
        smallJobInfo.setTriggerNextTime(0);

        smallJobInfo.setUpdateTime(new Date());
        smallJobInfoDao.update(smallJobInfo);
        return ReturnT.SUCCESS;
    }

    @Override
    public Map<String, Object> dashboardInfo() {
        return null;
    }

    @Override
    public ReturnT<Map<String, Object>> chartInfo(Date startDate, Date endDate) {
        return null;
    }

    private boolean isNumeric(String str) {
        try {
            int result = Integer.valueOf(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
