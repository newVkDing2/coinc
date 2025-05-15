package com.ruoyi.web.controller.bussiness;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bussiness.domain.vo.OwnVO;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.CachePrefix;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.ruoyi.bussiness.domain.KlineSymbol;
import com.ruoyi.bussiness.service.IKlineSymbolService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 数据源Controller
 * 
 * @author ruoyi
 * @date 2023-07-10
 */
@RestController
@RequestMapping("/bussiness/klineSymbol")
public class KlineSymbolController extends BaseController
{
    @Resource
    private IKlineSymbolService klineSymbolService;
    @Resource
    private RedisCache redisCache;

    /**
     * 查询数据源列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:klineSymbol:list')")
    @GetMapping("/list")
    public TableDataInfo list(KlineSymbol klineSymbol)
    {
        List<KlineSymbol> list = klineSymbolService.selectKlineSymbolList(klineSymbol);
        return getDataTable(list);
    }
    @GetMapping("/selectList")
    public AjaxResult selectList(KlineSymbol klineSymbol)
    {
        List<OwnVO> list = new ArrayList<>();
        klineSymbol.setMarket("binance");
        List<String> keys = redisCache.keys(CachePrefix.CURRENCY_PRICE.getPrefix() + "*").stream().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(keys)) {
            return AjaxResult.success(list);
        }
        List<KlineSymbol> klineSymbolList = klineSymbolService.list(new LambdaQueryWrapper<KlineSymbol>().eq(KlineSymbol::getMarket, "binance"));
        for (String key : keys) {
           String coin= key.substring(key.indexOf(":")+1,key.length());
            String regex =".*[A-Z].*";
            if (coin.matches(regex)) {
               continue;
            }
            klineSymbolList.stream().forEach(k->{
                if (coin.toUpperCase().equals(k.getSymbol().toUpperCase())){
                    OwnVO ownVO = new OwnVO();
                    ownVO.setMarket(k.getMarket());
                    ownVO.setCoin(coin);
                    ownVO.setPrice(new BigDecimal(redisCache.getCacheObject(key).toString()));
                    list.add(ownVO);
                }
            });
        }
        return AjaxResult.success(list);
    }

    /**
     * 导出数据源列表
     */
    @PreAuthorize("@ss.hasPermi('system:symbol:export')")
    @Log(title = "数据源", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, KlineSymbol klineSymbol)
    {
        List<KlineSymbol> list = klineSymbolService.selectKlineSymbolList(klineSymbol);
        ExcelUtil<KlineSymbol> util = new ExcelUtil<KlineSymbol>(KlineSymbol.class);
        util.exportExcel(response, list, "数据源数据");
    }

    /**
     * 获取数据源详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:symbol:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(klineSymbolService.selectKlineSymbolById(id));
    }

    /**
     * 新增数据源
     */
    @PreAuthorize("@ss.hasPermi('system:symbol:add')")
    @Log(title = "数据源", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody KlineSymbol klineSymbol)
    {
        return toAjax(klineSymbolService.insertKlineSymbol(klineSymbol));
    }

    /**
     * 修改数据源
     */
    @PreAuthorize("@ss.hasPermi('system:symbol:edit')")
    @Log(title = "数据源", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody KlineSymbol klineSymbol)
    {
        return toAjax(klineSymbolService.updateKlineSymbol(klineSymbol));
    }

    /**
     * 删除数据源
     */
    @PreAuthorize("@ss.hasPermi('system:symbol:remove')")
    @Log(title = "数据源", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(klineSymbolService.deleteKlineSymbolByIds(ids));
    }
}
