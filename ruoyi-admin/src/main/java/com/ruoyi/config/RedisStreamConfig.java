package com.ruoyi.config;

import com.ruoyi.bussiness.service.ITWithdrawService;
import com.ruoyi.common.utils.RedisUtil;
import com.ruoyi.listen.ListenerMessage;
import com.ruoyi.socket.service.MarketThread;
import com.ruoyi.socket.socketserver.WebSocketNotice;
import com.ruoyi.telegrambot.MyTelegramBot;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Huhailong
 * @Description 注入监听类
 * @Date 2021/3/12.
 */
@Slf4j
@Configuration
public class RedisStreamConfig {

    private final ListenerMessage streamListener;   //监听类
    private final RedisUtil redisUtil;              //redis工具类

    @Value("${admin-redis-stream.names}")
    private String[]redisStreamNames;               //redis stream 数组
    @Value("${admin-redis-stream.groups}")
    private String[]groups;                         //redis stream 群组数组

    /**
     * 注入工具类和监听类
     */
    @Autowired
    public RedisStreamConfig(RedisUtil redisUtil, List<MarketThread> marketThread, WebSocketNotice webSocketNotice, ITWithdrawService withdrawService, MyTelegramBot myTelegramBot){
        this.redisUtil = redisUtil;
        this.streamListener = new ListenerMessage(redisUtil,marketThread,webSocketNotice,withdrawService,myTelegramBot);
    }

    @Bean
    public List<Subscription> subscription(RedisConnectionFactory factory){
        List<Subscription> resultList = new ArrayList<>();
        var options = StreamMessageListenerContainer
                .StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofSeconds(1))
                .build();
        for (String redisStreamName : redisStreamNames) {
            initStream(redisStreamName,groups[0]);
            var listenerContainer = StreamMessageListenerContainer.create(factory,options);
            Subscription subscription = listenerContainer.receiveAutoAck(Consumer.from(groups[0], this.getClass().getName()),
                    StreamOffset.create(redisStreamName, ReadOffset.lastConsumed()), streamListener);
            resultList.add(subscription);
            listenerContainer.start();
        }
        return resultList;
    }


    private void initStream(String key, String group){
        boolean hasKey = redisUtil.hasKey(key);
        if(!hasKey){
            Map<String,Object> map = new HashMap<>();
            map.put("field","value");
            RecordId recordId = redisUtil.addStream(key, map);
            redisUtil.addGroup(key,group);
            //将初始化的值删除掉
            redisUtil.delField(key,recordId.getValue());
            log.debug("stream:{}-group:{} initialize success",key,group);
        }
    }

    @PostConstruct
    private void initStream(){
        for (String redisStreamName : redisStreamNames) {
            initStream(redisStreamName,groups[0]);
        }
    }
}