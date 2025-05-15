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
import com.ruoyi.bussiness.domain.TMingProduct;
import com.ruoyi.bussiness.service.ITMingProductService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * mingProductController
 * 
 * @author ruoyi
 * @date 2023-08-18
 */
@RestController
@RequestMapping("/bussiness/ming")
public class TMingProductController extends BaseController
{
    @Autowired
    private ITMingProductService tMingProductService;

    /**
     * 查询mingProduct列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ming:list')")
    @GetMapping("/list")
    public TableDataInfo list(TMingProduct tMingProduct)
    {
        startPage();
        List<TMingProduct> list = tMingProductService.selectTMingProductList(tMingProduct);
        return getDataTable(list);
    }

    /**
     * 导出mingProduct列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ming:export')")
    @Log(title = "mingProduct", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TMingProduct tMingProduct)
    {
        List<TMingProduct> list = tMingProductService.selectTMingProductList(tMingProduct);
        ExcelUtil<TMingProduct> util = new ExcelUtil<TMingProduct>(TMingProduct.class);
        util.exportExcel(response, list, "mingProduct数据");
    }

    /**
     * 获取mingProduct详细信息
     */
        @PreAuthorize("@ss.hasPermi('bussiness:ming:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tMingProductService.selectTMingProductById(id));
    }

    /**
     * 新增mingProduct
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ming:add')")
    @Log(title = "mingProduct", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TMingProduct tMingProduct)
    {
        return toAjax(tMingProductService.insertTMingProduct(tMingProduct));
    }

    /**
     * 修改mingProduct
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ming:edit')")
    @Log(title = "mingProduct", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TMingProduct tMingProduct)
    {
        return toAjax(tMingProductService.updateTMingProduct(tMingProduct));
    }

    /**
     * 删除mingProduct
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ming:remove')")
    @Log(title = "mingProduct", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tMingProductService.deleteTMingProductByIds(ids));
    }
}
