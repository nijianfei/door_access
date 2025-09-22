package com.csc.door.handler;

import com.csc.door.dto.MsgRecordStatus;
import com.csc.door.service.UdpMessageService;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class UdpServerHandler extends IoHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(UdpServerHandler.class);

    private ConcurrentMap<String, MsgRecordStatus> MsgRecordMap = new ConcurrentHashMap<>();

    @Autowired
    private UdpMessageService upMessageService;

    /*服务端接收消息处理*/
    @Override
    public void messageReceived(IoSession session, Object message) {
        String ip = (String) session.getAttribute("IP");
        log.info("【{}】session接收到message:{}", ip, message);
        // 1. 解析原始数据（通常为IoBuffer或byte[]）
        IoBuffer io = (IoBuffer) message;
        if (io.hasRemaining()) {
            byte[] validBytes = new byte[io.remaining()];
            io.get(validBytes, 0, io.remaining());
            upMessageService.processMsg(ip,validBytes);
        }
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        Object ip = session.getAttribute("IP");
        log.info("【{}】session与服务器断开连接...", ip);
        MsgRecordMap.remove(ip);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
        Object ip = session.getAttribute("IP");
        log.info("【{}】session与服务器超时,断开连接...", ip);
        MsgRecordMap.remove(ip);
    }


    @Override
    public void sessionCreated(IoSession session) {
        String clientIp = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
        log.info("服务器端成功创建一个【{}】session...", clientIp);
        MsgRecordStatus msgRecordStatus = MsgRecordMap.get(clientIp);
        if (Objects.isNull(msgRecordStatus)) {
            msgRecordStatus = new MsgRecordStatus();
            MsgRecordMap.put(clientIp, msgRecordStatus);
        }
        if (msgRecordStatus.getSessionCount() > 1) {
            session.closeNow();
        } else {
            msgRecordStatus.setSessionCount(msgRecordStatus.getSessionCount() + 1);
        }
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        String clientIp = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
        session.setAttribute("IP", clientIp);
        log.info("服务器端成功打开一个【{}】session...", clientIp);
    }

    /**
     * 异常来关闭session
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception {
        Object ip = session.getAttribute("IP");
        log.error("{}_session出现异常:{}", ip, cause.getMessage(), cause);
    }
}
