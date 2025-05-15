package com.ruoyi.web.controller.bussiness;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.bussiness.domain.TMineFinancial;
import com.ruoyi.bussiness.service.ITMineFinancialService;
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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 理财产品Controller
 * 
 * @author ruoyi
 * @date 2023-07-17
 */
@RestController
@RequestMapping("/bussiness/financial")
public class TMineFinancialController extends BaseController
{
    @Autowired
    private ITMineFinancialService tMineFinancialService;

    /**
     * 查询理财产品列表
     */
    @PreAuthorize("@ss.hasPermi('system:financial:list')")
    @GetMapping("/list")
    public TableDataInfo list(TMineFinancial tMineFinancial)
    {
        startPage();
        List<TMineFinancial> list = tMineFinancialService.selectTMineFinancialList(tMineFinancial);
        return getDataTable(list);
    }

    /**
     * 导出理财产品列表
     */
    @PreAuthorize("@ss.hasPermi('system:financial:export')")
    @Log(title = "理财产品", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TMineFinancial tMineFinancial)
    {
        List<TMineFinancial> list = tMineFinancialService.selectTMineFinancialList(tMineFinancial);
        ExcelUtil<TMineFinancial> util = new ExcelUtil<TMineFinancial>(TMineFinancial.class);
        util.exportExcel(response, list, "理财产品数据");
    }

    /**
     * 获取理财产品详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:financial:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tMineFinancialService.selectTMineFinancialById(id));
    }

    /**
     * 新增理财产品
     */
    @PreAuthorize("@ss.hasPermi('system:financial:add')")
    @Log(title = "理财产品", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TMineFinancial tMineFinancial)
    {
        return toAjax(tMineFinancialService.insertTMineFinancial(tMineFinancial));
    }

    /**
     * 修改理财产品
     */
    @PreAuthorize("@ss.hasPermi('system:financial:edit')")
    @Log(title = "理财产品", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TMineFinancial tMineFinancial)
    {
        return toAjax(tMineFinancialService.updateTMineFinancial(tMineFinancial));
    }

        /**
     * 删除理财产品
     */
    @PreAuthorize("@ss.hasPermi('system:financial:remove')")
    @Log(title = "理财产品", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tMineFinancialService.deleteTMineFinancialByIds(ids));
    }
}
