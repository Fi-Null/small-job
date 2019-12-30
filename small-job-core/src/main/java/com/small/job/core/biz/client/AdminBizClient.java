package com.small.job.core.biz.client;

import com.small.job.core.biz.AdminBiz;
import com.small.job.core.biz.model.HandleCallbackParam;
import com.small.job.core.biz.model.RegistryParam;
import com.small.job.core.biz.model.ReturnT;
import com.small.job.core.util.HttpUtil;

import java.util.List;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/27/19 2:18 PM
 */
public class AdminBizClient implements AdminBiz {

    public AdminBizClient() {
    }

    public AdminBizClient(String addressUrl, String accessToken) {
        this.addressUrl = addressUrl;
        this.accessToken = accessToken;

        // valid
        if (!this.addressUrl.endsWith("/")) {
            this.addressUrl = this.addressUrl + "/";
        }
    }

    private String addressUrl;
    private String accessToken;


    @Override
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList) {
        return HttpUtil.postBody(addressUrl + "api/callback", accessToken, callbackParamList, 3);
    }

    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        return HttpUtil.postBody(addressUrl + "api/registry", accessToken, registryParam, 3);
    }

    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        return HttpUtil.postBody(addressUrl + "api/registryRemove", accessToken, registryParam, 3);
    }
}
