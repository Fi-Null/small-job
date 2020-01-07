package com.small.job.admin.core.scheduler;

import com.small.job.admin.core.conf.SmallJobAdminConf;
import com.small.job.admin.core.thread.JobRegistryMonitorHelper;
import com.small.job.admin.core.thread.JobScheduleHelper;
import com.small.job.admin.core.thread.JobTriggerPoolHelper;
import com.small.job.core.biz.ExecutorBiz;
import com.small.rpc.remoting.invoker.call.CallType;
import com.small.rpc.remoting.invoker.reference.RpcReferenceBean;
import com.small.rpc.remoting.invoker.route.LoadBalance;
import com.small.rpc.remoting.net.netty_http.client.NettyHttpClient;
import com.small.rpc.serialize.hessian.HessianSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/30/19 4:13 PM
 */
public class SmallJobScheduler {
    private static final Logger logger = LoggerFactory.getLogger(SmallJobScheduler.class);

    public void init() throws Exception {

        // admin registry monitor run
        JobRegistryMonitorHelper.getInstance().start();

        // admin trigger pool start
        JobTriggerPoolHelper.toStart();

        // start-schedule
        JobScheduleHelper.getInstance().start();

        logger.info(">>>>>>>>> init small-job admin success.");
    }

    public void destroy() throws Exception {

        // stop-schedule
        JobScheduleHelper.getInstance().toStop();

        // admin trigger pool stop
        JobTriggerPoolHelper.toStop();

        // admin registry stop
        JobRegistryMonitorHelper.getInstance().toStop();
    }

    // ---------------------- executor-client ----------------------
    private static ConcurrentMap<String, ExecutorBiz> executorBizRepository = new ConcurrentHashMap<String, ExecutorBiz>();

    public static ExecutorBiz getExecutorBiz(String address) throws Exception {
        // valid
        if (address == null || address.trim().length() == 0) {
            return null;
        }

        // load-cache
        address = address.trim();
        ExecutorBiz executorBiz = executorBizRepository.get(address);
        if (executorBiz != null) {
            return executorBiz;
        }

        // set-cache
        RpcReferenceBean referenceBean = new RpcReferenceBean();
        referenceBean.setClient(NettyHttpClient.class);
        referenceBean.setSerializer(HessianSerializer.class);
        referenceBean.setCallType(CallType.SYNC);
        referenceBean.setLoadBalance(LoadBalance.ROUND);
        referenceBean.setIface(ExecutorBiz.class);
        referenceBean.setVersion(null);
        referenceBean.setTimeout(3000);
        referenceBean.setAddress(address);
        referenceBean.setAccessToken(SmallJobAdminConf.getAdminConfig().getAccessToken());
        referenceBean.setInvokeCallback(null);
        referenceBean.setInvokerFactory(null);

        executorBiz = (ExecutorBiz) referenceBean.getObject();

        executorBizRepository.put(address, executorBiz);
        return executorBiz;
    }
}
