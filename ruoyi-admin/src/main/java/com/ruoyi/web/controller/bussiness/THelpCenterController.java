package com.ruoyi.web.controller.bussiness;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.bussiness.domain.THelpCenterInfo;
import com.ruoyi.bussiness.service.ITHelpCenterInfoService;
import com.ruoyi.bussiness.service.ITHelpCenterService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
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
import com.ruoyi.bussiness.domain.THelpCenter;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 帮助中心Controller
 * 
 * @author ruoyi
 * @date 2023-08-17
 */
@RestController
@RequestMapping("/bussiness/helpcenter")
public class THelpCenterController extends BaseController
{
    @Autowired
    private ITHelpCenterService tHelpCenterService;
    @Resource
    private ITHelpCenterInfoService tHelpCenterInfoService;

    /**
     * 查询帮助中心列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:helpcenter:list')")
    @GetMapping("/list")
    public TableDataInfo list(THelpCenter tHelpCenter)
    {
        startPage();
        List<THelpCenter> list = tHelpCenterService.selectTHelpCenterList(tHelpCenter);
        return getDataTable(list);
    }

    /**
     * 导出帮助中心列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:helpcenter:export')")
    @Log(title = "帮助中心", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, THelpCenter tHelpCenter)
    {
        List<THelpCenter> list = tHelpCenterService.selectTHelpCenterList(tHelpCenter);
        ExcelUtil<THelpCenter> util = new ExcelUtil<THelpCenter>(THelpCenter.class);
        util.exportExcel(response, list, "帮助中心数据");
    }

    /**
     * 获取帮助中心详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:helpcenter:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tHelpCenterService.selectTHelpCenterById(id));
    }

    /**
     * 新增帮助中心
     */
    @PreAuthorize("@ss.hasPermi('bussiness:helpcenter:add')")
    @Log(title = "帮助中心", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody THelpCenter tHelpCenter)
    {
        return toAjax(tHelpCenterService.insertTHelpCenter(tHelpCenter));
    }

    /**
     * 修改帮助中心
     */
    @PreAuthorize("@ss.hasPermi('bussiness:helpcenter:edit')")
    @Log(title = "帮助中心", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody THelpCenter tHelpCenter)
    {
        return toAjax(tHelpCenterService.updateTHelpCenter(tHelpCenter));
    }

    /**
     * 删除帮助中心
     */
    @PreAuthorize("@ss.hasPermi('bussiness:helpcenter:remove')")
    @Log(title = "帮助中心", businessType = BusinessType.DELETE)
	@DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        THelpCenterInfo tHelpCenterInfo = new THelpCenterInfo();
        tHelpCenterInfo.setHelpCenterId(id);
        List<THelpCenterInfo> list = tHelpCenterInfoService.selectTHelpCenterInfoList(tHelpCenterInfo);
        if (!CollectionUtils.isEmpty(list)){
            return AjaxResult.error("该数据下存在子集，不允许删除");
        }
        return toAjax(tHelpCenterService.deleteTHelpCenterById(id));
    }
}
