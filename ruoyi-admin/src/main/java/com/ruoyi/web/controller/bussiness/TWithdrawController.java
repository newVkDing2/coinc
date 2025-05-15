package com.ruoyi.web.controller.bussiness;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bussiness.domain.TWithdraw;
import com.ruoyi.bussiness.domain.setting.ThirdPaySetting;
import com.ruoyi.bussiness.service.ITWithdrawService;
import com.ruoyi.bussiness.service.SettingService;
import com.ruoyi.bussiness.service.ThirdPayOutFactory;
import com.ruoyi.bussiness.service.ThirdPayOutService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.enums.ThirdTypeUncEmun;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.socket.socketserver.WebSocketNotice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 用户提现Controller
 *
 * @author ruoyi
 * @date 2023-07-24
 */
@RestController
@RequestMapping("/bussiness/withdraw")
public class TWithdrawController extends BaseController {
    @Resource
    private ITWithdrawService tWithdrawService;
    @Resource
    private WebSocketNotice webSocketNotice;
    @Resource
    private SettingService settingService;

    /**
     * 查询用户提现列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:withdraw:list')")
    @GetMapping("/list")
    public TableDataInfo list(TWithdraw tWithdraw) {
        startPage();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if (!user.isAdmin()) {
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")) {
                tWithdraw.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        List<TWithdraw> list = tWithdrawService.selectTWithdrawList(tWithdraw);
        return getDataTable(list);
    }

    /**
     * 导出用户提现列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:withdraw:export')")
    @Log(title = "用户提现", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TWithdraw tWithdraw) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if (!user.isAdmin()) {
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")) {
                tWithdraw.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        List<TWithdraw> list = tWithdrawService.selectTWithdrawList(tWithdraw);
        ExcelUtil<TWithdraw> util = new ExcelUtil<TWithdraw>(TWithdraw.class);
        util.exportExcel(response, list, "用户提现数据");
    }

    /**
     * 获取用户提现详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:order:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(tWithdrawService.selectTWithdrawById(id));
    }

    /**
     * 新增用户提现
     */
    @PreAuthorize("@ss.hasPermi('bussiness:withdraw:add')")
    @Log(title = "用户提现", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TWithdraw tWithdraw) {
        return toAjax(tWithdrawService.insertTWithdraw(tWithdraw));
    }

    /**
     * 修改用户提现
     */
    @PreAuthorize("@ss.hasPermi('bussiness:withdraw:edit')")
    @Log(title = "用户提现", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TWithdraw tWithdraw) {
        return toAjax(tWithdrawService.updateTWithdraw(tWithdraw));
    }

    /**
     * 删除用户提现
     */
    @PreAuthorize("@ss.hasPermi('bussiness:withdraw:remove')")
    @Log(title = "用户提现", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(tWithdrawService.deleteTWithdrawByIds(ids));
    }


    // 通过只改状态， 只能审核0级用户
    @PreAuthorize("@ss.hasPermi('bussiness:withdraw:edit')")
    @Log(title = "提现管理.锁定", businessType = BusinessType.UPDATE)
    @PostMapping("/lockorder")
    public AjaxResult lockorder(@RequestBody TWithdraw wi) {

        TWithdraw withdraw = tWithdrawService.getOne(new LambdaQueryWrapper<TWithdraw>().eq(TWithdraw::getId, wi.getId()));
        if (withdraw.getStatus() != 0) {
            return AjaxResult.error("订单状态不对，不能锁定");
        }
        withdraw.setStatus(3);
        withdraw.setUpdateBy(getUsername());
        int iwithdraw = tWithdrawService.updateTWithdraw(withdraw);
        return toAjax(iwithdraw);
    }

    @PreAuthorize("@ss.hasPermi('bussiness:withdraw:edit')")
    @Log(title = "提现管理.解锁", businessType = BusinessType.UPDATE)
    @PostMapping("/unlockorder")
    public AjaxResult unlockorder(@RequestBody TWithdraw wi) {
        TWithdraw withdraw = tWithdrawService.getOne(new LambdaQueryWrapper<TWithdraw>().eq(TWithdraw::getId, wi.getId()));
        if (withdraw.getStatus() != 3) {
            return AjaxResult.error("订单状态不对，不能解锁");
        }
        if (!SysUser.isAdmin(getUserId()) && !withdraw.getUpdateBy().equals(getUsername())) {
            return AjaxResult.error("订单已经被别人锁定");
        }
        withdraw.setStatus(0);
        withdraw.setUpdateBy(getUsername());
        int iwithdraw = tWithdrawService.updateTWithdraw(withdraw);
        return toAjax(iwithdraw);
    }

    @PreAuthorize("@ss.hasPermi('bussiness:withdraw:edit')")
    @Log(title = "提现管理.锁定判断", businessType = BusinessType.UPDATE)
    @PostMapping("/tryCheck")
    public AjaxResult trycheck(@RequestBody TWithdraw wi) {
        TWithdraw withdraw = tWithdrawService.getOne(new LambdaQueryWrapper<TWithdraw>().eq(TWithdraw::getId, wi.getId()));
        if (withdraw.getStatus() != 3) {
            return AjaxResult.error("订单状态不对，不能审核");
        }
        if (!SysUser.isAdmin(getUserId()) && !withdraw.getUpdateBy().equals(getUsername())) {
            return AjaxResult.error("订单已经被别人锁定");
        }
        return AjaxResult.success();
    }

    @Transactional
    @PreAuthorize("@ss.hasPermi('bussiness:withdraw:edit')")
    @Log(title = "提现管理.审核通过", businessType = BusinessType.UPDATE)
    @PostMapping("/review")
    public AjaxResult passOrder(@RequestBody TWithdraw wi) {
        TWithdraw withdraw = tWithdrawService.getOne(new LambdaQueryWrapper<TWithdraw>().eq(TWithdraw::getId, wi.getId()));
        if (withdraw.getStatus() != 3) {
            return AjaxResult.error("订单状态不对，不能审核");
        }
        if (!SysUser.isAdmin(getUserId()) && !withdraw.getUpdateBy().equals(getUsername())) {
            return AjaxResult.error("订单已经被别人锁定");
        }
        if (!withdraw.getToAdress().equals(wi.getToAdress())) {
            return AjaxResult.error("只有提现中的订单才能修改地址！");
        }
        withdraw.setStatus(1);
        withdraw.setUpdateBy(getUsername());
        withdraw.setRemark(wi.getRemark());
        withdraw.setWithDrawRemark(wi.getWithDrawRemark());
        ThirdPaySetting setting = settingService.getThirdPaySetting(ThirdTypeUncEmun.UNCDUN.getValue());
        if (Objects.nonNull(setting)) {
            if ("0".equals(setting.getThirdWithStatu())) {
                ThirdPayOutService thirdPayOutService = ThirdPayOutFactory.getThirdPayOut(setting.getCompanyName());
                JSONObject re = thirdPayOutService.payOut(withdraw, setting);
                if (Objects.nonNull(re)) {
                    if (re.getInteger("code") != 200) {
                        return AjaxResult.error("U盾提现异常");
                    }
                }
            }
        }
        int iwithdraw = tWithdrawService.updateTWithdraw(withdraw);
        //socket通知
        webSocketNotice.sendInfoAll(tWithdrawService, 1);
        return toAjax(iwithdraw);
    }

    @Transactional
    @PreAuthorize("@ss.hasPermi('bussiness:withdraw:edit')")
    @Log(title = "提现管理.审核失败", businessType = BusinessType.UPDATE)
    @PostMapping("/reject")
    public AjaxResult rejectOrder(@RequestBody TWithdraw withdraw) {
        String msg = tWithdrawService.rejectOrder(withdraw);
        //socket通知
        webSocketNotice.sendInfoAll(tWithdrawService, 1);
        return AjaxResult.success(msg);
    }

    @PreAuthorize("@ss.hasPermi('bussiness:withdraw:list')")
    @PostMapping("/getAllWithdraw")
    public AjaxResult getAllWithdraw(Integer type) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        String parentId = "";
        if (user != null) {
            String userType = user.getUserType();
            if (user.isAdmin() || ("0").equals(userType)) {
                parentId = null;
            } else {
                parentId = user.getUserId().toString();
            }
        }
        return AjaxResult.success(tWithdrawService.getAllWithdraw(parentId, type));
    }
}
