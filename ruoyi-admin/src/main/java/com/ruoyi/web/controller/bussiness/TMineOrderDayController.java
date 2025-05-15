package com.ruoyi.web.controller.bussiness;

import java.math.BigDecimal;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.bussiness.domain.TMineOrderDay;
import com.ruoyi.bussiness.service.ITMineOrderDayService;
import com.ruoyi.common.utils.poi.ExcelUtil;
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
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 理财每日结算Controller
 * 
 * @author ruoyi
 * @date 2023-07-17
 */
@RestController
@RequestMapping("/bussiness/day")
public class TMineOrderDayController extends BaseController
{
    @Autowired
    private ITMineOrderDayService tMineOrderDayService;

    /**
     * 查询理财每日结算列表
     */
    @PreAuthorize("@ss.hasPermi('system:day:list')")
    @GetMapping("/list")
    public TableDataInfo list(TMineOrderDay tMineOrderDay)
    {
        startPage();
        List<TMineOrderDay> list = tMineOrderDayService.selectTMineOrderDayList(tMineOrderDay);
        return getDataTable(list);
    }

    /**
     * 导出理财每日结算列表
     */
    @PreAuthorize("@ss.hasPermi('system:day:export')")
    @Log(title = "理财每日结算", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TMineOrderDay tMineOrderDay)
    {
        List<TMineOrderDay> list = tMineOrderDayService.selectTMineOrderDayList(tMineOrderDay);
        ExcelUtil<TMineOrderDay> util = new ExcelUtil<TMineOrderDay>(TMineOrderDay.class);
        util.exportExcel(response, list, "理财每日结算数据");
    }

    /**
     * 获取理财每日结算详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:day:query')")
    @GetMapping(value = "/{amount}")
    public AjaxResult getInfo(@PathVariable("amount") BigDecimal amount)
    {
        return success(tMineOrderDayService.selectTMineOrderDayByAmount(amount));
    }

    /**
     * 新增理财每日结算
     */
    @PreAuthorize("@ss.hasPermi('system:day:add')")
    @Log(title = "理财每日结算", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TMineOrderDay tMineOrderDay)
    {
        return toAjax(tMineOrderDayService.insertTMineOrderDay(tMineOrderDay));
    }

    /**
     * 修改理财每日结算
     */
    @PreAuthorize("@ss.hasPermi('system:day:edit')")
    @Log(title = "理财每日结算", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TMineOrderDay tMineOrderDay)
    {
        return toAjax(tMineOrderDayService.updateTMineOrderDay(tMineOrderDay));
    }

    /**
     * 删除理财每日结算
     */
    @PreAuthorize("@ss.hasPermi('system:day:remove')")
    @Log(title = "理财每日结算", businessType = BusinessType.DELETE)
	@DeleteMapping("/{amounts}")
    public AjaxResult remove(@PathVariable BigDecimal[] amounts)
    {
        return toAjax(tMineOrderDayService.deleteTMineOrderDayByAmounts(amounts));
    }
}
