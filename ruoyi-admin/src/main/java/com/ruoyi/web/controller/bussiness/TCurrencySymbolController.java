package com.ruoyi.web.controller.bussiness;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.ruoyi.bussiness.domain.TCurrencySymbol;
import com.ruoyi.bussiness.service.ITCurrencySymbolService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 币币交易币种配置Controller
 * 
 * @author ruoyi
 * @date 2023-07-25
 */
@RestController
@RequestMapping("/bussiness/currency/symbol")
public class TCurrencySymbolController extends BaseController
{
    @Autowired
    private ITCurrencySymbolService tCurrencySymbolService;


    @PreAuthorize("@ss.hasPermi('currency:symbol:addBatch')")
    @Log(title = "币币交易币种配置", businessType = BusinessType.INSERT)
    @PostMapping("/addBatch")
    public AjaxResult batchSave( String[] symbols)
    {
        tCurrencySymbolService.batchSave(symbols);
        return AjaxResult.success();
    }
    /**
     * 查询币币交易币种配置列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:currency/symbol:list')")
    @GetMapping("/list")
    public TableDataInfo list(TCurrencySymbol tCurrencySymbol)
    {
        startPage();
        List<TCurrencySymbol> list = tCurrencySymbolService.selectTCurrencySymbolList(tCurrencySymbol);
        return getDataTable(list);
    }

    /**
     * 导出币币交易币种配置列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:currency/symbol:export')")
    @Log(title = "币币交易币种配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TCurrencySymbol tCurrencySymbol)
    {
        List<TCurrencySymbol> list = tCurrencySymbolService.selectTCurrencySymbolList(tCurrencySymbol);
        ExcelUtil<TCurrencySymbol> util = new ExcelUtil<TCurrencySymbol>(TCurrencySymbol.class);
        util.exportExcel(response, list, "币币交易币种配置数据");
    }

    /**
     * 获取币币交易币种配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:currency/symbol:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tCurrencySymbolService.selectTCurrencySymbolById(id));
    }

    /**
     * 新增币币交易币种配置
     */
    @PreAuthorize("@ss.hasPermi('bussiness:currency/symbol:add')")
    @Log(title = "币币交易币种配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TCurrencySymbol tCurrencySymbol)
    {
        TCurrencySymbol currencySymbol = tCurrencySymbolService.getOne(new LambdaQueryWrapper<TCurrencySymbol>()
                .eq(TCurrencySymbol::getCoin, tCurrencySymbol.getCoin().toLowerCase()));
        if (currencySymbol!=null){
            return AjaxResult.error(currencySymbol.getCoin()+currencySymbol.getBaseCoin()+"交易对已经存在");
        }
        return toAjax(tCurrencySymbolService.insertTCurrencySymbol(tCurrencySymbol));
    }

    /**
     * 修改币币交易币种配置
     */
    @PreAuthorize("@ss.hasPermi('bussiness:currency/symbol:edit')")
    @Log(title = "币币交易币种配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TCurrencySymbol tCurrencySymbol)
    {TCurrencySymbol currencySymbol = tCurrencySymbolService.getOne(new LambdaQueryWrapper<TCurrencySymbol>()
            .eq(TCurrencySymbol::getCoin, tCurrencySymbol.getCoin().toLowerCase()));
        if (currencySymbol!=null && !currencySymbol.getId().equals(tCurrencySymbol.getId())){
            return AjaxResult.error(currencySymbol.getCoin()+currencySymbol.getBaseCoin()+"交易对已经存在");
        }
        return toAjax(tCurrencySymbolService.updateTCurrencySymbol(tCurrencySymbol));
    }

    /**
     * 删除币币交易币种配置
     */
    @PreAuthorize("@ss.hasPermi('bussiness:currency/symbol:remove')")
    @Log(title = "币币交易币种配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tCurrencySymbolService.deleteTCurrencySymbolByIds(ids));
    }
}
