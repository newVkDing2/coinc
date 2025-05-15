package com.ruoyi.quartz.task;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bussiness.domain.*;
import com.ruoyi.bussiness.domain.setting.AddMosaicSetting;
import com.ruoyi.bussiness.domain.setting.Setting;
import com.ruoyi.bussiness.service.*;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.*;
import com.ruoyi.common.utils.RedisUtil;
import com.ruoyi.common.utils.ucontract.ContractComputerUtil;
import com.ruoyi.socket.socketserver.WebSocketCoinOver;
import jnr.ffi.annotations.In;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


/**
 * U本位委托订单结算
 */
@RequiredArgsConstructor
@Slf4j
@Component("contractOrderTask")
public class ContractOrderTask {

    @Resource
    private ITContractOrderService contractOrderService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private ITAppAssetService assetService;
    @Resource
    private ITAppWalletRecordService appWalletRecordService;
    @Resource
    private ITContractCoinService contractCoinService;
    @Resource
    private ITContractPositionService contractPositionService;
    @Resource
    private ITAppUserService appUserService;
    @Resource
    private SettingService settingService;
    @Resource
    private RedisUtil redisUtil;
    @Value("${api-redis-stream.names}")
    private String redisStreamNames;
    public void settMent() {
        try {
            List<TContractOrder> list = contractOrderService.list(
                    new LambdaQueryWrapper<TContractOrder>()
                            .eq(TContractOrder::getStatus, ContractOrderStatusEmun.DEAL.getCode()));
            for (TContractOrder order : list) {
                settlement(order);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void settlement(TContractOrder order) {
        try {

            TAppUser appUser = appUserService.getById(order.getUserId());
            String symbol = order.getSymbol();
            //委托量
            BigDecimal delegateTotal = order.getDelegateTotal();

            BigDecimal delegatePrice = order.getDelegatePrice();
            //杠杆
            BigDecimal level = order.getLeverage();

            Integer type = order.getType();

            BigDecimal currentlyPrice = redisCache.getCacheObject(CachePrefix.CURRENCY_PRICE.getPrefix() + symbol);

            TContractCoin contractCoin = contractCoinService.selectContractCoinBySymbol(symbol);

            if (comparePrice(type, currentlyPrice, delegatePrice)) {
                //  usdt   成交  总资产-成交额  占用-成交额    被买入的币   总资产  +成交量-（成交量*手续费率）   可用资产+成交量-（成交量*手续费率）
                //实际成交额
                BigDecimal dealValue = ContractComputerUtil.getAmount(currentlyPrice, delegateTotal, level);
                order.setStatus(1);
                order.setDealNum(delegateTotal);
                order.setDealPrice(currentlyPrice);
                order.setDealValue(dealValue);
                order.setDealTime(new Date());

                BigDecimal fee = dealValue.multiply(contractCoin.getOpenFee()).setScale(6, RoundingMode.HALF_UP);
                order.setFee(fee);
                contractOrderService.updateTContractOrder(order);
                BigDecimal closePrice = ContractComputerUtil.getStrongPrice(level, type, currentlyPrice, delegateTotal, dealValue, fee);
                createPosition(symbol, currentlyPrice, closePrice, dealValue, delegateTotal, level, type, order.getDelegateType(), fee, order.getUserId(), order.getOrderNo(),0);
                // 1.1 查看用户usdt资产
                TAppAsset tAppAsset = assetService.getAssetByUserIdAndType(order.getUserId(), AssetEnum.CONTRACT_ASSETS.getCode());
                //usdt  变动
                BigDecimal amout = tAppAsset.getAmout();
                tAppAsset.setAmout(amout.subtract(dealValue));
                tAppAsset.setOccupiedAmount(tAppAsset.getOccupiedAmount().subtract(order.getDelegateValue()));
                tAppAsset.setAvailableAmount(tAppAsset.getAvailableAmount().add(order.getDelegateValue().subtract(dealValue)));
                assetService.updateTAppAsset(tAppAsset);
                appWalletRecordService.generateRecord(order.getUserId(), dealValue, RecordEnum.CONTRACT_TRANSACTIONSUB.getCode(), null, order.getOrderNo(), "合约交易-", amout, amout.subtract(dealValue), contractCoin.getBaseCoin().toLowerCase(),appUser.getAdminParentIds());

                //u本位打码
                Setting setting = settingService.get(SettingEnum.ADD_MOSAIC_SETTING.name());
                if (Objects.nonNull(setting)){
                    AddMosaicSetting addMosaic = JSONUtil.toBean(setting.getSettingValue(), AddMosaicSetting.class);
                    if (Objects.nonNull(addMosaic) && Objects.nonNull(addMosaic.getIsOpen()) && addMosaic.getIsOpen() && Objects.nonNull(addMosaic.getContractIsOpen()) && addMosaic.getContractIsOpen()){
                        appUser.setTotleAmont(appUser.getTotleAmont().add(order.getDelegateValue()));
                        appUserService.updateTotleAmont(appUser);
                    }
                }

                HashMap<String, Object> object = new HashMap<>();
                object.put("settlement","2");
                redisUtil.addStream(redisStreamNames,object);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean comparePrice(Integer type, BigDecimal currentlyPrice, BigDecimal delegatePrice) {
        if (0 == type) {
            //限价于大于当前价
            if (delegatePrice.compareTo(currentlyPrice) >= 0) {
                return true;
            }
        } else {
            //限价小于等于当前价
            if (delegatePrice.compareTo(currentlyPrice) <= 0) {
                return true;
            }
        }
        return false;
    }

    //创建仓位对象
    private TContractPosition createPosition(String symbol, BigDecimal openPrice, BigDecimal closePrice, BigDecimal amount, BigDecimal num, BigDecimal level, Integer type, Integer delegateType, BigDecimal openFee, Long uerId, String serialId, Integer deliveryDays) {
        TContractPosition tContractPosition = new TContractPosition();
        BigDecimal bigDecimal = amount.subtract(openFee);
        tContractPosition.setSymbol(symbol);
        tContractPosition.setAmount(bigDecimal);
        tContractPosition.setAdjustAmount(bigDecimal);
        tContractPosition.setLeverage(level);
        tContractPosition.setClosePrice(closePrice);
        tContractPosition.setOpenNum(num);
        tContractPosition.setOpenPrice(openPrice);
        tContractPosition.setOpenFee(openFee);
        tContractPosition.setStatus(0);
        tContractPosition.setType(type);
        tContractPosition.setDelegateType(delegateType);
        tContractPosition.setCreateTime(new Date());
        tContractPosition.setRemainMargin(bigDecimal);
        tContractPosition.setOrderNo(serialId);
        tContractPosition.setUserId(uerId);
        tContractPosition.setEntrustmentValue(num.multiply(openPrice).setScale(6,RoundingMode.HALF_UP));
        tContractPosition.setDeliveryDays(0);
        tContractPosition.setSubTime(new Date());
        contractPositionService.save(tContractPosition);
        return tContractPosition;
    }

}
