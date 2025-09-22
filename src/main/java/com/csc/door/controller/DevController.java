package com.csc.door.controller;

import com.alibaba.fastjson2.JSONObject;
import com.csc.door.response.BaseResult;
import com.csc.door.service.DoorCmdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Object connect(@RequestBody Map<String,Object> request) {
        log.debug("verify/connect mock 接收到客户端的请求:{}", JSONObject.toJSONString(request));
        BaseResult status = BaseResult.success();
        return status;
    }

    @PostMapping("verify/card")
    public Object card(@RequestBody Map<String,Object> request) {
        log.error("verify/card mock 接收到客户端的请求:{}", JSONObject.toJSONString(request));
        if (status) {
            status = false;
            return BaseResult.success();
        }else{
            status = true;
            return BaseResult.failure("出错了...");
        }
    }

    @PostMapping("verify/qrcode")
    public Object qrcode(@RequestBody Map<String,Object> request) {
        log.error("verify/qrcode mock 接收到客户端的请求:{}", JSONObject.toJSONString(request));
        if (status) {
            status = false;
            return BaseResult.success();
        }else{
            status = true;
            return BaseResult.failure("出错了...");
        }
    }

    @PostMapping("verify/touchSensing")
    public Object touchSensing(@RequestBody Map<String,Object> request) {
        log.error("verify/touchSensing mock 接收到客户端的请求:{}", JSONObject.toJSONString(request));
        if (status) {
            status = false;
            return BaseResult.success();
        }else{
            status = true;
            return BaseResult.failure("出错了...");
        }
    }

    @PostMapping("dev_ops/search_devices")
    public Object searchDevices() {
        doorCmdService.searchDevice();
        return BaseResult.success();
    }
}

