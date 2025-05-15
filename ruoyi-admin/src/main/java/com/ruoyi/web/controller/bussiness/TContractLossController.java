package com.ruoyi.web.controller.bussiness;

import com.ruoyi.bussiness.domain.TContractLoss;
import com.ruoyi.bussiness.service.ITContractLossService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 止盈止损表Controller
 * 
 * @author ruoyi
 * @date 2023-07-25
 */
@RestController
@RequestMapping("/bussiness/contractLoss")
public class TContractLossController extends BaseController
{
    @Autowired
    private ITContractLossService tContractLossService;

    /**
     * 查询止盈止损表列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:contractLoss:list')")
    @GetMapping("/list")
    public TableDataInfo list(TContractLoss tContractLoss)
    {
        startPage();
        List<TContractLoss> list = tContractLossService.selectTContractLossList(tContractLoss);
        return getDataTable(list);
    }

    /**
     * 导出止盈止损表列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:contractLoss:export')")
    @Log(title = "止盈止损表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TContractLoss tContractLoss)
    {
        List<TContractLoss> list = tContractLossService.selectTContractLossList(tContractLoss);
        ExcelUtil<TContractLoss> util = new ExcelUtil<TContractLoss>(TContractLoss.class);
        util.exportExcel(response, list, "止盈止损表数据");
    }

    /**
     * 获取止盈止损表详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:contractLoss:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tContractLossService.selectTContractLossById(id));
    }

    /**
     * 新增止盈止损表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:contractLoss:add')")
    @Log(title = "止盈止损表", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TContractLoss tContractLoss)
    {
        return toAjax(tContractLossService.insertTContractLoss(tContractLoss));
    }

    /**
     * 修改止盈止损表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:contractLoss:edit')")
    @Log(title = "止盈止损表", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TContractLoss tContractLoss)
    {
        return toAjax(tContractLossService.updateTContractLoss(tContractLoss));
    }

    /**
     * 删除止盈止损表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:contractLoss:remove')")
    @Log(title = "止盈止损表", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tContractLossService.deleteTContractLossByIds(ids));
    }
    @PostMapping("/settMent")
    @ResponseBody
    public AjaxResult settMent(@RequestBody TContractLoss contractLoss) {
        String result = tContractLossService.cntractLossSett( contractLoss);
        if (!"success".equals(result)) {
            return AjaxResult.error(result);
        }
        return AjaxResult.success();
    }
}
