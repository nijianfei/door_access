package com.csc.door.controller;

import com.csc.door.query.PermGetByIPandnoQuery;
import com.csc.door.query.PermGetBycardidQuery;
import com.csc.door.query.RecordDownloadQuery;
import com.csc.door.request.DoorCntrRequest;
import com.csc.door.request.DoorSetRequest;
import com.csc.door.request.LightCntrRequest;
import com.csc.door.request.PermSyncRequest;
import com.csc.door.response.BaseResult;
import com.csc.door.service.DoorCmdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class DoorController {


    @Autowired
    private DoorCmdService doorCmdService;

    /*1.门禁控制*/
    @PostMapping("door_cntr")
    public Object doorCntr(@RequestBody DoorCntrRequest request) {
        doorCmdService.openDoor(request);
        return BaseResult.success();
    }

    /*2.	门禁设置（门禁设置为门禁状态设置，和门禁控制有区分）*/
    @PostMapping("door_set")
    public Object doorSet(@RequestBody DoorSetRequest request) {
        doorCmdService.doorSet(request);
        return BaseResult.success();
    }

    /*3.	服务器时钟同步*/
    @RequestMapping("clock_sync")
    public Object clockSync() {
        return doorCmdService.clockSync();
    }

    /*4.	门禁控制器状态取得（3.0修正）*/
    @RequestMapping("status_check")
    public Object statusCheck() {
        return doorCmdService.controlStatus();
    }


    /*5.	门禁记录下载*/
    @PostMapping("record_download")
    public Object recordDownload(@RequestBody RecordDownloadQuery query) {
        return doorCmdService.accessRecord(query);
    }

    /*6.	门禁卡权限设置（全量）*/
    @PostMapping("perm_sync")
    public Object permSync(@RequestBody List<PermSyncRequest> requests) {

        return null;
    }


    /*7.	门禁卡权限设置（增量及变化）*/
    @PostMapping("perm_sync_incre")
    public Object permSyncIncre(@RequestBody List<PermSyncRequest> requests) {

        return null;
    }

    /*8.	门禁卡权限删除*/
    @PostMapping("perm_sync_removeall")
    public Object permSyncRemoveall(@RequestBody List<String> requests) {

        return null;
    }


    /*9.	门禁卡权限查询（全部）*/
    @RequestMapping("perm_get_all")
    public Object permGetAll() {

        return null;
    }


    /*10.	门禁卡权限查询（指定门）*/
    @RequestMapping("perm_get_byIPandno")
    public Object PermGetByIPandno(@RequestBody PermGetByIPandnoQuery query) {

        return null;
    }


    /*11.	门禁卡权限查询（指定卡）*/
    @RequestMapping("perm_get_bycardid")
    public Object permGetBycardid(@RequestBody PermGetBycardidQuery query) {

        return null;
    }

    /*12 读头灯光反馈*/
    @RequestMapping("light_cntr")
    public Object lightCntr(@RequestBody List<LightCntrRequest> requests) {
        doorCmdService.lightCntr(requests);
        return BaseResult.success();
    }

    /*12 读头灯光反馈*/
    @RequestMapping("search_device")
    public Object searchDevice() {
        doorCmdService.searchDevice();
        return BaseResult.success();
    }


}

