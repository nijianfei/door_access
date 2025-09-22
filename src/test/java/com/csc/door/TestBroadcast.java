package com.csc.door;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.net.*;

public class TestBroadcast {
    private static final int TIMEOUT = 5000; // 5秒超时
    private static final int MAX_TRIES = 3;  // 最大重试次数
    private static final int BROADCAST_PORT = 60000; // 广播端口

    public static void main(String[] args) {
        String message = "17940000000000000000000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000";
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(null);
            // 设置重用地址选项（可选，但推荐）
            socket.setReuseAddress(true);
            // 绑定到特定IP和端口
            InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getByName("172.16.121.1"), 0);
            socket.bind(inetSocketAddress);

            // 关键设置：启用广播
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT);

            byte[] sendData = Hex.decodeHex(message);
            byte[] receiveData = new byte[64];

            System.out.println("Broadcasting message: " + message);
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
            do {
                try {
                    socket.receive(receivePacket);
                    receivedResponse = true;
                    System.out.println("Received response from " +
                            receivePacket.getAddress() + ":" +
                            receivePacket.getPort() + " - " + Hex.encodeHexString(receivePacket.getData()));
                } catch (SocketTimeoutException e) {
                    tries++;
                    System.out.println("Timeout - Retrying (" + tries + "/" + MAX_TRIES + ")");
                }
            } while (!receivedResponse && tries < MAX_TRIES);

            if (!receivedResponse) {
                System.out.println("No response after " + MAX_TRIES + " tries");
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        } catch (DecoderException e) {
            System.err.println("Hex decoding error: " + e.getMessage());
        } finally {
            socket.close();
        }
    }
}