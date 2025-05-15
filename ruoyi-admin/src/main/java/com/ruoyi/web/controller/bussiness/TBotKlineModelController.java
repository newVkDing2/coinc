package com.ruoyi.web.controller.bussiness;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.bussiness.domain.TContractCoin;
import com.ruoyi.bussiness.domain.TCurrencySymbol;
import com.ruoyi.bussiness.domain.vo.SymbolCoinConfigVO;
import com.ruoyi.bussiness.domain.vo.TBotKlineModelVO;
import com.ruoyi.bussiness.service.ITContractCoinService;
import com.ruoyi.bussiness.service.ITCurrencySymbolService;
import com.ruoyi.bussiness.service.ITSecondCoinConfigService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.bussiness.domain.TBotKlineModel;
import com.ruoyi.bussiness.service.ITBotKlineModelService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 控线配置Controller
 * 
 * @author ruoyi
 * @date 2023-08-09
 */
@RestController
@RequestMapping("/bussiness/model")
public class TBotKlineModelController extends BaseController
{
    @Autowired
    private ITBotKlineModelService tBotKlineModelService;
    @Resource
    private ITSecondCoinConfigService tSecondCoinConfigService;
    @Resource
    private ITCurrencySymbolService tCurrencySymbolService;
    @Resource
    private ITContractCoinService tContractCoinService;
    /**
     * 查询控线配置列表
     */
    @GetMapping("/list")
    public TableDataInfo list(TBotKlineModel tBotKlineModel)
    {
        startPage();
        List<TBotKlineModel> list = tBotKlineModelService.selectTBotKlineModelList(tBotKlineModel);
        return getDataTable(list);
    }

    /**
     * 导出控线配置列表
     */

    @Log(title = "控线配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TBotKlineModel tBotKlineModel)
    {
        List<TBotKlineModel> list = tBotKlineModelService.selectTBotKlineModelList(tBotKlineModel);
        ExcelUtil<TBotKlineModel> util = new ExcelUtil<TBotKlineModel>(TBotKlineModel.class);
        util.exportExcel(response, list, "控线配置数据");
    }

    /**
     * 获取控线配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:trade-robot:detail')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tBotKlineModelService.selectTBotKlineModelById(id));
    }

    /**
     * 新增控线配置
     */

    @Log(title = "控线配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TBotKlineModelVO tBotKlineModel)
    {
        return toAjax(tBotKlineModelService.insertTBotInfo(tBotKlineModel));
    }

    /**
     * 修改控线配置
     */
    @PreAuthorize("@ss.hasPermi('bussiness:trade-robot:edit')")
    @Log(title = "控线配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TBotKlineModelVO tBotKlineModel)
    {
        return toAjax(tBotKlineModelService.updateTBotKlineModel(tBotKlineModel));
    }

    /**
     * 删除控线配置
     */
    @PreAuthorize("@ss.hasPermi('bussiness:trade-robot:remove')")
    @Log(title = "控线配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tBotKlineModelService.deleteTBotKlineModelByIds(ids));
    }
    @PostMapping("/symbolList")
    public TableDataInfo list()
    {
        List<TCurrencySymbol> rtn = new ArrayList<>();
        List<SymbolCoinConfigVO> coinList = tSecondCoinConfigService.getSymbolList();
        for (SymbolCoinConfigVO coin: coinList ) {
            if(coin.getMarket().equals("binance")){
            TCurrencySymbol  tCurrencySymbol1 = new TCurrencySymbol();
            tCurrencySymbol1.setSymbol(coin.getSymbol());
            tCurrencySymbol1.setCoin(coin.getCoin());
            tCurrencySymbol1.setShowSymbol(coin.getShowSymbol());
            rtn.add(tCurrencySymbol1);
            }
        }
        List<TCurrencySymbol> currencyList = tCurrencySymbolService.getSymbolList();
        for (TCurrencySymbol coin: currencyList ) {
            TCurrencySymbol  tCurrencySymbol1 = new TCurrencySymbol();
            tCurrencySymbol1.setSymbol(coin.getSymbol());
            tCurrencySymbol1.setCoin(coin.getCoin());
            tCurrencySymbol1.setShowSymbol(coin.getShowSymbol());
            rtn.add(tCurrencySymbol1);
        }
        List<TContractCoin> contractList = tContractCoinService.getCoinList();
        for (TContractCoin coin: contractList ) {
            TCurrencySymbol  tCurrencySymbol1 = new TCurrencySymbol();
            tCurrencySymbol1.setSymbol(coin.getSymbol());
            tCurrencySymbol1.setCoin(coin.getCoin());
            tCurrencySymbol1.setShowSymbol(coin.getShowSymbol());
            rtn.add(tCurrencySymbol1);
        }
        Set<TCurrencySymbol> objects = new TreeSet<>(Comparator.comparing(o->(o.getShowSymbol())));
        objects.addAll(rtn);

        return getDataTable(Arrays.asList(objects.toArray()));
    }
}
