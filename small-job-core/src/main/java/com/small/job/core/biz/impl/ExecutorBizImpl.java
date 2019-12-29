package com.small.job.core.biz.impl;

import com.small.job.core.biz.ExecutorBiz;
import com.small.job.core.biz.model.ReturnT;
import com.small.job.core.biz.model.TriggerParam;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/27/19 2:19 PM
 */
public class ExecutorBizImpl implements ExecutorBiz {

    @Override
    public ReturnT<String> beat() {
        return null;
    }

    @Override
    public ReturnT<String> idleBeat(int jobId) {
        return null;
    }

    @Override
    public ReturnT<String> kill(int jobId) {
        return null;
    }

    @Override
    public ReturnT<String> run(TriggerParam triggerParam) {
        return null;
    }
}
