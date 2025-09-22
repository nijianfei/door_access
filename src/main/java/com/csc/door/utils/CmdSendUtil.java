package com.csc.door.utils;

import com.csc.door.handler.UdpClientHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.codehaus.commons.nullanalysis.NotNull;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class CmdSendUtil {
    private CmdSendUtil() {
    }

    public static final int WGPacketSize = 64;             //报文长度
    public static final int ControllerPort = 60000;        //控制器端口
    private static Map<String, CmdSendUtil> cache = new HashMap<>();

    private int timeout = 300;
    private int sleepTime = 30;
    private Queue<byte[]> queue;
    private IoConnector connector;
    private ConnectFuture connFuture;

    private String sourceId;


    public byte[] sendCmd(@NotNull byte[] cmd) {
        return getInfo(cmd);
    }


    public CmdSendUtil CommOpen(String ip, int port, String sourceId) {
        queue = new ConcurrentLinkedQueue<byte[]>();
        connector = new NioDatagramConnector();
        connector.setHandler(new UdpClientHandler());
        connFuture = connector.connect(new InetSocketAddress(ip, port), new InetSocketAddress(sourceId, 0));
        this.sourceId = sourceId;
        return this;
    }


    //获取数据
    public byte[] getInfo(byte[] command) {
        byte[] bytCommand = command;
        IoBuffer b;
        IoSession session = connFuture.getSession();
        Boolean bSent = false;
        if (session != null) {
            if (session.isConnected()) {
                b = IoBuffer.allocate(bytCommand.length);
                b.put(bytCommand);
                b.flip();
                session.write(b);
                bSent = true;
            }
        }

        int bSuccess = 0;
        int tries = 3;
        long xid = getXidOfCommand(bytCommand);
        byte[] bytget = null;
        while ((tries--) > 0) {
            long startTicks = Calendar.getInstance().getTimeInMillis(); // DateTime.Now.Ticks;
            long CommTimeoutMsMin = timeout;
            long endTicks = startTicks + CommTimeoutMsMin;
            if (startTicks > endTicks) {
                //System.out.println("超时");
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            long startIndex = 0;
            while (endTicks > Calendar.getInstance().getTimeInMillis()) {
                if (!bSent)  //没有发送过....
                {
                    session = connFuture.getSession();
                    if (session != null) {
                        if (session.isConnected()) {
                            b = IoBuffer.allocate(bytCommand.length);
                            b.put(bytCommand);
                            b.flip();
                            session.write(b);
                            bSent = true;
                        }
                    }
                }
                if (!queue.isEmpty()) {
                    synchronized (queue) {
                        bytget = queue.poll();
                    }
                    if ((bytget[0] == bytCommand[0]) //类型一致
                            && (bytget[1] == bytCommand[1]) //功能号一致
                            && (xid == getXidOfCommand(bytget)))  //序列号对应
                    {
                        bSuccess = 1;
                        break; // return ret;
                    } else {
                        //System.out.printf("无效包 xid=%d\r\n", WgUdpComm.getXidOfCommand(bytget));
                    }
                } else {
                    if ((startTicks + 1) < Calendar.getInstance().getTimeInMillis()) {
                    } else if (startIndex > 10) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        startIndex++;
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            if (bSuccess > 0) {
                break;
            } else {
                // System.out.println("重试....");
                session = connFuture.getSession();
                if (session != null) {
                    if (session.isConnected()) {
                        b = IoBuffer.allocate(bytCommand.length);
                        b.put(bytCommand);
                        b.flip();
                        session.write(b);
                    }
                }
            }
        }

        if (bSuccess > 0) {
            //System.out.println("通信 成功");
            return bytget;
        } else {
            //System.out.println("通信 失败....");
        }
        return null;
    }

    private static long getXidOfCommand(byte[] cmd) //获取指令中的xid
    {
        long ret = -1;
        if (cmd.length >= WGPacketSize) {
            ret = getLongByByte(cmd, 40, 4);
        }
        return ret;
    }

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

    public static CmdSendUtil instance(String ip) {
        return instance(ip, 300, 30);
    }

    public static CmdSendUtil instance(String ip, int timeout, int sleepTime) {
        if (Objects.isNull(cache.get(ip))) {
            synchronized (CmdSendUtil.class) {
                if (Objects.isNull(cache.get(ip))) {
                    CmdSendUtil instance = new CmdSendUtil().CommOpen(ip, ControllerPort,"192.168.0.1");
                    cache.put(ip, instance);
                }
            }
        }
        CmdSendUtil cmdSendUtil = cache.get(ip);
        cmdSendUtil.timeout = timeout;
        cmdSendUtil.sleepTime = sleepTime;
        return cmdSendUtil;
    }

    public void offerQueue(byte[] validBytes) {
        queue.offer(validBytes);
    }


    private static final int TIMEOUT = 5000; // 5秒超时
    private static final int MAX_TRIES = 3;  // 最大重试次数
    private static final int BROADCAST_PORT = 60000; // 广播端口

    public List<byte[]> broadcastSend(byte[] sendData) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(null);
            // 设置重用地址选项（可选，但推荐）
            socket.setReuseAddress(true);
            // 绑定到特定IP和端口
            InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getByName(sourceId), 0);
            socket.bind(inetSocketAddress);

            // 关键设置：启用广播
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT);
            byte[] receiveData = new byte[64];
            System.out.println("Bound to local address: " + inetSocketAddress.getAddress() + ":" + inetSocketAddress.getPort());

            // 发送到所有可能的广播地址
            InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");// 全局广播地址
            try {
                DatagramPacket sendPacket = new DatagramPacket(
                        sendData, sendData.length, broadcastAddr, BROADCAST_PORT
                );
                socket.send(sendPacket);
                System.out.println("Sent to " + broadcastAddr);
            } catch (IOException e) {
                System.err.println("Error sending to " + broadcastAddr + ": " + e.getMessage());
            }


            // 等待回复
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            int tries = 0;
            boolean receivedResponse = false;
            List<byte[]> bytes = new ArrayList<>();
            do {
                try {
                    socket.receive(receivePacket);
                    receivedResponse = true;
                    System.out.println("Received response from " +
                            receivePacket.getAddress() + ":" +
                            receivePacket.getPort() + " - " + Hex.encodeHexString(receivePacket.getData()));
                    bytes.add(receivePacket.getData());
                } catch (SocketTimeoutException e) {
                    tries++;
                    System.out.println("Timeout - Retrying (" + tries + "/" + MAX_TRIES + ")");
                }
            } while (!receivedResponse && tries < MAX_TRIES);

            if (!receivedResponse) {
                System.out.println("No response after " + MAX_TRIES + " tries");
            }
            return bytes;
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }  finally {
            socket.close();
        }
    }
}
