package com.small.job.admin.controller;

import com.small.job.admin.dao.SmallJobGroupDao;
import com.small.job.admin.dao.SmallJobInfoDao;
import com.small.job.admin.dao.SmallJobRegistryDao;
import com.small.job.admin.model.SmallJobGroup;
import com.small.job.admin.model.SmallJobRegistry;
import com.small.job.core.biz.model.ReturnT;
import com.small.job.core.enums.RegistryConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;

/**
 * @ClassName JobGroupController
 * @Description TODO
 * @Author xiangke
 * @Date 2020/1/1 23:19
 * @Version 1.0
 **/
@Controller
@RequestMapping("/jobgroup")
public class JobGroupController {

    @Resource
    public SmallJobInfoDao smallJobInfoDao;
    @Resource
    public SmallJobGroupDao smallJobGroupDao;
    @Resource
    private SmallJobRegistryDao smallJobRegistryDao;

    @RequestMapping
    public String index(Model model) {

        // job group (executor)
        List<SmallJobGroup> list = smallJobGroupDao.findAll();

        model.addAttribute("list", list);
        return "jobgroup/jobgroup.index";
    }

    @RequestMapping("/save")
    @ResponseBody
    public ReturnT<String> save(SmallJobGroup SmallJobGroup) {

        // valid
        if (SmallJobGroup.getAppName() == null || SmallJobGroup.getAppName().trim().length() == 0) {
            return new ReturnT<String>(500, ("system_please_input") + "AppName");
        }
        if (SmallJobGroup.getAppName().length() < 4 || SmallJobGroup.getAppName().length() > 64) {
            return new ReturnT<String>(500, "jobgroup_field_appName_length");
        }
        if (SmallJobGroup.getTitle() == null || SmallJobGroup.getTitle().trim().length() == 0) {
            return new ReturnT<String>(500, ("system_please_input" + "jobgroup_field_title"));
        }
        if (SmallJobGroup.getAddressType() != 0) {
            if (SmallJobGroup.getAddressList() == null || SmallJobGroup.getAddressList().trim().length() == 0) {
                return new ReturnT<String>(500, "jobgroup_field_addressType_limit");
            }
            String[] addresss = SmallJobGroup.getAddressList().split(",");
            for (String item : addresss) {
                if (item == null || item.trim().length() == 0) {
                    return new ReturnT<String>(500, "jobgroup_field_registryList_unvalid");
                }
            }
        }

        int ret = smallJobGroupDao.save(SmallJobGroup);
        return (ret > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
    }

    @RequestMapping("/update")
    @ResponseBody
    public ReturnT<String> update(SmallJobGroup SmallJobGroup) {
        // valid
        if (SmallJobGroup.getAppName() == null || SmallJobGroup.getAppName().trim().length() == 0) {
            return new ReturnT<String>(500, ("system_please_input" + "AppName"));
        }
        if (SmallJobGroup.getAppName().length() < 4 || SmallJobGroup.getAppName().length() > 64) {
            return new ReturnT<String>(500, "jobgroup_field_appName_length");
        }
        if (SmallJobGroup.getTitle() == null || SmallJobGroup.getTitle().trim().length() == 0) {
            return new ReturnT<String>(500, ("system_please_input" + "jobgroup_field_title"));
        }
        if (SmallJobGroup.getAddressType() == 0) {
            // 0=自动注册
            List<String> registryList = findRegistryByAppName(SmallJobGroup.getAppName());
            String addressListStr = null;
            if (registryList != null && !registryList.isEmpty()) {
                Collections.sort(registryList);
                addressListStr = "";
                for (String item : registryList) {
                    addressListStr += item + ",";
                }
                addressListStr = addressListStr.substring(0, addressListStr.length() - 1);
            }
            SmallJobGroup.setAddressList(addressListStr);
        } else {
            // 1=手动录入
            if (SmallJobGroup.getAddressList() == null || SmallJobGroup.getAddressList().trim().length() == 0) {
                return new ReturnT<String>(500, "jobgroup_field_addressType_limit");
            }
            String[] addresss = SmallJobGroup.getAddressList().split(",");
            for (String item : addresss) {
                if (item == null || item.trim().length() == 0) {
                    return new ReturnT<String>(500, "jobgroup_field_registryList_unvalid");
                }
            }
        }

        int ret = smallJobGroupDao.update(SmallJobGroup);
        return (ret > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
    }

    private List<String> findRegistryByAppName(String appNameParam) {
        HashMap<String, List<String>> appAddressMap = new HashMap<String, List<String>>();
        List<SmallJobRegistry> list = smallJobRegistryDao.findAll(RegistryConfig.DEAD_TIMEOUT, new Date());
        if (list != null) {
            for (SmallJobRegistry item : list) {
                if (RegistryConfig.RegistType.EXECUTOR.name().equals(item.getRegistryGroup())) {
                    String appName = item.getRegistryKey();
                    List<String> registryList = appAddressMap.get(appName);
                    if (registryList == null) {
                        registryList = new ArrayList<String>();
                    }

                    if (!registryList.contains(item.getRegistryValue())) {
                        registryList.add(item.getRegistryValue());
                    }
                    appAddressMap.put(appName, registryList);
                }
            }
        }
        return appAddressMap.get(appNameParam);
    }

    @RequestMapping("/remove")
    @ResponseBody
    public ReturnT<String> remove(int id) {

        // valid
        int count = smallJobInfoDao.pageListCount(0, 10, id, -1, null, null, null);
        if (count > 0) {
            return new ReturnT<String>(500, "jobgroup_del_limit_0");
        }

        List<SmallJobGroup> allList = smallJobGroupDao.findAll();
        if (allList.size() == 1) {
            return new ReturnT<String>(500, "jobgroup_del_limit_1");
        }

        int ret = smallJobGroupDao.remove(id);
        return (ret > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
    }

    @RequestMapping("/loadById")
    @ResponseBody
    public ReturnT<SmallJobGroup> loadById(int id) {
        SmallJobGroup jobGroup = smallJobGroupDao.load(id);
        return jobGroup != null ? new ReturnT<SmallJobGroup>(jobGroup) : new ReturnT<SmallJobGroup>(ReturnT.FAIL_CODE, null);
    }

}
