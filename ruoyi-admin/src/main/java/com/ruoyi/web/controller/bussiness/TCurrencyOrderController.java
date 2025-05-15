package com.ruoyi.web.controller.bussiness;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

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
import com.ruoyi.bussiness.domain.TCurrencyOrder;
import com.ruoyi.bussiness.service.ITCurrencyOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 币币交易订单Controller
 * 
 * @author ruoyi
 * @date 2023-07-25
 */
@RestController
@RequestMapping("/bussiness/currency/order")
public class TCurrencyOrderController extends BaseController
{
    @Autowired
    private ITCurrencyOrderService tCurrencyOrderService;

    /**
     * 查询币币交易订单列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:currency:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(TCurrencyOrder tCurrencyOrder)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if(!user.isAdmin()){
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")){
                tCurrencyOrder.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        startPage();
        List<TCurrencyOrder> list = tCurrencyOrderService.selectTCurrencyOrderList(tCurrencyOrder);
        return getDataTable(list);
    }

    /**
     * 导出币币交易订单列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:currency:order:export')")
    @Log(title = "币币交易订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TCurrencyOrder tCurrencyOrder)
    {
        List<TCurrencyOrder> list = tCurrencyOrderService.selectTCurrencyOrderList(tCurrencyOrder);
        ExcelUtil<TCurrencyOrder> util = new ExcelUtil<TCurrencyOrder>(TCurrencyOrder.class);
        util.exportExcel(response, list, "币币交易订单数据");
    }

    /**
     * 获取币币交易订单详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:currency:order:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tCurrencyOrderService.selectTCurrencyOrderById(id));
    }

    /**
     * 新增币币交易订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:currency:order:add')")
    @Log(title = "币币交易订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TCurrencyOrder tCurrencyOrder)
    {
        return toAjax(tCurrencyOrderService.insertTCurrencyOrder(tCurrencyOrder));
    }

    /**
     * 修改币币交易订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:currency:order:edit')")
    @Log(title = "币币交易订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TCurrencyOrder tCurrencyOrder)
    {
        return toAjax(tCurrencyOrderService.updateTCurrencyOrder(tCurrencyOrder));
    }

    /**
     * 删除币币交易订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:currency:order:remove')")
    @Log(title = "币币交易订单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tCurrencyOrderService.deleteTCurrencyOrderByIds(ids));
    }
}
