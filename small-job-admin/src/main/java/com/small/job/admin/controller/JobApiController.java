package com.small.job.admin.controller;

import com.small.job.admin.controller.annotation.PermissionLimit;
import com.small.job.admin.core.conf.SmallJobAdminConf;
import com.small.job.admin.util.JacksonUtil;
import com.small.job.core.biz.AdminBiz;
import com.small.job.core.biz.model.HandleCallbackParam;
import com.small.job.core.biz.model.RegistryParam;
import com.small.job.core.biz.model.ReturnT;
import com.small.job.core.util.HttpUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @ClassName JobApiController
 * @Description TODO
 * @Author xiangke
 * @Date 2020/1/1 22:48
 * @Version 1.0
 **/
@Controller
@RequestMapping("/api")
public class JobApiController {

    @Resource
    private AdminBiz adminBiz;

    // ---------------------- base ----------------------

    /**
     * valid access token
     */
    private void validAccessToken(HttpServletRequest request) {
        if (SmallJobAdminConf.getAdminConfig().getAccessToken() != null
                && SmallJobAdminConf.getAdminConfig().getAccessToken().trim().length() > 0
                && !SmallJobAdminConf.getAdminConfig().getAccessToken().equals(request.getHeader(HttpUtil.SMALL_JOB_ACCESS_TOKEN))) {
            throw new RuntimeException("The access token is wrong.");
        }
    }

    /**
     * parse Param
     */
    private Object parseParam(String data, Class<?> parametrized, Class<?>... parameterClasses) {
        Object param = null;
        try {
            if (parameterClasses != null) {
                param = JacksonUtil.readValue(data, parametrized, parameterClasses);
            } else {
                param = JacksonUtil.readValue(data, parametrized);
            }
        } catch (Exception e) {
        }
        if (param == null) {
            throw new RuntimeException("The request data invalid.");
        }
        return param;
    }


    @RequestMapping("/callback")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> callback(HttpServletRequest request, @RequestBody(required = false) String data) {
        // valid
        validAccessToken(request);

        // param
        List<HandleCallbackParam> callbackParamList = (List<HandleCallbackParam>) parseParam(data, List.class, HandleCallbackParam.class);

        // invoke
        return adminBiz.callback(callbackParamList);
    }

    /**
     * registry
     *
     * @param data
     * @return
     */
    @RequestMapping("/registry")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> registry(HttpServletRequest request, @RequestBody(required = false) String data) {
        // valid
        validAccessToken(request);

        // param
        RegistryParam registryParam = (RegistryParam) parseParam(data, RegistryParam.class);

        // invoke
        return adminBiz.registry(registryParam);
    }

    /**
     * registry remove
     *
     * @param data
     * @return
     */
    @RequestMapping("/registryRemove")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> registryRemove(HttpServletRequest request, @RequestBody(required = false) String data) {
        // valid
        validAccessToken(request);

        // param
        RegistryParam registryParam = (RegistryParam) parseParam(data, RegistryParam.class);

        // invoke
        return adminBiz.registryRemove(registryParam);
    }

}
