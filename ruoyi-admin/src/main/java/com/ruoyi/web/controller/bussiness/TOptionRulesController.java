package com.ruoyi.web.controller.bussiness;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.enums.OptionRulesEnum;
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
import com.ruoyi.bussiness.domain.TOptionRules;
import com.ruoyi.bussiness.service.ITOptionRulesService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 前台文本配置Controller
 * 
 * @author ruoyi
 * @date 2023-07-19
 */
@RestController
@RequestMapping("/bussiness/option/rules")
public class TOptionRulesController extends BaseController
{
    @Autowired
    private ITOptionRulesService tOptionRulesService;

    /**
     * 查询前台文本配置列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:rules:list')")
    @GetMapping("/list")
    public TableDataInfo list(TOptionRules tOptionRules)
    {
        tOptionRules.setType(OptionRulesEnum.valueOf(tOptionRules.getKey()).getCode());
        startPage();
        List<TOptionRules> list = tOptionRulesService.selectTOptionRulesList(tOptionRules);
        return getDataTable(list);
    }

    /**
     * 查询前台文本配置菜单列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:rules:labelList')")
    @GetMapping("/labelList")
    public TableDataInfo labelList()
    {
        return getDataTable(OptionRulesEnum.getEnum());
    }

    /**
     * 导出前台文本配置列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:rules:export')")
    @Log(title = "前台文本配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TOptionRules tOptionRules)
    {
        List<TOptionRules> list = tOptionRulesService.selectTOptionRulesList(tOptionRules);
        ExcelUtil<TOptionRules> util = new ExcelUtil<TOptionRules>(TOptionRules.class);
        util.exportExcel(response, list, "前台文本配置数据");
    }

    /**
     * 获取前台文本配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:rules:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tOptionRulesService.selectTOptionRulesById(id));
    }

    /**
     * 新增前台文本配置
     */
    @PreAuthorize("@ss.hasPermi('bussiness:rules:add')")
    @Log(title = "前台文本配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TOptionRules tOptionRules)
    {
        tOptionRules.setType(OptionRulesEnum.valueOf(tOptionRules.getKey()).getCode());
        return toAjax(tOptionRulesService.insertTOptionRules(tOptionRules));
    }

    /**
     * 修改前台文本配置
     */
    @PreAuthorize("@ss.hasPermi('bussiness:rules:edit')")
    @Log(title = "前台文本配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TOptionRules tOptionRules)
    {
        return toAjax(tOptionRulesService.updateTOptionRules(tOptionRules));
    }

    /**
     * 删除前台文本配置
     */
    @PreAuthorize("@ss.hasPermi('bussiness:rules:remove')")
    @Log(title = "前台文本配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tOptionRulesService.deleteTOptionRulesByIds(ids));
    }
}
