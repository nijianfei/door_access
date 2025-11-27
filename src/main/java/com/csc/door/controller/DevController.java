package com.csc.door.controller;

import com.alibaba.fastjson2.JSONObject;
import com.csc.door.response.BaseResult;
import com.csc.door.service.DoorCmdService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@Slf4j
@RestController
public class DevController {
    @Autowired
    private DoorCmdService doorCmdService;
    boolean status = true;

    @PostMapping("verify/connect")
    public Object connect(@RequestBody Map<String, Object> request) {
        log.debug("verify/connect mock 接收到客户端的请求:{}", JSONObject.toJSONString(request));
        BaseResult status = BaseResult.success();
        return status;
    }

    @PostMapping("verify/card")
    public Object card(@RequestBody Map<String, Object> request) {
        log.error("verify/card mock 接收到客户端的请求:{}", JSONObject.toJSONString(request));
        if (status) {
            status = false;
            return BaseResult.success();
        } else {
            status = true;
            return BaseResult.failure("出错了...");
        }
    }

    @PostMapping("verify/qrcode")
    public Object qrcode(@RequestBody Map<String, Object> request) {
        log.error("verify/qrcode mock 接收到客户端的请求:{}", JSONObject.toJSONString(request));
        if (status) {
            status = false;
            return BaseResult.success();
        } else {
            status = true;
            return BaseResult.failure("出错了...");
        }
    }

    @PostMapping("verify/touchSensing")
    public Object touchSensing(@RequestBody Map<String, Object> request) {
        log.error("verify/touchSensing mock 接收到客户端的请求:{}", JSONObject.toJSONString(request));
        if (status) {
            status = false;
            return BaseResult.success();
        } else {
            status = true;
            return BaseResult.failure("出错了...");
        }
    }

    @PostMapping("dev_ops/search_devices")
    public Object searchDevices() {
        doorCmdService.searchDevice();
        return BaseResult.success();
    }

    @GetMapping("/client-ip")
    public String getClientIp(HttpServletRequest request) {
        String ip = getClientIpAddress(request);
        return "Client IP: " + ip + System.lineSeparator();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_REAL_IP",
                "HTTP_PROXY_CLIENT_IP",
                "HTTP_WL_PROXY_CLIENT_IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                System.out.println(header + "_ip:" + ip);
                // X-Forwarded-For 可能包含多个 IP，第一个是原始客户端 IP
                if (header.equalsIgnoreCase("X-Forwarded-For")) {
                    ip = ip.split(",")[0].trim();
                }

            }
        }
        System.out.println("RemoteAddr:" + request.getRemoteAddr());
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For 可能包含多个 IP，第一个是原始客户端 IP
                if (header.equalsIgnoreCase("X-Forwarded-For")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        // 如果没有代理，直接获取 remoteAddr
        return request.getRemoteAddr();
    }
}

