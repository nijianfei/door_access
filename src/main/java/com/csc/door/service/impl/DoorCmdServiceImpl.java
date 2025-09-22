package com.csc.door.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.csc.door.at8000.WgUdpCommShort;
import com.csc.door.dao.ControllerConfigDao;
import com.csc.door.dto.CmdParamDto;
import com.csc.door.dto.ControllerConfigDto;
import com.csc.door.dto.ControllerConfigsDto;
import com.csc.door.enums.FunctionCodeEnum;
import com.csc.door.enums.ResultStatusEnum;
import com.csc.door.handler.UdpServerHandler;
import com.csc.door.query.RecordDownloadQuery;
import com.csc.door.request.DoorCntrRequest;
import com.csc.door.request.DoorSetRequest;
import com.csc.door.request.LightCntrRequest;
import com.csc.door.response.AccessRecordResponse;
import com.csc.door.response.ClockSyncResponse;
import com.csc.door.response.ControllerStatusResponse;
import com.csc.door.service.DoorCmdService;
import com.csc.door.utils.CmdBuildUtil;
import com.csc.door.utils.CmdParseUtil;
import com.csc.door.utils.CmdSendUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class DoorCmdServiceImpl implements DoorCmdService {

    @Autowired
    private UdpServerHandler udpIoHandler;

    @Autowired
    private ControllerConfigsDto controllerConfigsDto;

    @Autowired
    private ControllerConfigDao controllerConfigDao;


    @Override
    public void openDoor(DoorCntrRequest request) {
        int doorNO = Integer.parseInt(request.getControllerNo());
        int cardId = Integer.parseInt(request.getCardId());
        ControllerConfigDto controllerConfigDto = controllerConfigsDto.get(request.getControllerIp());
        CmdParamDto param = CmdParamDto.builder().devSn(controllerConfigDto.getSn())
                .doorNo(doorNO).card(cardId).inOrout(request.getInOrout()).build();
        byte[] cmd = CmdBuildUtil.buildCmd(FunctionCodeEnum.X40, param);
        byte[] bytes = CmdSendUtil.instance(request.getControllerIp()).sendCmd(cmd);
        Map<String, Object> resultMap = CmdParseUtil.parseCmd(FunctionCodeEnum.X40, bytes);
        if ((boolean) resultMap.get("success")) {
            remoteLight(controllerConfigDto.getSn(), request.getControllerIp(),
                    Integer.parseInt(request.getControllerNo()), request.getInOrout(), 1);
        } else {
            remoteLight(controllerConfigDto.getSn(), request.getControllerIp(),
                    Integer.parseInt(request.getControllerNo()), request.getInOrout(), 1);
        }
    }

    @Override
    public void doorSet(DoorSetRequest request) {
        ControllerConfigDto controllerConfigDto = controllerConfigsDto.get(request.getControllerIp());
        CmdParamDto param = CmdParamDto.builder().devSn(controllerConfigDto.getSn())
                .doorNo(Integer.parseInt(request.getControllerNo())).controlType(Integer.parseInt(request.getControllerOprcls()))
                .doorOpeningDelay(Integer.parseInt(request.getControllerDelay())).build();
        byte[] cmd = CmdBuildUtil.buildCmd(FunctionCodeEnum.X80, param);
        byte[] bytes = CmdSendUtil.instance(request.getControllerIp()).sendCmd(cmd);
        Map<String, Object> resultMap = CmdParseUtil.parseCmd(FunctionCodeEnum.X80, bytes);
        String value = new StringBuilder().append(cmd[8]).append(cmd[9]).append(cmd[10]).toString();

        if (value.equals(resultMap.get("value"))) {

        }
    }

    @Override
    public List<ClockSyncResponse> clockSync() {
        List<ClockSyncResponse> resultList = new ArrayList<>();
        Date currentTime = new Date();
        for (ControllerConfigDto confDto : controllerConfigsDto.getControllers()) {
            CmdParamDto param = CmdParamDto.builder().devSn(confDto.getSn()).currentTime(currentTime).build();
            byte[] cmd = CmdBuildUtil.buildCmd(FunctionCodeEnum.X30, param);
            byte[] bytes = CmdSendUtil.instance(confDto.getIp()).sendCmd(cmd);
            Map<String, Object> resultMap = CmdParseUtil.parseCmd(FunctionCodeEnum.X30, bytes);
            ClockSyncResponse response = new ClockSyncResponse();
            response.setControllerIp(confDto.getIp());
            if (!Objects.equals(resultMap.get("time"), DateFormatUtils.format(currentTime, "yyyyMMddHHmmss"))) {
                response.setResultCls(ResultStatusEnum.FAILURE.code);
            } else {
                response.setResultCls(ResultStatusEnum.SUCCESS.code);
            }
            resultList.add(response);
        }
        resultList.sort(Comparator.comparing(ClockSyncResponse::getControllerIp));
        return resultList;

    }

    @Override
    public List<ControllerStatusResponse> controlStatus() {

        List<ControllerStatusResponse> statusResponses = new ArrayList<>();
        for (ControllerConfigDto controller : controllerConfigsDto.getControllers()) {
            CmdParamDto param = CmdParamDto.builder().devSn(controller.getSn()).build();
            byte[] cmd = CmdBuildUtil.buildCmd(FunctionCodeEnum.X20, param);
            byte[] bytes = CmdSendUtil.instance(controller.getIp()).sendCmd(cmd);
            Map<String, Object> resultMap = CmdParseUtil.parseCmd(FunctionCodeEnum.X20, bytes);
            int[] doorLock = (int[]) resultMap.get("doorLock");
            int[] doorBtn = (int[]) resultMap.get("doorBtn");
            int doorCount = Integer.parseInt(String.valueOf(controller.getSn()).substring(0, 1));
            for (int i = 1; i <= doorCount; i++) {
                param.setDoorNo(i);
                byte[] cmd2 = CmdBuildUtil.buildCmd(FunctionCodeEnum.X82, param);
                byte[] bytes2 = CmdSendUtil.instance(controller.getIp()).sendCmd(cmd2);
                Map<String, Object> resultMap2 = CmdParseUtil.parseCmd(FunctionCodeEnum.X82, bytes2);
                log.info("读取门控制状态:{}", JSONObject.toJSONString(resultMap2));
                ControllerStatusResponse status = ControllerStatusResponse.builder().controllerIp(controller.getIp()).controllerNo(String.valueOf(i))
                        .controllerStatusCls(String.valueOf(resultMap.get("controllerStatusCls"))).doorOprcls(String.valueOf(resultMap2.get("controllerOprcls")))
                        .doorId(controller.getDoorName(i)).doorLock(String.valueOf(doorLock[i])).doorBtn(String.valueOf(doorBtn[i])).build();
                statusResponses.add(status);
            }
            log.info("控制器状态" + JSONObject.toJSONString(resultMap));
        }

        return statusResponses;
    }

    byte[] byte1024 = new byte[1024];

    @Override
    public Object accessRecord(RecordDownloadQuery query) {
        String recordDateFrom = query.getRecordDateFrom();
        String recordDateTo = query.getRecordDateTo();
        List<AccessRecordResponse> resultList = new ArrayList<>();
        for (ControllerConfigDto configDto : controllerConfigsDto.getControllers()) {
            long firstRecordIndex = 0;
            long lastRecordIndex = 0xffffffff;
            AccessRecordResponse firstRecord = getRecord(configDto, firstRecordIndex);
            log.info("SN[{}]最早的一条记录索引号:{}", configDto.getSn(), firstRecord.getRecordSrc());
            AccessRecordResponse lastRecord = getRecord(configDto, lastRecordIndex);
            long lastRecordNum = Long.parseLong(lastRecord.getRecordSrc());
            log.info("SN[{}]最后的一条记录索引号:{}", configDto.getSn(), lastRecordNum);
            long recordStartNo = getReadRecordIndex(configDto);
            log.info("SN[{}]已读取过的记录索引号:{}", configDto.getSn(), recordStartNo);
            Map<String, Object> resultMap1 = null;
            long startRecordIndex = recordStartNo + 1;
            for (int i = 0; i < lastRecordNum-recordStartNo; i++) {
                CmdParamDto param1 = CmdParamDto.builder().devSn(configDto.getSn()).recordNo(startRecordIndex + i).build();
                byte[] cmd1 = CmdBuildUtil.buildCmd(FunctionCodeEnum.XB0, param1);
                System.arraycopy(cmd1, 0, byte1024, i * 64, 64);
            }
            byte[] bytes1 = CmdSendUtil.instance(configDto.getIp()).sendCmd(byte1024);
            log.info("测试LOG:{}", CmdBuildUtil.bytesToHex(bytes1));
            for (int i = 0; i < lastRecordNum-recordStartNo; i++) {
                byte[] tempByte = new byte[64];
                System.arraycopy(bytes1, i * 64, tempByte, 0, 64);
                Map<String, Object> stringObjectMap = CmdParseUtil.parseCmd(FunctionCodeEnum.XB0, tempByte);
                AccessRecordResponse recordResponse = JSONObject.parseObject(JSONObject.toJSONString(stringObjectMap), AccessRecordResponse.class);
                recordResponse.setControllerIp(configDto.getIp());
                resultList.add(recordResponse);
            }

//            do {
//                try {
//                    CmdParamDto param1 = CmdParamDto.builder().devSn(configDto.getSn()).recodeNo(recordStartNo++).build();
//                    byte[] cmd1 = CmdBuildUtil.buildCmd(FunctionCodeEnum.XB0, param1);
//                    byte[] bytes1 = CmdSendUtil.instance(configDto.getIp()).sendCmd(cmd1);
//                    resultMap1 = CmdParseUtil.parseCmd(FunctionCodeEnum.XB0, bytes1);
//                    AccessRecordResponse recordResponse = JSONObject.parseObject(JSONObject.toJSONString(resultMap1), AccessRecordResponse.class);
//                    recordResponse.setControllerIp(configDto.getIp());
//                    //保存后,设置索引
//                    resultList.add(recordResponse);
//                } catch (Exception e) {
//                    log.error("查询出入记录异常:{}", e.getMessage(), e);
//                    throw new RuntimeException(e);
//                }
//            } while (resultMap1 != null && !resultMap1.isEmpty());

            saveReadRecordIndex(configDto, lastRecordNum-2);//TODO  -2 测试使用,上线需删除
        }
        log.info("测试LOG_LIST:{}", JSONObject.toJSONString(resultList, JSONWriter.Feature.PrettyFormat));
        return resultList;
    }

    private AccessRecordResponse getRecord(ControllerConfigDto configDto, long firstRecordIndex) {
        CmdParamDto param1 = CmdParamDto.builder().devSn(configDto.getSn()).recordNo(firstRecordIndex).build();
        byte[] cmd1 = CmdBuildUtil.buildCmd(FunctionCodeEnum.XB0, param1);
        byte[] bytes1 = CmdSendUtil.instance(configDto.getIp()).sendCmd(cmd1);
        Map<String, Object> stringObjectMap = CmdParseUtil.parseCmd(FunctionCodeEnum.XB0, bytes1);
        AccessRecordResponse recordResponse = JSONObject.parseObject(JSONObject.toJSONString(stringObjectMap), AccessRecordResponse.class);
        return recordResponse;
    }

    private long getReadRecordIndex(ControllerConfigDto configDto) {
        CmdParamDto param = CmdParamDto.builder().devSn(configDto.getSn()).build();
        byte[] cmd = CmdBuildUtil.buildCmd(FunctionCodeEnum.XB4, param);
        byte[] bytes = CmdSendUtil.instance(configDto.getIp()).sendCmd(cmd);
        Map<String, Object> resultMap = CmdParseUtil.parseCmd(FunctionCodeEnum.XB4, bytes);
        long recordNo = (long) resultMap.get("recordNo");
        log.info("ip:{},recordNo:{}", configDto.getIp(), recordNo);
        return recordNo;
    }

    private boolean saveReadRecordIndex(ControllerConfigDto configDto, long recordIndex) {
        CmdParamDto param = CmdParamDto.builder().devSn(configDto.getSn()).recordNo(recordIndex).build();
        byte[] cmd = CmdBuildUtil.buildCmd(FunctionCodeEnum.XB2, param);
        byte[] bytes = CmdSendUtil.instance(configDto.getIp()).sendCmd(cmd);
        Map<String, Object> resultMap = CmdParseUtil.parseCmd(FunctionCodeEnum.XB2, bytes);
        boolean isSuccess = (boolean) resultMap.get("success");
        log.info("saveReadRecordIndex - ip:{}, set recordNo:{} success:{}", configDto.getIp(), recordIndex, isSuccess);
        return isSuccess;
    }

    @Override
    public void lightCntr(List<LightCntrRequest> requests) {
        for (LightCntrRequest lightCntrRequest : requests) {
            log.info("lightCntr-Start");
            String controllerIp = lightCntrRequest.getControllerIp();
            remoteLight(controllerConfigsDto.get(controllerIp).getSn(), controllerIp,
                    lightCntrRequest.getControllerNo(), lightCntrRequest.getInOrout(), lightCntrRequest.getColourCls());
            log.info("lightCntr-End");
        }
    }

    @Override
    public void searchDevice() {
        byte[] cmd1 = CmdBuildUtil.buildCmd(FunctionCodeEnum.X94, CmdParamDto.builder().build());
        List<byte[]> bytes1 = CmdSendUtil.instance("255.255.255.255", 10000, 3000).broadcastSend(cmd1);
        List<Map<String, Object>> maps = CmdParseUtil.parseCmd(FunctionCodeEnum.X94, bytes1);
        log.info("扫描到的设备:{}", JSONObject.toJSONString(maps));
        configDevice(maps);
    }

    public void initDevice(){

    }
    public void configDevice(List<Map<String, Object>> maps) {
        List<ControllerConfigDto> byServerIdente = controllerConfigDao.findByServerIdente();
        log.info("控制器DB 配置列表:{}", JSONObject.toJSONString(byServerIdente));
        ControllerConfigDto configDto = byServerIdente.get(0);

        Map<String, Object> stringObjectMap = maps.get(0);
        long sn = Long.parseLong(stringObjectMap.get("sn").toString());

        CmdParamDto param = CmdParamDto.builder().devSn(sn).serverIp(configDto.getServerIp()).serverPort(configDto.getServerPort()).deviceIp(configDto.getIp()).
                subnetMask(configDto.getSubnetMask()).gateway(configDto.getGateway()).clientTimeOut("10").build();

        /*1.19	设置接收服务器的IP和端口 定时上报时间间隔*/
        byte[] cmd = CmdBuildUtil.buildCmd(FunctionCodeEnum.X90, param);
        List<byte[]> bytes = CmdSendUtil.instance("255.255.255.255").broadcastSend(cmd);
        List<Map<String, Object>> maps1 = CmdParseUtil.parseCmd(FunctionCodeEnum.X90, bytes);
        log.info("设置 接收服务器的IP和端口结果:{}", JSONObject.toJSONString(maps1));

        log.info("开始设置控制器 网络配置IP 掩码 网关 ...");
        cmd = CmdBuildUtil.buildCmd(FunctionCodeEnum.X96, param);
        CmdSendUtil.instance("255.255.255.255").broadcastSend(cmd);

        byte[] cmd12 = CmdBuildUtil.buildCmd(FunctionCodeEnum.X94, CmdParamDto.builder().devSn(sn).build());
        List<byte[]> bytes12 = CmdSendUtil.instance("255.255.255.255", 10000, 3000).broadcastSend(cmd12);
        List<Map<String, Object>> maps12 = CmdParseUtil.parseCmd(FunctionCodeEnum.X94, bytes12);
        log.info("扫描到的设备-确认修改内容:{}", JSONObject.toJSONString(maps12));

        byte[] cmd2 = CmdBuildUtil.buildCmd(FunctionCodeEnum.XA0, param);
        byte[] byte2 = CmdSendUtil.instance(configDto.getIp()).sendCmd(cmd2);
        Map<String, Object> map2 = CmdParseUtil.parseCmd(FunctionCodeEnum.XA0, byte2);
        log.info("设置 电脑控制刷卡是否开门 结果:{}", JSONObject.toJSONString(map2));

        byte[] cmd3 = CmdBuildUtil.buildCmd(FunctionCodeEnum.X8E, param);
        byte[] byte3 = CmdSendUtil.instance(configDto.getIp()).sendCmd(cmd3);
        Map<String, Object> map3 = CmdParseUtil.parseCmd(FunctionCodeEnum.X8E, byte3);
        log.info("设置记录 门磁 按钮 报警 事件参数 结果:{}", JSONObject.toJSONString(map3));


        byte[] cmd4 = CmdBuildUtil.buildCmd(FunctionCodeEnum.XF2, param);
        byte[] byte4 = CmdSendUtil.instance(configDto.getIp()).sendCmd(cmd4);
        Map<String, Object> map4 = CmdParseUtil.parseCmd(FunctionCodeEnum.XF2, byte4);
        log.info("设置 电脑控制刷卡超时时间 结果:{}", JSONObject.toJSONString(map4));
    }

    public void remoteLight(long lSn, String sIp, int iDoorNo, int iInorOut, int iStatus) {
        // 类型转换：long转String后取首字符
        int num = Integer.parseInt(Long.toString(lSn).substring(0, 1));
        byte b = 0;

        // 门号逻辑判断
        if (num == 4) {
            switch (iDoorNo) {
                case 1:
                    b = (byte) 0xc1;
                    break;
                case 2:
                    b = (byte) 0xc2;
                    break;
                case 3:
                    b = (byte) 0xc3;
                    break;
                case 4:
                    b = (byte) 0xc4;
                    break;
                default:
                    return;
            }
        } else if (num == 1 || num == 2) {
            if (iDoorNo == 1) {
                b = iInorOut == 2 ? (byte) 0xc2 : (byte) 0xc1;
            } else if (iDoorNo == 2) {
                b = iInorOut == 2 ? (byte) 0xc4 : (byte) 0xc3;
            } else {
                return;
            }
        } else {
            return;
        }
        int _Global_xid = 0;
        byte[] baseCmd = new byte[64];
        baseCmd[0] = 0x17;
        baseCmd[1] = FunctionCodeEnum.X40.code;
        System.arraycopy(WgUdpCommShort.longToByte(lSn), 0, baseCmd, 4, 4);
        byte[] cmd = new byte[56];
        cmd[0] = b;
        cmd[2] = 0;
        System.arraycopy(cmd, 0, baseCmd, 8, cmd.length);
        System.arraycopy(WgUdpCommShort.longToByte(++_Global_xid), 0, baseCmd, 40, 4);
        try {
            log.info("remoteLight send1->{}", CmdBuildUtil.bytesToHex(baseCmd));
            byte[] bytes = CmdSendUtil.instance(sIp).sendCmd(baseCmd);
            log.info("remoteLight rev1->{}", CmdBuildUtil.bytesToHex(bytes));
            if (bytes != null) {
                byte[] recv = bytes;
                if (recv[8] == 1) {
                    // 状态控制逻辑
                    int loopCount = (iStatus == 0) ? 2 : 1;
                    for (int i = 0; i < loopCount; i++) {
                        // 灯光操作序列
                        cmd[2] = 10;
                        System.arraycopy(cmd, 0, baseCmd, 8, cmd.length);
                        System.arraycopy(WgUdpCommShort.longToByte(++_Global_xid), 0, baseCmd, 40, 4);
                        log.info("remoteLight send2->{}", CmdBuildUtil.bytesToHex(baseCmd));
                        recv = CmdSendUtil.instance(sIp).sendCmd(baseCmd);
                        log.info("remoteLight rev2->{}", CmdBuildUtil.bytesToHex(recv));
                        if (recv[8] != 1) {
                            return;
                        }

                        Thread.sleep(100); // 线程休眠[16](@ref)

                        cmd[2] = 0;
                        System.arraycopy(cmd, 0, baseCmd, 8, cmd.length);
                        System.arraycopy(WgUdpCommShort.longToByte(++_Global_xid), 0, baseCmd, 40, 4);
                        log.info("remoteLight send3->{}", CmdBuildUtil.bytesToHex(baseCmd));
                        recv = CmdSendUtil.instance(sIp).sendCmd(baseCmd);
                        log.info("remoteLight rev3->{}", CmdBuildUtil.bytesToHex(recv));
                        if (recv[8] != 1) {
                            return;
                        }
                        Thread.sleep(100);
                    }
                    return; // 操作成功
                }
            }
        } catch (InterruptedException e) { // 线程中断处理
            Thread.currentThread().interrupt();
        }
    }
}
