package com.ruoyi.quartz.task;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bussiness.domain.*;
import com.ruoyi.bussiness.domain.setting.FinancialSettlementSetting;
import com.ruoyi.bussiness.domain.setting.MingSettlementSetting;
import com.ruoyi.bussiness.domain.setting.Setting;
import com.ruoyi.bussiness.mapper.TMineFinancialMapper;
import com.ruoyi.bussiness.service.*;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.AssetEnum;
import com.ruoyi.common.enums.CommonEnum;
import com.ruoyi.common.enums.RecordEnum;
import com.ruoyi.common.enums.SettingEnum;
import com.ruoyi.common.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Component("mingOrderTask")
@Slf4j
public class MingOrderTask {


    @Resource
    private SettingService settingService;
    @Resource
    private ITMingOrderService mingOrderService;
    @Resource
    private ITAppUserService appUserService;

    @Resource
    private ITAppAssetService tAppAssetService;
    @Resource
    private ITAppWalletRecordService walletRecordService;


    /**
     * 到期结算 每日结算   指定收益入库
     */
    public void mingOrderSett() {
        try {
            //查看系统配置  获取结算方式
            Setting setting = settingService.get(SettingEnum.MING_SETTLEMENT_SETTING.name());
            MingSettlementSetting settlementSetting = JSONUtil.toBean(setting.getSettingValue(), MingSettlementSetting.class);
            //查看系统今日待下发收益的订单
            List<TMingOrder> list1 = mingOrderService.list(new LambdaQueryWrapper<TMingOrder>().eq(TMingOrder::getStatus, 0L));
            if (CollectionUtils.isEmpty(list1)) {
                return;
            }
            for (TMingOrder tMineOrder : list1) {
                settlement(tMineOrder, settlementSetting);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void settlement(TMingOrder order, MingSettlementSetting setting) {
        try {
            TAppUser appUser = appUserService.getById(order.getUserId());
            BigDecimal amount = order.getAmount();
            //资产
            TAppAsset tAppAsset = tAppAssetService.getOne(new LambdaQueryWrapper<TAppAsset>().eq(TAppAsset::getType, AssetEnum.PLATFORM_ASSETS.getCode()).eq(TAppAsset::getUserId, order.getUserId()).eq(TAppAsset::getSymbol, "usdt"));
            //  TMingProduct tMingProduct = mingProductService.selectTMingProductById(planId);            //获取利率  最大 最小 中间 随机
            BigDecimal dayRatio = getRatio(order.getMinOdds(), order.getMaxOdds());
            //获取对应产品的日收益
            BigDecimal earn = amount.multiply(dayRatio).divide(new BigDecimal(100)).setScale(6, RoundingMode.UP);
            BigDecimal availableAmount = tAppAsset.getAvailableAmount();
            BigDecimal  allEarn= order.getAmount();;
            //判断 日结 、  指定日结 、订单到期结算
            if (Objects.equals(CommonEnum.ONE.getCode(), setting.getSettlementType())) {
                //每日返息
                tAppAsset.setAmout(tAppAsset.getAmout().add(earn));
                tAppAsset.setAvailableAmount(tAppAsset.getAvailableAmount().add(earn));
                tAppAssetService.updateTAppAsset(tAppAsset);
                walletRecordService.generateRecord(order.getUserId(), earn, RecordEnum.MINING_REBATE.getCode(), "", order.getOrderNo(), RecordEnum.FINANCIAL_SETTLEMENT.getInfo(), availableAmount, availableAmount.add(earn), tAppAsset.getSymbol(), appUser.getAdminParentIds());
            }
            if (Objects.equals(CommonEnum.TWO.getCode(), setting.getSettlementType())) {
                //日结
                //最后一天结算本金+收益  否则只结算收益
                walletRecordService.generateRecord(order.getUserId(), earn, RecordEnum.MINING_REBATE.getCode(), "", order.getOrderNo(), "每日收益累计，利息不返回", earn, order.getAccumulaEarn().add(earn), tAppAsset.getSymbol(), appUser.getAdminParentIds());
            }
            //判断 是否产品到期
            if (DateUtil.daysBetween(order.getEndTime(), new Date()) == 0) {
                order.setStatus(1);
                //到期返本金
                if (Objects.equals(CommonEnum.ONE.getCode(), setting.getSettlementType())) {
                    tAppAsset.setAmout(tAppAsset.getAmout().add(allEarn));
                    tAppAsset.setAvailableAmount(tAppAsset.getAvailableAmount().add(allEarn));
                    tAppAssetService.updateTAppAsset(tAppAsset);
                }
                //到期结算本金和利息
                if (Objects.equals(CommonEnum.TWO.getCode(), setting.getSettlementType())) {
                    allEarn = order.getAmount().add(order.getAccumulaEarn());
                    tAppAsset.setAmout(tAppAsset.getAmout().add(allEarn));
                    tAppAsset.setAvailableAmount(tAppAsset.getAvailableAmount().add(allEarn));
                    tAppAssetService.updateTAppAsset(tAppAsset);
                }
                walletRecordService.generateRecord(order.getUserId(), allEarn, RecordEnum.MINING_SETTLEMENT.getCode(), "", order.getOrderNo(), RecordEnum.MINING_SETTLEMENT.getInfo(), availableAmount, availableAmount.add(allEarn), tAppAsset.getSymbol(), appUser.getAdminParentIds());
            }
            order.setAccumulaEarn(order.getAccumulaEarn().add(earn));
            mingOrderService.updateTMingOrder(order);
        } catch (Exception e) {
            log.error("理财订单异常结算, MinderOrder:{}", order);
        }
    }


    //获取订单的利率
    private BigDecimal getRatio(BigDecimal minOdds, BigDecimal maxOdds) {
        return queryHongBao(minOdds.doubleValue(), maxOdds.doubleValue());
    }

    private static BigDecimal queryHongBao(double min, double max) {
        Random rand = new Random();
        double result;
        result = min + (rand.nextDouble() * (max - min));
        return new BigDecimal(result).setScale(4, RoundingMode.DOWN);
    }


}
