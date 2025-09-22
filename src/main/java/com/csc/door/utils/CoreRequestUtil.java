package com.csc.door.utils;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.csc.door.request.CoreRequest;
import com.csc.door.response.BaseResult;

import java.util.Map;

public class CoreRequestUtil {
    private String sourceUrl;
    private String targetUrl;

    private CoreRequestUtil(String sourceUrl, String targetUrl) {
        this.sourceUrl = sourceUrl;
        this.targetUrl = targetUrl;
    }

    public boolean heartbeat() {
        try {
            String result = HttpClientUtil.post(targetUrl + "/verify/connect", JSONObject.toJSONString(Map.of("nodeServer", sourceUrl)), null, 2000, 2000);
            BaseResult baseResult = JSONObject.parseObject(result, BaseResult.class);
            return baseResult.isSuccess();
        } catch (Exception e) {
            return false;
        }
    }

    public BaseResult touchSensing(CoreRequest param) {
        String result = HttpClientUtil.post(targetUrl + "/verify/touchSensing", JSONObject.toJSONString(param, JSONWriter.Feature.IgnoreNonFieldGetter));
        return JSONObject.parseObject(result, BaseResult.class);
    }

    public BaseResult card(CoreRequest param) {
        String result = HttpClientUtil.post(targetUrl + "/verify/card", JSONObject.toJSONString(param, JSONWriter.Feature.IgnoreNonFieldGetter));
        return JSONObject.parseObject(result, BaseResult.class);
    }

    public BaseResult qrcode(CoreRequest param) {
        String result = HttpClientUtil.post(targetUrl + "/verify/qrcode", JSONObject.toJSONString(param, JSONWriter.Feature.IgnoreNonFieldGetter));
        return JSONObject.parseObject(result, BaseResult.class);
    }

    public static CoreRequestUtil getInstance(String sourceUrl, String targetUrl ){
        return new CoreRequestUtil(sourceUrl, targetUrl);
    }
}
