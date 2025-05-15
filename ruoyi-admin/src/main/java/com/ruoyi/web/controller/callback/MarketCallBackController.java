package com.ruoyi.web.controller.callback;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bussiness.domain.KlineSymbol;
import com.ruoyi.bussiness.service.IKlineSymbolService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.trc.TRC;
import com.ruoyi.common.utils.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/marketCallBack")
public class MarketCallBackController {

    @Resource
    private IKlineSymbolService klineSymbolService;

    /**
     * 同步币种信息
     * @param klineSymbol
     * @return
     */
    @PostMapping("/coin")
    public AjaxResult list(@RequestBody KlineSymbol klineSymbol)
    {
        String msg = vaildateParam(klineSymbol);
        if(StringUtils.isNotEmpty(msg)){
            return AjaxResult.error(msg);
        }
        KlineSymbol klineSymbol1 = klineSymbolService.getOne(new LambdaQueryWrapper<KlineSymbol>().eq(KlineSymbol::getSymbol, klineSymbol.getSymbol()));
        if(null != klineSymbol1){
            klineSymbol.setId(klineSymbol1.getId());
        }
        klineSymbolService.saveOrUpdate(klineSymbol);
        return AjaxResult.success();
    }

    private String vaildateParam(KlineSymbol klineSymbol) {
        if(null == klineSymbol){
            return  "参数不能为空";
        }
        if(StringUtils.isBlank(klineSymbol.getSymbol())){
            return  "币种不能为空";
        }
        if(StringUtils.isBlank(klineSymbol.getMarket())){
            return  "交易所不能为空";
        }
        if(StringUtils.isBlank(klineSymbol.getSlug())){
            return  "币种名称不能为空";
        }
        if(null == klineSymbol.getStatus()){
            return  "币种是否启用不能为空";
        }
        return "";
    }

    public static void main(String[] args) {
      HttpResponse execute = HttpUtil.createGet("https://api.dnqwe.pro/v1/common/currencys").execute();
        if (execute.isOk()){
            final String result = execute.body();
            System.out.println(result);
            JSONObject ret = JSONObject.parseObject(result);
            com.alibaba.fastjson.JSONArray a =(com.alibaba.fastjson.JSONArray) ret.get("data");
            System.out.println(result);
        }
    }


}
