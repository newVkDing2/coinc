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
import com.ruoyi.bussiness.domain.THomeSetter;
import com.ruoyi.bussiness.service.ITHomeSetterService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 规则说明Controller
 * 
 * @author ruoyi
 * @date 2023-07-19
 */
@RestController
@RequestMapping("/bussiness/home/setter")
public class THomeSetterController extends BaseController
{
    @Autowired
    private ITHomeSetterService tHomeSetterService;

    /**
     * 查询规则说明列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:setter:list')")
    @GetMapping("/list")
    public TableDataInfo list(THomeSetter tHomeSetter)
    {
        startPage();
        List<THomeSetter> list = tHomeSetterService.selectTHomeSetterList(tHomeSetter);
        return getDataTable(list);
    }

    /**
     * 导出规则说明列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:setter:export')")
    @Log(title = "规则说明", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, THomeSetter tHomeSetter)
    {
        List<THomeSetter> list = tHomeSetterService.selectTHomeSetterList(tHomeSetter);
        ExcelUtil<THomeSetter> util = new ExcelUtil<THomeSetter>(THomeSetter.class);
        util.exportExcel(response, list, "规则说明数据");
    }

    /**
     * 获取规则说明详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:setter:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tHomeSetterService.selectTHomeSetterById(id));
    }

    /**
     * 新增规则说明
     */
    @PreAuthorize("@ss.hasPermi('bussiness:setter:add')")
    @Log(title = "规则说明", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody THomeSetter tHomeSetter)
    {
        return toAjax(tHomeSetterService.insertTHomeSetter(tHomeSetter));
    }

    /**
     * 修改规则说明
     */
    @PreAuthorize("@ss.hasPermi('bussiness:setter:edit')")
    @Log(title = "规则说明", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody THomeSetter tHomeSetter)
    {
        return toAjax(tHomeSetterService.updateTHomeSetter(tHomeSetter));
    }

    /**
     * 删除规则说明
     */
    @PreAuthorize("@ss.hasPermi('bussiness:setter:remove')")
    @Log(title = "规则说明", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tHomeSetterService.deleteTHomeSetterByIds(ids));
    }
}
