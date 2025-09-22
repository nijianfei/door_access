package com.csc.door.enums;

public enum FunctionCodeEnum {
    X94((byte)0x94,"搜索控制器"),
    X96((byte)0x96,"设置控制器的IP地址"),
    X20((byte)0x20,"查询控制器状态(实时监控用)"),
    X32((byte)0x32,"读取日期时间"),
    X30((byte)0x30,"设置日期时间"),
    XB0((byte)0xB0,"获取指定索引号的记录"),
    XB2((byte)0xB2,"设置已读取过的记录索引号"),
    XB4((byte)0xB4,"获取已读取过的记录索引号"),
    X40((byte)0x40,"远程开门"),
    X50((byte)0x50,"权限添加或修改"),
    X52((byte)0x52,"权限删除(单个删除)"),
    X54((byte)0x54,"权限清空(全部清掉)"),
    X58((byte)0x58,"权限总数读取"),
    X5A((byte)0x5A,"权限查询"),
    X5C((byte)0x5C,"获取指定索引号的权限"),
    X80((byte)0x80,"设置门控制参数(在线/延时)"),
    X82((byte)0x82,"读取门控制参数(在线/延时)"),
    X90((byte)0x90,"设置接收服务器的IP和端口 "),
    X92((byte)0x92,"读取接收服务器的IP和端口"),
    X56((byte)0x56,"权限按从小到大顺序添加"),
    XA0((byte)0xA0,"设置电脑控制刷卡是否开门"),

    X8E((byte)0x8E,"设置记录 门磁 按钮 报警 事件参数"),
    XF2((byte)0xF2,"其他设置"),


    ;
    public final byte code;
    public final String name;

    FunctionCodeEnum(byte code, String name) {
        this.code = code;
        this.name = name;
    }
}
