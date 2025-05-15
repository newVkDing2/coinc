package com.ruoyi.listen;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.bussiness.domain.TAppRecharge;
import com.ruoyi.bussiness.domain.TWithdraw;
import com.ruoyi.bussiness.service.ITWithdrawService;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.enums.RecordEnum;
import com.ruoyi.common.utils.RedisUtil;
import com.ruoyi.socket.service.MarketThread;
import com.ruoyi.socket.socketserver.WebSocketNotice;
import com.ruoyi.telegrambot.MyTelegramBot;
import com.ruoyi.util.BotMessageBuildUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ListenerMessage implements StreamListener<String, MapRecord<String, String, String>> {

    ITWithdrawService withdrawService;
    RedisUtil redisUtil;
    List<MarketThread> marketThread;
    WebSocketNotice webSocketNotice;

    private MyTelegramBot myTelegramBot;
    public ListenerMessage(RedisUtil redisUtil, List<MarketThread> marketThread, WebSocketNotice webSocketNotice, ITWithdrawService withdrawService,MyTelegramBot myTelegramBot){
        this.redisUtil = redisUtil;
        this.marketThread = marketThread;
        this.webSocketNotice = webSocketNotice;
        this.withdrawService = withdrawService;
        this.myTelegramBot = myTelegramBot;
    }

    @Override
    public void onMessage(MapRecord<String, String, String> entries) {
        try{
            //check用于验证key和对应消息是否一直
            log.debug("stream name :{}, body:{}, check:{}",entries.getStream(),entries.getValue(),(entries.getStream().equals(entries.getValue().get("name"))));
            String stream = entries.getStream();
            switch (stream) {
                case "notice_key":
                    Map<String,String> map =entries.getValue();
                    if(map.containsKey(CacheConstants.WITHDRAW_KEY)){
                        webSocketNotice.sendInfoAll(withdrawService,1);
                    }
                    if(map.containsKey(CacheConstants.RECHARGE_KEY)){
                        webSocketNotice.sendInfoAll(withdrawService,2);

                    }
                    if(map.containsKey(CacheConstants.VERIFIED_KEY)){
                        webSocketNotice.sendInfoAll(withdrawService,3);
                    }
                    if(map.containsKey(CacheConstants.WITHDRAW_KEY_BOT)){
                        String s = map.get(CacheConstants.WITHDRAW_KEY_BOT);
                        TWithdraw withdraw = JSON.parseObject(s, TWithdraw.class);
                        SendMessage sendMessage = BotMessageBuildUtils.buildText(RecordEnum.WITHDRAW.getCode(), null, withdraw);
                        myTelegramBot.toSend(sendMessage);
                    }
                    if(map.containsKey(CacheConstants.RECHARGE_KEY_BOT)){
                        String s = map.get(CacheConstants.RECHARGE_KEY_BOT);
                        TAppRecharge recharge = JSON.parseObject(s, TAppRecharge.class);
                        SendMessage sendMessage = BotMessageBuildUtils.buildText(RecordEnum.RECHARGE.getCode(), recharge, null);
                        myTelegramBot.toSend(sendMessage);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + stream);
            }
            redisUtil.delField(entries.getStream(),entries.getId().getValue());
        }catch (Exception e){
            log.error("error message:{}",e.getMessage());
        }
    }

}
