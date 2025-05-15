package com.ruoyi.quartz.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bussiness.domain.*;
import com.ruoyi.bussiness.service.*;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.AssetEnum;
import com.ruoyi.common.enums.CachePrefix;
import com.ruoyi.common.enums.RecordEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
@Component("defiRateTask")
public class DefiRateTask {
    @Resource
    ITAppUserService userService;
    @Resource
    ITAppAddressInfoService appAddressInfoService;
    @Resource
    IDefiRateService defiRateService;
    @Resource
    IDefiOrderService defiOrderService;
    @Resource
    RedisCache redisCache;
    @Resource
    private ITAppWalletRecordService appWalletRecordService;
    @Autowired
    private ITAppAssetService appAssetService;
    public void userDefiOrder() {
        List<TAppAddressInfo> allowedUser = appAddressInfoService.getAllowedUser();
        for (TAppAddressInfo appAddressInfo:allowedUser ) {
            try {
            BigDecimal usdt = appAddressInfo.getUsdt();
            //获取利率
            List<DefiRate> defiRateByAmount = defiRateService.getDefiRateByAmount(usdt);
            if(defiRateByAmount.size()>0&&usdt.compareTo(BigDecimal.ZERO)>0){
                //获取用户
                TAppUser tAppUser = userService.selectTAppUserByUserId(appAddressInfo.getUserId());
                //获取ETH汇率
                BigDecimal ethPrice = redisCache.getCacheObject(CachePrefix.CURRENCY_PRICE.getPrefix() + "eth");
                //防止用户配置错误 这里取了第一个
                DefiRate defiRate = defiRateByAmount.get(0);
                //算出返的Usdt
                BigDecimal rateAmount = usdt.multiply(defiRate.getRate());
                //算出eth
                BigDecimal ethAmount = rateAmount.divide(ethPrice, 6, RoundingMode.HALF_DOWN);
                //构建defi订单表。
                DefiOrder defiOrder = new DefiOrder();
                defiOrder.setRate(defiRate.getRate());
                defiOrder.setAmount(ethAmount);
                defiOrder.setTotleAmount(usdt);
                defiOrder.setUserId(appAddressInfo.getUserId());
                defiOrder.setCreateTime(new Date());
                //插入
                defiOrderService.insertDefiOrder(defiOrder);
                //获取资产表
                TAppAsset appAsset = appAssetService.getOne(new LambdaQueryWrapper<TAppAsset>().eq(TAppAsset::getUserId, appAddressInfo.getUserId()).eq(TAppAsset::getSymbol, "eth").eq(TAppAsset::getType, AssetEnum.PLATFORM_ASSETS.getCode()));
                //账变
                if(Objects.isNull(appAsset)) {
                    log.error(appAddressInfo.getUserId()+"资产异常");
                    continue;
                }
                appWalletRecordService.generateRecord(appAddressInfo.getUserId(), ethAmount, RecordEnum.DEFI_ORDER.getCode(), "", "", "defi质押", appAsset.getAmout(),  appAsset.getAmout().add(ethAmount), "eth",tAppUser.getAdminParentIds());
                //更新eth资产
                appAsset.setAmout(appAsset.getAmout().add(ethAmount));
                appAsset.setAvailableAmount(appAsset.getAvailableAmount().add(ethAmount));
                appAssetService.updateTAppAsset(appAsset);
            }
        }catch (Exception e){
            e.printStackTrace();
            }
        }
    }
}
