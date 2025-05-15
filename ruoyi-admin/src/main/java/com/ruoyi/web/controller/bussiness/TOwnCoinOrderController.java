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
import com.ruoyi.bussiness.domain.TOwnCoinOrder;
import com.ruoyi.bussiness.service.ITOwnCoinOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 申购订单Controller
 * 
 * @author ruoyi
 * @date 2023-09-20
 */
@RestController
@RequestMapping("/bussiness/ownCoinOrder")
public class TOwnCoinOrderController extends BaseController
{
    @Autowired
    private ITOwnCoinOrderService tOwnCoinOrderService;

    /**
     * 查询申购订单列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ownCoinOrder:list')")
    @GetMapping("/list")
    public TableDataInfo list(TOwnCoinOrder tOwnCoinOrder)
    {
        startPage();
        List<TOwnCoinOrder> list = tOwnCoinOrderService.selectTOwnCoinOrderList(tOwnCoinOrder);
        return getDataTable(list);
    }

    /**
     * 导出申购订单列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ownCoinOrder:export')")
    @Log(title = "申购订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TOwnCoinOrder tOwnCoinOrder)
    {
        List<TOwnCoinOrder> list = tOwnCoinOrderService.selectTOwnCoinOrderList(tOwnCoinOrder);
        ExcelUtil<TOwnCoinOrder> util = new ExcelUtil<TOwnCoinOrder>(TOwnCoinOrder.class);
        util.exportExcel(response, list, "申购订单数据");
    }

    /**
     * 获取申购订单详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ownCoinOrder:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tOwnCoinOrderService.selectTOwnCoinOrderById(id));
    }

    /**
     * 新增申购订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ownCoinOrder:add')")
    @Log(title = "申购订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TOwnCoinOrder tOwnCoinOrder)
    {
        return toAjax(tOwnCoinOrderService.insertTOwnCoinOrder(tOwnCoinOrder));
    }

    /**
     * 修改申购订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ownCoinOrder:edit')")
    @Log(title = "申购订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TOwnCoinOrder tOwnCoinOrder)
    {
        return toAjax(tOwnCoinOrderService.updateTOwnCoinOrder(tOwnCoinOrder));
    }

    /**
     * 删除申购订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ownCoinOrder:remove')")
    @Log(title = "申购订单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tOwnCoinOrderService.deleteTOwnCoinOrderByIds(ids));
    }

    /**
     * 审批（修改）申购订单
     *
     * @param tOwnCoinOrder
     * @return
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ownCoinOrder:edit')")
    @Log(title = "申购订单", businessType = BusinessType.UPDATE)
    @PostMapping("/editPlacing")
    public AjaxResult editPlacing(@RequestBody TOwnCoinOrder tOwnCoinOrder)
    {
        return tOwnCoinOrderService.editPlacing(tOwnCoinOrder);
    }
}
