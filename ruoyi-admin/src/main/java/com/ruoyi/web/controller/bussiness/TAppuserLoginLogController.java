package com.ruoyi.web.controller.bussiness;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.bussiness.domain.TAppuserLoginLog;
import com.ruoyi.bussiness.service.ITAppuserLoginLogService;
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

import java.util.List;

/**
 * 系统访问记录Controller
 * 
 * @author ruoyi
 * @date 2023-06-30
 */
@RestController
@RequestMapping("/bussiness/log")
public class TAppuserLoginLogController extends BaseController
{
    @Resource
    private ITAppuserLoginLogService tAppuserLoginLogService;

    /**
     * 查询系统访问记录列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(TAppuserLoginLog tAppuserLoginLog)
    {
        startPage();
        List<TAppuserLoginLog> list = tAppuserLoginLogService.selectTAppuserLoginLogList(tAppuserLoginLog);
        return getDataTable(list);
    }

    /**
     * 导出系统访问记录列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:log:export')")
    @Log(title = "系统访问记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TAppuserLoginLog tAppuserLoginLog)
    {
        List<TAppuserLoginLog> list = tAppuserLoginLogService.selectTAppuserLoginLogList(tAppuserLoginLog);
        ExcelUtil<TAppuserLoginLog> util = new ExcelUtil<TAppuserLoginLog>(TAppuserLoginLog.class);
        util.exportExcel(response, list, "系统访问记录数据");
    }

    /**
     * 获取系统访问记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:log:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tAppuserLoginLogService.selectTAppuserLoginLogById(id));
    }

    /**
     * 新增系统访问记录
     */
    @PreAuthorize("@ss.hasPermi('bussiness:log:add')")
    @Log(title = "系统访问记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TAppuserLoginLog tAppuserLoginLog)
    {
        return toAjax(tAppuserLoginLogService.insertTAppuserLoginLog(tAppuserLoginLog));
    }

    /**
     * 修改系统访问记录
     */
    @PreAuthorize("@ss.hasPermi('bussiness:log:edit')")
    @Log(title = "系统访问记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TAppuserLoginLog tAppuserLoginLog)
    {
        return toAjax(tAppuserLoginLogService.updateTAppuserLoginLog(tAppuserLoginLog));
    }

    /**
     * 删除系统访问记录
     */
    @PreAuthorize("@ss.hasPermi('bussiness:log:remove')")
    @Log(title = "系统访问记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tAppuserLoginLogService.deleteTAppuserLoginLogByIds(ids));
    }
}
