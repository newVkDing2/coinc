package com.ruoyi.web.controller.bussiness;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.ruoyi.bussiness.domain.TMarkets;
import com.ruoyi.bussiness.service.ITMarketsService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 支持交易所Controller
 * 
 * @author ruoyi
 * @date 2023-06-26
 */
@RestController
@RequestMapping("/bussiness/markets")
public class TMarketsController extends BaseController
{
    @Autowired
    private ITMarketsService tMarketsService;

    /**
     * 查询支持交易所列表
     */
    @PreAuthorize("@ss.hasPermi('system:markets:list')")
    @GetMapping("/list")
    public TableDataInfo list(TMarkets tMarkets)
    {
        startPage();
        List<TMarkets> list = tMarketsService.selectTMarketsList(tMarkets);
        return getDataTable(list);
    }

    /**
     * 导出支持交易所列表
     */
    @PreAuthorize("@ss.hasPermi('system:markets:export')")
    @Log(title = "支持交易所", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TMarkets tMarkets)
    {
        List<TMarkets> list = tMarketsService.selectTMarketsList(tMarkets);
        ExcelUtil<TMarkets> util = new ExcelUtil<TMarkets>(TMarkets.class);
        util.exportExcel(response, list, "支持交易所数据");
    }

    /**
     * 获取支持交易所详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:markets:query')")
    @GetMapping(value = "/{slug}")
    public AjaxResult getInfo(@PathVariable("slug") String slug)
    {
        return success(tMarketsService.selectTMarketsBySlug(slug));
    }

    /**
     * 新增支持交易所
     */
    @PreAuthorize("@ss.hasPermi('system:markets:add')")
    @Log(title = "支持交易所", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TMarkets tMarkets)
    {
        return toAjax(tMarketsService.insertTMarkets(tMarkets));
    }

    /**
     * 修改支持交易所
     */
    @PreAuthorize("@ss.hasPermi('system:markets:edit')")
    @Log(title = "支持交易所", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TMarkets tMarkets)
    {
        return toAjax(tMarketsService.updateTMarkets(tMarkets));
    }

    /**
     * 删除支持交易所
     */
    @PreAuthorize("@ss.hasPermi('system:markets:remove')")
    @Log(title = "支持交易所", businessType = BusinessType.DELETE)
	@DeleteMapping("/{slugs}")
    public AjaxResult remove(@PathVariable String[] slugs)
    {
        return toAjax(tMarketsService.deleteTMarketsBySlugs(slugs));
    }
}
