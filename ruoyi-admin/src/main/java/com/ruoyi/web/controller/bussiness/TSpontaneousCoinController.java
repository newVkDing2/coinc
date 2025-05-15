package com.ruoyi.web.controller.bussiness;

import java.util.List;
import java.util.Objects;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bussiness.domain.KlineSymbol;
import com.ruoyi.bussiness.domain.TOwnCoin;
import com.ruoyi.bussiness.service.IKlineSymbolService;
import com.ruoyi.bussiness.service.ITOwnCoinService;
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
import com.ruoyi.bussiness.domain.TSpontaneousCoin;
import com.ruoyi.bussiness.service.ITSpontaneousCoinService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 自发币种配置Controller
 * 
 * @author ruoyi
 * @date 2023-10-08
 */
@RestController
@RequestMapping("/bussiness/spontaneousCoin")
public class TSpontaneousCoinController extends BaseController
{
    @Autowired
    private ITOwnCoinService tOwnCoinService;
    @Resource
    private ITSpontaneousCoinService tSpontaneousCoinService;
    @Resource
    private IKlineSymbolService klineSymbolService;

    /**
     * 查询自发币种配置列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:spontaneousCoin:list')")
    @GetMapping("/list")
    public TableDataInfo list(TSpontaneousCoin tSpontaneousCoin)
    {
        startPage();
        List<TSpontaneousCoin> list = tSpontaneousCoinService.selectTSpontaneousCoinList(tSpontaneousCoin);
        return getDataTable(list);
    }

    /**
     * 导出自发币种配置列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:spontaneousCoin:export')")
    @Log(title = "自发币种配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TSpontaneousCoin tSpontaneousCoin)
    {
        List<TSpontaneousCoin> list = tSpontaneousCoinService.selectTSpontaneousCoinList(tSpontaneousCoin);
        ExcelUtil<TSpontaneousCoin> util = new ExcelUtil<TSpontaneousCoin>(TSpontaneousCoin.class);
        util.exportExcel(response, list, "自发币种配置数据");
    }

    /**
     * 获取自发币种配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:spontaneousCoin:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tSpontaneousCoinService.selectTSpontaneousCoinById(id));
    }

    /**
     * 新增自发币种配置
     */
    @PreAuthorize("@ss.hasPermi('bussiness:spontaneousCoin:add')")
    @Log(title = "自发币种配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TSpontaneousCoin tSpontaneousCoin)
    {
        tSpontaneousCoin.setCoin(tSpontaneousCoin.getCoin().toLowerCase());
        TSpontaneousCoin oldSpontaneousCoin = tSpontaneousCoinService.getOne(new LambdaQueryWrapper<TSpontaneousCoin>().eq(TSpontaneousCoin::getCoin, tSpontaneousCoin.getCoin()));
        TOwnCoin tOwnCoin = tOwnCoinService.getOne(new LambdaQueryWrapper<TOwnCoin>().eq(TOwnCoin::getCoin, tSpontaneousCoin.getCoin()));
        KlineSymbol oldklineSymbol = klineSymbolService.getOne(new LambdaQueryWrapper<KlineSymbol>()
                .eq(KlineSymbol::getSymbol, tSpontaneousCoin.getCoin())
                .and(k->k.eq(KlineSymbol::getMarket,"binance").or().eq(KlineSymbol::getMarket,"echo")));
        if (Objects.nonNull(oldSpontaneousCoin) || Objects.nonNull(tOwnCoin) || Objects.nonNull(oldklineSymbol)){
            return AjaxResult.error(tSpontaneousCoin.getCoin()+"已经存在");
        }
        return toAjax(tSpontaneousCoinService.insertTSpontaneousCoin(tSpontaneousCoin));
    }

    /**
     * 修改自发币种配置
     */
    @PreAuthorize("@ss.hasPermi('bussiness:spontaneousCoin:edit')")
    @Log(title = "自发币种配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TSpontaneousCoin tSpontaneousCoin)
    {
        return toAjax(tSpontaneousCoinService.updateTSpontaneousCoin(tSpontaneousCoin));
    }

    /**
     * 删除自发币种配置
     */
    @PreAuthorize("@ss.hasPermi('bussiness:spontaneousCoin:remove')")
    @Log(title = "自发币种配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        return toAjax(tSpontaneousCoinService.deleteTSpontaneousCoinById(id));
    }
}
