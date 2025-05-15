package com.ruoyi.web.controller.bussiness;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.bussiness.service.ITHelpCenterInfoService;
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
import com.ruoyi.bussiness.domain.THelpCenterInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 帮助中心问题详情Controller
 * 
 * @author ruoyi
 * @date 2023-08-17
 */
@RestController
@RequestMapping("/bussiness/helpCenterInfo")
public class THelpCenterInfoController extends BaseController
{
    @Autowired
    private ITHelpCenterInfoService tHelpCenterInfoService;

    /**
     * 查询帮助中心问题详情列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:helpCenterInfo:list')")
    @GetMapping("/list")
    public TableDataInfo list(THelpCenterInfo tHelpCenterInfo)
    {
        startPage();
        List<THelpCenterInfo> list = tHelpCenterInfoService.selectTHelpCenterInfoList(tHelpCenterInfo);
        return getDataTable(list);
    }

    /**
     * 导出帮助中心问题详情列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:helpCenterInfo:export')")
    @Log(title = "帮助中心问题详情", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, THelpCenterInfo tHelpCenterInfo)
    {
        List<THelpCenterInfo> list = tHelpCenterInfoService.selectTHelpCenterInfoList(tHelpCenterInfo);
        ExcelUtil<THelpCenterInfo> util = new ExcelUtil<THelpCenterInfo>(THelpCenterInfo.class);
        util.exportExcel(response, list, "帮助中心问题详情数据");
    }

    /**
     * 获取帮助中心问题详情详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:helpCenterInfo:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tHelpCenterInfoService.selectTHelpCenterInfoById(id));
    }

    /**
     * 新增帮助中心问题详情
     */
    @PreAuthorize("@ss.hasPermi('bussiness:helpCenterInfo:add')")
    @Log(title = "帮助中心问题详情", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody THelpCenterInfo tHelpCenterInfo)
    {
        return toAjax(tHelpCenterInfoService.insertTHelpCenterInfo(tHelpCenterInfo));
    }

    /**
     * 修改帮助中心问题详情
     */
    @PreAuthorize("@ss.hasPermi('bussiness:helpCenterInfo:edit')")
    @Log(title = "帮助中心问题详情", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody THelpCenterInfo tHelpCenterInfo)
    {
        return toAjax(tHelpCenterInfoService.updateTHelpCenterInfo(tHelpCenterInfo));
    }

    /**
     * 删除帮助中心问题详情
     */
    @PreAuthorize("@ss.hasPermi('bussiness:helpCenterInfo:remove')")
    @Log(title = "帮助中心问题详情", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tHelpCenterInfoService.deleteTHelpCenterInfoByIds(ids));
    }
}
