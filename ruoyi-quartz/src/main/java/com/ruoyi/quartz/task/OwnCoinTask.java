package com.ruoyi.quartz.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bussiness.domain.*;
import com.ruoyi.bussiness.service.*;
import com.ruoyi.common.enums.AssetEnum;
import com.ruoyi.common.enums.RecordEnum;
import com.ruoyi.common.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component("OwnCoinTask")
@Slf4j
public class OwnCoinTask {

    @Resource
    private ITOwnCoinOrderService itOwnCoinOrderService;
    @Resource
    private ITOwnCoinService ownCoinService;
    @Resource
    private ITAppAssetService tAppAssetService;
    @Resource
    private ITAppWalletRecordService appWalletRecordService;
    @Resource
    private ITAppUserService tAppUserService;
    @Resource
    private IKlineSymbolService klineSymbolService;
    @Resource
    private ITSecondCoinConfigService tSecondCoinConfigService;
    @Resource
    private ITSecondPeriodConfigService tSecondPeriodConfigService;
    @Resource
    private RedisUtil redisUtil;
    @Value("${api-redis-stream.names}")
    private String redisStreamNames;

    /**
     * 更改发币状态
     * 每天00:01执行一次
     */
    //@Scheduled(cron = "0 1 0 * * ?")
    public void ownCoinStartTask() {
        LambdaQueryWrapper<TOwnCoin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(TOwnCoin::getBeginTime, new Date());
        queryWrapper.eq(TOwnCoin::getStatus, 1);
        List<TOwnCoin> list = ownCoinService.list(queryWrapper);
        for (TOwnCoin tOwnCoin : list) {
            tOwnCoin.setStatus(2);
        }
        ownCoinService.saveOrUpdateBatch(list);
    }

    /**
     * 发币结束 申购资产发送
     */
    //@Scheduled(cron = "0 1 0 * * ?")
    @Transactional
    public void ownCoinEndTask() {
        //更改发行状态
        LambdaQueryWrapper<TOwnCoin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(TOwnCoin::getEndTime, new Date());
        queryWrapper.eq(TOwnCoin::getStatus, 2);
        List<TOwnCoin> list = ownCoinService.list(queryWrapper);
        for (TOwnCoin tOwnCoin : list) {
            tOwnCoin.setStatus(3);
            //查询订单
            List<TOwnCoinOrder> list1 = itOwnCoinOrderService.list(new LambdaQueryWrapper<TOwnCoinOrder>().eq(TOwnCoinOrder::getStatus, "1").eq(TOwnCoinOrder::getOwnId, tOwnCoin.getId()));
            for (TOwnCoinOrder tOwnCoinOrder : list1) {
                tOwnCoinOrder.setStatus("2");
                //订单结算
                BigDecimal amount = new BigDecimal(tOwnCoinOrder.getNumber().toString());
                String ownCoin = tOwnCoinOrder.getOwnCoin();
                //创建资产
                TAppUser user = tAppUserService.getById(tOwnCoinOrder.getUserId());
                Map<String, TAppAsset> assetMap1 = tAppAssetService.getAssetByUserIdList(tOwnCoinOrder.getUserId());
                if (!assetMap1.containsKey(ownCoin.toLowerCase() + tOwnCoinOrder.getUserId())) {
                    tAppAssetService.createAsset(user, ownCoin.toLowerCase(), AssetEnum.PLATFORM_ASSETS.getCode());

                }
                Map<String, TAppAsset> assetMap = tAppAssetService.getAssetByUserIdList(tOwnCoinOrder.getUserId());
                TAppAsset asset = assetMap.get(ownCoin.toLowerCase() + tOwnCoinOrder.getUserId());
                BigDecimal availableAmount = asset.getAvailableAmount();
                tAppAssetService.updateTAppAsset(
                        TAppAsset.builder()
                                .symbol(ownCoin.toLowerCase())
                                .userId(tOwnCoinOrder.getUserId())
                                .amout(asset.getAmout().add(amount))
                                .availableAmount(asset.getAvailableAmount().add(amount))
                                .type(AssetEnum.PLATFORM_ASSETS.getCode())
                                .build());
                //帐变
                appWalletRecordService.generateRecord(user.getUserId(), amount, RecordEnum.OWN_COIN_BUY.getCode(), user.getLoginName(), tOwnCoinOrder.getOrderId(), RecordEnum.OWN_COIN_BUY.getInfo(), availableAmount, availableAmount.add(amount), ownCoin.toLowerCase(), user.getAdminParentIds());
            }
            itOwnCoinOrderService.saveOrUpdateBatch(list1);
            //订单结算完成  将币种添加至币种列表
            KlineSymbol klineSymbol = new KlineSymbol()
                    .setSymbol(tOwnCoin.getCoin().toLowerCase())
                    .setMarket("echo")
                    .setSlug(tOwnCoin.getCoin().toUpperCase())
                    .setLogo(tOwnCoin.getLogo())
                    .setReferCoin(tOwnCoin.getReferCoin())
                    .setReferMarket(tOwnCoin.getReferMarket())
                    .setProportion(tOwnCoin.getProportion());
            klineSymbolService.save(klineSymbol);

//            //监听获取新的kline
//            HashMap<String, Object> object = new HashMap<>();
//            object.put(tOwnCoin.getCoin(),tOwnCoin.getReferCoin()+"usdt");
//            redisUtil.addStream(redisStreamNames,object);

            //自动添加到秒合约
            TSecondCoinConfig tSecondCoinConfig = new TSecondCoinConfig();
            tSecondCoinConfig.setCoin(tOwnCoin.getCoin().toLowerCase());
            tSecondCoinConfig.setBaseCoin("usdt");
            tSecondCoinConfig.setShowSymbol(tOwnCoin.getShowSymbol());
            tSecondCoinConfig.setSort(999L);
            tSecondCoinConfig.setStatus(1L);
            tSecondCoinConfig.setMarket("echo");
            tSecondCoinConfig.setSymbol(tOwnCoin.getCoin().toLowerCase() + "usdt");
            tSecondCoinConfig.setShowFlag(1L);
            tSecondCoinConfig.setCreateTime(new Date());
            tSecondCoinConfig.setLogo(tOwnCoin.getLogo());
            tSecondCoinConfig.setType(2);
            TSecondCoinConfig btc = tSecondCoinConfigService.getOne(new LambdaQueryWrapper<TSecondCoinConfig>().eq(TSecondCoinConfig::getCoin, "btc"));
            if (null != btc) {
                tSecondCoinConfig.setPeriodId(btc.getId());
            }
            tSecondCoinConfigService.insertSecondCoin(tSecondCoinConfig);
        }
        ownCoinService.saveOrUpdateBatch(list);

    }

}
