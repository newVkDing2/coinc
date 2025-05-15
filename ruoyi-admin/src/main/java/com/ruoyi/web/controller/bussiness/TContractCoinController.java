package com.ruoyi.web.controller.bussiness;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.ruoyi.bussiness.domain.TContractCoin;
import com.ruoyi.bussiness.service.ITContractCoinService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * U本位合约币种Controller
 *
 * @author michael
 * @date 2023-07-20
 */
@RestController
@RequestMapping("/bussiness/ucontract")
public class TContractCoinController extends BaseController {
    @Autowired
    private ITContractCoinService tContractCoinService;

    /**
     * 查询U本位合约币种列表
     */
        @PreAuthorize("@ss.hasPermi('bussiness:ucontract:list')")
    @GetMapping("/list")
    public TableDataInfo list(TContractCoin tContractCoin) {
        startPage();
        List<TContractCoin> list = tContractCoinService.selectTContractCoinList(tContractCoin);
        return getDataTable(list);
    }

    /**
     * 导出U本位合约币种列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ucontract:export')")
    @Log(title = "U本位合约币种", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TContractCoin tContractCoin) {
        List<TContractCoin> list = tContractCoinService.selectTContractCoinList(tContractCoin);
        ExcelUtil<TContractCoin> util = new ExcelUtil<TContractCoin>(TContractCoin.class);
        util.exportExcel(response, list, "U本位合约币种数据");
    }

    /**
     * 获取U本位合约币种详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ucontract:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(tContractCoinService.selectTContractCoinById(id));
    }

    /**
     * 新增U本位合约币种
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ucontract:add')")
    @Log(title = "U本位合约币种", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TContractCoin tContractCoin) {
        int result = tContractCoinService.insertTContractCoin(tContractCoin);
        if (10001==result){
            return AjaxResult.error("币种请勿重复添加！");
        }
        return toAjax(tContractCoinService.insertTContractCoin(tContractCoin));
    }

    /**
     * 修改U本位合约币种
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ucontract:edit')")
    @Log(title = "U本位合约币种", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TContractCoin tContractCoin) {
        return toAjax(tContractCoinService.updateTContractCoin(tContractCoin));
    }

    /**
     * 删除U本位合约币种
     */
    @PreAuthorize("@ss.hasPermi('bussiness:ucontract:remove')")
    @Log(title = "U本位合约币种", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(tContractCoinService.deleteTContractCoinByIds(ids));
    }
}
