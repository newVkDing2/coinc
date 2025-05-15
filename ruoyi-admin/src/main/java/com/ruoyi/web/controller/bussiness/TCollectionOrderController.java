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
import com.ruoyi.bussiness.domain.TCollectionOrder;
import com.ruoyi.bussiness.service.ITCollectionOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 【请填写功能名称】Controller
 * 
 * @author ruoyi
 * @date 2023-09-08
 */
@RestController
@RequestMapping("/bussiness/collectionOrder")
public class TCollectionOrderController extends BaseController
{
    @Autowired
    private ITCollectionOrderService tCollectionOrderService;

    /**
     * 查询【请填写功能名称】列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:collectionorder:list')")
    @GetMapping("/list")
    public TableDataInfo list(TCollectionOrder tCollectionOrder)
    {
        startPage();
        List<TCollectionOrder> list = tCollectionOrderService.selectTCollectionOrderList(tCollectionOrder);
        return getDataTable(list);
    }

    /**
     * 导出【请填写功能名称】列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:collectionorder:export')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TCollectionOrder tCollectionOrder)
    {
        List<TCollectionOrder> list = tCollectionOrderService.selectTCollectionOrderList(tCollectionOrder);
        ExcelUtil<TCollectionOrder> util = new ExcelUtil<TCollectionOrder>(TCollectionOrder.class);
        util.exportExcel(response, list, "【请填写功能名称】数据");
    }

    /**
     * 获取【请填写功能名称】详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:collectionorder:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tCollectionOrderService.selectTCollectionOrderById(id));
    }

    /**
     * 新增【请填写功能名称】
     */
    @PreAuthorize("@ss.hasPermi('bussiness:collectionorder:add')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TCollectionOrder tCollectionOrder)
    {
        return toAjax(tCollectionOrderService.insertTCollectionOrder(tCollectionOrder));
    }

    /**
     * 修改【请填写功能名称】
     */
    @PreAuthorize("@ss.hasPermi('bussiness:collectionorder:edit')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TCollectionOrder tCollectionOrder)
    {
        return toAjax(tCollectionOrderService.updateTCollectionOrder(tCollectionOrder));
    }

    /**
     * 删除【请填写功能名称】
     */
    @PreAuthorize("@ss.hasPermi('bussiness:collectionorder:remove')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tCollectionOrderService.deleteTCollectionOrderByIds(ids));
    }
}
