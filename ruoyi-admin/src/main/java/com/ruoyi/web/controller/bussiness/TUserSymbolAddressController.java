package com.ruoyi.web.controller.bussiness;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.bussiness.domain.setting.ThirdPaySetting;
import com.ruoyi.bussiness.service.SettingService;
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
import com.ruoyi.bussiness.domain.TUserSymbolAddress;
import com.ruoyi.bussiness.service.ITUserSymbolAddressService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 用户币种充值地址Controller
 *
 * @author ruoyi
 * @date 2023-07-12
 */
@RestController
@RequestMapping("/bussiness/symbol/address")
public class TUserSymbolAddressController extends BaseController {
    @Autowired
    private ITUserSymbolAddressService tUserSymbolAddressService;
    @Autowired
    private SettingService settingService;
    /**
     * 查询用户币种充值地址列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:symbol/address:list')")
    @GetMapping("/list")
    public TableDataInfo list(TUserSymbolAddress tUserSymbolAddress) {
        startPage();
        List<TUserSymbolAddress> list = tUserSymbolAddressService.selectTUserSymbolAddressList(tUserSymbolAddress);
        return getDataTable(list);
    }

    /**
     * 导出用户币种充值地址列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:symbol/address:export')")
    @Log(title = "用户币种充值地址", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TUserSymbolAddress tUserSymbolAddress) {
        List<TUserSymbolAddress> list = tUserSymbolAddressService.selectTUserSymbolAddressList(tUserSymbolAddress);
        ExcelUtil<TUserSymbolAddress> util = new ExcelUtil<TUserSymbolAddress>(TUserSymbolAddress.class);
        util.exportExcel(response, list, "用户币种充值地址数据");
    }

    /**
     * 获取用户币种充值地址详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:symbol/address:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(tUserSymbolAddressService.selectTUserSymbolAddressById(id));
    }

    /**
     * 新增用户币种充值地址
     */
    @PreAuthorize("@ss.hasPermi('bussiness:symbol/address:add')")
    @Log(title = "用户币种充值地址", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TUserSymbolAddress tUserSymbolAddress) {
        int re = tUserSymbolAddressService.insertTUserSymbolAddress(tUserSymbolAddress);
        if (10001 == re) {
            return AjaxResult.error("相同币种，请勿重复添加地址");
        }
        return toAjax(re);
    }

    /**
     * 修改用户币种充值地址
     */
    @PreAuthorize("@ss.hasPermi('bussiness:symbol/address:edit')")
    @Log(title = "用户币种充值地址", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TUserSymbolAddress tUserSymbolAddress) {
        return toAjax(tUserSymbolAddressService.updateTUserSymbolAddress(tUserSymbolAddress));
    }

    /**
     * 删除用户币种充值地址
     */
    @PreAuthorize("@ss.hasPermi('bussiness:symbol/address:remove')")
    @Log(title = "用户币种充值地址", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(tUserSymbolAddressService.deleteTUserSymbolAddressByIds(ids));
    }

    @PostMapping("/getAdress")
    public AjaxResult getAdress(String coin, String symbol) {
        Map<String, String> map = tUserSymbolAddressService.getAdredssByCoin(coin, symbol, getUserId());
        return AjaxResult.success(map);
    }

    @PostMapping("/checkuType")
    public AjaxResult checkuType() {
        ThirdPaySetting  thirdPaySetting= settingService.getThirdPaySetting("301");
        return AjaxResult.success(thirdPaySetting);
    }


}
