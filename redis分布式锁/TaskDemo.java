package com.ningpai.clusterTask.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

@Component
@Slf4j
public class TaskDemo {

    @Autowired
    private JedisPool jedisPool;

    @Scheduled(cron = "0/10 * * ? * *")
    public void task2(){
        String uuid = UUID.randomUUID().toString();
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            boolean task = RedisLock.tryGetDistributedLock("task", uuid, jedis);
            if (task) {
                log.info("开始执行定时任务");
                System.out.println("11111111111");
                log.info("定时任务结束");
                RedisLock.releaseDistributedLock("task",uuid,jedis);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }
    }

}
