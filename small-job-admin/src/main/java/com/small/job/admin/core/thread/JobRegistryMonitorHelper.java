package com.small.job.admin.core.thread;

import com.small.job.admin.core.conf.SmallJobAdminConf;
import com.small.job.admin.model.SmallJobGroup;
import com.small.job.admin.model.SmallJobRegistry;
import com.small.job.core.enums.RegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 1/7/20 4:15 PM
 */
public class JobRegistryMonitorHelper {
    private static Logger logger = LoggerFactory.getLogger(JobRegistryMonitorHelper.class);

    private static JobRegistryMonitorHelper instance = new JobRegistryMonitorHelper();

    public static JobRegistryMonitorHelper getInstance() {
        return instance;
    }

    private Thread registryThread;

    private volatile boolean toStop = false;

    public void start() {
        registryThread = new Thread(() -> {
            while (!toStop) {
                try {
                    // auto registry group
                    List<SmallJobGroup> groupList = SmallJobAdminConf.getAdminConfig().getSmallJobGroupDao().findByAddressType(0);

                    if (!CollectionUtils.isEmpty(groupList)) {
                        // remove dead address (admin/executor)
                        List<SmallJobRegistry> registryList = SmallJobAdminConf.getAdminConfig().getSmallJobRegistryDao().findAll(RegistryConfig.DEAD_TIMEOUT, new Date());

                        List<Integer> ids = registryList.stream()
                                .map(smallJobRegistry -> smallJobRegistry.getId())
                                .collect(Collectors.toList());
                        if (!CollectionUtils.isEmpty(ids)) {
                            SmallJobAdminConf.getAdminConfig().getSmallJobRegistryDao().removeDead(ids);
                        }

                        // fresh online address (admin/executor)
                        Map<String, List<SmallJobRegistry>> appAddressMap = registryList.stream()
                                .filter(item -> RegistryConfig.RegistType.EXECUTOR.name().equals(item.getRegistryGroup()))
                                .collect(Collectors.groupingBy(SmallJobRegistry::getRegistryKey));

                        // fresh group address
                        groupList.stream().forEach(group -> {
                            List<SmallJobRegistry> smallRegistryList = appAddressMap.get(group.getAppName());
                            //sort
                            List<SmallJobRegistry> sortRegistryList = smallRegistryList.stream()
                                    .sorted(Comparator.comparing(SmallJobRegistry::getRegistryValue))
                                    .collect(Collectors.toList());
                            //conbine address
                            String addressListStr = sortRegistryList.stream()
                                    .map(registry -> registry.getRegistryValue())
                                    .collect(Collectors.joining(","));

                            group.setAddressList(addressListStr);
                            SmallJobAdminConf.getAdminConfig().getSmallJobGroupDao().update(group);
                        });

                    }
                } catch (Exception e) {
                    if (!toStop) {
                        logger.error(">>>>>>>>>>> Small-job, job registry monitor thread error:{}", e);
                    }
                }
                try {
                    TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                } catch (InterruptedException e) {
                    if (!toStop) {
                        logger.error(">>>>>>>>>>> Small-job, job registry monitor thread error:{}", e);
                    }
                }
            }
            logger.info(">>>>>>>>>>> Small-job, job registry monitor thread stop");
        });
        registryThread.setDaemon(true);
        registryThread.setName("Small-job, admin JobRegistryMonitorHelper");
        registryThread.start();
    }

    public void toStop() {
        toStop = true;
        // interrupt and wait
        registryThread.interrupt();
        try {
            registryThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
