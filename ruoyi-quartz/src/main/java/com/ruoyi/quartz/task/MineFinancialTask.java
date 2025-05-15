package com.ruoyi.quartz.task;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bussiness.domain.*;
import com.ruoyi.bussiness.domain.setting.FinancialSettlementSetting;
import com.ruoyi.bussiness.domain.setting.Setting;
import com.ruoyi.bussiness.mapper.TMineFinancialMapper;
import com.ruoyi.bussiness.service.*;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.*;
import com.ruoyi.common.utils.DateUtil;
import com.ruoyi.common.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component("mineFinancialTask")
@Slf4j
public class MineFinancialTask {


    @Resource
    private SettingService settingService;
    @Resource
    private TMineFinancialMapper tMineFinancialMapper;
    @Resource
    private ITAppUserService appUserService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private ITAppAssetService tAppAssetService;
    @Resource
    private ITMineOrderService orderService;
    @Resource
    private ITMineUserService mineUserService;
    @Resource
    private ITAppWalletRecordService walletRecordService;
    @Resource
    private ITMineOrderDayService mineOrderDayService;


    /**
     * 到期结算 每日结算   指定收益入库
     */
    public void mineFinancialTask() {
        try {
            //查看系统配置  获取结算方式
            Setting setting = settingService.get(SettingEnum.FINANCIAL_SETTLEMENT_SETTING.name());
            FinancialSettlementSetting settlementSetting = JSONUtil.toBean(setting.getSettingValue(), FinancialSettlementSetting.class);
            //查看系统今日待下发收益的订单
            List<TMineOrder> list1 = orderService.list(new LambdaQueryWrapper<TMineOrder>().eq(TMineOrder::getStatus, 0L).eq(TMineOrder::getType, 0L));
            if (CollectionUtils.isEmpty(list1)){
                return;
            }
            for (TMineOrder tMineOrder : list1) {
                settlement(tMineOrder,settlementSetting);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void settlement(TMineOrder order, FinancialSettlementSetting setting) {
        try {
            TAppUser appUser = appUserService.getById(order.getUserId());
            BigDecimal amount = order.getAmount();
            //资产
            TAppAsset asset = tAppAssetService.getAssetByUserIdAndType(order.getUserId(), AssetEnum.FINANCIAL_ASSETS.getCode());
            //获取利率  最大 最小 中间 随机
            BigDecimal dayRatio=getRatio(order.getMinOdds(),order.getMaxOdds());
            //获取对应产品的日收益
            BigDecimal earn = amount.multiply(dayRatio).divide(new BigDecimal(100)).setScale(6, RoundingMode.UP);
            //查看是否之前结算过
            TMineOrderDay mineOrderDay = mineOrderDayService.getOne(new LambdaQueryWrapper<TMineOrderDay>().eq(TMineOrderDay::getStatus,1).eq(TMineOrderDay::getOrderNo, order.getOrderNo()));
            if(mineOrderDay !=null ){
                mineOrderDay.setEarn(mineOrderDay.getEarn().add(earn));
            }else {
                mineOrderDay=new TMineOrderDay();
                //组装结算订单
                mineOrderDay.setAddress(order.getAdress());
                mineOrderDay.setOdds(dayRatio);
                mineOrderDay.setOrderNo(order.getOrderNo());
                mineOrderDay.setEarn(earn);
                mineOrderDay.setPlanId(order.getPlanId());
                mineOrderDay.setAmount(amount);
                mineOrderDay.setCreateTime(new Date());
                mineOrderDay.setType(0L);
            }

            //判断 日结 、  指定日结 、订单到期结算
            if(Objects.equals(CommonEnum.ONE.getCode(), setting.getSettlementType())){
                //指定日期结算
                mineOrderDay.setStatus(CommonEnum.ONE.getCode());
                mineOrderDayService.saveOrUpdate(mineOrderDay);
            }
            if(Objects.equals(CommonEnum.TWO.getCode(), setting.getSettlementType())){
                //日结
                //最后一天结算本金+收益  否则只结算收益
                BigDecimal availableAmount = asset.getAvailableAmount();
                asset.setAmout(asset.getAmout().add(earn));
                asset.setAvailableAmount(asset.getAvailableAmount().add(earn));
                mineOrderDay.setStatus(CommonEnum.TWO.getCode());
                tAppAssetService.updateTAppAsset(asset);
                mineOrderDayService.saveOrUpdate(mineOrderDay);
                walletRecordService.generateRecord(order.getUserId(), earn, RecordEnum.FINANCIAL_SETTLEMENT.getCode(),"",order.getOrderNo(),RecordEnum.FINANCIAL_SETTLEMENT.getInfo(),availableAmount,asset.getAvailableAmount(),asset.getSymbol(),appUser.getAdminParentIds());
                //返利
                //itActivityMineService.caseBackToFather(wallet.getUserId(), m.getAccumulaEarn(), wallet.getUserName(), orderNo);
            }
            if(Objects.equals(CommonEnum.THREE.getCode(), setting.getSettlementType())){
                //产品到期结算
                if(DateUtil.daysBetween(order.getEndTime(),new Date())==0){
                    mineOrderDay.setStatus(CommonEnum.TWO.getCode());
                    mineOrderDayService.saveOrUpdate(mineOrderDay);
                    BigDecimal earn1 = mineOrderDay.getEarn();
                    BigDecimal availableAmount = asset.getAvailableAmount();
                    asset.setAmout(asset.getAmout().add(earn1));
                    asset.setAvailableAmount(asset.getAvailableAmount().add(earn1));
                    tAppAssetService.updateTAppAsset(asset);
                    walletRecordService.generateRecord(order.getUserId(), earn1, RecordEnum.FINANCIAL_SETTLEMENT.getCode(),"",order.getOrderNo(),RecordEnum.FINANCIAL_SETTLEMENT.getInfo(),availableAmount,asset.getAvailableAmount(),asset.getSymbol(),appUser.getAdminParentIds());
                    //返利
                    //itActivityMineService.caseBackToFather(wallet.getUserId(), m.getAccumulaEarn(), wallet.getUserName(), orderNo);
                }else {
                    mineOrderDay.setStatus(CommonEnum.ONE.getCode());
                    mineOrderDayService.saveOrUpdate(mineOrderDay);
                }
            }
            order.setAccumulaEarn(mineOrderDay.getEarn());
            //判断 是否产品到期
            if(DateUtil.daysBetween(order.getEndTime(),new Date())==0){
                order.setStatus(1L);
            }
            orderService.updateTMineOrder(order);
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
        return new BigDecimal(result).setScale(4, RoundingMode.UP);
    }

    /**
     * 指定日期结算
     */
    public void  specifiedDateSettlement(){
        //查看系统配置  获取结算方式
        Setting setting = settingService.get(SettingEnum.FINANCIAL_SETTLEMENT_SETTING.name());
        FinancialSettlementSetting settlementSetting = JSONUtil.toBean(setting.getSettingValue(), FinancialSettlementSetting.class);
        if(CommonEnum.ONE.getCode().equals(settlementSetting.getSettlementType())){
            // 查找未结算的收益
            List<TMineOrderDay> list = mineOrderDayService.list(new LambdaQueryWrapper<TMineOrderDay>().eq(TMineOrderDay::getStatus, CommonEnum.ONE.getCode()));
            if(CollectionUtils.isEmpty(list)){
                return;
            }
            for (TMineOrderDay mineOrderDay : list) {
                String orderNo = mineOrderDay.getOrderNo();
                //查找订单
                TMineOrder order = orderService.getOne(new LambdaQueryWrapper<TMineOrder>().eq(TMineOrder::getOrderNo, orderNo));
                TAppUser appUser = appUserService.getById(order.getUserId());
                if(CommonEnum.ONE.getCode().equals(order.getStatus().intValue())){
                    TAppAsset asset = tAppAssetService.getAssetByUserIdAndType(order.getUserId(), AssetEnum.FINANCIAL_ASSETS.getCode());
                    mineOrderDay.setStatus(CommonEnum.TWO.getCode());
                    mineOrderDayService.saveOrUpdate(mineOrderDay);
                    BigDecimal earn = mineOrderDay.getEarn();
                    BigDecimal availableAmount = asset.getAvailableAmount();
                    asset.setAmout(asset.getAmout().add(earn));
                    asset.setAvailableAmount(asset.getAvailableAmount().add(earn));
                    tAppAssetService.updateTAppAsset(asset);
                    walletRecordService.generateRecord(order.getUserId(), earn, RecordEnum.FINANCIAL_SETTLEMENT.getCode(),"",order.getOrderNo(),RecordEnum.FINANCIAL_SETTLEMENT.getInfo(),availableAmount,asset.getAvailableAmount(),asset.getSymbol(),appUser.getAdminParentIds());
                    //返利
                    //itActivityMineService.caseBackToFather(wallet.getUserId(), m.getAccumulaEarn(), wallet.getUserName(), orderNo);
                }
            }
        }

    }
}
