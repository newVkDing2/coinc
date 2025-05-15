package com.ruoyi.web.controller.bussiness;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bussiness.domain.TSymbolManage;
import com.ruoyi.bussiness.service.ITSymbolManageService;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.StringUtils;
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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 币种管理Controller
 * 
 * @author ruoyi
 * @date 2023-07-12
 */
@RestController
@RequestMapping("/bussiness/symbolmanage")
public class TSymbolManageController extends BaseController
{
    @Autowired
    private ITSymbolManageService tSymbolManageService;

    /**
     * 查询币种管理列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:symbolmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(TSymbolManage tSymbolManage)
    {
        startPage();
        List<TSymbolManage> list = tSymbolManageService.selectTSymbolManageList(tSymbolManage);
        return getDataTable(list);
    }

    /**
     * 导出币种管理列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:symbolmanage:export')")
    @Log(title = "币种管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TSymbolManage tSymbolManage)
    {
        List<TSymbolManage> list = tSymbolManageService.selectTSymbolManageList(tSymbolManage);
        ExcelUtil<TSymbolManage> util = new ExcelUtil<TSymbolManage>(TSymbolManage.class);
        util.exportExcel(response, list, "币种管理数据");
    }

    /**
     * 获取币种管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:symbolmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tSymbolManageService.selectTSymbolManageById(id));
    }

    /**
     * 新增币种管理
     */
    @PreAuthorize("@ss.hasPermi('bussiness:symbolmanage:add')")
    @Log(title = "币种管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TSymbolManage tSymbolManage)
    {
        TSymbolManage oldManage = tSymbolManageService.getOne(new LambdaQueryWrapper<TSymbolManage>().eq(TSymbolManage::getSymbol, tSymbolManage.getSymbol()));
        if (StringUtils.isNotNull(oldManage)){
            return AjaxResult.error(tSymbolManage.getSymbol()+"币种已经存在");
        }
        return toAjax(tSymbolManageService.insertTSymbolManage(tSymbolManage));
    }

    /**
     * 新增币种管理
     */
    @PreAuthorize("@ss.hasPermi('bussiness:symbolmanage:addBatch')")
    @Log(title = "币种管理", businessType = BusinessType.INSERT)
    @PostMapping("/addBatch")
    public AjaxResult addBatch(String[] symbols)
    {
        String msg = "";
        TSymbolManage tSymbolManage = new TSymbolManage();
        tSymbolManage.setEnable("1");
        List<String> oldManage = tSymbolManageService.selectSymbolList(new TSymbolManage());
        if (StringUtils.isEmpty(symbols)){
            return AjaxResult.error("新增币种为空");
        }
        if (!CollectionUtils.isEmpty(oldManage)){
            for (int i = 0; i < symbols.length; i++) {
                if (oldManage.contains(symbols[i])){
                    msg+=symbols[i]+",";
                }
            }
        }
        if (StringUtils.isNotBlank(msg)){
            return AjaxResult.error("币种已经存在"+msg.substring(0,msg.length()-1));
        }
        tSymbolManageService.addBatch(symbols);
        return AjaxResult.success();
    }

    /**
     * 修改币种管理
     */
    @PreAuthorize("@ss.hasPermi('bussiness:symbolmanage:edit')")
    @Log(title = "币种管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TSymbolManage tSymbolManage)
    {
        TSymbolManage oldManage = tSymbolManageService.getOne(new LambdaQueryWrapper<TSymbolManage>().eq(TSymbolManage::getSymbol, tSymbolManage.getSymbol()));
        if (StringUtils.isNotNull(oldManage) && !oldManage.getId().equals(tSymbolManage.getId())){
            return AjaxResult.error(tSymbolManage.getSymbol()+"币种已经存在");
        }
        return toAjax(tSymbolManageService.updateTSymbolManage(tSymbolManage));
    }

    /**
     * 删除币种管理
     */
        @PreAuthorize("@ss.hasPermi('bussiness:symbolmanage:remove')")
    @Log(title = "币种管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tSymbolManageService.deleteTSymbolManageByIds(ids));
    }
}
