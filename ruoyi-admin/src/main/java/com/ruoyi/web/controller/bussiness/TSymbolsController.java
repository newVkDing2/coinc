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
import com.ruoyi.bussiness.domain.TSymbols;
import com.ruoyi.bussiness.service.ITSymbolsService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 支持币种Controller
 * 
 * @author ruoyi
 * @date 2023-06-26
 */
@RestController
@RequestMapping("/bussiness/symbols")
public class TSymbolsController extends BaseController
{
    @Autowired
    private ITSymbolsService tSymbolsService;

    /**
     * 查询支持币种列表
     */
    @PreAuthorize("@ss.hasPermi('system:symbols:list')")
    @GetMapping("/list")
    public TableDataInfo list(TSymbols tSymbols)
    {
        startPage();
        List<TSymbols> list = tSymbolsService.selectTSymbolsList(tSymbols);
        return getDataTable(list);
    }

    /**
     * 导出支持币种列表
     */
    @PreAuthorize("@ss.hasPermi('system:symbols:export')")
    @Log(title = "支持币种", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TSymbols tSymbols)
    {
        List<TSymbols> list = tSymbolsService.selectTSymbolsList(tSymbols);
        ExcelUtil<TSymbols> util = new ExcelUtil<TSymbols>(TSymbols.class);
        util.exportExcel(response, list, "支持币种数据");
    }

    /**
     * 获取支持币种详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:symbols:query')")
    @GetMapping(value = "/{slug}")
    public AjaxResult getInfo(@PathVariable("slug") String slug)
    {
        return success(tSymbolsService.selectTSymbolsBySlug(slug));
    }

    /**
     * 新增支持币种
     */
    @PreAuthorize("@ss.hasPermi('system:symbols:add')")
    @Log(title = "支持币种", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TSymbols tSymbols)
    {
        return toAjax(tSymbolsService.insertTSymbols(tSymbols));
    }

    /**
     * 修改支持币种
     */
    @PreAuthorize("@ss.hasPermi('system:symbols:edit')")
    @Log(title = "支持币种", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TSymbols tSymbols)
    {
        return toAjax(tSymbolsService.updateTSymbols(tSymbols));
    }

    /**
     * 删除支持币种
     */
    @PreAuthorize("@ss.hasPermi('system:symbols:remove')")
    @Log(title = "支持币种", businessType = BusinessType.DELETE)
	@DeleteMapping("/{slugs}")
    public AjaxResult remove(@PathVariable String[] slugs)
    {
        return toAjax(tSymbolsService.deleteTSymbolsBySlugs(slugs));
    }
}
