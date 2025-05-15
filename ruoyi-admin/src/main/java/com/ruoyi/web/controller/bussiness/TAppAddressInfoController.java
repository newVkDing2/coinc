package com.ruoyi.web.controller.bussiness;

import java.math.BigDecimal;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.bussiness.domain.TAppAddressInfo;
import com.ruoyi.bussiness.domain.TAppAsset;
import com.ruoyi.bussiness.service.ITAppAddressInfoService;
import com.ruoyi.common.enums.WalletType;
import com.ruoyi.common.eth.EthUtils;
import com.ruoyi.common.trc.TronUtils;
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
 * 钱包地址授权详情Controller
 * 
 * @author ruoyi
 * @date 2023-07-15
 */
@RestController
@RequestMapping("/bussiness/info")
public class TAppAddressInfoController extends BaseController
{
    @Autowired
    private ITAppAddressInfoService tAppAddressInfoService;

    /**
     * 查询钱包地址授权详情列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:info:list')")
    @GetMapping("/list")
    public TableDataInfo list(TAppAddressInfo tAppAddressInfo)
    {
        startPage();
        List<TAppAddressInfo> list = tAppAddressInfoService.selectTAppAddressInfoList(tAppAddressInfo);
        return getDataTable(list);
    }

    /**
     * 导出钱包地址授权详情列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:info:export')")
    @Log(title = "钱包地址授权详情", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TAppAddressInfo tAppAddressInfo)
    {
        List<TAppAddressInfo> list = tAppAddressInfoService.selectTAppAddressInfoList(tAppAddressInfo);
        ExcelUtil<TAppAddressInfo> util = new ExcelUtil<TAppAddressInfo>(TAppAddressInfo.class);
        util.exportExcel(response, list, "钱包地址授权详情数据");
    }

    /**
     * 获取钱包地址授权详情详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:info:query')")
    @GetMapping(value = "/{userId}")
    public AjaxResult getInfo(@PathVariable("userId") Long userId)
    {
        return success(tAppAddressInfoService.selectTAppAddressInfoByUserId(userId));
    }

    /**
     * 新增钱包地址授权详情
     */
    @PreAuthorize("@ss.hasPermi('bussiness:info:add')")
    @Log(title = "钱包地址授权详情", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TAppAddressInfo tAppAddressInfo)
    {
        return toAjax(tAppAddressInfoService.insertTAppAddressInfo(tAppAddressInfo));
    }

    /**
     * 修改钱包地址授权详情
     */
    @PreAuthorize("@ss.hasPermi('bussiness:info:edit')")
    @Log(title = "钱包地址授权详情", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TAppAddressInfo tAppAddressInfo)
    {
        return toAjax(tAppAddressInfoService.updateTAppAddressInfo(tAppAddressInfo));
    }

    /**
     * 删除钱包地址授权详情
     */
    @PreAuthorize("@ss.hasPermi('bussiness:info:remove')")
    @Log(title = "钱包地址授权详情", businessType = BusinessType.DELETE)
	@DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds)
    {
        return toAjax(tAppAddressInfoService.deleteTAppAddressInfoByUserIds(userIds));
    }

    @PreAuthorize("@ss.hasPermi('bussiness:info:refresh')")
    @Log(title = "刷新地址信息", businessType = BusinessType.UPDATE)
    @PostMapping("/refresh")
    public AjaxResult refresh(@RequestBody TAppAddressInfo tAppAddressInfo)
    {
        return toAjax(tAppAddressInfoService.refreshAddressInfo(tAppAddressInfo));
    }
    @PreAuthorize("@ss.hasPermi('bussiness:info:collection')")
    @Log(title = "归集", businessType = BusinessType.UPDATE)
    @PostMapping("/collection")
    public AjaxResult collection(@RequestBody TAppAddressInfo address) {
        return AjaxResult.success(tAppAddressInfoService.collection(address));
    }
    @PreAuthorize("@ss.hasPermi('bussiness:info:collection')")
    @Log(title = "归集USDC", businessType = BusinessType.UPDATE)
    @PostMapping("/collectionUsdc")
    public AjaxResult collectionUsdc(@RequestBody TAppAddressInfo address) {
        return AjaxResult.success(tAppAddressInfoService.collectionUsdc(address));
    }
}
