package com.ningpai.clusterTask.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
@Slf4j
public class TaskDemo {

    @Scheduled(cron = "0/10 * * ? * *")
    public void task(){
        log.info("开始执行定时任务");
        boolean flag = ScheduleTaskUtils.start(10*1000,"task");
        if(flag) {
            try {
                String ip = InetAddress.getLocalHost().getHostAddress();
                log.info(ip + "执行了定时任务task");
            } catch (Exception e) {
                e.printStackTrace();
            }
            ScheduleTaskUtils.end("task");
        }
        log.info("定时任务结束");
    }

    @Scheduled(cron = "0/10 * * ? * *")
    public void task2(){
        log.info("开始执行定时任务");
        boolean flag = ScheduleTaskUtils.start(10*1000,"task2");
        if(flag) {
            try {
                String ip = InetAddress.getLocalHost().getHostAddress();
                log.info(ip + "执行了定时任务task2222");
            } catch (Exception e) {
                e.printStackTrace();
            }
            ScheduleTaskUtils.end("task2");
        }
        log.info("定时任务结束");
    }
}
