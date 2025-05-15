package com.ruoyi.web.controller.bussiness;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.bussiness.domain.DefiOrder;
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
import com.ruoyi.bussiness.service.IDefiOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * defi订单Controller
 * 
 * @author ruoyi
 * @date 2023-08-17
 */
@RestController
@RequestMapping("/bussiness/orderdefi")
public class DefiOrderController extends BaseController
{
    @Autowired
    private IDefiOrderService defiOrderService;

    /**
     * 查询defi订单列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:orderdefi:list')")
    @GetMapping("/list")
    public TableDataInfo list(DefiOrder defiOrder)
    {
        startPage();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if(!user.isAdmin()){
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")){
                defiOrder.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        List<DefiOrder> list = defiOrderService.selectDefiOrderList(defiOrder);
        return getDataTable(list);
    }

    /**
     * 导出defi订单列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:orderdefi:export')")
    @Log(title = "defi订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DefiOrder defiOrder)
    {
        List<DefiOrder> list = defiOrderService.selectDefiOrderList(defiOrder);
        ExcelUtil<DefiOrder> util = new ExcelUtil<DefiOrder>(DefiOrder.class);
        util.exportExcel(response, list, "defi订单数据");
    }

    /**
     * 获取defi订单详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:orderdefi:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(defiOrderService.selectDefiOrderById(id));
    }

    /**
     * 新增defi订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:orderdefi:add')")
    @Log(title = "defi订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DefiOrder defiOrder)
    {
        return toAjax(defiOrderService.insertDefiOrder(defiOrder));
    }

    /**
     * 修改defi订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:orderdefi:edit')")
    @Log(title = "defi订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DefiOrder defiOrder)
    {
        return toAjax(defiOrderService.updateDefiOrder(defiOrder));
    }

    /**
     * 删除defi订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:orderdefi:remove')")
    @Log(title = "defi订单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(defiOrderService.deleteDefiOrderByIds(ids));
    }
}
