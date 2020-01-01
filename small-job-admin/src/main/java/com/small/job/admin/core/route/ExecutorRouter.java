package com.small.job.admin.core.route;

import com.small.job.core.biz.model.ReturnT;
import com.small.job.core.biz.model.TriggerParam;

import java.util.List;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/30/19 3:41 PM
 */
public interface ExecutorRouter {
    /**
     * route address
     *
     * @param addressList
     * @return  ReturnT.content=address
     */
    ReturnT<String> route(TriggerParam triggerParam, List<String> addressList);

}
