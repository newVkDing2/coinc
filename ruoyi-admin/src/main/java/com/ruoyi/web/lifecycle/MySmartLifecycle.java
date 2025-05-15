package com.ruoyi.web.lifecycle;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.CachePrefix;
import com.ruoyi.common.utils.SpringContextUtil;
import com.ruoyi.telegrambot.MyTelegramBot;
import com.ruoyi.telegrambot.TelegramBotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
@Slf4j
@Component
public class MySmartLifecycle implements SmartLifecycle {

    private volatile boolean running = false;

    /*** true：让Lifecycle类所在的上下文在调用`refresh`时,能够自己自动进行回调* false：表明组件打算通过显式的start()调用来启动，类似于普通的Lifecycle实现。*/
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /*** 很多框架中，把真正逻辑写在stop()方法内。比如quartz和Redis的spring支持包*/
    @Override
    public void stop(Runnable callback) {
        System.out.println("stop(callback)");
        stop();
        callback.run();
    }

    @Override
    public void start() {
        log.info("SmartLifecycle  生命周期初始化 ==========开始");
        //初始化机器人配置
        MyTelegramBot myTelegramBot = SpringContextUtil.getBean(MyTelegramBot.class);
        myTelegramBot.initMyTelegramBot();
        log.info("myTelegramBot  初始化 ==========结束");
        //启动机器人
        TelegramBotConfig telegramBotConfig = SpringContextUtil.getBean(TelegramBotConfig.class);
        try {
            log.info("开始创建机器人链接 TelegramBot============开始");
            telegramBotConfig.start();
            log.info("开始创建机器人链接 TelegramBot============结束");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }        running = true;
    }

    @Override
    public void stop() {
        TelegramBotConfig telegramBotConfig = SpringContextUtil.getBean(TelegramBotConfig.class);
        telegramBotConfig.stop();
        RedisCache redisCache = SpringContextUtil.getBean(RedisCache.class);
        redisCache.deleteObject("notice_key");
        redisCache.deleteObject("notice");
        redisCache.deleteObject(CachePrefix.ORDER_SECOND_CONTRACT.getPrefix()+"*");
        running = false;
    }

    @Override
    public boolean isRunning() {
        System.out.println("isRunning()");
        return running;
    }

    /*** 阶段值。越小：start()方法越靠前，stop()方法越靠后*/
    @Override
    public int getPhase() {
        System.out.println("getPhase()");
        return 0;
    }
}