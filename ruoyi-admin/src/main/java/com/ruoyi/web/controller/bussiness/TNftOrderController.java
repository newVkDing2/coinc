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
import com.ruoyi.bussiness.domain.TNftOrder;
import com.ruoyi.bussiness.service.ITNftOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * nft订单Controller
 * 
 * @author ruoyi
 * @date 2023-09-01
 */
@RestController
@RequestMapping("/bussiness/nftOrder")
public class TNftOrderController extends BaseController
{
    @Autowired
    private ITNftOrderService tNftOrderService;

    /**
     * 查询nft订单列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:nftOrder:list')")
    @GetMapping("/list")
    public TableDataInfo list(TNftOrder tNftOrder)
    {
        startPage();
        List<TNftOrder> list = tNftOrderService.selectTNftOrderList(tNftOrder);
        return getDataTable(list);
    }

    /**
     * 导出nft订单列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:nftOrder:export')")
    @Log(title = "nft订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TNftOrder tNftOrder)
    {
        List<TNftOrder> list = tNftOrderService.selectTNftOrderList(tNftOrder);
        ExcelUtil<TNftOrder> util = new ExcelUtil<TNftOrder>(TNftOrder.class);
        util.exportExcel(response, list, "nft订单数据");
    }

    /**
     * 获取nft订单详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:nftOrder:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tNftOrderService.selectTNftOrderById(id));
    }

    /**
     * 新增nft订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:nftOrder:add')")
    @Log(title = "nft订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TNftOrder tNftOrder)
    {
        return toAjax(tNftOrderService.insertTNftOrder(tNftOrder));
    }

    /**
     * 修改nft订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:nftOrder:edit')")
    @Log(title = "nft订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TNftOrder tNftOrder)
    {
        return toAjax(tNftOrderService.updateTNftOrder(tNftOrder));
    }

    /**
     * 删除nft订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:nftOrder:remove')")
    @Log(title = "nft订单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tNftOrderService.deleteTNftOrderByIds(ids));
    }
}
