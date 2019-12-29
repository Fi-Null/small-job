package com.small.job.core.handler;

import com.small.job.core.biz.model.ReturnT;

import java.lang.reflect.InvocationTargetException;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/27/19 2:42 PM
 */
public interface IJobHandler {
    /**
     * success
     */
    ReturnT<String> SUCCESS = new ReturnT<String>(200, null);
    /**
     * fail
     */
    ReturnT<String> FAIL = new ReturnT<String>(500, null);
    /**
     * fail timeout
     */
    ReturnT<String> FAIL_TIMEOUT = new ReturnT<String>(502, null);


    /**
     * execute handler, invoked when executor receives a scheduling request
     *
     * @param param
     * @return
     * @throws Exception
     */
    public abstract ReturnT<String> execute(String param) throws Exception;


    /**
     * init handler, invoked when JobThread init
     */
    public void init() throws InvocationTargetException, IllegalAccessException;


    /**
     * destroy handler, invoked when JobThread destroy
     */
    public void destroy() throws InvocationTargetException, IllegalAccessException;

}
