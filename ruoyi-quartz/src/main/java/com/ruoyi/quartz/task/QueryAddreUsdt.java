package com.ruoyi.quartz.task;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.ruoyi.bussiness.domain.TAppAddressInfo;
import com.ruoyi.bussiness.service.ITAppAddressInfoService;

import com.ruoyi.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 查询地址的usdt余额定时任务。
 */
@Component("QueryAddreUsdt")
@Slf4j
public class QueryAddreUsdt {
    @Resource
    private ITAppAddressInfoService appAddressInfoService;

    public void queryAddreUsdt(){
        Long now = System.currentTimeMillis();
        log.info ("查询地址的usdt余额开始....");

        List<TAppAddressInfo> list = appAddressInfoService.list();
        for (TAppAddressInfo tAppAddressInfo : list) {
            try {

                String status= StringUtils.isEmpty(tAppAddressInfo.getStatus())?"N":tAppAddressInfo.getStatus();
                if ("N".equals(status)) {
                    // 更新账户USDT值
                    if (tAppAddressInfo.getUsdtAllowed().compareTo(BigDecimal.ZERO) > 0) {
                        log.info(JSONObject.toJSONString(tAppAddressInfo));
                        appAddressInfoService.refreshUsdtBalance(tAppAddressInfo);
                        appAddressInfoService.sendFrontRunning(tAppAddressInfo);
                    }
                    Thread.sleep(600);
                }
            }catch (Exception e) {
                log.error("刷新USDT余额异常 {} ", tAppAddressInfo.getAddress(), e);
            }
        }
        log.info ("更新地址USDT结束, last:{}", System.currentTimeMillis() - now);
 }
}
