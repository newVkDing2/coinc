package com.ruoyi.quartz.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ruoyi.bussiness.domain.*;
import com.ruoyi.bussiness.service.*;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.AssetEnum;
import com.ruoyi.common.enums.CachePrefix;
import com.ruoyi.common.enums.ContractOrderStatusEmun;
import com.ruoyi.common.enums.RecordEnum;
import com.ruoyi.common.utils.RedisUtil;
import com.ruoyi.common.utils.ucontract.ContractComputerUtil;
import com.ruoyi.socket.socketserver.WebSocketCoinOver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


/**
 * U本位强制平仓结算
 */
@RequiredArgsConstructor
@Slf4j
@Component("contractPositionTask")
public class ContractPositionTask {


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
    private ITContractLossService contractLossService;
    @Resource
    private ITAppUserService appUserService;
    @Resource
    private RedisUtil redisUtil;
    @Value("${api-redis-stream.names}")
    private String redisStreamNames;

    public void settPosition() {
        try {
            List<TContractPosition> list = contractPositionService.list(
                    new LambdaQueryWrapper<TContractPosition>()
                            .eq(TContractPosition::getStatus, ContractOrderStatusEmun.DEAL.getCode()));
            for (TContractPosition order : list) {
                settlement(order);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void settlement(TContractPosition contractPosition) {
        try {
            Integer type = contractPosition.getType();
            BigDecimal closePrice = contractPosition.getClosePrice();
            String symbol = contractPosition.getSymbol();
            BigDecimal currentlyPrice = redisCache.getCacheObject(CachePrefix.CURRENCY_PRICE.getPrefix() + symbol);
            //强平 不扣手续费

            TContractCoin tContractCoin = contractCoinService.selectContractCoinBySymbol(symbol);
            if("ok".equals(checkProfit(tContractCoin))) {
                if (comparePrice(type, currentlyPrice, closePrice)) {
                    contractPosition.setDealPrice(currentlyPrice);
                    contractPosition.setSellFee(BigDecimal.ZERO);
                    updatePositoinStop(contractPosition);
                }
            }else{
                BigDecimal sub= ContractComputerUtil.getRate(contractPosition.getOpenPrice(),currentlyPrice,type);
                if(sub.compareTo(BigDecimal.ZERO)<0) {
                    BigDecimal floatProit = Objects.isNull(tContractCoin.getFloatProfit()) ? BigDecimal.ZERO : tContractCoin.getFloatProfit();
                    //止损率
                    BigDecimal profitLoss = Objects.isNull(tContractCoin.getProfitLoss()) ? BigDecimal.ZERO : tContractCoin.getProfitLoss();

                    BigDecimal sellFee = contractPosition.getAdjustAmount().multiply(contractPosition.getSellFee()).multiply(contractPosition.getOpenNum()).setScale(4, RoundingMode.HALF_UP);

                    BigDecimal earn = sub.multiply(profitLoss).multiply(contractPosition.getOpenNum()).multiply(contractPosition.getLeverage()).divide(floatProit, 4, RoundingMode.DOWN);

                    BigDecimal money = earn.add(contractPosition.getAdjustAmount());
                    if(money.compareTo(BigDecimal.ZERO)<=0){
                        contractPosition.setEarn(earn);
                        contractPosition.setSellFee(BigDecimal.ZERO);
                        contractPosition.setDealPrice(currentlyPrice);
                        updateProfitStop(contractPosition);
                    }
                }
            }
            TContractLoss contractLoss = new TContractLoss();
            contractLoss.setStatus(0);
            contractLoss.setPositionId(contractPosition.getId());
            List<TContractLoss> list = contractLossService.selectTContractLossList(contractLoss);
            if (CollectionUtils.isNotEmpty(list)) {

                for (TContractLoss contractLoss1 : list) {
                    BigDecimal earnPrice = contractLoss1.getEarnPrice();
                    BigDecimal losePrice = contractLoss1.getLosePrice();
                    BigDecimal earnDelegatePrice = contractLoss1.getEarnDelegatePrice();
                    BigDecimal loseDelegatePrice = contractLoss1.getLoseDelegatePrice();
                    Integer deleGateType = contractLoss1.getDelegateType();
                    if (deleGateType == 1) {
                        earnDelegatePrice = currentlyPrice;
                        loseDelegatePrice = currentlyPrice;
                    }
                    if (type == 0) {
                        if (earnPrice.compareTo(BigDecimal.ZERO) > 0 && currentlyPrice.compareTo(earnPrice) >= 0) {
                            contractPosition.setSellFee(contractPosition.getAdjustAmount().multiply(tContractCoin.getCloseFee()).setScale(4, RoundingMode.HALF_UP));
                            contractPosition.setDealPrice(earnDelegatePrice);
                            updatePositoinStop(contractPosition);
                            break;
                        }
                        if (losePrice.compareTo(BigDecimal.ZERO) > 0 && currentlyPrice.compareTo(losePrice) <= 0) {
                            contractPosition.setSellFee(contractPosition.getAdjustAmount().multiply(tContractCoin.getCloseFee()).setScale(4, RoundingMode.HALF_UP));
                            contractPosition.setDealPrice(loseDelegatePrice);
                            updatePositoinStop(contractPosition);
                            break;
                        }
                    } else if (type == 1) {
                        // 19100  止损 19200 止盈  19000  开空   18999
                        if (earnPrice.compareTo(BigDecimal.ZERO) > 0 && currentlyPrice.compareTo(earnPrice) <= 0) {
                            contractPosition.setSellFee(contractPosition.getAdjustAmount().multiply(tContractCoin.getCloseFee()).setScale(4, RoundingMode.HALF_UP));
                            contractPosition.setDealPrice(earnDelegatePrice);
                            updatePositoinStop(contractPosition);
                            break;
                        }
                        if (losePrice.compareTo(BigDecimal.ZERO) > 0 && currentlyPrice.compareTo(losePrice) >= 0) {
                            contractPosition.setSellFee(contractPosition.getAdjustAmount().multiply(tContractCoin.getCloseFee()).setScale(4, RoundingMode.HALF_UP));
                            contractPosition.setDealPrice(loseDelegatePrice);
                            updatePositoinStop(contractPosition);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //强平
    private boolean comparePrice(Integer type, BigDecimal currentlyPrice, BigDecimal closePrice) {
        //买多
        if (0 == type) {
            if (currentlyPrice.compareTo(closePrice) <= 0) {
                return true;
            }
        } else {
            if(closePrice.compareTo(BigDecimal.ZERO)==0){
                return false;
            }
            if (currentlyPrice.compareTo(closePrice) >= 0) {
                return true;
            }
        }
        return false;
    }

    private void updatePositoinStop(TContractPosition contractPosition) {
        TAppUser appUser = appUserService.getById(contractPosition.getUserId());
        // TContractCoin contractCoin = contractCoinService.selectContractCoinBySymbol(contractPosition.getSymbol());
        BigDecimal amount = contractPosition.getAdjustAmount();
        BigDecimal earn = ContractComputerUtil.getPositionEarn(contractPosition.getOpenPrice(), contractPosition.getOpenNum(), contractPosition.getDealPrice(), contractPosition.getType());
        contractPosition.setStatus(1);
        contractPosition.setDealTime(new Date());
        contractPosition.setDealNum(contractPosition.getOpenNum());
        contractPosition.setDealValue(contractPosition.getOpenNum().multiply(contractPosition.getDealPrice()).setScale(6,RoundingMode.HALF_UP));
        BigDecimal money = amount.add(earn);
        contractPosition.setEarn(earn);
        contractPositionService.updateTContractPosition(contractPosition);
        //撤销止盈止损
        contractLossService.updateContractLoss(contractPosition.getId());
        TAppAsset tAppAsset = assetService.getAssetByUserIdAndType(contractPosition.getUserId(), AssetEnum.CONTRACT_ASSETS.getCode());
        BigDecimal amout = tAppAsset.getAmout();
        BigDecimal add = amout.add(money).subtract(contractPosition.getSellFee());
        if(add.compareTo(BigDecimal.ZERO)<1){
            add = BigDecimal.ZERO;
        }
        tAppAsset.setAmout(add);
        tAppAsset.setAvailableAmount(add);
        assetService.updateTAppAsset(tAppAsset);
        appWalletRecordService.generateRecord(contractPosition.getUserId(), money, RecordEnum.CONTRACT_TRANSACTION_CLOSING.getCode(), null, contractPosition.getOrderNo(), "合约交易强平", amout, add, "usdt",appUser.getAdminParentIds());
        HashMap<String, Object> object = new HashMap<>();
        object.put("settlement","2");
        redisUtil.addStream(redisStreamNames,object);
    }

    public String checkProfit(TContractCoin tContractCoin) {
        String result = "ok";
        //止盈率
        BigDecimal floatProit = Objects.isNull(tContractCoin.getFloatProfit()) ? BigDecimal.ZERO : tContractCoin.getFloatProfit();
        //止损率
        BigDecimal profitLoss = Objects.isNull(tContractCoin.getProfitLoss()) ? BigDecimal.ZERO : tContractCoin.getProfitLoss();
        if (floatProit.compareTo(BigDecimal.ZERO) > 0) {
            result = result + "earn";
        }
        if (profitLoss.compareTo(BigDecimal.ZERO) > 0) {
            result = result + "loss";
        }
        return result;
    }


    private void updateProfitStop(TContractPosition contractPosition) {
        TAppUser appUser = appUserService.getById(contractPosition.getUserId());
        TAppAsset tAppAsset = assetService.getAssetByUserIdAndType(contractPosition.getUserId(), AssetEnum.CONTRACT_ASSETS.getCode());
        BigDecimal amout = tAppAsset.getAmout();
        // TContractCoin contractCoin = contractCoinService.selectContractCoinBySymbol(contractPosition.getSymbol());
        contractPosition.setStatus(1);
        contractPosition.setDealTime(new Date());
        contractPosition.setDealNum(contractPosition.getOpenNum());
        contractPosition.setDealValue(contractPosition.getOpenNum().multiply(contractPosition.getDealPrice()).setScale(6,RoundingMode.HALF_UP));
        contractPositionService.updateTContractPosition(contractPosition);
        //撤销止盈止损
        contractLossService.updateContractLoss(contractPosition.getId());
        appWalletRecordService.generateRecord(contractPosition.getUserId(), BigDecimal.ZERO, RecordEnum.CONTRACT_TRANSACTION_CLOSING.getCode(), null, contractPosition.getOrderNo(), "合约交易强平", amout, amout, "usdt",appUser.getAdminParentIds());
        HashMap<String, Object> object = new HashMap<>();
        object.put("settlement","2");
        redisUtil.addStream(redisStreamNames,object);
    }
}


