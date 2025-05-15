package com.ruoyi.quartz.task;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bussiness.domain.*;
import com.ruoyi.bussiness.domain.setting.AddMosaicSetting;
import com.ruoyi.bussiness.domain.setting.Setting;
import com.ruoyi.bussiness.mapper.TAppUserMapper;
import com.ruoyi.bussiness.service.*;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.*;
import com.ruoyi.common.utils.BigDecimalUtil;
import com.ruoyi.common.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


/**
 * 秒合约结算
 */
@RequiredArgsConstructor
@Slf4j
@Component("secondContractTask")
public class SecondContractTask {

    @Resource
    private ITSecondContractOrderService secondContractOrderService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private ITAppAssetService assetService ;
    @Resource
    private ITAppWalletRecordService appWalletRecordService;
    @Resource
    private ITAppUserService appUserService;
    @Resource
    private ITAppUserDetailService appUserDetailService;
    @Resource
    private ITSecondCoinConfigService secondCoinConfigService;
    @Resource
    private SettingService settingService;
    @Resource
    private RedisUtil redisUtil;
    @Value("${api-redis-stream.names}")
    private String redisStreamNames;

    public void secondContract() {
        try {
            //查找所有正在参与的秒合约订单  状态为参与中  结束时间小于当前时间
            List<TSecondContractOrder> list = secondContractOrderService.list(
                    new LambdaQueryWrapper<TSecondContractOrder>()
                            .eq(TSecondContractOrder::getStatus, "0")
                            .lt(TSecondContractOrder::getCloseTime,new Date().getTime())
            );
            for (TSecondContractOrder order : list) {
                TAppUser tAppUser = appUserService.selectTAppUserByUserId(order.getUserId());
                settlement(order, tAppUser);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void settlement(TSecondContractOrder order, TAppUser tAppUser) {


        try {
            //设置开奖结果  赢 1  输 2  平 3
            String openResult = "1";
            //返还金额
            BigDecimal returnAmount = BigDecimal.ZERO;
            //投注金额
            BigDecimal betAmount = order.getBetAmount();
            //类型  涨跌  1 涨   0 跌
            Integer type = Integer.parseInt(order.getBetContent());
            //赔率
            BigDecimal rate = order.getRate();
            //买入价格
            BigDecimal openPrice = order.getOpenPrice();
            //订单标识   0正常  1包赢  2包输
            Integer sign = order.getSign();
            //判断币种类型
            String coinSymbol = order.getCoinSymbol();
            //当前最新价格
            BigDecimal newPrice = redisCache.getCacheObject(CachePrefix.CURRENCY_PRICE.getPrefix() + order.getCoinSymbol());
            TSecondCoinConfig one = secondCoinConfigService.getOne(new LambdaQueryWrapper<TSecondCoinConfig>().eq(TSecondCoinConfig::getCoin, coinSymbol));
            if(one != null && 2!=one.getType()){
                newPrice=redisCache.getCacheObject(CachePrefix.CURRENCY_PRICE.getPrefix() + order.getCoinSymbol().toUpperCase());
            }
            if(0 != order.getSign()){
                newPrice=getClosePrice(openPrice,newPrice, sign,type);
            }
            TAppUserDetail tAppUserDetail = appUserDetailService.getOne(new LambdaQueryWrapper<TAppUserDetail>().eq(TAppUserDetail::getUserId, tAppUser.getUserId()));
            //连赢场次
            int winNum=tAppUserDetail.getWinNum()==null?0:tAppUserDetail.getWinNum();
            //连输场次
            int loseNum=tAppUserDetail.getLoseNum()==null?0:tAppUserDetail.getLoseNum();

            //用户输赢表示  0正常  1包赢  2包输
            Integer buff = tAppUser.getBuff();
            if(buff==0){
                //连赢操作
                if(winNum>0  && sign==0 ){
                    tAppUserDetail.setWinNum(winNum-1);
                    appUserDetailService.updateById(tAppUserDetail);
                    newPrice=getClosePrice(openPrice,newPrice, 1,type);
                    sign=1;
                }
                //连输操作
                if(loseNum>0  && sign==0 ){
                    tAppUserDetail.setLoseNum(loseNum-1);
                    appUserDetailService.updateById(tAppUserDetail);
                    newPrice=getClosePrice(openPrice,newPrice, 2,type);
                    sign=2;
                }
            }
            if(buff==1){
                sign=1;
                newPrice=getClosePrice(openPrice,newPrice, 1,type);
            }
            if(buff==2){
                newPrice=getClosePrice(openPrice,newPrice, 2,type);
                sign=2;
            }

            //   1 涨   0 跌
            if(CommonEnum.TRUE.getCode().equals(type)){
                //类型为买涨
                //判断输赢
                if(openPrice.compareTo(newPrice)>0){
                    //输  判断 全赔  半赔 全输
                    openResult="2";
                    if(rate.compareTo(BigDecimal.ONE)<0){
                        //半赔
                        //计算待返金额
                        returnAmount= betAmount.multiply(BigDecimal.ONE.subtract(rate));
                    }
                    if(rate.compareTo(BigDecimal.ONE)>=0){
                        //全赔
                        //计算待返金额
                        returnAmount= BigDecimal.ZERO;
                    }
                    if(null != order.getRateFlag() && order.getRateFlag()){
                        returnAmount= BigDecimal.ZERO;
                    }
                }
                if(openPrice.compareTo(newPrice)<0){
                    //赢  判断 全赔  半赔
                    if(rate.compareTo(BigDecimal.ONE)<0){
                        //半赔
                        //计算待返金额
                        returnAmount= betAmount.multiply(BigDecimal.ONE.add(rate));
                    }
                    if(rate.compareTo(BigDecimal.ONE)>=0){
                        //全赔
                        //订单结算  修改订单状态
                        returnAmount= betAmount.add(betAmount.multiply(rate));
                    }
                }
                if(openPrice.compareTo(newPrice)==0){
                    //平  //返还订单金额
                    //订单结算  修改订单状态
                    openResult="3";
                    returnAmount= betAmount;
                }
            }else {
                //跌
                if(openPrice.compareTo(newPrice)>0){
                    //赢  判断 全赔  半赔
                    if(rate.compareTo(BigDecimal.ONE)<0){
                        //半赔
                        //计算待返金额
                        returnAmount= betAmount.multiply(BigDecimal.ONE.add(rate));
                    }
                    if(rate.compareTo(BigDecimal.ONE)>=0){
                        //全赔
                        //计算待返金额
                        returnAmount= betAmount.add(betAmount.multiply(rate));
                    }

                }
                if(openPrice.compareTo(newPrice)<0){
                    //输  判断 全赔  半赔
                    openResult="2";
                    if(rate.compareTo(BigDecimal.ONE)<0){
                        //半赔
                        //计算待返金额
                        returnAmount= betAmount.multiply(BigDecimal.ONE.subtract(rate));
                    }
                    if(rate.compareTo(BigDecimal.ZERO)==0){
                        //全赔
                        returnAmount= BigDecimal.ZERO;
                    }
                    if(null != order.getRateFlag() && order.getRateFlag()){
                        returnAmount= BigDecimal.ZERO;
                    }

                }
                if(openPrice.compareTo(newPrice)==0){
                    //平  //返还订单金额
                    openResult="3";
                    returnAmount= betAmount;
                }
            }
            if(returnAmount.compareTo(BigDecimal.ZERO)>0){
                //返回至 用户资产
                Map<String, TAppAsset> assetByUserIdList = assetService.getAssetByUserIdList(order.getUserId());
                TAppAsset appAsset = assetByUserIdList.get(order.getBaseSymbol()+ order.getUserId());
                BigDecimal availableAmount = appAsset.getAvailableAmount();
                appAsset.setAmout(appAsset.getAmout().add(returnAmount));
                appAsset.setAvailableAmount(availableAmount.add(returnAmount));
                assetService.updateTAppAsset(appAsset);
                //添加账变
                appWalletRecordService.generateRecord(order.getUserId(),returnAmount, RecordEnum.OPTION_SETTLEMENT.getCode(),"", order.getOrderNo(),RecordEnum.OPTION_SETTLEMENT.getInfo(),availableAmount,availableAmount.add(returnAmount), order.getBaseSymbol(),tAppUser.getAdminParentIds());

            }
            //订单结算  修改订单状态
            order.setOpenResult(openResult);
            order.setClosePrice(newPrice);
            order.setStatus(1);
            order.setRewardAmount(returnAmount);
            order.setSign(sign);
            secondContractOrderService.updateById(order);

//            //秒合约打码
//            Setting setting = settingService.get(SettingEnum.ADD_MOSAIC_SETTING.name());
//            if (Objects.nonNull(setting)){
//                AddMosaicSetting addMosaic = JSONUtil.toBean(setting.getSettingValue(), AddMosaicSetting.class);
//                if (Objects.nonNull(addMosaic) && Objects.nonNull(addMosaic.getIsOpen()) && addMosaic.getIsOpen() && Objects.nonNull(addMosaic.getSencordIsOpen()) && addMosaic.getSencordIsOpen()){
//                    tAppUser.setTotleAmont(tAppUser.getTotleAmont().add(order.getBetAmount()));
//                    appUserService.updateTotleAmont(tAppUser);
//                }
//            }

            HashMap<String, Object> object = new HashMap<>();
            object.put("settlement","3");
            redisUtil.addStream(redisStreamNames,object);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param openPrice 开盘价
     * @param closePrice 收盘价
     * @param sign 标识 订单标记 0正常  1包赢  2包输
     * @param type 跌涨  1 涨   0 跌
     * @return
     */
    public static BigDecimal getClosePrice(BigDecimal openPrice, BigDecimal closePrice, Integer sign, Integer type) {
        // 包赢,买涨,实际涨  买跌,实际跌    则返回原关盘价
        if (sign == 1) {
            // 包赢
            if ((1==type && closePrice.compareTo(openPrice) > 0) ||  (0==type && closePrice.compareTo(openPrice) < 0))
            {
                return closePrice;
            }
        }
        if (sign == 2){
            if ((1==type && closePrice.compareTo(openPrice) < 0) ||(0==type && closePrice.compareTo(openPrice) > 0))
            {
                // 包输     买涨,实际跌    买跌,实际涨   则返回原关盘价
                return closePrice;
            }
        }

        BigDecimal diff;
        // 防止24421.540000返回6位小数,stripTrailingZeros()去掉后面的0
        int digits = getNumberDecimalDigits(openPrice.stripTrailingZeros().toPlainString());
        if (digits == 0) {
            // 没有小数,就生成0-1之间的小数
            diff = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble());
        } else {
            // 有小数,就生成最后一位小数*1-10之间的数额,比如2位小数,就是0.01*(1-10)之间的随机数
            diff = BigDecimal.valueOf((double) 1 / Math.pow(10, digits) * (ThreadLocalRandom.current().nextInt(10) + 1));
        }

        if (sign == 1) {
            // 包赢,买涨,要涨/买跌,要跌
            if (1==type) { // 涨
                closePrice = openPrice.add(diff);
            } else if (0==type) { // 跌
                closePrice = openPrice.subtract(diff);
            }
        } else {
            // 包输,买涨,要跌/买跌,要涨
            if (1==type) { // 涨
                closePrice = openPrice.subtract(diff);
            } else if (0==type) { // 跌
                closePrice = openPrice.add(diff);
            }
        }

        return closePrice;
    }

    // 获取小数位个数
    public static int getNumberDecimalDigits(String number) {
        if (!number.contains(".")) {
            return 0;
        }
        return number.length() - (number.indexOf(".") + 1);
    }



}
