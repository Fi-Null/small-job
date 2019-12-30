package com.small.job.admin.core.thread;

import com.small.job.admin.core.enums.TriggerTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/30/19 8:43 PM
 */
public class RingThread extends Thread {
    private static Logger logger = LoggerFactory.getLogger(RingThread.class);

    @Override
    public void run() {
        boolean ringThreadToStop = JobScheduleHelper.isRingThreadToStop();

        // align second
        try {
            TimeUnit.MILLISECONDS.sleep(1000 - System.currentTimeMillis() % 1000);
        } catch (InterruptedException e) {
            if (!ringThreadToStop) {
                logger.error(e.getMessage(), e);
            }
        }

        while (!ringThreadToStop) {

            try {
                // second data
                List<Integer> ringItemData = new ArrayList<>();
                int nowSecond = Calendar.getInstance().get(Calendar.SECOND);   // 避免处理耗时太长，跨过刻度，向前校验一个刻度；
                for (int i = 0; i < 2; i++) {
                    List<Integer> tmpData = JobScheduleHelper.getRingData().remove((nowSecond + 60 - i) % 60);
                    if (tmpData != null) {
                        ringItemData.addAll(tmpData);
                    }
                }

                // ring trigger
                logger.debug(">>>>>>>>>>> small-job, time-ring beat : " + nowSecond + " = " + Arrays.asList(ringItemData));
                if (ringItemData.size() > 0) {
                    // do trigger
                    for (int jobId : ringItemData) {
                        // do trigger
                        JobTriggerPoolHelper.trigger(jobId, TriggerTypeEnum.CRON, -1, null, null);
                    }
                    // clear
                    ringItemData.clear();
                }
            } catch (Exception e) {
                if (!ringThreadToStop) {
                    logger.error(">>>>>>>>>>> small-job, JobScheduleHelper#ringThread error:{}", e);
                }
            }

            // next second, align second
            try {
                TimeUnit.MILLISECONDS.sleep(1000 - System.currentTimeMillis() % 1000);
            } catch (InterruptedException e) {
                if (!ringThreadToStop) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        logger.info(">>>>>>>>>>> small-job, JobScheduleHelper#ringThread stop");
    }
}
