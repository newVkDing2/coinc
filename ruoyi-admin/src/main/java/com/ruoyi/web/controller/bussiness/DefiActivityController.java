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
import com.ruoyi.bussiness.domain.DefiActivity;
import com.ruoyi.bussiness.service.IDefiActivityService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 空投活动Controller
 * 
 * @author ruoyi
 * @date 2023-08-17
 */
@RestController
@RequestMapping("/bussiness/activitydefi")
public class DefiActivityController extends BaseController
{
    @Autowired
    private IDefiActivityService defiActivityService;

    /**
     * 查询空投活动列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:activitydefi:list')")
    @GetMapping("/list")
    public TableDataInfo list(DefiActivity defiActivity)
    {
        startPage();
        List<DefiActivity> list = defiActivityService.selectDefiActivityList(defiActivity);
        return getDataTable(list);
    }

    /**
     * 导出空投活动列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:activitydefi:export')")
    @Log(title = "空投活动", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DefiActivity defiActivity)
    {
        List<DefiActivity> list = defiActivityService.selectDefiActivityList(defiActivity);
        ExcelUtil<DefiActivity> util = new ExcelUtil<DefiActivity>(DefiActivity.class);
        util.exportExcel(response, list, "空投活动数据");
    }

    /**
     * 获取空投活动详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:activitydefi:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(defiActivityService.selectDefiActivityById(id));
    }

    /**
     * 新增空投活动
     */
    @PreAuthorize("@ss.hasPermi('bussiness:activitydefi:add')")
    @Log(title = "空投活动", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DefiActivity defiActivity)
    {
        return toAjax(defiActivityService.insertDefiActivity(defiActivity));
    }

    /**
     * 修改空投活动
     */
    @PreAuthorize("@ss.hasPermi('bussiness:activitydefi:edit')")
    @Log(title = "空投活动", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DefiActivity defiActivity)
    {
        return toAjax(defiActivityService.updateDefiActivity(defiActivity));
    }

    /**
     * 删除空投活动
     */
    @PreAuthorize("@ss.hasPermi('bussiness:activitydefi:remove')")
    @Log(title = "空投活动", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(defiActivityService.deleteDefiActivityByIds(ids));
    }
}
