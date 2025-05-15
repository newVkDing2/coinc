package com.ruoyi.quartz.task;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bussiness.domain.TAppAsset;
import com.ruoyi.bussiness.domain.TAppUser;
import com.ruoyi.bussiness.domain.TCurrencyOrder;
import com.ruoyi.bussiness.domain.TCurrencySymbol;
import com.ruoyi.bussiness.domain.setting.AddMosaicSetting;
import com.ruoyi.bussiness.domain.setting.Setting;
import com.ruoyi.bussiness.mapper.TCurrencySymbolMapper;
import com.ruoyi.bussiness.service.*;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.AssetEnum;
import com.ruoyi.common.enums.CachePrefix;
import com.ruoyi.common.enums.RecordEnum;
import com.ruoyi.common.enums.SettingEnum;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.RedisUtil;
import com.ruoyi.socket.socketserver.WebSocketCoinOver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
@Component("currencyOrderTask")
public class CurrencyOrderTask {

    @Resource

    private ITCurrencyOrderService tCurrencyOrderService;
    @Resource
    private ITAppUserService tAppUserService;
    @Resource
    private ITAppAssetService tAppAssetService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private TCurrencySymbolMapper tCurrencySymbolMapper;
    @Resource
    private ITAppWalletRecordService appWalletRecordService;
    @Resource
    private WebSocketCoinOver webSocketCoinOver;
    @Resource
    private SettingService settingService;
    @Resource
    private RedisUtil redisUtil;
    @Value("${api-redis-stream.names}")
    private String redisStreamNames;

    @Async("myTaskAsyncPool")
    public void currencyOrderSettlement(){
        TCurrencyOrder searchCurrencyOrder = new TCurrencyOrder();
        searchCurrencyOrder.setDelegateType(0);
        searchCurrencyOrder.setStatus(0);
        //订单key
        String key = CachePrefix.REDIS_KEY_CURRENCY_ORDER.getPrefix();
        //加锁
        if(redisCache.hasKey(key)){
            return;
        }
            List<TCurrencyOrder> list = tCurrencyOrderService.selectTCurrencyOrderList(searchCurrencyOrder);
            if (!CollectionUtils.isEmpty(list)){
                boolean b1 = redisCache.tryLock(key, "1",50000);
                if(!b1){
                    return;
                }
                try {
                for (TCurrencyOrder tCurrencyOrder:list) {
                    TAppUser user = tAppUserService.getById(tCurrencyOrder.getUserId());
                    String symbol = tCurrencyOrder.getSymbol();
                    String coin = tCurrencyOrder.getCoin();
                    TCurrencySymbol currencySymbol = tCurrencySymbolMapper.selectOne(new LambdaQueryWrapper<TCurrencySymbol>().eq(TCurrencySymbol::getCoin, symbol).eq(TCurrencySymbol::getBaseCoin, coin).eq(TCurrencySymbol::getEnable, "1"));
                    if(Objects.isNull(currencySymbol)){
                        redisCache.deleteObject(key);
                        continue;
                    }

                    BigDecimal symolSettle = tCurrencyOrder.getSymbol().equals("usdt") ? BigDecimal.ONE : redisCache.getCacheObject(CachePrefix.CURRENCY_PRICE.getPrefix() + tCurrencyOrder.getSymbol());
                    BigDecimal coinSettle = tCurrencyOrder.getCoin().equals("usdt") ? BigDecimal.ONE : redisCache.getCacheObject(CachePrefix.CURRENCY_PRICE.getPrefix() + tCurrencyOrder.getCoin());
                    BigDecimal settlePrice = symolSettle.divide(coinSettle,8, RoundingMode.DOWN);

                    //校验
                    String result = tCurrencyOrderService.checkOrder(tCurrencyOrder,currencySymbol,settlePrice);
                    if (!result.equals("success")){
                        redisCache.deleteObject(key);
                        continue;
                    }
//                    Integer symbolPrecision = currencySymbol.getCoinPrecision()==null?0:currencySymbol.getCoinPrecision();
//                    Integer coinPrecision = currencySymbol.getBasePrecision()==null?0:currencySymbol.getBasePrecision();
                    Long userId = user.getUserId();
                    TAppAsset tAppAsset = tAppAssetService.getOne(new LambdaQueryWrapper<TAppAsset>().eq(TAppAsset::getType, AssetEnum.PLATFORM_ASSETS.getCode()).eq(TAppAsset::getUserId, userId).eq(TAppAsset::getSymbol, tCurrencyOrder.getCoin()));
                    //判断是否需要创建资产账户
                    if (Objects.isNull(tAppAsset)){
                        tAppAssetService.createAsset(user,tCurrencyOrder.getCoin(),AssetEnum.PLATFORM_ASSETS.getCode());
                    }
                    //查询交易币种资产
                    TAppAsset addAsset = tAppAssetService.getOne(new LambdaQueryWrapper<TAppAsset>().eq(TAppAsset::getUserId, userId).eq(TAppAsset::getSymbol, tCurrencyOrder.getSymbol()).eq(TAppAsset::getType, AssetEnum.PLATFORM_ASSETS.getCode()));
                    //查询结算币种资产
                    TAppAsset subtractAsset = tAppAssetService.getOne(new LambdaQueryWrapper<TAppAsset>().eq(TAppAsset::getUserId, userId).eq(TAppAsset::getSymbol, tCurrencyOrder.getCoin()).eq(TAppAsset::getType, AssetEnum.PLATFORM_ASSETS.getCode()));
                   //委托价格
                    BigDecimal delegatePrice = tCurrencyOrder.getDelegatePrice();
                    //委托总量
                    BigDecimal delegateTotal = tCurrencyOrder.getDelegateTotal();
                    //手续费率
                    BigDecimal ratio = currencySymbol.getFeeRate().divide(new BigDecimal("100"));
                    //手续费
                    BigDecimal fee=BigDecimal.ZERO;
                    //成交量
                    BigDecimal dealNum =BigDecimal.ZERO;
                    //增加资产
                    BigDecimal addPrice=BigDecimal.ZERO;
                    // 扣减资产
                    BigDecimal subtractPrice=BigDecimal.ZERO ;
                    //成交总量
                    if (tCurrencyOrderService.checkPrice(tCurrencyOrder.getType(),settlePrice,delegatePrice)){
                        if (tCurrencyOrder.getType()==0){
                            dealNum=delegateTotal;
                            subtractPrice = settlePrice.multiply(delegateTotal).setScale(6, RoundingMode.DOWN);
                            tCurrencyOrder.setDealNum(dealNum);
                            tCurrencyOrder.setDealValue(subtractPrice);
                            fee=dealNum.multiply(ratio);
                            addPrice=dealNum.subtract(fee);
                        }else {
                            dealNum=settlePrice.multiply(delegateTotal).setScale(6, RoundingMode.DOWN);
                            subtractPrice = delegateTotal;
                            tCurrencyOrder.setDealNum(subtractPrice);
                            tCurrencyOrder.setDealValue(dealNum);
                            fee=dealNum.multiply(ratio);
                            addPrice=dealNum.subtract(fee);
                        }
                        tCurrencyOrder.setDealPrice(settlePrice);
                        tCurrencyOrder.setFee(fee);
                        settlementOccupiedCurrencyOrder(tCurrencyOrder,symbol,coin,userId,subtractPrice,addPrice,subtractAsset,addAsset,tCurrencyOrder.getType());
                    }
                }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    redisCache.deleteObject(key);
                }
            }
    }

    /**
     * 结算冻结资产 增加对应币种 入账变
     *
     * @param tCurrencyOrder
     * @param symbol
     * @param coin
     * @param userId
     * @param subtractPrice
     * @param addPrice
     * @param subtractAsset
     * @param addAsset
     * @param type
     */
    @Transactional
    public void settlementOccupiedCurrencyOrder(TCurrencyOrder tCurrencyOrder, String symbol, String coin, Long userId, BigDecimal subtractPrice, BigDecimal addPrice, TAppAsset subtractAsset, TAppAsset addAsset, Integer type){
        TAppUser appUser = tAppUserService.getById(userId);
        TAppAsset asset = new TAppAsset();
        if(0!=tCurrencyOrder.getType()){
            String temp=coin;
            coin=symbol;
            symbol=temp;
            asset=addAsset;
            addAsset=subtractAsset;
            subtractAsset=asset;
        }
        tCurrencyOrder.setUserId(userId);
        tCurrencyOrder.setDealTime(DateUtils.getNowDate());
        tCurrencyOrder.setUpdateTime(DateUtils.getNowDate());
        tCurrencyOrder.setStatus(1);

        BigDecimal subtract = subtractAsset.getOccupiedAmount().subtract(subtractPrice);
        tCurrencyOrderService.updateTCurrencyOrder(tCurrencyOrder);
        tAppAssetService.settlementOccupiedCurrencyOrder(userId, coin,subtractPrice,subtract);
        tAppAssetService.addAssetByUserId(userId, symbol, addPrice);
        appWalletRecordService.generateRecord(userId, subtractPrice, RecordEnum.CURRENCY_TRADINGSUB.getCode(), null, tCurrencyOrder.getOrderNo(),  RecordEnum.CURRENCY_TRADINGSUB.getInfo(), subtractAsset.getAmout(), subtractAsset.getAmout().subtract(subtractPrice), coin,appUser.getAdminParentIds());
        appWalletRecordService.generateRecord(userId, addPrice, RecordEnum.CURRENCY_TRADINGADD.getCode(), null, tCurrencyOrder.getOrderNo(), RecordEnum.CURRENCY_TRADINGADD.getInfo(), addAsset.getAmout(), addAsset.getAmout().add(addPrice),symbol,appUser.getAdminParentIds());

        //币币打码
        Setting setting = settingService.get(SettingEnum.ADD_MOSAIC_SETTING.name());
        if (Objects.nonNull(setting)){
            AddMosaicSetting addMosaic = JSONUtil.toBean(setting.getSettingValue(), AddMosaicSetting.class);
            if (Objects.nonNull(addMosaic) && Objects.nonNull(addMosaic.getIsOpen()) && addMosaic.getIsOpen() && Objects.nonNull(addMosaic.getCurrencyIsOpen()) && addMosaic.getCurrencyIsOpen()){
                BigDecimal price = BigDecimal.ONE;
                try {
                    if (!coin.equals("usdt")){
                        price = redisCache.getCacheObject(CachePrefix.CURRENCY_PRICE.getPrefix() + coin.toLowerCase());
                        appUser.setTotleAmont(appUser.getTotleAmont().add(subtractPrice.multiply(price)));
                    }else{
                        appUser.setTotleAmont(appUser.getTotleAmont().add(subtractPrice));
                    }
                    tAppUserService.updateTotleAmont(appUser);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        HashMap<String, Object> object = new HashMap<>();
        object.put("settlement","1");
        redisUtil.addStream(redisStreamNames,object);
    }
}
