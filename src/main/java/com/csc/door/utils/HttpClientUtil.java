package com.csc.door.utils;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class HttpClientUtil {

    private static final int CONNECT_TIMEOUT = 5 * 1000;
    private static final int SOCKET_TIMEOUT = 10 * 1000;

    public static String get(String url, Map<String, Object> params) {
        return get(url, params, null, null, null);
    }

    public static String get(String url, Map<String, Object> params, Map<String, String> heads, Integer connectTimeout, Integer socketTimeout) {
        connectTimeout = connectTimeout != null ? connectTimeout : CONNECT_TIMEOUT;
        socketTimeout = socketTimeout != null ? socketTimeout : SOCKET_TIMEOUT;
        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout).build();
        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build()) {
            String newUrl = buildFullUrl(url, params);
            HttpGet request = new HttpGet(newUrl);
            // 添加请求头（可选）
            if (heads != null) {
                heads.forEach((k, v) -> request.setHeader(k, v));
            }
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String result = EntityUtils.toString(response.getEntity());
                log.debug("get url:{} params:{} code:{} result:{}", url, JSONObject.toJSONString(params), response.getStatusLine().getStatusCode(), result);
                return result;
            }
        } catch (Exception e) {
            log.error("get url:{} params:{} err:{}", url, JSONObject.toJSONString(params), e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static String buildFullUrl(String url, Map<String, Object> params) {
        if (params == null) {
            return url;
        }
        String param = params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).sorted().collect(Collectors.joining("&"));
        String hyphen = url.contains("?") ? "&" : "?" ;
        return url + hyphen + param;
    }


    public static String post(String url, String json) {
        return post(url, json, null, null, null);
    }

    public static String post(String url, String json, Map<String, String> heads, Integer connectTimeout, Integer socketTimeout) {
        connectTimeout = connectTimeout != null ? connectTimeout : CONNECT_TIMEOUT;
        socketTimeout = socketTimeout != null ? socketTimeout : SOCKET_TIMEOUT;
        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout).build();
        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build()) {
            HttpPost post = new HttpPost(url);
            // 设置 JSON 请求体
            StringEntity entity = new StringEntity(json, Charset.forName("utf8"));
            entity.setContentType("application/json");
            post.setEntity(entity);
            // 添加请求头
            post.addHeader("Content-Type", "application/json");
            if (heads != null) {
                heads.forEach((k, v) -> post.setHeader(k, v));
            }
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                String result = EntityUtils.toString(response.getEntity());
                log.debug("post url:{} params:{} code:{} result:{}", url, JSONObject.toJSONString(json), response.getStatusLine().getStatusCode(), result);
                return result;
            }
        } catch (Exception e) {
            log.error("post url:{} params:{} err:{}", url, json, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
