package com.ruoyi.web.controller.bussiness;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.bussiness.domain.TActivityRecharge;
import com.ruoyi.bussiness.service.ITActivityRechargeService;
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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 充值活动Controller
 * 
 * @author ruoyi
 * @date 2023-07-05
 */
@RestController
@RequestMapping("/bussiness/activityrecharge")
public class TActivityRechargeController extends BaseController
{
    @Autowired
    private ITActivityRechargeService tActivityRechargeService;

    /**
     * 查询充值活动列表
     */
    @PreAuthorize("@ss.hasPermi('system:recharge:list')")
    @GetMapping("/list")
    public TableDataInfo list(TActivityRecharge tActivityRecharge)
    {
        startPage();
        List<TActivityRecharge> list = tActivityRechargeService.selectTActivityRechargeList(tActivityRecharge);
        return getDataTable(list);
    }

    /**
     * 导出充值活动列表
     */
    @PreAuthorize("@ss.hasPermi('system:recharge:export')")
    @Log(title = "充值活动", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TActivityRecharge tActivityRecharge)
    {
        List<TActivityRecharge> list = tActivityRechargeService.selectTActivityRechargeList(tActivityRecharge);
        ExcelUtil<TActivityRecharge> util = new ExcelUtil<TActivityRecharge>(TActivityRecharge.class);
        util.exportExcel(response, list, "充值活动数据");
    }

    /**
     * 获取充值活动详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:recharge:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tActivityRechargeService.selectTActivityRechargeById(id));
    }

    /**
     * 新增充值活动
     */
    @PreAuthorize("@ss.hasPermi('system:recharge:add')")
    @Log(title = "充值活动", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TActivityRecharge tActivityRecharge)
    {
        return toAjax(tActivityRechargeService.insertTActivityRecharge(tActivityRecharge));
    }

    /**
     * 修改充值活动
     */
    @PreAuthorize("@ss.hasPermi('system:recharge:edit')")
    @Log(title = "充值活动", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TActivityRecharge tActivityRecharge)
    {
        return toAjax(tActivityRechargeService.updateTActivityRecharge(tActivityRecharge));
    }

    /**
     * 删除充值活动
     */
    @PreAuthorize("@ss.hasPermi('system:recharge:remove')")
    @Log(title = "充值活动", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tActivityRechargeService.deleteTActivityRechargeByIds(ids));
    }
}
