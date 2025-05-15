package com.ruoyi.web.controller.bussiness;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ruoyi.bussiness.domain.TAppAsset;
import com.ruoyi.bussiness.domain.TAppUserDetail;
import com.ruoyi.bussiness.domain.vo.UserBonusVO;
import com.ruoyi.bussiness.service.ITAppAssetService;
import com.ruoyi.bussiness.service.ITAppUserDetailService;
import com.ruoyi.bussiness.service.ITWithdrawService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.socket.socketserver.WebSocketNotice;
import io.swagger.annotations.ApiOperation;
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
import com.ruoyi.bussiness.domain.TAppUser;
import com.ruoyi.bussiness.service.ITAppUserService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 玩家用户Controller
 *
 * @author shenshen
 * @date 2023-06-27
 */
@RestController
@RequestMapping("/bussiness/user")
public class TAppUserController extends BaseController {
    @Resource
    private ITAppUserService tAppUserService;
    @Resource
    private ITAppAssetService appAssetService;
    @Resource
    private ITAppUserDetailService userDetailService;
    @Resource
    private ITWithdrawService tWithdrawService;
    @Resource
    private WebSocketNotice webSocketNotice;
    /**
     * 查询admin下的玩家用户列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(TAppUser tAppUser) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if(!user.isAdmin()){
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")){
                tAppUser.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        startPage();
        List<TAppUser> list = tAppUserService.getTAppUserList(tAppUser);
//        changeAppParentIds(list);
        return getDataTable(list);
    }

    protected void changeUserAppParentIds(TAppUser appUser){
        if(appUser != null && !StringUtils.isEmpty(appUser.getAppParentIds())){
            String []arr = appUser.getAppParentIds().split(",");
            appUser.setAppParentIds(arr[arr.length -1]);
        }
    }
    protected void changeAppParentIds(List<TAppUser> list){
        for (TAppUser appUser:list) {
            changeUserAppParentIds(appUser);
        }
    }
    /**
     * 导出玩家用户列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:user:export')")
    @Log(title = "玩家用户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TAppUser tAppUser) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if(!user.isAdmin()){
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")){
                tAppUser.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        List<TAppUser> list = tAppUserService.selectTAppUserList(tAppUser);
        changeAppParentIds(list);
        ExcelUtil<TAppUser> util = new ExcelUtil<TAppUser>(TAppUser.class);
        util.exportExcel(response, list, "玩家用户数据");
    }

    @PreAuthorize("@ss.hasPermi('bussiness:user:getListByPledge')")
    @GetMapping("/getListByPledge")
    public TableDataInfo getListByPledge(TAppUser tAppUser) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if(!user.isAdmin()){
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")){
                tAppUser.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        List<TAppUser> list = tAppUserService.getListByPledge(tAppUser);
        changeAppParentIds(list);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('bussiness:user:selectUnboundAppUser')")
    @GetMapping("/selectUnboundAppUser")
    public TableDataInfo selectUnboundAppUser(TAppUser tAppUser) {
        startPage();
        List<TAppUser> list = tAppUserService.selectUnboundAppUser(tAppUser);
        changeAppParentIds(list);
        return getDataTable(list);
    }

    /**
     * 获取玩家用户详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:user:query')")
    @GetMapping(value = "/{userId}")
    public AjaxResult getInfo(@PathVariable("userId") Long userId) {
        Map<String, Object> map = new HashMap<>();

        TAppUser user = tAppUserService.selectTAppUserByUserId(userId);
//        changeUserAppParentIds(user);
        TAppUserDetail one = userDetailService.getOne(new LambdaQueryWrapper<TAppUserDetail>().eq(TAppUserDetail::getUserId, userId));
        TAppAsset asset = new TAppAsset();
        asset.setUserId(userId);
        List<TAppAsset> tAppAssets = appAssetService.selectTAppAssetList(asset);
        map.put("user", user);
        map.put("deteil", one);
        map.put("asset", tAppAssets);
        return success(map);
    }


    /**
     * 修改玩家用户
     */
    @PreAuthorize("@ss.hasPermi('bussiness:user:add')")
    @Log(title = "玩家用户", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TAppUser tAppUser) {
        return toAjax(tAppUserService.addTAppUser(tAppUser));
    }

    /**
     * 修改玩家用户
     */
    @PreAuthorize("@ss.hasPermi('bussiness:user:edit')")
    @Log(title = "玩家用户", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TAppUser tAppUser) {
        return toAjax(tAppUserService.updateTAppUser(tAppUser));
    }

    /**
     * 删除玩家用户
     */
    @PreAuthorize("@ss.hasPermi('bussiness:user:remove')")
    @Log(title = "玩家用户", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds) {
        return toAjax(tAppUserService.deleteTAppUserByUserIds(userIds));
    }

    @PreAuthorize("@ss.hasPermi('bussiness:user:sendBous')")
    @Log(title = "赠送彩金", businessType = BusinessType.UPDATE)
    @PostMapping("/sendBous")
    public AjaxResult sendBonus(@RequestBody UserBonusVO userBounsVO) {   //获取操作后台用户

        String username = getUsername();
        userBounsVO.setCreateBy(username);
        appAssetService.sendBouns(userBounsVO);
        return success();
    }
    @PreAuthorize("@ss.hasPermi('bussiness:user:subAmount')")
    @Log(title = "人工上下分", businessType = BusinessType.UPDATE)
    @PostMapping("/subAmount")
    public AjaxResult subAmount(@RequestBody UserBonusVO userBounsVO) {   //获取操作后台用户
        String username = getUsername();
        userBounsVO.setCreateBy(username);
        int i = appAssetService.subAmount(userBounsVO);
        if(i==0){
            return success();
        }else {
            return error();
        }

    }

    @PreAuthorize("@ss.hasPermi('bussiness:user:realName')")
    @Log(title = "实名认证审核", businessType = BusinessType.UPDATE)
    @PostMapping("/realName")
    public AjaxResult realName(@RequestBody TAppUserDetail tAppUserDetail) {
        tAppUserService.realName(tAppUserDetail);
        //socket通知
        webSocketNotice.sendInfoAll(tWithdrawService,3);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('bussiness:user:realName')")
    @Log(title = "重置实名认证", businessType = BusinessType.UPDATE)
    @PostMapping("/reSetRealName")
    public AjaxResult reSetRealName(@RequestBody TAppUserDetail tAppUserDetail) {
        tAppUserService.reSetRealName(tAppUserDetail);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('bussiness:user:buff')")
    @Log(title = "包输包赢设置", businessType = BusinessType.UPDATE)
    @PostMapping("/buff")
    public AjaxResult buff(@RequestBody TAppUser tAppUser) {
        tAppUserService.updateTAppUser(tAppUser);
        return success();
    }
    @PreAuthorize("@ss.hasPermi('bussiness:user:updatePwd')")
    @Log(title = "重置登录密码", businessType = BusinessType.UPDATE)
    @PostMapping("/updateLoginPwd")
    public AjaxResult updateLoginPwd(@RequestBody TAppUser tAppUser) {
        tAppUser.setLoginPassword(SecurityUtils.encryptPassword(tAppUser.getLoginPassword()));
        tAppUserService.updateTAppUser(tAppUser);
        return success();
    }
    @PreAuthorize("@ss.hasPermi('bussiness:user:updatePwd')")
    @Log(title = "重置交易密码", businessType = BusinessType.UPDATE)
    @PostMapping("/updateTransPwd")
    public AjaxResult updateTransPwd(@RequestBody TAppUserDetail tAppUser) {
        TAppUserDetail tAppUserDetai = tAppUserService.selectUserDetailByUserId(tAppUser.getUserId());
        String userTardPwd = tAppUser.getUserTardPwd();
        if (!StringUtils.isEmpty(userTardPwd)) {
            tAppUserDetai.setUserTardPwd(SecurityUtils.encryptPassword(userTardPwd));
        }
        userDetailService.update(tAppUserDetai, new UpdateWrapper<TAppUserDetail>().eq("user_id", tAppUser.getUserId()));
        return success();
    }

    @PreAuthorize("@ss.hasPermi('bussiness:user:realName')")
    @Log(title = "重置交易密码", businessType = BusinessType.UPDATE)
    @PostMapping("/updateRealName")
    public AjaxResult updateRealName(@RequestBody TAppUserDetail tAppUser) {
        TAppUserDetail tAppUserDetai = tAppUserService.selectUserDetailByUserId(tAppUser.getUserId());

        userDetailService.update(tAppUserDetai, new UpdateWrapper<TAppUserDetail>().eq("user_id", tAppUser.getUserId()));
        return success();
    }


    @PreAuthorize("@ss.hasPermi('bussiness:user:updateUserAppIds')")
    @Log(title = "修改玩家用户上级代理", businessType = BusinessType.UPDATE)
    @PutMapping("/updateUserAppIds")
    public AjaxResult updateUserAppIds(Long appUserId, Long agentUserId) {
        return toAjax(tAppUserService.updateUserAppIds(appUserId,agentUserId));
    }

    @PreAuthorize("@ss.hasPermi('bussiness:user:updateBlackStatus')")
    @Log(title = "修改用户拉黑状态", businessType = BusinessType.UPDATE)
    @PutMapping("/updateBlackStatus")
    public AjaxResult updateBlackStatus (@RequestBody TAppUser tAppUser){
        return toAjax(tAppUserService.updateBlackStatus(tAppUser));
    }

}
