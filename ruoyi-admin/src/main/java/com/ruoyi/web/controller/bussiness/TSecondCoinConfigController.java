package com.ruoyi.web.controller.bussiness;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bussiness.domain.TSecondCoinConfig;
import com.ruoyi.bussiness.domain.vo.SecondCoinCopyVO;
import com.ruoyi.bussiness.service.ITSecondCoinConfigService;
import com.ruoyi.common.utils.SecurityUtils;
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
 * 秒合约币种配置Controller
 * 
 * @author ruoyi
 * @date 2023-07-11
 */
@RestController
@RequestMapping("/bussiness/coin")
public class TSecondCoinConfigController extends BaseController
{
    @Autowired
    private ITSecondCoinConfigService tSecondCoinConfigService;

    /**
     * 查询秒合约币种配置列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:config:list')")
    @GetMapping("/list")
    public TableDataInfo list(TSecondCoinConfig tSecondCoinConfig)
    {
        startPage();
        List<TSecondCoinConfig> list = tSecondCoinConfigService.selectTSecondCoinConfigList(tSecondCoinConfig);
        return getDataTable(list);
    }
    @PreAuthorize("@ss.hasPermi('bussiness:coin:config:list')")
    @GetMapping("/copylist")
    public AjaxResult copylist(TSecondCoinConfig tSecondCoinConfig)
    {
        List<TSecondCoinConfig> list = tSecondCoinConfigService.selectTSecondCoinConfigList(tSecondCoinConfig);
        return AjaxResult.success(list);
    }

    /**
     * 导出秒合约币种配置列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:config:export')")
    @Log(title = "秒合约币种配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TSecondCoinConfig tSecondCoinConfig)
    {
        List<TSecondCoinConfig> list = tSecondCoinConfigService.selectTSecondCoinConfigList(tSecondCoinConfig);
        ExcelUtil<TSecondCoinConfig> util = new ExcelUtil<TSecondCoinConfig>(TSecondCoinConfig.class);
        util.exportExcel(response, list, "秒合约币种配置数据");
    }

    /**
     * 获取秒合约币种配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:config:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tSecondCoinConfigService.selectTSecondCoinConfigById(id));
    }

    /**
     * 新增秒合约币种配置
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:config:add')")
    @Log(title = "秒合约币种配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TSecondCoinConfig tSecondCoinConfig)
    {
        TSecondCoinConfig one = tSecondCoinConfigService.getOne(new LambdaQueryWrapper<TSecondCoinConfig>().eq(TSecondCoinConfig::getCoin, tSecondCoinConfig.getCoin()));
        if(null != one){
            return AjaxResult.success("请勿重复添加！");
        }
        tSecondCoinConfig.setCreateBy(SecurityUtils.getUsername());
        return toAjax(tSecondCoinConfigService.insertSecondCoin(tSecondCoinConfig));
    }
    /**
     * 一键添加秒合约币种
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:config:add')")
    @Log(title = "秒合约币种配置", businessType = BusinessType.INSERT)
    @PostMapping("batchSave/{coins}")
    public AjaxResult batchSave(@PathVariable String[] coins)
    {
        tSecondCoinConfigService.batchSave(coins);
        return AjaxResult.success();
    }

    /**
     * 修改秒合约币种配置
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:config:edit')")
    @Log(title = "秒合约币种配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TSecondCoinConfig tSecondCoinConfig)
    {
        tSecondCoinConfig.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(tSecondCoinConfigService.updateTSecondCoinConfig(tSecondCoinConfig));
    }

    /**
     * 删除秒合约币种配置
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:config:remove')")
    @Log(title = "秒合约币种配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tSecondCoinConfigService.deleteTSecondCoinConfigByIds(ids));
    }
    @PreAuthorize("@ss.hasPermi('bussiness:coin:config:bathCopy')")
    @Log(title = "查看已有的周期配置币种", businessType = BusinessType.DELETE)
    @PostMapping("/query/bathCopy")
    public AjaxResult bathCopy()
    {
        return AjaxResult.success(tSecondCoinConfigService.selectBathCopySecondCoinConfigList());
    }

    /**
     * 周期配置批量复制
     * @return
     */
    @PostMapping("/bathCopyIng")
    public AjaxResult bathCopyIng(@RequestBody SecondCoinCopyVO secondCoinCopyVO)
    {
        return toAjax(tSecondCoinConfigService.bathCopyIng(secondCoinCopyVO));
    }
}
