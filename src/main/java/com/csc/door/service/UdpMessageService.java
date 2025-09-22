package com.csc.door.service;

public interface UdpMessageService {
    void processMsg(String ip , byte[] message);
}
