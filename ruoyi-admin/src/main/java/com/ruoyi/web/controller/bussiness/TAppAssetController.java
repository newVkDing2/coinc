package com.ruoyi.web.controller.bussiness;

import java.math.BigDecimal;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.bussiness.domain.TAppAddressInfo;
import com.ruoyi.bussiness.domain.TAppAsset;
import com.ruoyi.bussiness.service.ITAppAssetService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
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
 * 玩家资产Controller
 * 
 * @author ruoyi
 * @date 2023-06-30
 */
@RestController
@RequestMapping("/bussiness/asset")
public class TAppAssetController extends BaseController
{
    @Autowired
    private ITAppAssetService tAppAssetService;

    /**
     * 查询玩家资产列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:asset:list')")
    @GetMapping("/list")
    public TableDataInfo list(TAppAsset tAppAsset)
    {

        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if(!user.isAdmin()){
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")){
                tAppAsset.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        startPage();
        List<TAppAsset> list = tAppAssetService.selectTAppAssetList(tAppAsset);
        return getDataTable(list);
    }

    /**
     * 导出玩家资产列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:asset:export')")
    @Log(title = "玩家资产", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TAppAsset tAppAsset)
    {
        List<TAppAsset> list = tAppAssetService.selectTAppAssetList(tAppAsset);
        ExcelUtil<TAppAsset> util = new ExcelUtil<TAppAsset>(TAppAsset.class);
        util.exportExcel(response, list, "玩家资产数据");
    }

    /**
     * 获取玩家资产详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:asset:query')")
    @GetMapping(value = "/{userId}")
    public AjaxResult getInfo(@PathVariable("userId") Long userId)
    {
        return success(tAppAssetService.selectTAppAssetByUserId(userId));
    }

    /**
     * 新增玩家资产
     */
    @PreAuthorize("@ss.hasPermi('bussiness:asset:add')")
    @Log(title = "玩家资产", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TAppAsset tAppAsset)
    {
        return toAjax(tAppAssetService.insertTAppAsset(tAppAsset));
    }

    /**
     * 修改玩家资产
     */
    @PreAuthorize("@ss.hasPermi('bussiness:asset:edit')")
    @Log(title = "玩家资产", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TAppAsset tAppAsset)
    {
        return toAjax(tAppAssetService.updateTAppAsset(tAppAsset));
    }

    /**
     * 删除玩家资产
     */
    @PreAuthorize("@ss.hasPermi('bussiness:asset:remove')")
    @Log(title = "玩家资产", businessType = BusinessType.DELETE)
	@DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds)
    {
        return toAjax(tAppAssetService.deleteTAppAssetByUserIds(userIds));
    }


}
