package com.ruoyi.web.controller.bussiness;

import com.ruoyi.bussiness.domain.TAppRecharge;
import com.ruoyi.bussiness.service.ITAppRechargeService;
import com.ruoyi.bussiness.service.ITAppUserService;
import com.ruoyi.bussiness.service.ITWithdrawService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.OrderBySetting;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.socket.socketserver.WebSocketNotice;
import com.ruoyi.system.service.ISysConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * 用户充值Controller
 * 
 * @author ruoyi
 * @date 2023-07-04
 */
@RestController
@RequestMapping("/bussiness/recharge")
public class TAppRechargeController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(TAppRechargeController.class);
    @Resource
    private ITAppRechargeService tAppRechargeService;

    @Resource
    private ITWithdrawService tWithdrawService;
    @Resource
    private WebSocketNotice webSocketNotice;
    /**
     * 获取用户充值详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:recharge:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        TAppRecharge appRecharge = tAppRechargeService.getById(id);
        if(Objects.nonNull(appRecharge)){
            Date creatTime=appRecharge.getCreateTime();
            Map<String,Object> params=new HashMap<>();
            params.put("date",Objects.nonNull(creatTime)?creatTime.getTime():0L);
            appRecharge.setParams(params);
        }
        return AjaxResult.success(appRecharge);
    }

    /**
     * 用户充值列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:recharge:list')")
    @GetMapping("/list")
    public TableDataInfo list(TAppRecharge tAppRecharge) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        String orderBy = pageDomain.getOrderBy();
        if (StringUtils.isNotBlank(orderBy)){
            OrderBySetting.value="a.";
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if(!user.isAdmin()){
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")){
                tAppRecharge.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        startPage();
        return getDataTable(tAppRechargeService.selectRechargeList(tAppRecharge));
    }

    @Log(title = "充值信息", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('bussiness:recharge:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, TAppRecharge tAppRecharge)
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        String orderBy = pageDomain.getOrderBy();
        if (StringUtils.isNotBlank(orderBy)){
            OrderBySetting.value="a.";
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if(!user.isAdmin()){
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")){
                tAppRecharge.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        List<TAppRecharge> list = tAppRechargeService.selectRechargeList(tAppRecharge);
        ExcelUtil<TAppRecharge> util = new ExcelUtil<TAppRecharge>(TAppRecharge.class);
        util.exportExcel(response, list, "用户数据");
    }

    /**
     * 用户充值审核
     */
    @RepeatSubmit(interval = 5000, message = "请求过于频繁")
    @PreAuthorize("@ss.hasPermi('bussiness:recharge:passOrder')")
    @PostMapping("/passOrder")
    public AjaxResult passOrder(@RequestBody TAppRecharge tAppRecharge) {
        String msg = tAppRechargeService.passOrder(tAppRecharge);
        if (StringUtils.isNotBlank(msg)){
            return  AjaxResult.error(msg);
        }
       //socket通知
        webSocketNotice.sendInfoAll(tWithdrawService,2);
        return AjaxResult.success();
    }

    /**
     * 用户充值审核
     */
    @PreAuthorize("@ss.hasPermi('bussiness:recharge:failedOrder')")
    @PostMapping("/failedOrder")
    public AjaxResult failedOrder(@RequestBody TAppRecharge tAppRecharge) {
        String msg = tAppRechargeService.failedOrder(tAppRecharge);
        if (StringUtils.isNotBlank(msg)){
            AjaxResult.error(msg);
        }
        //socket通知
        webSocketNotice.sendInfoAll(tWithdrawService,2);
        return AjaxResult.success();
    }

    /**
     * 用户充值审核
     */
    @PreAuthorize("@ss.hasPermi('bussiness:recharge:list')")
    @PostMapping("/getAllRecharge")
    public AjaxResult getAllRecharge(Integer type) {
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
        return AjaxResult.success(tAppRechargeService.getAllRecharge(parentId,type));
    }
}
