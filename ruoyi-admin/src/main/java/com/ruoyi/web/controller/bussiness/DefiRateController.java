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
import com.ruoyi.bussiness.domain.DefiRate;
import com.ruoyi.bussiness.service.IDefiRateService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * defi挖矿利率配置Controller
 * 
 * @author ruoyi
 * @date 2023-08-17
 */
@RestController
@RequestMapping("/bussiness/ratedefi")
public class DefiRateController extends BaseController
{
    @Autowired
    private IDefiRateService defiRateService;

    /**
     * 查询defi挖矿利率配置列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ratedefi:list')")
    @GetMapping("/list")
    public TableDataInfo list(DefiRate defiRate)
    {
        startPage();
        List<DefiRate> list = defiRateService.selectDefiRateList(defiRate);
        return getDataTable(list);
    }

    /**
     * 导出defi挖矿利率配置列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ratedefi:export')")
    @Log(title = "defi挖矿利率配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DefiRate defiRate)
    {
        List<DefiRate> list = defiRateService.selectDefiRateList(defiRate);
        ExcelUtil<DefiRate> util = new ExcelUtil<DefiRate>(DefiRate.class);
        util.exportExcel(response, list, "defi挖矿利率配置数据");
    }

    /**
     * 获取defi挖矿利率配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ratedefi:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(defiRateService.selectDefiRateById(id));
    }

    /**
     * 新增defi挖矿利率配置
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ratedefi:add')")
    @Log(title = "defi挖矿利率配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DefiRate defiRate)
    {
        return toAjax(defiRateService.insertDefiRate(defiRate));
    }

    /**
     * 修改defi挖矿利率配置
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ratedefi:edit')")
    @Log(title = "defi挖矿利率配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DefiRate defiRate)
    {
        return toAjax(defiRateService.updateDefiRate(defiRate));
    }

    /**
     * 删除defi挖矿利率配置
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ratedefi:remove')")
    @Log(title = "defi挖矿利率配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(defiRateService.deleteDefiRateByIds(ids));
    }
}
