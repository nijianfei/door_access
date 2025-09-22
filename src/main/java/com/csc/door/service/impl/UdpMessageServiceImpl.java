package com.csc.door.service.impl;

import com.csc.door.at8000.WgUdpCommShort;
import com.csc.door.dto.ClientMsgDto;
import com.csc.door.dto.ControllerConfigDto;
import com.csc.door.dto.ControllerConfigsDto;
import com.csc.door.dto.MsgRecordStatus;
import com.csc.door.request.CoreRequest;
import com.csc.door.request.DoorCntrRequest;
import com.csc.door.request.LightCntrRequest;
import com.csc.door.response.BaseResult;
import com.csc.door.service.UdpMessageService;
import com.csc.door.utils.CmdBuildUtil;
import com.csc.door.utils.CoreRequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
public class UdpMessageServiceImpl implements UdpMessageService {

    @Lazy // 添加延迟加载注解
    @Autowired
    private DoorCmdServiceImpl doorCmdServiceImpl;
    @Autowired
    private ControllerConfigsDto controllerConfigsDto;

    @Value("${core-server.url}")
    private String targetUrl;
    @Value("${local-server.url}")
    private String localUrl;
    private ConcurrentMap<String, MsgRecordStatus> MsgRecordMap = new ConcurrentHashMap<>();
    private long recordIndex = 0;

    @Override
    public void processMsg(String ip, byte[] message) {
        if (message == null || message.length < 8) {
            log.info("接收到来自控制器[{}]非法的数据包... {}", ip, message);
        }
        long sn = WgUdpCommShort.getLongByByte(message, 4, 4);
        ControllerConfigDto controllerConfigDto = controllerConfigsDto.get(ip);
        if (controllerConfigDto == null) {
            log.info("接收到来自控制器SN = {} IP = {} 的数据包.. 在控制器配置表中不存在,忽略此数据包. DEBUG HEX:[{}]", sn, ip, CmdBuildUtil.bytesToHex(message));
            return;
        }
        log.info("接收到来自控制器SN = {} IP = {} 的{}数据包..", sn, ip, message[1] == 0x22 ? "二维码" : "");
        if (message[1] == 0x20) {
            long recordIndexGet = WgUdpCommShort.getLongByByte(message, 8, 4);
            if (recordIndex < recordIndexGet) {
                recordIndex = recordIndexGet;
                ClientMsgDto msg = displayRecordInformation(message);
                if (msg.getRecordType() != 1) {//只处理刷卡
                    return;
                }
                //判断时间是否有效????
                msg.getRecordTime();
                //请求中台 ,有权限开门
                CoreRequest cardRequest = CoreRequest.builder().controllerIp(ip).controllerNo(String.valueOf(msg.getRecordDoorNO()))
                        .doorName(controllerConfigDto.getDoorName(msg.getRecordDoorNO())).cardId(String.valueOf(msg.getRecordCardNO()))
                        .inOrout(String.valueOf(msg.getRecordInOrOut())).triggerTime(msg.getRecordTime()).build();
                BaseResult cardResult = CoreRequestUtil.getInstance(localUrl, targetUrl).card(cardRequest);
                if (cardResult.isSuccess()) {
                    DoorCntrRequest param = DoorCntrRequest.builder()
                            .cardId(String.valueOf(msg.getRecordCardNO())).inOrout(msg.getRecordInOrOut())
                            .controllerIp(ip).controllerNo(String.valueOf(msg.getRecordDoorNO())).sn(sn).build();
                    doorCmdServiceImpl.openDoor(param);
                }if(cardResult.isFailure()){
                    //请求中台 ,无权限红色灯光
                    doorCmdServiceImpl.lightCntr(List.of(new LightCntrRequest(ip,msg.getRecordDoorNO(),msg.getRecordInOrOut(),0)));
                }
            }
        } else if (message[1] == 0x22) {//只处理二维码
            ClientMsgDto msg = dealQRData(message);
            //请求中台 ,有权限开门
            CoreRequest qrcodeRequest = CoreRequest.builder().controllerIp(ip).controllerNo(String.valueOf(msg.getRecordDoorNO()))
                    .doorName(controllerConfigDto.getDoorName(msg.getRecordDoorNO())).qrcode(String.valueOf(msg.getQrCode()))
                    .inOrout(String.valueOf(msg.getRecordInOrOut())).triggerTime(msg.getRecordTime()).build();
            BaseResult qrcodeResult = CoreRequestUtil.getInstance(localUrl, targetUrl).qrcode(qrcodeRequest);
            if (qrcodeResult.isSuccess()) {
                DoorCntrRequest param = DoorCntrRequest.builder()
                        .cardId(String.valueOf(msg.getRecordCardNO())).inOrout(msg.getRecordInOrOut())
                        .controllerIp(ip).controllerNo(String.valueOf(msg.getRecordDoorNO())).sn(sn).build();
                doorCmdServiceImpl.openDoor(param);
            }if(qrcodeResult.isFailure()){
                //请求中台 ,无权限红色灯光
                doorCmdServiceImpl.lightCntr(List.of(new LightCntrRequest(ip,msg.getRecordDoorNO(),msg.getRecordInOrOut(),0)));
            }
        } else {
            log.info("接收到来自控制器SN = {} IP = {} 的未知类型数据包..", sn, ip, CmdBuildUtil.bytesToHex(message));
        }
    }

    public static ClientMsgDto displayRecordInformation(byte[] recvBuff) {
        ClientMsgDto.ClientMsgDtoBuilder builder = ClientMsgDto.builder();
        StringBuilder sb = new StringBuilder(System.lineSeparator());
        //8-11	最后一条记录的索引号
        //(=0表示没有记录)	4	0x00000000
        long recordIndex = WgUdpCommShort.getLongByByte(recvBuff, 8, 4);


        //12	记录类型
        //0=无记录
        //1=刷卡记录
        //2=门磁,按钮, 设备启动, 远程开门记录
        //3=报警记录	1
        int recordType = WgUdpCommShort.getIntByByte(recvBuff[12]);

        //13	有效性(0 表示不通过, 1表示通过)	1
        int recordValid = WgUdpCommShort.getIntByByte(recvBuff[13]);

        //14	门号(1,2,3,4)	1
        int recordDoorNO = WgUdpCommShort.getIntByByte(recvBuff[14]);

        //15	进门/出门(1表示进门, 2表示出门)	1	0x01
        int recordInOrOut = WgUdpCommShort.getIntByByte(recvBuff[15]);

        //16-19	卡号(类型是刷卡记录时)
        //或编号(其他类型记录)	4
        long recordCardNO = WgUdpCommShort.getLongByByte(recvBuff, 16, 4);


        //20-26	刷卡时间:
        //年月日时分秒 (采用BCD码)见设置时间部分的说明
        String recordTime = String.format("%02X%02X-%02X-%02X %02X:%02X:%02X", WgUdpCommShort.getIntByByte(recvBuff[20]), WgUdpCommShort.getIntByByte(recvBuff[21]), WgUdpCommShort.getIntByByte(recvBuff[22]), WgUdpCommShort.getIntByByte(recvBuff[23]), WgUdpCommShort.getIntByByte(recvBuff[24]), WgUdpCommShort.getIntByByte(recvBuff[25]), WgUdpCommShort.getIntByByte(recvBuff[26]));

        //2012.12.11 10:49:59	7
        //27	记录原因代码(可以查 "刷卡记录说明.xls"文件的ReasonNO)
        //处理复杂信息才用	1
        int reason = WgUdpCommShort.getIntByByte(recvBuff[27]);

        //0=无记录
        //1=刷卡记录
        //2=门磁,按钮, 设备启动, 远程开门记录
        //3=报警记录	1	
        //0xFF=表示指定索引位的记录已被覆盖掉了.  请使用索引0, 取回最早一条记录的索引值
        if (recordType == 0) {
            sb.append(String.format("索引位=%u  无记录", recordIndex)).append(System.lineSeparator());
        } else if (recordType == 0xff) {
            sb.append(" 指定索引位的记录已被覆盖掉了,请使用索引0, 取回最早一条记录的索引值").append(System.lineSeparator());
        } else if (recordType == 1) //2015-06-10 08:49:31 显示记录类型为卡号的数据
        {
            //卡号
            sb.append(String.format("  索引位 = %d  ", recordIndex)).append(System.lineSeparator());
            sb.append(String.format("  卡号 = %d", recordCardNO)).append(System.lineSeparator());
            sb.append(String.format("  门号 = %d", recordDoorNO)).append(System.lineSeparator());
            sb.append(String.format("  进出 = %s", recordInOrOut == 1 ? "进门" : "出门")).append(System.lineSeparator());
            sb.append(String.format("  有效 = %s", recordValid == 1 ? "通过" : "禁止")).append(System.lineSeparator());
            sb.append(String.format("  时间 = %s", recordTime)).append(System.lineSeparator());
            sb.append(String.format("  描述 = %s", getReasonDetailChinese(reason))).append(System.lineSeparator());
        } else if (recordType == 2) {
            //其他处理
            //门磁,按钮, 设备启动, 远程开门记录
            sb.append(String.format("  索引位 = %d  非刷卡记录", recordIndex)).append(System.lineSeparator());
            sb.append(String.format("  编号 = %d", recordCardNO)).append(System.lineSeparator());
            sb.append(String.format("  门号 = %d", recordDoorNO)).append(System.lineSeparator());
            sb.append(String.format("  时间 = %s", recordTime)).append(System.lineSeparator());
            sb.append(String.format("  描述 = %s", getReasonDetailChinese(reason))).append(System.lineSeparator());
        } else if (recordType == 3) {
            //其他处理
            //报警记录
            sb.append(String.format("  索引位 = %d  报警记录", recordIndex)).append(System.lineSeparator());
            sb.append(String.format("  编号 = %d", recordCardNO)).append(System.lineSeparator());
            sb.append(String.format("  门号 = %d", recordDoorNO)).append(System.lineSeparator());
            sb.append(String.format("  时间 = %s", recordTime)).append(System.lineSeparator());
            sb.append(String.format("  描述 = %s", getReasonDetailChinese(reason))).append(System.lineSeparator());
        } else {
            log.warn("存在未知上报类型:{},未处理...", recordType);
        }
        log.info(sb.toString());
        return builder.recordIndex(recordIndex).recordType(recordType).recordValid(recordValid)
                .recordCardNO(recordCardNO).recordInOrOut(recordInOrOut).recordDoorNO(recordDoorNO)
                .recordTime(recordTime).reason(reason).recordDetail(getReasonDetailChinese(reason)).build();
    }

    public static String getReasonDetailChinese(int Reason) //中文
    {
        if (Reason > 45) {
            return "";
        }
        if (Reason <= 0) {
            return "";
        }
        return RecordDetails[(Reason - 1) * 4 + 3]; //中文信息
    }

    public static String RecordDetails[] = {
//记录原因 (类型中 SwipePass 表示通过; SwipeNOPass表示禁止通过; ValidEvent 有效事件(如按钮 门磁 超级密码开门); Warn 报警事件)
//代码  类型   英文描述  中文描述
            "1", "SwipePass", "Swipe", "刷卡开门", "2", "SwipePass", "Swipe Close", "刷卡关", "3", "SwipePass", "Swipe Open", "刷卡开", "4", "SwipePass", "Swipe Limited Times", "刷卡开门(带限次)", "5", "SwipeNOPass", "Denied Access: PC Control", "刷卡禁止通过: 电脑控制", "6", "SwipeNOPass", "Denied Access: No PRIVILEGE", "刷卡禁止通过: 没有权限", "7", "SwipeNOPass", "Denied Access: Wrong PASSWORD", "刷卡禁止通过: 密码不对", "8", "SwipeNOPass", "Denied Access: AntiBack", "刷卡禁止通过: 反潜回", "9", "SwipeNOPass", "Denied Access: More Cards", "刷卡禁止通过: 多卡", "10", "SwipeNOPass", "Denied Access: First Card Open", "刷卡禁止通过: 首卡", "11", "SwipeNOPass", "Denied Access: Door Set NC", "刷卡禁止通过: 门为常闭", "12", "SwipeNOPass", "Denied Access: InterLock", "刷卡禁止通过: 互锁", "13", "SwipeNOPass", "Denied Access: Limited Times", "刷卡禁止通过: 受刷卡次数限制", "14", "SwipeNOPass", "Denied Access: Limited Person Indoor", "刷卡禁止通过: 门内人数限制", "15", "SwipeNOPass", "Denied Access: Invalid Timezone", "刷卡禁止通过: 卡过期或不在有效时段", "16", "SwipeNOPass", "Denied Access: In Order", "刷卡禁止通过: 按顺序进出限制", "17", "SwipeNOPass", "Denied Access: SWIPE GAP LIMIT", "刷卡禁止通过: 刷卡间隔约束", "18", "SwipeNOPass", "Denied Access", "刷卡禁止通过: 原因不明", "19", "SwipeNOPass", "Denied Access: Limited Times", "刷卡禁止通过: 刷卡次数限制", "20", "ValidEvent", "Push Button", "按钮开门", "21", "ValidEvent", "Push Button Open", "按钮开", "22", "ValidEvent", "Push Button Close", "按钮关", "23", "ValidEvent", "Door Open", "门打开[门磁信号]", "24", "ValidEvent", "Door Closed", "门关闭[门磁信号]", "25", "ValidEvent", "Super Password Open Door", "超级密码开门", "26", "ValidEvent", "Super Password Open", "超级密码开", "27", "ValidEvent", "Super Password Close", "超级密码关", "28", "Warn", "Controller Power On", "控制器上电", "29", "Warn", "Controller Reset", "控制器复位", "30", "Warn", "Push Button Invalid: Disable", "按钮不开门: 按钮禁用", "31", "Warn", "Push Button Invalid: Forced Lock", "按钮不开门: 强制关门", "32", "Warn", "Push Button Invalid: Not On Line", "按钮不开门: 门不在线", "33", "Warn", "Push Button Invalid: InterLock", "按钮不开门: 互锁", "34", "Warn", "Threat", "胁迫报警", "35", "Warn", "Threat Open", "胁迫报警开", "36", "Warn", "Threat Close", "胁迫报警关", "37", "Warn", "Open too long", "门长时间未关报警[合法开门后]", "38", "Warn", "Forced Open", "强行闯入报警", "39", "Warn", "Fire", "火警", "40", "Warn", "Forced Close", "强制关门", "41", "Warn", "Guard Against Theft", "防盗报警", "42", "Warn", "7*24Hour Zone", "烟雾煤气温度报警", "43", "Warn", "Emergency Call", "紧急呼救报警", "44", "RemoteOpen", "Remote Open Door", "操作员远程开门", "45", "RemoteOpen", "Remote Open Door By USB Reader", "发卡器确定发出的远程开门"};


    private static ClientMsgDto dealQRData(byte[] recv) {
        ClientMsgDto.ClientMsgDtoBuilder builder = ClientMsgDto.builder();
        // 8-11 二维码数据长度
        int qrDataLen = (int) WgUdpCommShort.getLongByByte(recv, 8, 4);

        // 13 串口号
        int serialPort = recv[13] & 0xFF; // Java需要显式处理byte转无符号

        // 14 门号
        int recordDoorNO = recv[14] & 0xFF;
        builder.recordDoorNO(recordDoorNO);

        // 15 进出状态
        int recordInOrOut = recv[15] & 0xFF;
        builder.recordInOrOut(recordInOrOut);
        // 40-43 流水号
        long cmdSequenceId = WgUdpCommShort.getLongByByte(recv, 40, 4);

        if (qrDataLen >= 1) {
            byte[] qrData = new byte[qrDataLen];
            System.arraycopy(recv, 64, qrData, 0, qrDataLen); // Java数组复制语法

            // 日志输出（需要实现log方法）
            log.info(String.format("流水号=%d 二维码原始数据:\n        %s\n",
                    cmdSequenceId,
                    Arrays.toString(qrData)));

            // 字符串转换（注意GB2312编码）
            String qrString = new String(qrData, Charset.forName("GB2312")).trim();
            log.info(String.format("流水号=%d 二维码字符串:\n        %s",
                    cmdSequenceId,
                    qrString));
            builder.qrCode(qrString);
            builder.recordTime(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        }
        return builder.build();
    }
}
