package com.ningpai.clusterTask.utils;

import com.ningpai.clusterTask.bean.TScheduleCluster;
import com.ningpai.clusterTask.service.TScheduleClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Date;

@Component
public class ScheduleTaskUtils implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleTaskUtils.class);

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ScheduleTaskUtils.applicationContext = applicationContext;
    }

    public static boolean isValidMachine(long maxExpireTime, String taskName){
        boolean validFlag = false;
        try {
            TScheduleClusterService tScheduleClusterService =
                    applicationContext.getBean(TScheduleClusterService.class);
            // 根据任务名查询一条数据
            TScheduleCluster tScheduleCluster = tScheduleClusterService.queryOneTask(taskName);
            Integer execute = tScheduleCluster.getExecute();
            long preUpdateTime = tScheduleCluster.getUpdateTime().getTime();
            String ip = InetAddress.getLocalHost().getHostAddress();
            long curTime = System.currentTimeMillis();

            TScheduleCluster updateScheduleCluster = new TScheduleCluster();
            updateScheduleCluster.setId(tScheduleCluster.getId());
            updateScheduleCluster.setExecute(1);
            Integer version = tScheduleCluster.getVersion();
            if(version >= 999999999){
                updateScheduleCluster.setVersion(0);
            }else {
                updateScheduleCluster.setVersion(version + 1);
            }
            updateScheduleCluster.setExecuteIp(ip);
            updateScheduleCluster.setUpdateTime(new Date(curTime));
            if (execute == 0 && preUpdateTime + maxExpireTime - 1000 < curTime) {
                // 更新任务执行状态为正在执行
                int result = tScheduleClusterService.updateOneTask(updateScheduleCluster, version);
                if(result == 1){
                    validFlag = true;
                }
            } else if (preUpdateTime + maxExpireTime - 1000 < curTime) {
                // 更新任务执行状态为正在执行
                int result = tScheduleClusterService.updateOneTask(updateScheduleCluster, version);
                if(result == 1){
                    validFlag = true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return validFlag;
    }

    public static boolean start(long maxExpireTime, String taskName){
        // 开始执行定时任务
        return isValidMachine(maxExpireTime,taskName);
    }

    public static void end(String taskName){
        // 结束定时任务
        TScheduleClusterService tScheduleClusterService =
                applicationContext.getBean(TScheduleClusterService.class);
        tScheduleClusterService.updateToZero(taskName);
    }
}
