package com.csc.door.server;


import com.csc.door.handler.UdpServerHandler;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.ExpiringSessionRecycler;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.*;

@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "mina.server")
public class UdpServer {
    private String ip;
    private int port;
    private int bossThreads;
    private int workerCoreThreads;
    private int workerMaxThreads;
    private int idleTimeout;
    private int bufferSize;

    private boolean serverStart = false;


    @Autowired
    private UdpServerHandler udpIoHandler;

    private NioDatagramAcceptor acceptor;

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


    @Bean
    public IoAcceptor ioAcceptor() throws IOException, DecoderException {
        // 创建 NIO Acceptor
        acceptor = new NioDatagramAcceptor();
        acceptor.setHandler(udpIoHandler);
        // 绑定 IP 和端口
        InetSocketAddress address = new InetSocketAddress(ip, port);
        acceptor.bind(address);
        log.info("绑定ip:[{}],port:[{}]成功,进入接收服务器监控状态....", ip, port);
        // 配置会话参数
        DatagramSessionConfig sessionConfig = acceptor.getSessionConfig();
        sessionConfig.setReadBufferSize(bufferSize);
        sessionConfig.setSendBufferSize(bufferSize);
        sessionConfig.setReaderIdleTime( idleTimeout);
        sessionConfig.setWriterIdleTime(0);
        sessionConfig.setReuseAddress(true);


        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(workerCoreThreads, workerMaxThreads, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));

        // 配置线程池
        ExecutorFilter executorFilter = new ExecutorFilter(threadPoolExecutor);
        acceptor.getFilterChain().addLast("udpSession-threadPool", executorFilter);

        // 每5秒打印一次线程池状态
        scheduler.scheduleAtFixedRate(() -> {
            if (threadPoolExecutor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor pool = threadPoolExecutor;
                log.info("[Monitor]  Active={}, coreSize={}, PoolSize={}, Queue={}, Completed={}"
                        , pool.getActiveCount(), pool.getCorePoolSize(), pool.getPoolSize(), pool.getQueue().size(), pool.getCompletedTaskCount());
            }
        }, 0, 30, TimeUnit.SECONDS);
        serverStart = true;
        return acceptor;
    }

    // 显式配置会话回收器
    @Bean
    public ExpiringSessionRecycler sessionRecycler() {
        return new ExpiringSessionRecycler(60 * 1000); // 60秒回收延迟
    }

    public void stopMonitor() {
        serverStart = false;
        acceptor.unbind();
    }

    public void startMonitor() throws Exception {
        serverStart = true;
        acceptor.bind(new InetSocketAddress(port)); // 重新绑定
    }

    @PreDestroy
    public void shutdown() {
        if (acceptor != null) {
            acceptor.getManagedSessions().values().forEach(session -> session.closeNow());
            acceptor.unbind();
            acceptor.dispose();
        }
        serverStart = false;
    }

}
