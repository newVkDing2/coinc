package com.ruoyi.web.controller.bussiness;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.bussiness.domain.TSecondPeriodConfig;
import com.ruoyi.bussiness.service.ITSecondPeriodConfigService;
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
 * 秒合约币种周期配置Controller
 * 
 * @author ruoyi
 * @date 2023-07-11
 */
@RestController
@RequestMapping("/bussiness/period")
public class TSecondPeriodConfigController extends BaseController
{
    @Autowired
    private ITSecondPeriodConfigService tSecondPeriodConfigService;

    /**
     * 查询秒合约币种周期配置列表
     */
    @PreAuthorize("@ss.hasPermi('period:config:list')")
    @GetMapping("/list")
    public TableDataInfo list(TSecondPeriodConfig tSecondPeriodConfig)
    {
        startPage();
        List<TSecondPeriodConfig> list = tSecondPeriodConfigService.selectTSecondPeriodConfigList(tSecondPeriodConfig);
        return getDataTable(list);
    }

    /**
     * 导出秒合约币种周期配置列表
     */
    @PreAuthorize("@ss.hasPermi('period:config:export')")
    @Log(title = "秒合约币种周期配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TSecondPeriodConfig tSecondPeriodConfig)
    {
        List<TSecondPeriodConfig> list = tSecondPeriodConfigService.selectTSecondPeriodConfigList(tSecondPeriodConfig);
        ExcelUtil<TSecondPeriodConfig> util = new ExcelUtil<TSecondPeriodConfig>(TSecondPeriodConfig.class);
        util.exportExcel(response, list, "秒合约币种周期配置数据");
    }

    /**
     * 获取秒合约币种周期配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('period:config:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tSecondPeriodConfigService.selectTSecondPeriodConfigById(id));
    }

    /**
     * 新增秒合约币种周期配置
     */
    @PreAuthorize("@ss.hasPermi('period:config:add')")
    @Log(title = "秒合约币种周期配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TSecondPeriodConfig tSecondPeriodConfig)
    {
        return toAjax(tSecondPeriodConfigService.insertTSecondPeriodConfig(tSecondPeriodConfig));
    }

    /**
     * 修改秒合约币种周期配置
     */
    @PreAuthorize("@ss.hasPermi('period:config:edit')")
    @Log(title = "秒合约币种周期配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TSecondPeriodConfig tSecondPeriodConfig)
    {
        return toAjax(tSecondPeriodConfigService.updateTSecondPeriodConfig(tSecondPeriodConfig));
    }

    /**
     * 删除秒合约币种周期配置
     */
    @PreAuthorize("@ss.hasPermi('period:config:remove')")
    @Log(title = "秒合约币种周期配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tSecondPeriodConfigService.deleteTSecondPeriodConfigByIds(ids));
    }
}
