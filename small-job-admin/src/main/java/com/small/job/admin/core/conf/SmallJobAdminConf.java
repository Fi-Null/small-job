package com.small.job.admin.core.conf;

import com.small.job.admin.core.scheduler.SmallJobScheduler;
import com.small.job.admin.dao.SmallJobGroupDao;
import com.small.job.admin.dao.SmallJobInfoDao;
import com.small.job.admin.dao.SmallJobLogDao;
import com.small.job.admin.dao.SmallJobRegistryDao;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/30/19 4:21 PM
 */
@Component
public class SmallJobAdminConf implements InitializingBean, DisposableBean {

    private static SmallJobAdminConf adminConfig = null;

    public static SmallJobAdminConf getAdminConfig() {
        return adminConfig;
    }


    // ---------------------- smallJobScheduler ----------------------
    private SmallJobScheduler smallJobScheduler;

    @Override
    public void afterPropertiesSet() throws Exception {
        adminConfig = this;

        smallJobScheduler = new SmallJobScheduler();
        smallJobScheduler.init();
    }

    @Override
    public void destroy() throws Exception {
        smallJobScheduler.destroy();
    }

    // ---------------------- smallJobScheduler ----------------------
    // conf

    @Value("${small.job.accessToken}")
    private String accessToken;

    @Value("${spring.mail.username}")
    private String emailUserName;

    @Value("${small.job.triggerpool.fast.max}")
    private int triggerPoolFastMax;

    @Value("${small.job.triggerpool.slow.max}")
    private int triggerPoolSlowMax;

    // dao, service
    @Resource
    private SmallJobLogDao smallJobLogDao;
    @Resource
    private SmallJobInfoDao smallJobInfoDao;
    @Resource
    private SmallJobRegistryDao smallJobRegistryDao;
    @Resource
    private SmallJobGroupDao smallJobGroupDao;

    @Resource
    private JavaMailSender mailSender;
    @Resource
    private DataSource dataSource;


    public String getAccessToken() {
        return accessToken;
    }

    public String getEmailUserName() {
        return emailUserName;
    }

    public int getTriggerPoolFastMax() {
        if (triggerPoolFastMax < 200) {
            return 200;
        }
        return triggerPoolFastMax;
    }

    public int getTriggerPoolSlowMax() {
        if (triggerPoolSlowMax < 100) {
            return 100;
        }
        return triggerPoolSlowMax;
    }

    public SmallJobLogDao getSmallJobLogDao() {
        return smallJobLogDao;
    }

    public void setSmallJobLogDao(SmallJobLogDao smallJobLogDao) {
        this.smallJobLogDao = smallJobLogDao;
    }

    public SmallJobInfoDao getSmallJobInfoDao() {
        return smallJobInfoDao;
    }

    public void setSmallJobInfoDao(SmallJobInfoDao smallJobInfoDao) {
        this.smallJobInfoDao = smallJobInfoDao;
    }

    public SmallJobRegistryDao getSmallJobRegistryDao() {
        return smallJobRegistryDao;
    }

    public void setSmallJobRegistryDao(SmallJobRegistryDao smallJobRegistryDao) {
        this.smallJobRegistryDao = smallJobRegistryDao;
    }

    public SmallJobGroupDao getSmallJobGroupDao() {
        return smallJobGroupDao;
    }

    public void setSmallJobGroupDao(SmallJobGroupDao smallJobGroupDao) {
        this.smallJobGroupDao = smallJobGroupDao;
    }

    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}