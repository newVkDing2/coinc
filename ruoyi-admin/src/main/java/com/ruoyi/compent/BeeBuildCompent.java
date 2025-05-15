package com.ruoyi.compent;

import cc.block.data.api.BlockccApiClientFactory;
import cc.block.data.api.BlockccApiRestClient;
import cc.block.data.api.domain.BlockccResponse;
import cc.block.data.api.domain.market.Market;
import cc.block.data.api.domain.market.Symbol;
import cc.block.data.api.domain.market.request.MarketParam;
import cc.block.data.api.domain.market.request.SymbolParam;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.bussiness.domain.KlineSymbol;
import com.ruoyi.bussiness.domain.TMarkets;
import com.ruoyi.bussiness.domain.TSymbols;
import com.ruoyi.bussiness.mapper.KlineSymbolMapper;
import com.ruoyi.bussiness.mapper.TSymbolsMapper;
import com.ruoyi.bussiness.service.ITMarketsService;
import com.ruoyi.bussiness.service.ITSymbolsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class BeeBuildCompent {
    public static final Logger log = LoggerFactory.getLogger(BeeBuildCompent.class);
    @Value("${mifeng.api.eth}")
    private String apiKey;
    @Value("${mifeng.api.host}")
    private String host;
    @Autowired
    ITMarketsService marketsService;
    @Autowired
    ITSymbolsService symbolsService;
    @Autowired
    KlineSymbolMapper klineSymbolMapper;

 //  @PostConstruct
    private void buildHuobiCoin(){
        HttpResponse execute = HttpUtil.createGet("https://api.djqwe.pro/v1/common/currencys").execute();
        if (execute.isOk()){
             String result = execute.body();
            JSONObject ret = JSONObject.parseObject(result);
            JSONArray data =(JSONArray) ret.get("data");
            for (Object coin: data) {
                KlineSymbol query = new KlineSymbol();
                String coin1 = (String) coin;
                query.setSymbol(coin1);
                query.setMarket("huobi");
                List<KlineSymbol> klineSymbols = klineSymbolMapper.selectKlineSymbolList(query);
                if(null==klineSymbols||klineSymbols.size()<1){
                    KlineSymbol klineSymbol = new KlineSymbol();
                    klineSymbol.setMarket("huobi");
                    klineSymbol.setSlug(coin1.toUpperCase());
                    klineSymbol.setSymbol(coin1);
                    TSymbols tSymbols = new TSymbols();
                    tSymbols.setSymbol(coin1.toUpperCase());
                    List<TSymbols> tSymbols1 = symbolsService.selectTSymbolsList(tSymbols);
                    if(null!=tSymbols1&&tSymbols1.size()>0){
                        TSymbols tSymbols2 = tSymbols1.get(0);
                        klineSymbol.setLogo(tSymbols2.getLogoUrl());
                    }
                    klineSymbolMapper.insertKlineSymbol(klineSymbol);
                }
            }
        }
    }
///sapi/v1/convert/exchangeInfo
  //  @PostConstruct
    public void buildBinanceCoin(){
        HttpResponse execute = HttpUtil.createGet("https://dqwe.asdqe.com/fapi/v1/exchangeInfo").execute();
        if (execute.isOk()){
            String result = execute.body();
            JSONObject ret = JSONObject.parseObject(result);
            JSONArray data =(JSONArray) ret.get("symbols");
            for (Object coin: data) {
                KlineSymbol query = new KlineSymbol();
                JSONObject coin1 = (JSONObject) coin;
                String symbol = (String) coin1.get("baseAsset");
                query.setSymbol(symbol);
                query.setMarket("binance");
                List<KlineSymbol> klineSymbols = klineSymbolMapper.selectKlineSymbolList(query);
                if(null==klineSymbols||klineSymbols.size()<1){
                    KlineSymbol klineSymbol = new KlineSymbol();
                    klineSymbol.setMarket("binance");
                    klineSymbol.setSlug(symbol.toUpperCase());
                    klineSymbol.setSymbol(symbol);
                    TSymbols tSymbols = new TSymbols();
                    tSymbols.setSymbol(symbol.toUpperCase());
                    List<TSymbols> tSymbols1 = symbolsService.selectTSymbolsList(tSymbols);
                    if(null!=tSymbols1&&tSymbols1.size()>0){
                        TSymbols tSymbols2 = tSymbols1.get(0);
                        klineSymbol.setLogo(tSymbols2.getLogoUrl());
                    }
                    klineSymbolMapper.insertKlineSymbol(klineSymbol);
                }
            }
        }
    }
//
    //@PostConstruct
    public void buildExCoin(){
        HttpResponse execute = HttpUtil.createGet("https://api-q.dssa.com/exchangeSymbol.php?exchName=WH")
                .header("referer", "https://dad2wq.esda.com/")
                .header("Host", "api-q.vfwqq.com")
                .header("Origin", "https://quote.xqweq.com").execute();
        String logo ="https://adminapi.gsadtads.top/profile/waihui/#.png";
        KlineSymbol query = new KlineSymbol();
        if (execute.isOk()){
            String result = execute.body();
            JSONArray objects = JSONArray.parseArray(result);
            for (int a  = 0 ;a<objects.size();a++){
                JSONObject o =(JSONObject) objects.get(a);
                String symbol = (String)o.get("i");
                query.setSymbol(symbol);
                query.setMarket("mt5");
                List<KlineSymbol> klineSymbols = klineSymbolMapper.selectKlineSymbolList(query);
                if(null==klineSymbols||klineSymbols.size()<1){
                    KlineSymbol klineSymbol = new KlineSymbol();
                    klineSymbol.setMarket("mt5");
                    klineSymbol.setSlug(symbol.toUpperCase());
                    klineSymbol.setSymbol(symbol);
                    klineSymbol.setLogo(logo.replace("#",symbol));
                    klineSymbolMapper.insertKlineSymbol(klineSymbol);
                }
            }
        }
    }
    public static void main(String[] args) {
        BeeBuildCompent beeBuildCompent =new BeeBuildCompent();
        beeBuildCompent.buildExCoin();
    }
}
