package com.ruoyi.web.controller.bussiness;

import com.ruoyi.bussiness.domain.TLoadProduct;
import com.ruoyi.bussiness.service.ITLoadProductService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 借贷产品Controller
 * 
 * @author ruoyi
 * @date 2023-07-13
 */
@RestController
@RequestMapping("/bussiness/load/product")
public class TLoadProductController extends BaseController
{
    @Autowired
    private ITLoadProductService tLoadProductService;

    /**
     * 查询借贷产品列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:load/product:list')")
    @GetMapping("/list")
    public TableDataInfo list(TLoadProduct tLoadProduct)
    {
        startPage();
        List<TLoadProduct> list = tLoadProductService.selectTLoadProductList(tLoadProduct);
        return getDataTable(list);
    }

    /**
     * 导出借贷产品列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:load/product:export')")
    @Log(title = "借贷产品", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TLoadProduct tLoadProduct)
    {
        List<TLoadProduct> list = tLoadProductService.selectTLoadProductList(tLoadProduct);
        ExcelUtil<TLoadProduct> util = new ExcelUtil<TLoadProduct>(TLoadProduct.class);
        util.exportExcel(response, list, "借贷产品数据");
    }

    /**
     * 获取借贷产品详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:load/product:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tLoadProductService.selectTLoadProductById(id));
    }

    /**
     * 新增借贷产品
     */
    @PreAuthorize("@ss.hasPermi('bussiness:load/product:add')")
    @Log(title = "借贷产品", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TLoadProduct tLoadProduct)
    {
        return toAjax(tLoadProductService.insertTLoadProduct(tLoadProduct));
    }

    /**
     * 修改借贷产品
     */
    @PreAuthorize("@ss.hasPermi('bussiness:load/product:edit')")
    @Log(title = "借贷产品", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TLoadProduct tLoadProduct)
    {
        return toAjax(tLoadProductService.updateTLoadProduct(tLoadProduct));
    }

    /**
     * 删除借贷产品
     */
    @PreAuthorize("@ss.hasPermi('bussiness:load/product:remove')")
    @Log(title = "借贷产品", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tLoadProductService.deleteTLoadProductByIds(ids));
    }
}
