package com.ruoyi.web.controller.bussiness;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bussiness.domain.TAppAsset;
import com.ruoyi.bussiness.domain.TAppUser;
import com.ruoyi.bussiness.domain.TLoadProduct;
import com.ruoyi.bussiness.domain.setting.LoadSetting;
import com.ruoyi.bussiness.domain.setting.Setting;
import com.ruoyi.bussiness.service.*;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.enums.SettingEnum;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.bussiness.domain.TLoadOrder;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 贷款订单Controller
 * 
 * @author ruoyi
 * @date 2023-07-14
 */
@RestController
@RequestMapping("/bussiness/load/order")
public class TLoadOrderController extends BaseController
{
    @Autowired
    private ITLoadOrderService tLoadOrderService;
    @Resource
    private SettingService settingService;

    /**
     * 查询贷款订单列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:load/order:list')")
    @GetMapping("/list")
    public TableDataInfo list(TLoadOrder tLoadOrder)
    {
        startPage();
        List<TLoadOrder> list = tLoadOrderService.selectTLoadOrderList(tLoadOrder);
        return getDataTable(list);
    }

    /**
     * 导出贷款订单列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:load/order:export')")
    @Log(title = "贷款订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TLoadOrder tLoadOrder)
    {
        List<TLoadOrder> list = tLoadOrderService.selectTLoadOrderList(tLoadOrder);
        ExcelUtil<TLoadOrder> util = new ExcelUtil<TLoadOrder>(TLoadOrder.class);
        util.exportExcel(response, list, "贷款订单数据");
    }

    /**
     * 获取贷款订单详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:load/order:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tLoadOrderService.selectTLoadOrderById(id));
    }

    /**
     * 新增贷款订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:load/order:add')")
    @Log(title = "贷款订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TLoadOrder tLoadOrder)
    {
        return toAjax(tLoadOrderService.insertTLoadOrder(tLoadOrder));
    }

    /**
     * 修改贷款订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:load/order:edit')")
    @Log(title = "贷款订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TLoadOrder tLoadOrder)
    {
        return toAjax(tLoadOrderService.updateTLoadOrder(tLoadOrder));
    }

    /**
     * 删除贷款订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:load/order:remove')")
    @Log(title = "贷款订单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tLoadOrderService.deleteTLoadOrderByIds(ids));
    }

    /**
     * 借贷订单list
     * @param tLoadOrder
     * @return
     */
    @PreAuthorize("@ss.hasPermi('bussiness:loadOrder:orderList')")
    @GetMapping("/orderList")
    public TableDataInfo orderList(TLoadOrder tLoadOrder) {
        startPage();

        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if(!user.isAdmin()){
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")){
                tLoadOrder.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        Setting setting = settingService.get(SettingEnum.LOAD_SETTING.name());
        LoadSetting loadSetting = JSONUtil.toBean(setting.getSettingValue(), LoadSetting.class);
        BigDecimal overRwate = loadSetting.getOverdueRate();
        if(StringUtils.isNull(loadSetting.getOverdueRate())){
            overRwate= new BigDecimal("0.025");
        }
        List<TLoadOrder> list = tLoadOrderService.selectTLoadOrderList(tLoadOrder);
        for (TLoadOrder loadOrder1:list) {
            if(Objects.isNull(loadOrder1.getFinalRepayTime())){
                continue;
            }
            int enddays = DateUtils.daysBetween(loadOrder1.getFinalRepayTime(), new Date());
            //逾期
            if(enddays>0){
                if (loadOrder1.getStatus() == 1) {
                    loadOrder1.setStatus(4);
                    tLoadOrderService.updateTLoadOrder(loadOrder1);
                    loadOrder1.setLastInstets(loadOrder1.getDisburseAmount().multiply(new BigDecimal(enddays)).multiply(overRwate));
                    loadOrder1.setDays(enddays);
                }
            }
            if(loadOrder1.getStatus()==4){
                loadOrder1.setLastInstets(loadOrder1.getDisburseAmount().multiply(new BigDecimal(enddays)).multiply(overRwate));
                loadOrder1.setDays(enddays);
            }
        }
        return getDataTable(list);
    }

    /**
     * 订单审核通过
     * @param tLoadOrder
     * @return
     */
    @PreAuthorize("@ss.hasPermi('bussiness:loadOrder:passTLoadOrder')")
    @PostMapping("/passTLoadOrder")
    public AjaxResult passTLoadOrder(@RequestBody TLoadOrder tLoadOrder) {
        return tLoadOrderService.passTLoadOrder(tLoadOrder);
    }


    /**
     * 拒绝
     */
    @PreAuthorize("@ss.hasPermi('bussiness:loadOrder:refuseTLoadOrder')")
    @PostMapping("/refuseTLoadOrder")
    public AjaxResult refuseTLoadOrder(@RequestBody TLoadOrder reject) {
        reject.setStatus(2);
        return AjaxResult.success(tLoadOrderService.updateTLoadOrder(reject));
    }

    /**
     * 查看
     * @param id
     * @return
     */
    @GetMapping("/getTLoadOrder/{id}")
    public AjaxResult getTLoadOrder(@PathVariable("id") Long id) {
        TLoadOrder tLoadOrder = tLoadOrderService.selectTLoadOrderById(id);
        Setting setting = settingService.get(SettingEnum.LOAD_SETTING.name());
        LoadSetting loadSetting = JSONUtil.toBean(setting.getSettingValue(), LoadSetting.class);
        BigDecimal overRwate = loadSetting.getOverdueRate();
        if(StringUtils.isNull(loadSetting.getOverdueRate())){
            overRwate= new BigDecimal("0.025");
        }
        if(Objects.nonNull(tLoadOrder.getFinalRepayTime())) {
            int enddays = DateUtils.daysBetween(tLoadOrder.getFinalRepayTime(), new Date());
            //逾期
            if (enddays > 0) {
                if (tLoadOrder.getStatus() == 3) {
                    tLoadOrder.setStatus(2);
                    tLoadOrderService.updateTLoadOrder(tLoadOrder);
                }
                tLoadOrder.setLastInstets(tLoadOrder.getAmount().multiply(new BigDecimal(enddays)).multiply(overRwate));
            }
        }
        return success(tLoadOrder);
    }

    /**
     * 还款
     * @param id
     * @return
     */
    @PostMapping("/repayment")
    public AjaxResult repayment(Long id) {
        return toAjax(tLoadOrderService.repayment(id));
    }
}
