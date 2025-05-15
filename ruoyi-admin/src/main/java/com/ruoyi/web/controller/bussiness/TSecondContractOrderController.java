package com.ruoyi.web.controller.bussiness;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.bussiness.domain.TSecondContractOrder;
import com.ruoyi.bussiness.service.ITSecondContractOrderService;
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
 * 秒合约订单Controller
 * 
 * @author ruoyi
 * @date 2023-07-13
 */
@RestController
@RequestMapping("/bussiness/secondContractOrder")
public class TSecondContractOrderController extends BaseController
{
    @Autowired
    private ITSecondContractOrderService tSecondContractOrderService;

    /**
     * 查询秒合约订单列表
     */
    @PreAuthorize("@ss.hasPermi('secondContractOrder:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(TSecondContractOrder tSecondContractOrder)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if(!user.isAdmin()){
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")){
                tSecondContractOrder.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        startPage();
        List<TSecondContractOrder> list = tSecondContractOrderService.selectTSecondContractOrderList(tSecondContractOrder);
        return getDataTable(list);
    }

    /**
     * 导出秒合约订单列表
     */
    @PreAuthorize("@ss.hasPermi('secondContractOrder:order:export')")
    @Log(title = "秒合约订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TSecondContractOrder tSecondContractOrder)
    {
        List<TSecondContractOrder> list = tSecondContractOrderService.selectTSecondContractOrderList(tSecondContractOrder);
        ExcelUtil<TSecondContractOrder> util = new ExcelUtil<TSecondContractOrder>(TSecondContractOrder.class);
        util.exportExcel(response, list, "秒合约订单数据");
    }

    /**
     * 获取秒合约订单详细信息
     */
    @PreAuthorize("@ss.hasPermi('secondContractOrder:order:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tSecondContractOrderService.selectTSecondContractOrderById(id));
    }

    /**
     * 新增秒合约订单
     */
    @PreAuthorize("@ss.hasPermi('secondContractOrder:order:add')")
    @Log(title = "秒合约订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TSecondContractOrder tSecondContractOrder)
    {
        return toAjax(tSecondContractOrderService.insertTSecondContractOrder(tSecondContractOrder));
    }

        /**
     * 修改秒合约订单
     */
    @PreAuthorize("@ss.hasPermi('secondContractOrder:order:edit')")
    @Log(title = "秒合约订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TSecondContractOrder tSecondContractOrder)
    {
        return toAjax(tSecondContractOrderService.updateTSecondContractOrder(tSecondContractOrder));
    }

    /**
     * 删除秒合约订单
     */
    @PreAuthorize("@ss.hasPermi('secondContractOrder:order:remove')")
    @Log(title = "秒合约订单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tSecondContractOrderService.deleteTSecondContractOrderByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('secondContractOrder:order:edit')")
    @Log(title = "秒合约订单", businessType = BusinessType.UPDATE)
    @PostMapping("/buff")
    public AjaxResult buff(@RequestBody TSecondContractOrder tSecondContractOrder)
    {
        return toAjax(tSecondContractOrderService.updateTSecondContractOrder(tSecondContractOrder));
    }

}
