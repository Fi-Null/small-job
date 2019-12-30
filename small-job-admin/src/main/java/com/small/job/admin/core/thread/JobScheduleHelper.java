package com.small.job.admin.core.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/30/19 3:54 PM
 */
public class JobScheduleHelper {
    private static Logger logger = LoggerFactory.getLogger(JobScheduleHelper.class);

    public static final long PRE_READ_MS = 5000;    // pre read

    private static JobScheduleHelper instance = new JobScheduleHelper();

    public static JobScheduleHelper getInstance() {
        return instance;
    }

    private Thread scheduleThread;
    private Thread ringThread;
    private static volatile boolean scheduleThreadToStop = false;
    private static volatile boolean ringThreadToStop = false;
    private static volatile Map<Integer, List<Integer>> ringData = new ConcurrentHashMap<>();

    public void start() {
        // schedule thread
        scheduleThread = new ScheduleThread();
        scheduleThread.setDaemon(true);
        scheduleThread.setName("small-job, admin JobScheduleHelper#scheduleThread");
        scheduleThread.start();

        // ring thread
        ringThread = new RingThread();
        ringThread.setDaemon(true);
        ringThread.setName("small-job, admin JobScheduleHelper#ringThread");
        ringThread.start();
    }


    public void toStop() {
        // 1、stop schedule
        scheduleThreadToStop = true;
        try {
            TimeUnit.SECONDS.sleep(1);  // wait
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        if (scheduleThread.getState() != Thread.State.TERMINATED) {
            // interrupt and wait
            scheduleThread.interrupt();
            try {
                scheduleThread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        // if has ring data
        boolean hasRingData = false;
        if (!ringData.isEmpty()) {
            for (int second : ringData.keySet()) {
                List<Integer> tmpData = ringData.get(second);
                if (tmpData != null && tmpData.size() > 0) {
                    hasRingData = true;
                    break;
                }
            }
        }
        if (hasRingData) {
            try {
                TimeUnit.SECONDS.sleep(8);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        // stop ring (wait job-in-memory stop)
        ringThreadToStop = true;
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        if (ringThread.getState() != Thread.State.TERMINATED) {
            // interrupt and wait
            ringThread.interrupt();
            try {
                ringThread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        logger.info(">>>>>>>>>>> small-job, JobScheduleHelper stop");
    }


    public static Map<Integer, List<Integer>> getRingData() {
        return ringData;
    }

    public static void setRingData(Map<Integer, List<Integer>> ringData) {
        JobScheduleHelper.ringData = ringData;
    }

    public static boolean isScheduleThreadToStop() {
        return scheduleThreadToStop;
    }

    public static void setScheduleThreadToStop(boolean scheduleThreadToStop) {
        JobScheduleHelper.scheduleThreadToStop = scheduleThreadToStop;
    }

    public static boolean isRingThreadToStop() {
        return ringThreadToStop;
    }

    public static void setRingThreadToStop(boolean ringThreadToStop) {
        JobScheduleHelper.ringThreadToStop = ringThreadToStop;
    }
}
