package com.ruoyi.web.controller.bussiness;

import com.ruoyi.bussiness.domain.TAppRecharge;
import com.ruoyi.bussiness.domain.TAppWalletRecord;
import com.ruoyi.bussiness.service.ITAppWalletRecordService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 用户信息Controller
 * 
 * @author ruoyi
 * @date 2023-07-04
 */
@RestController
@RequestMapping("/bussiness/wallet/record")
public class TAppWalletRecordController extends BaseController
{
    @Autowired
    private ITAppWalletRecordService tAppWalletRecordService;

    /**
     * 查询用户信息列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(TAppWalletRecord tAppWalletRecord)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if(!user.isAdmin()){
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")){
                tAppWalletRecord.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        startPage();
        List<TAppWalletRecord> list = tAppWalletRecordService.selectTAppWalletRecordList(tAppWalletRecord);
        return getDataTable(list);
    }

    /**
     * 导出账变信息
     */
//    @PreAuthorize("@ss.hasPermi('bussiness:record:export')")
    @Log(title = "账变信息", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, TAppWalletRecord tAppWalletRecord)
    {
//        LoginUser loginUser = SecurityUtils.getLoginUser();
//        SysUser user = loginUser.getUser();
//        if(!user.isAdmin()){
//            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")){
//                tAppWalletRecord.setAdminParentIds(String.valueOf(user.getUserId()));
//            }
//        }
        List<TAppWalletRecord> list = tAppWalletRecordService.selectTAppWalletRecordList(tAppWalletRecord);
        ExcelUtil<TAppWalletRecord> util = new ExcelUtil<TAppWalletRecord>(TAppWalletRecord.class);
        util.exportExcel(response, list, "用户信息数据");
    }

    /**
     * 获取用户信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:record:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tAppWalletRecordService.selectTAppWalletRecordById(id));
    }

    /**
     * 新增用户信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:record:add')")
    @Log(title = "用户信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TAppWalletRecord tAppWalletRecord)
    {
        return toAjax(tAppWalletRecordService.insertTAppWalletRecord(tAppWalletRecord));
    }

    /**
     * 修改用户信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:record:edit')")
    @Log(title = "用户信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TAppWalletRecord tAppWalletRecord)
    {
        return toAjax(tAppWalletRecordService.updateTAppWalletRecord(tAppWalletRecord));
    }

    /**
     * 删除用户信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:record:remove')")
    @Log(title = "用户信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tAppWalletRecordService.deleteTAppWalletRecordByIds(ids));
    }

    @PostMapping("/statisticsAmount")
    public AjaxResult statisticsAmount() {
        return AjaxResult.success(tAppWalletRecordService.statisticsAmount());
    }

}
