package com.csc.door.at8000;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.FilterEvent;

import java.net.InetSocketAddress;
import java.util.Queue;

public class WgUdpCommShortHandler implements IoHandler {

	public Queue<byte[]> queue;

	public WgUdpCommShortHandler() {
	}

	public WgUdpCommShortHandler(Queue<byte[]> queue) {
		super();
		this.queue = queue;
	}

	public void exceptionCaught(IoSession session, Throwable e)
			throws Exception {
		e.printStackTrace();
		session.close(true);
	}

	public void messageReceived(IoSession session, Object message)
			throws Exception {
		String clientIp = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
		System.out.println( clientIp + " ... " + message.toString());

		IoBuffer io = (IoBuffer) message;
		if (io.hasRemaining())
		{
			byte[] validBytes = new byte[io.remaining()];
			io.get(validBytes,0,io.remaining());
			if ((validBytes.length == WgUdpCommShort.WGPacketSize)
					&& (validBytes[0] == WgUdpCommShort.Type))  //型号固定
			{
				synchronized (queue)
		         {
  				   queue.offer(validBytes);
		         }
			}
			else
			{
				//System.out.print("收到无效数据包: ????\r\n");
			}
			//System.out.println("");
		}
	}

	public void messageSent(IoSession session, Object message) throws Exception {

	}

	@Override
	public void inputClosed(IoSession ioSession) throws Exception {

	}

	@Override
	public void event(IoSession ioSession, FilterEvent filterEvent) throws Exception {

	}

	public void sessionClosed(IoSession session) throws Exception {

	}

	public void sessionCreated(IoSession session) throws Exception {

	}

	public void sessionIdle(IoSession session, IdleStatus idle)
			throws Exception {

	}

	public void sessionOpened(IoSession session) throws Exception {

	}

}
