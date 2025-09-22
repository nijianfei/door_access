package com.csc.door.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum RecordTypeEnum {
    //记录原因 (类型中 SwipePass 表示通过; SwipeNOPass表示禁止通过; ValidEvent 有效事件(如按钮 门磁 超级密码开门); Warn 报警事件)
    //代码  类型   英文描述  中文描述
    C1(1, "SwipePass", "Swipe", "刷卡开门"),
    C2(2, "SwipePass", "Swipe Close", "刷卡关"),
    C3(3, "SwipePass", "Swipe Open", "刷卡开"),
    C4(4, "SwipePass", "Swipe Limited Times", "刷卡开门(带限次)"),
    C5(5, "SwipeNOPass", "Denied Access: PC Control", "刷卡禁止通过: 电脑控制"),
    C6(6, "SwipeNOPass", "Denied Access: No PRIVILEGE", "刷卡禁止通过: 没有权限"),
    C7(7, "SwipeNOPass", "Denied Access: Wrong PASSWORD", "刷卡禁止通过: 密码不对"),
    C8(8, "SwipeNOPass", "Denied Access: AntiBack", "刷卡禁止通过: 反潜回"),
    C9(9, "SwipeNOPass", "Denied Access: More Cards", "刷卡禁止通过: 多卡"),
    C10(10, "SwipeNOPass", "Denied Access: First Card Open", "刷卡禁止通过: 首卡"),
    C11(11, "SwipeNOPass", "Denied Access: Door Set NC", "刷卡禁止通过: 门为常闭"),
    C12(12, "SwipeNOPass", "Denied Access: InterLock", "刷卡禁止通过: 互锁"),
    C13(13, "SwipeNOPass", "Denied Access: Limited Times", "刷卡禁止通过: 受刷卡次数限制"),
    C14(14, "SwipeNOPass", "Denied Access: Limited Person Indoor", "刷卡禁止通过: 门内人数限制"),
    C15(15, "SwipeNOPass", "Denied Access: Invalid Timezone", "刷卡禁止通过: 卡过期或不在有效时段"),
    C16(16, "SwipeNOPass", "Denied Access: In Order", "刷卡禁止通过: 按顺序进出限制"),
    C17(17, "SwipeNOPass", "Denied Access: SWIPE GAP LIMIT", "刷卡禁止通过: 刷卡间隔约束"),
    C18(18, "SwipeNOPass", "Denied Access", "刷卡禁止通过: 原因不明"),
    C19(19, "SwipeNOPass", "Denied Access: Limited Times", "刷卡禁止通过: 刷卡次数限制"),
    C20(20, "ValidEvent", "Push Button", "按钮开门"),
    C21(21, "ValidEvent", "Push Button Open", "按钮开"),
    C22(22, "ValidEvent", "Push Button Close", "按钮关"),
    C23(23, "ValidEvent", "Door Open", "门打开[门磁信号]"),
    C24(24, "ValidEvent", "Door Closed", "门关闭[门磁信号]"),
    C25(25, "ValidEvent", "Super Password Open Door", "超级密码开门"),
    C26(26, "ValidEvent", "Super Password Open", "超级密码开"),
    C27(27, "ValidEvent", "Super Password Close", "超级密码关"),
    C28(28, "Warn", "Controller Power On", "控制器上电"),
    C29(29, "Warn", "Controller Reset", "控制器复位"),
    C30(30, "Warn", "Push Button Invalid: Disable", "按钮不开门: 按钮禁用"),
    C31(31, "Warn", "Push Button Invalid: Forced Lock", "按钮不开门: 强制关门"),
    C32(32, "Warn", "Push Button Invalid: Not On Line", "按钮不开门: 门不在线"),
    C33(33, "Warn", "Push Button Invalid: InterLock", "按钮不开门: 互锁"),
    C34(34, "Warn", "Threat", "胁迫报警"),
    C35(35, "Warn", "Threat Open", "胁迫报警开"),
    C36(36, "Warn", "Threat Close", "胁迫报警关"),
    C37(37, "Warn", "Open too long", "门长时间未关报警[合法开门后]"),
    C38(38, "Warn", "Forced Open", "强行闯入报警"),
    C39(39, "Warn", "Fire", "火警"),
    C40(40, "Warn", "Forced Close", "强制关门"),
    C41(41, "Warn", "Guard Against Theft", "防盗报警"),
    C42(42, "Warn", "7*24Hour Zone", "烟雾煤气温度报警"),
    C43(43, "Warn", "Emergency Call", "紧急呼救报警"),
    C44(44, "RemoteOpen", "Remote Open Door", "操作员远程开门"),
    C45(45, "RemoteOpen", "Remote Open Door By USB Reader", "发卡器确定发出的远程开门"),

    ;


    RecordTypeEnum(int code, String type, String englishDetail, String chineseDetail) {
        this.code = code;
        this.type = type;
        this.englishDetail = englishDetail;
        this.chineseDetail = chineseDetail;
    }

    private static Map<Integer, RecordTypeEnum> codeMapping = Arrays.stream(RecordTypeEnum.values()).collect(Collectors.toMap(RecordTypeEnum::getCode, Function.identity()));
    private int code;
    private String type;
    private String englishDetail;
    private String chineseDetail;

    public static RecordTypeEnum getEnum(int code){
        return codeMapping.get(code);
    }
    public static String getChineseDetail(int code){
        RecordTypeEnum typeEnum = codeMapping.get(code);
        if (typeEnum == null) {
            return "" ;
        }
        return typeEnum.getChineseDetail();
    }
}
