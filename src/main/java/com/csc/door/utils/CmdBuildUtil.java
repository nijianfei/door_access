package com.csc.door.utils;

import com.alibaba.fastjson2.JSONObject;
import com.csc.door.dto.CmdParamDto;
import com.csc.door.enums.FunctionCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.codehaus.commons.nullanalysis.NotNull;

import java.util.Date;

@Slf4j
public class CmdBuildUtil {

    public static final int WGPacketSize = 64;             //报文长度
    public static final byte Type = 0x17; //2015-04-30 08:50:29 0x19;					//类型
    public static final int ControllerPort = 60000;        //控制器端口
    public static final long SpecialFlag = 0x55AAAA55;      //特殊标识 防止误操作
    private static long _Global_xid = 0;
    private static byte[] flag = new byte[]{0x55, (byte) 0xAA, (byte) 0xAA, 0x55};


    public static byte[] buildCmd(@NotNull FunctionCodeEnum fCode, @NotNull CmdParamDto param) {
        byte[] cmdBuff = getCmdBuff(fCode);
        //SN
        System.arraycopy(longToByte(param.getDevSn()), 0, cmdBuff, 4, 4);
        //流水号
        System.arraycopy(longToByte(++_Global_xid), 0, cmdBuff, 40, 4);
        switch (fCode) {
            //1.10	远程开门(模拟卡号)
            case X40 -> {
                //卡号
                System.arraycopy(longToByte(param.getCard()), 0, cmdBuff, 20, 4);
                //忽略卡号验证
                cmdBuff[32] = 0x5A;
                //端口号
                cmdBuff[8] = (byte) (param.getDoorNo() & 0xff);
                //进出标识
                cmdBuff[28] = (byte) (param.getDoorNo() & 0xff);
            }

            /*设置控制器*/
            case X80 -> {
                //端口号
                cmdBuff[8] = (byte) (param.getDoorNo() & 0xff);
                //控制方式
                cmdBuff[9] = (byte) (param.getControlType() & 0xff);
                //开门延时
                cmdBuff[10] = (byte) (param.getDoorOpeningDelay() & 0xff);

            }

            /*读取控制器设置*/
            case X82 -> {
                //端口号
                cmdBuff[8] = (byte) (param.getDoorNo() & 0xff);
            }

            //
            case X20, XB4 -> {
            }
            /*获取指定索引号的记录*/
            case XB0 -> {
                System.arraycopy(longToByte(param.getRecordNo()), 0, cmdBuff, 8, 4);
            }
            /*设置已读取过记录的索引号*/
            case XB2 -> {
                System.arraycopy(longToByte(param.getRecordNo()), 0, cmdBuff, 8, 4);
                System.arraycopy(flag, 0, cmdBuff, 12, 4);
            }

            //设置日期时间
            case X30 -> {
                //时间
                System.arraycopy(bcdEncode(param.getCurrentTime()), 0, cmdBuff, 8, 7);
            }

            //1.2	搜索控制器
            case X94 -> {
                break;
            }

            //1.3	设置接收服务器的IP和端口
            case X90 -> {
                String serverIp = param.getServerIp();
                String[] ipSplit = serverIp.split("\\.");
                int index = 8;
                for (String str : ipSplit) {
                    cmdBuff[index++] = (byte) (Integer.parseInt(str) & 0xFF);
                }
                System.arraycopy(longToByte(Long.parseLong(param.getServerPort())), 0, cmdBuff, 12, 2);
                cmdBuff[14] = (byte) (Integer.parseInt(param.getClientTimeOut()) & 0xFF);
                System.arraycopy(flag, 0, cmdBuff, 20, 4);
            }

            //1.3	设置控制器的IP地址
            case X96 -> {
                String deviceIp = param.getDeviceIp();
                String[] ipSplit = deviceIp.split("\\.");
                int index = 8;
                for (String str : ipSplit) {
                    cmdBuff[index++] = (byte) (Integer.parseInt(str) & 0xFF);
                }
                String subnetMask = param.getSubnetMask();
                String[] subnetMaskSplit = subnetMask.split("\\.");
                index = 12;
                for (String str : subnetMaskSplit) {
                    cmdBuff[index++] = (byte) (Integer.parseInt(str) & 0xFF);
                }
                String gateway = param.getGateway();
                String[] gatewaySplit = gateway.split("\\.");
                index = 16;
                for (String str : gatewaySplit) {
                    cmdBuff[index++] = (byte) (Integer.parseInt(str) & 0xFF);
                }
                System.arraycopy(flag, 0, cmdBuff, 20, 4);
            }

            /*1.25	设置电脑控制刷卡是否开门*/
            case XA0 -> {
                System.arraycopy(flag, 0, cmdBuff, 8, 4);
                cmdBuff[12] = 1 & 0xFF;
            }

            /*设置记录 门磁 按钮 报警 事件参数*/
            case X8E -> {
                cmdBuff[8] = 0 & 0xFF;
            }

            /*顺序上传权限*/
            case X56 -> {
                /*约束:  权限按卡号的由小到大顺序排列, 指定总的权限数和当前权限的索引号(从1开始)
                [此指令只能由某台PC单独完成从1到最后一条权限的下发操作. 不能由多台PC同时操作..]
                采用此指令时, 不要先清空权限.
                权限必须是全部上传完成后, 才生效. [权限数在8万以内]. 如果上传过程中, 中断未完成, 则系统仍使用之前的权限. */
                System.arraycopy(longToByte(param.getCard()), 0, cmdBuff, 8, 4);
                System.arraycopy(longToByte(param.getEffectiveDate()), 0, cmdBuff, 12, 4);
                System.arraycopy(longToByte(param.getExpiryDate()), 0, cmdBuff, 16, 4);
                cmdBuff[20] = 0x01;
                cmdBuff[21] = 0x01;
                cmdBuff[22] = 0x01;
                cmdBuff[23] = 0x01;
                System.arraycopy(longToByte(0), 0, cmdBuff, 24, 3);
            }

            case XF2 -> {
                System.arraycopy(flag, 0, cmdBuff, 8, 4);
                String serverTimeOut = param.getServerTimeOut();
                String serverControl = param.getServerControl();
                if (StringUtils.isNotBlank(serverTimeOut)) {
                    /*电脑控制生效超时时间*/
                    int timeOUt = Integer.parseInt(serverTimeOut);
                    cmdBuff[13] = 0x3B;
                    cmdBuff[15] = (byte) (timeOUt & 0XFF);
                } else if (StringUtils.isNotBlank(serverControl)) {
                    /*二维码透传*/
                    int control = Integer.parseInt(serverControl);
                    System.arraycopy(new byte[]{(byte) 0xf1, (byte) 0x01, (byte) control}, 0, cmdBuff, 13, 3);
                } else {
                    log.warn("指令类型[{}]-无效参数,serverTimeOut:{} , serverControl:{}", fCode.name, serverTimeOut, serverControl);
                }
            }
            default -> throw new RuntimeException("不支持的FunctionCode");
        }
        log.info("指令类型[{}]-预览:{}", fCode.name, bytesToHex(cmdBuff));
        return cmdBuff;
    }

    public static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, 0, "");
    }

    public static String bytesToHex(byte[] bytes, int splitGroupLen, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (splitGroupLen == 0) {
                sb.append(String.format("%02X", bytes[i]));
            } else {
                if ((i + 1) % splitGroupLen == 0 && i != bytes.length - 1) {
                    sb.append(String.format("%02X", bytes[i])).append(delimiter);
                } else {
                    sb.append(String.format("%02X", bytes[i]));
                }
            }
        }
        return sb.toString();
    }

    private static byte[] getCmdBuff(FunctionCodeEnum fCode) {
        byte[] buff = new byte[WGPacketSize];
        buff[0] = Type;
        buff[1] = fCode.code;
        return buff;
    }

    public static byte[] longToByte(long number) {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; i++) {
            b[i] = (byte) (number % 256);
            number >>= 8;
        }
        return b;
    }

    public static byte[] intToByte(int number) {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; i++) {
            b[i] = (byte) (number % 256);
            number >>= 8;
        }
        return b;
    }

    //将带符号的bt转换为不带符号的int类型数据
    public static int getIntByByte(byte bt) {  //bt 转换为无符号的int
        if (bt < 0) {
            return (bt + 256);
        } else {
            return bt;
        }
    }


    //从字节转换为 long型数据, 最大长度为8字节 低位在前, 高位在后...
    //bytlen (1--8), 不在此范围则返回 -1
    public static long getLongByByte(byte[] data, int startIndex, int bytlen) {
        long ret = -1;
        if ((bytlen >= 1) && (bytlen <= 8)) {
            ret = getIntByByte(data[startIndex + bytlen - 1]);
            for (int i = 1; i < bytlen; i++) {
                ret <<= 8;
                ret += getIntByByte(data[startIndex + bytlen - 1 - i]);
            }
        }
        return ret;
    }

    public static byte[] bcdEncode(Date time) {
        byte[] bytes = new byte[7];
        String timeStr = DateFormatUtils.format(time, "yyyy-MM-dd-HH-mm-ss");
        timeStr = timeStr.substring(0, 2) + "-" + timeStr.substring(2);
        String[] timeSplit = timeStr.split("-");
        for (int i = 0; i < timeSplit.length; i++) {
            int decInt = Integer.parseInt(timeSplit[i]);
            byte bcd = (byte) (((decInt / 10) << 4) | (decInt % 10));
            bytes[i] = bcd;
        }
        return bytes;
    }

    public static String bcdTimeDecode(byte[] bytes) {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            int bcd = bytes[i];
            int decimal = (bcd >> 4) * 10 + (bcd & 0x0F);
            String decStr = StringUtils.leftPad(String.valueOf(decimal), 2, "0");
            temp.append(decStr);
        }
        return temp.toString();
    }

    public static String bcdTDecode(byte[] bytes, int startIndex, int len) {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int bcd = bytes[startIndex + i];
            temp.append((bcd >> 4) * 10 + (bcd & 0x0F));
        }
        return temp.toString();
    }

    public static String getIntStrByByte(byte[] bytes, int startIndex, int len, String delimiter) {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int value = getIntByByte(bytes[startIndex + i]);
            if (i == len - 1) {
                temp.append(value);
            } else {
                temp.append(value).append(delimiter);
            }
        }
        return temp.toString();
    }

    public static void main(String[] args) {
        Date time = new Date();
        String timeStr = DateFormatUtils.format(time, "yyyy-MM-dd-HH-mm-ss");
        System.out.println(timeStr);
        byte[] bytes = bcdEncode(time);
        System.out.println(JSONObject.toJSONString(bytesToHex(bytes)));

        String decode = bcdTimeDecode(bytes);
        System.out.println(decode);
    }


}
