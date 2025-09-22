package com.csc.door.utils;

import com.csc.door.enums.FunctionCodeEnum;
import com.csc.door.enums.RecordTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.commons.nullanalysis.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CmdParseUtil {

    public static List<Map<String, Object>> parseCmd(@NotNull FunctionCodeEnum fCode, @NotNull List<byte[]> bytesList) {
        List<Map<String, Object>> listMap = new ArrayList<>();
        for (byte[] bytes : bytesList) {
            listMap.add(parseCmd(fCode, bytes));
        }
        return listMap;
    }

    public static Map<String, Object> parseCmd(@NotNull FunctionCodeEnum fCode, @NotNull byte[] bytes) {
        log.info("parseCmd-fuc[{}] HEX -> {}", fCode, CmdBuildUtil.bytesToHex(bytes));
        Map<String, Object> temp = new HashMap<>();
        if (bytes == null) {
            return temp;
        }
        switch (fCode) {
            //返回状态 公用parse
            case X40, XB2, X90 ,XA0 ,X8E -> {
                boolean success = false;
                if (CmdBuildUtil.getIntByByte(bytes[8]) == 1) {
                    success = true;
                }
                temp.put("success", success);
            }
            //设置日期时间
            case X30 -> {
                byte[] tempBuff = new byte[7];
                System.arraycopy(bytes, 8, tempBuff, 0, 7);
                String decode = CmdBuildUtil.bcdTimeDecode(tempBuff);
                temp.put("time", decode);
            }
            //1.7	获取指定索引号的记录
            case XB0 -> {
                parseRecordInformation(bytes, temp);
            }

            //设置日期时间
            case XB4 -> {
                temp.put("recordNo", CmdBuildUtil.getLongByByte(bytes, 8, 4));
            }
            //1.17	设置门控制参数
            case X80 -> {
                temp.put("value", new StringBuilder().append(bytes[8]).append(bytes[9]).append(bytes[10]).toString());
            }
            //1.18	读取门控制参数
            case X82 -> {
                temp.put("controllerNo", CmdBuildUtil.getIntByByte(bytes[8]));
                temp.put("controllerOprcls", CmdBuildUtil.getIntByByte(bytes[9]));
                temp.put("controllerDelay", CmdBuildUtil.getIntByByte(bytes[10]));
            }
            //1.4	查询控制器状态
            case X20 -> {
                parseControllerStatus(bytes, temp);
            }
            //1.2	搜索控制器
            case X94 -> {
                temp.put("sn", CmdBuildUtil.getLongByByte(bytes, 4, 4));
                temp.put("ip", CmdBuildUtil.getIntStrByByte(bytes, 8, 4, "."));
                temp.put("subnetMask", CmdBuildUtil.getIntStrByByte(bytes, 12, 4, "."));
                temp.put("gateway", CmdBuildUtil.getIntStrByByte(bytes, 16, 4, "."));
                byte[] tempBuff = new byte[6];
                System.arraycopy(bytes, 20, tempBuff, 0, 6);
                temp.put("mac", CmdBuildUtil.bytesToHex(tempBuff, 1, "-"));
                temp.put("ver", CmdBuildUtil.bcdTDecode(bytes, 26, 2));
                temp.put("verTime", CmdBuildUtil.bcdTDecode(bytes, 28, 4));
            }
            default -> throw new RuntimeException("不支持的FunctionCode");
        }
        return temp;
    }

    private static void parseControllerStatus(byte[] bytes, Map<String, Object> temp) {
        //	其他信息
        int[] doorStatus = new int[4];
        //28	1号门门磁(0表示关上, 1表示打开)	1	0x00
        doorStatus[1 - 1] = CmdBuildUtil.getIntByByte(bytes[28]);
        //29	2号门门磁(0表示关上, 1表示打开)	1	0x00
        doorStatus[2 - 1] = CmdBuildUtil.getIntByByte(bytes[29]);
        //30	3号门门磁(0表示关上, 1表示打开)	1	0x00
        doorStatus[3 - 1] = CmdBuildUtil.getIntByByte(bytes[30]);
        //31	4号门门磁(0表示关上, 1表示打开)	1	0x00
        doorStatus[4 - 1] = CmdBuildUtil.getIntByByte(bytes[31]);
        temp.put("doorLock", doorStatus);
        int[] pbStatus = new int[4];
        //32	1号门按钮(0表示松开, 1表示按下)	1	0x00
        pbStatus[1 - 1] = CmdBuildUtil.getIntByByte(bytes[32]);
        //33	2号门按钮(0表示松开, 1表示按下)	1	0x00
        pbStatus[2 - 1] = CmdBuildUtil.getIntByByte(bytes[33]);
        //34	3号门按钮(0表示松开, 1表示按下)	1	0x00
        pbStatus[3 - 1] = CmdBuildUtil.getIntByByte(bytes[34]);
        //35	4号门按钮(0表示松开, 1表示按下)	1	0x00
        pbStatus[4 - 1] = CmdBuildUtil.getIntByByte(bytes[35]);
        temp.put("doorBtn", pbStatus);
        //36	故障号
        //等于0 无故障
        //不等于0, 有故障(先重设时间, 如果还有问题, 则要返厂家维护)	1
        int errCode = CmdBuildUtil.getIntByByte(bytes[36]);
        temp.put("controllerStatusCls", errCode);
        //37	控制器当前时间
        //时	1	0x21
        //38	分	1	0x30
        //39	秒	1	0x58

        //40-43	流水号	4
        long sequenceId = CmdBuildUtil.getLongByByte(bytes, 40, 4);

        //48
        //特殊信息1(依据实际使用中返回)
        //键盘按键信息	1
        //49	继电器状态	1
        int relayStatus = CmdBuildUtil.getIntByByte(bytes[49]);

        //50	门磁状态的8-15bit位[火警/强制锁门]
        //Bit0  强制锁门
        //Bit1  火警
        int otherInputStatus = CmdBuildUtil.getIntByByte(bytes[50]);
        if ((otherInputStatus & 0x1) > 0) {
            //强制锁门
        }
        if ((otherInputStatus & 0x2) > 0) {
            //火警
        }

        //51	V5.46版本支持 控制器当前年	1	0x13
        //52	V5.46版本支持 月	1	0x06
        //53	V5.46版本支持 日	1	0x22

        String controllerTime; //控制器当前时间
        controllerTime = String.format("20%02X-%02X-%02X %02X:%02X:%02X", CmdBuildUtil.getIntByByte(bytes[51])
                , CmdBuildUtil.getIntByByte(bytes[52]), CmdBuildUtil.getIntByByte(bytes[53]), CmdBuildUtil.getIntByByte(bytes[37])
                , CmdBuildUtil.getIntByByte(bytes[38]), CmdBuildUtil.getIntByByte(bytes[39]));
        temp.put("controllerTime", controllerTime);
    }

    public static void parseRecordInformation(byte[] recvBuff, Map<String, Object> temp) {
        //8-11	最后一条记录的索引号
        //(=0表示没有记录)	4	0x00000000
        long recordIndex = CmdBuildUtil.getLongByByte(recvBuff, 8, 4);

        //12	记录类型
        //0=无记录
        //1=刷卡记录
        //2=门磁,按钮, 设备启动, 远程开门记录
        //3=报警记录	1
        int recordType = CmdBuildUtil.getIntByByte(recvBuff[12]);

        //13	有效性(0 表示不通过, 1表示通过)	1
        int recordValid = CmdBuildUtil.getIntByByte(recvBuff[13]);

        //14	门号(1,2,3,4)	1
        int recordDoorNO = CmdBuildUtil.getIntByByte(recvBuff[14]);

        //15	进门/出门(1表示进门, 2表示出门)	1	0x01
        int recordInOrOut = CmdBuildUtil.getIntByByte(recvBuff[15]);

        //16-19	卡号(类型是刷卡记录时)
        //或编号(其他类型记录)	4
        long recordCardNO = CmdBuildUtil.getLongByByte(recvBuff, 16, 4);

        //20-26	刷卡时间:
        //年月日时分秒 (采用BCD码)见设置时间部分的说明
        String recordTime = String.format("%02X%02X-%02X-%02X %02X:%02X:%02X",
                CmdBuildUtil.getIntByByte(recvBuff[20]), CmdBuildUtil.getIntByByte(recvBuff[21]),
                CmdBuildUtil.getIntByByte(recvBuff[22]), CmdBuildUtil.getIntByByte(recvBuff[23]),
                CmdBuildUtil.getIntByByte(recvBuff[24]), CmdBuildUtil.getIntByByte(recvBuff[25]),
                CmdBuildUtil.getIntByByte(recvBuff[26]));

        //2012.12.11 10:49:59	7
        //27	记录原因代码(可以查 "刷卡记录说明.xls"文件的ReasonNO)
        //处理复杂信息才用	1
        int reason = CmdBuildUtil.getIntByByte(recvBuff[27]);
        StringBuilder sb = new StringBuilder();
        //0=无记录
        //1=刷卡记录
        //2=门磁,按钮, 设备启动, 远程开门记录
        //3=报警记录	1
        //0xFF=表示指定索引位的记录已被覆盖掉了.  请使用索引0, 取回最早一条记录的索引值
        if (recordType == 0) {
            log.info(String.format("索引位=%d  无记录", recordIndex));
        } else if (recordType == 0xff) {
            log.info(" 指定索引位的记录已被覆盖掉了,请使用索引0, 取回最早一条记录的索引值");
        } else if (recordType == 1) //2015-06-10 08:49:31 显示记录类型为卡号的数据
        {
            temp.put("recordTypeCls", recordType);
            temp.put("recordSrc", recordIndex);
            temp.put("cardId", recordCardNO);
            temp.put("controllerNo", recordDoorNO);
            temp.put("sInorOut", recordInOrOut);
            temp.put("resultCls", recordValid == 1 ? 70 : 90);
            temp.put("recordTimestamp", recordTime);
            temp.put("recordDetail", RecordTypeEnum.getChineseDetail(reason));
            sb.append(String.format("  卡号 = %d", recordCardNO)).append(System.lineSeparator());
            sb.append(String.format("  门号 = %d", recordDoorNO)).append(System.lineSeparator());
            sb.append(String.format("  进出 = %s", recordInOrOut == 1 ? "进门" : "出门")).append(System.lineSeparator());
            sb.append(String.format("  有效 = %s", recordValid == 1 ? "通过" : "禁止")).append(System.lineSeparator());
            sb.append(String.format("  时间 = %s", recordTime)).append(System.lineSeparator());
            sb.append(String.format("  描述 = %s", RecordTypeEnum.getChineseDetail(reason))).append(System.lineSeparator());
        } else if (recordType == 2 || recordType == 3) {
            //其他处理
            //门磁,按钮, 设备启动, 远程开门记录
            temp.put("recordTypeCls", recordType);
            temp.put("recordSrc", recordIndex);
            temp.put("cardId", recordCardNO);
            temp.put("controllerNo", recordDoorNO);
            temp.put("recordTimestamp", recordTime);
            if (RecordTypeEnum.C44.getCode() == 44) {
                temp.put("resultCls", 70);
            }
            temp.put("recordDetail", RecordTypeEnum.getChineseDetail(reason));
            sb.append(String.format("索引位=%d  %s", recordIndex, recordType == 2 ? "非刷卡记录" : recordType == 3 ? "报警记录" : "")).append(System.lineSeparator());
            sb.append(String.format("  编号 = %d", recordCardNO)).append(System.lineSeparator());
            sb.append(String.format("  门号 = %d", recordDoorNO)).append(System.lineSeparator());
            sb.append(String.format("  时间 = %s", recordTime)).append(System.lineSeparator());
            sb.append(String.format("  描述 = %s", RecordTypeEnum.getChineseDetail(reason))).append(System.lineSeparator());
        }
    }

}
