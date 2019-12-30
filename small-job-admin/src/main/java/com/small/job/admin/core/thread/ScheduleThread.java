package com.small.job.admin.core.thread;

import com.small.job.admin.core.conf.SmallJobAdminConf;
import com.small.job.admin.core.cron.CronExpression;
import com.small.job.admin.core.enums.TriggerTypeEnum;
import com.small.job.admin.model.SmallJobInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/30/19 8:31 PM
 */
public class ScheduleThread extends Thread {

    private static Logger logger = LoggerFactory.getLogger(ScheduleThread.class);

    @Override
    public void run() {

        boolean scheduleThreadToStop = JobScheduleHelper.isScheduleThreadToStop();

        try {
            TimeUnit.MILLISECONDS.sleep(5000 - System.currentTimeMillis() % 1000);
        } catch (InterruptedException e) {
            if (!scheduleThreadToStop) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.info(">>>>>>>>> init small-job admin scheduler success.");

        // pre-read count: treadpool-size * trigger-qps (each trigger cost 50ms, qps = 1000/50 = 20)
        int preReadCount = (SmallJobAdminConf.getAdminConfig().getTriggerPoolFastMax() + SmallJobAdminConf.getAdminConfig().getTriggerPoolSlowMax()) * 20;

        while (!scheduleThreadToStop) {
            // Scan Job
            long start = System.currentTimeMillis();

            Connection conn = null;
            Boolean connAutoCommit = null;
            PreparedStatement preparedStatement = null;

            boolean preReadSuc = true;
            try {

                conn = SmallJobAdminConf.getAdminConfig().getDataSource().getConnection();
                connAutoCommit = conn.getAutoCommit();
                conn.setAutoCommit(false);

                preparedStatement = conn.prepareStatement("select * from small_job_lock where lock_name = 'schedule_lock' for update");
                preparedStatement.execute();

                // tx start

                // 1、pre read
                long nowTime = System.currentTimeMillis();
                List<SmallJobInfo> scheduleList = SmallJobAdminConf.getAdminConfig()
                        .getSmallJobInfoDao()
                        .scheduleJobQuery(nowTime + JobScheduleHelper.PRE_READ_MS, preReadCount);
                if (scheduleList != null && scheduleList.size() > 0) {
                    // 2、push time-ring
                    for (SmallJobInfo jobInfo : scheduleList) {

                        // time-ring jump
                        if (nowTime > jobInfo.getTriggerNextTime() + JobScheduleHelper.PRE_READ_MS) {
                            // 2.1、trigger-expire > 5s：pass && make next-trigger-time
                            logger.warn(">>>>>>>>>>> small-job, schedule misfire, jobId = " + jobInfo.getId());

                            // fresh next
                            refreshNextValidTime(jobInfo, new Date());

                        } else if (nowTime > jobInfo.getTriggerNextTime()) {
                            // 2.2、trigger-expire < 5s：direct-trigger && make next-trigger-time

                            // 1、trigger
                            JobTriggerPoolHelper.trigger(jobInfo.getId(), TriggerTypeEnum.CRON, -1, null, null);
                            logger.debug(">>>>>>>>>>> small-job, schedule push trigger : jobId = " + jobInfo.getId());

                            // 2、fresh next
                            refreshNextValidTime(jobInfo, new Date());

                            // next-trigger-time in 5s, pre-read again
                            if (jobInfo.getTriggerStatus() == 1 && nowTime + JobScheduleHelper.PRE_READ_MS > jobInfo.getTriggerNextTime()) {

                                // 1、make ring second
                                int ringSecond = (int) ((jobInfo.getTriggerNextTime() / 1000) % 60);

                                // 2、push time ring
                                pushTimeRing(ringSecond, jobInfo.getId());

                                // 3、fresh next
                                refreshNextValidTime(jobInfo, new Date(jobInfo.getTriggerNextTime()));

                            }

                        } else {
                            // 2.3、trigger-pre-read：time-ring trigger && make next-trigger-time

                            // 1、make ring second
                            int ringSecond = (int) ((jobInfo.getTriggerNextTime() / 1000) % 60);

                            // 2、push time ring
                            pushTimeRing(ringSecond, jobInfo.getId());

                            // 3、fresh next
                            refreshNextValidTime(jobInfo, new Date(jobInfo.getTriggerNextTime()));

                        }

                    }

                    // 3、update trigger info
                    for (SmallJobInfo jobInfo : scheduleList) {
                        SmallJobAdminConf.getAdminConfig().getSmallJobInfoDao().scheduleUpdate(jobInfo);
                    }

                } else {
                    preReadSuc = false;
                }

                // tx stop
            } catch (Exception e) {
                if (!scheduleThreadToStop) {
                    logger.error(">>>>>>>>>>> small-job, JobScheduleHelper#scheduleThread error:{}", e);
                }
            } finally {
                // commit
                if (conn != null) {
                    try {
                        conn.commit();
                    } catch (SQLException e) {
                        if (!scheduleThreadToStop) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    try {
                        conn.setAutoCommit(connAutoCommit);
                    } catch (SQLException e) {
                        if (!scheduleThreadToStop) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        if (!scheduleThreadToStop) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }

                // close PreparedStatement
                if (null != preparedStatement) {
                    try {
                        preparedStatement.close();
                    } catch (SQLException e) {
                        if (!scheduleThreadToStop) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
            long cost = System.currentTimeMillis() - start;


            // Wait seconds, align second
            if (cost < 1000) {  // scan-overtime, not wait
                try {
                    // pre-read period: success > scan each second; fail > skip this period;
                    TimeUnit.MILLISECONDS.sleep((preReadSuc ? 1000 : JobScheduleHelper.PRE_READ_MS) - System.currentTimeMillis() % 1000);
                } catch (InterruptedException e) {
                    if (!scheduleThreadToStop) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }

        }

        logger.info(">>>>>>>>>>> small-job, JobScheduleHelper#scheduleThread stop");
    }

    private void refreshNextValidTime(SmallJobInfo jobInfo, Date fromTime) throws ParseException {
        Date nextValidTime = new CronExpression(jobInfo.getJobCron()).getNextValidTimeAfter(fromTime);
        if (nextValidTime != null) {
            jobInfo.setTriggerLastTime(jobInfo.getTriggerNextTime());
            jobInfo.setTriggerNextTime(nextValidTime.getTime());
        } else {
            jobInfo.setTriggerStatus(0);
            jobInfo.setTriggerLastTime(0);
            jobInfo.setTriggerNextTime(0);
        }
    }

    private void pushTimeRing(int ringSecond, int jobId) {
        // push async ring
        List<Integer> ringItemData = JobScheduleHelper.getRingData().get(ringSecond);
        if (ringItemData == null) {
            ringItemData = new ArrayList<>();
            JobScheduleHelper.getRingData().put(ringSecond, ringItemData);
        }
        ringItemData.add(jobId);

        logger.debug(">>>>>>>>>>> small-job, schedule push time-ring : "
                + ringSecond + " = " + Arrays.asList(ringItemData));
    }

}
