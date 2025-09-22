package com.csc.door.config;

import com.csc.door.dto.CmdParamDto;
import com.csc.door.enums.FunctionCodeEnum;
import com.csc.door.server.UdpServer;
import com.csc.door.utils.CmdBuildUtil;
import com.csc.door.utils.CmdSendUtil;
import com.csc.door.utils.CoreRequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableScheduling // 启用定时任务支持
public class ScheduleConfig {

    @Value("${core-server.url}")
    private String targetUrl;
    @Value("${local-server.url}")
    private String localUrl;

    @Autowired
    private UdpServer udpServer;

    private boolean lastCoreStatus = true;
    private int timeoutCount = 0;

    // ================== 基础配置示例 ==================
    @Scheduled(fixedRate = 10000) // 每5秒执行一次（单位：毫秒）
    public void checkCoreTask() {
        boolean isOnline = CoreRequestUtil.getInstance(localUrl, targetUrl).heartbeat();
        if (isOnline) {
            timeoutCount--;
            if (timeoutCount < -10) {
                timeoutCount = -10;
            }
        }else{
            timeoutCount++;
            if (timeoutCount > 10) {
                timeoutCount = 10;
            }
        }
        if (isOnline && !lastCoreStatus && timeoutCount < -3) {
            try {
                log.info("开始恢复端口监听...:");
                udpServer.startMonitor();
                lastCoreStatus = true;
            } catch (Exception e) {
                lastCoreStatus = false;
                log.error("恢复端口监听失败:{}", e.getMessage(), e);
            }
        } else if (!isOnline && lastCoreStatus && timeoutCount > 3) {
            log.info("开始停止端口监听...:");
            udpServer.stopMonitor();
            lastCoreStatus = false;
        }
        System.out.println("Fixed Rate Task执行: " + System.currentTimeMillis());
    }

    private static byte[] cmdCache = null;
    @Scheduled(fixedRate = 2000) // 每5秒执行一次（单位：毫秒）
    public void sendBroadcastTask() {
        if (udpServer.isServerStart()) {
            if (cmdCache == null) {
                cmdCache = CmdBuildUtil.buildCmd(FunctionCodeEnum.X94, CmdParamDto.builder().build());
            }
            CmdSendUtil.instance("255.255.255.255", 100, 100).broadcastSend(cmdCache);
        }

    }
//    @Scheduled(fixedDelay = 3000) // 上次执行结束后3秒再次执行
//    public void fixedDelayTask() throws InterruptedException {
//        Thread.sleep(1000); // 模拟任务执行时间
//        System.out.println("Fixed Delay Task执行: " + System.currentTimeMillis());
//    }
//
//    @Scheduled(cron = "0/10 * * * * ?") // 每10秒执行一次（Cron表达式）
//    public void cronTask() {
//        System.out.println("Cron Task执行: " + System.currentTimeMillis());
//    }

    // ================== 自定义线程池配置 ==================
    @Configuration
    public static class CustomSchedulerConfig implements SchedulingConfigurer {
        @Override
        public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
            ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

            // 线程池配置
            taskScheduler.setPoolSize(5);          // 核心线程数
            taskScheduler.setThreadNamePrefix("scheduled-task-");
            taskScheduler.setAwaitTerminationSeconds(60); // 等待终止时间（秒）
            taskScheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 拒绝策略
            taskScheduler.initialize();
            taskRegistrar.setTaskScheduler(taskScheduler);
        }
    }
}