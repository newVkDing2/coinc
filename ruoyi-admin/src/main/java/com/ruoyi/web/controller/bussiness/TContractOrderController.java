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
import com.ruoyi.bussiness.domain.TContractOrder;
import com.ruoyi.bussiness.service.ITContractOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * U本位委托Controller
 * 
 * @author michael
 * @date 2023-07-20
 */
@RestController
@RequestMapping("/bussiness/contractOrder")
public class TContractOrderController extends BaseController
{
    @Autowired
    private ITContractOrderService tContractOrderService;

    /**
     * 查询U本位委托列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:contractOrder:list')")
    @GetMapping("/list")
    public TableDataInfo list(TContractOrder tContractOrder)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if(!user.isAdmin()){
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")){
                tContractOrder.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        startPage();
        List<TContractOrder> list = tContractOrderService.selectTContractOrderList(tContractOrder);
        return getDataTable(list);
    }

    /**
     * 导出U本位委托列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:contractOrder:export')")
    @Log(title = "U本位委托", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TContractOrder tContractOrder)
    {
        List<TContractOrder> list = tContractOrderService.selectTContractOrderList(tContractOrder);
        ExcelUtil<TContractOrder> util = new ExcelUtil<TContractOrder>(TContractOrder.class);
        util.exportExcel(response, list, "U本位委托数据");
    }

    /**
     * 获取U本位委托详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:contractOrder:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tContractOrderService.selectTContractOrderById(id));
    }

    /**
     * 新增U本位委托
     */
    @PreAuthorize("@ss.hasPermi('bussiness:contractOrder:add')")
    @Log(title = "U本位委托", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TContractOrder tContractOrder)
    {
        return toAjax(tContractOrderService.insertTContractOrder(tContractOrder));
    }

    /**
     * 修改U本位委托
     */
    @PreAuthorize("@ss.hasPermi('bussiness:contractOrder:edit')")
    @Log(title = "U本位委托", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TContractOrder tContractOrder)
    {
        return toAjax(tContractOrderService.updateTContractOrder(tContractOrder));
    }

    /**
     * 删除U本位委托
     */
    @PreAuthorize("@ss.hasPermi('bussiness:contractOrder:remove')")
    @Log(title = "U本位委托", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tContractOrderService.deleteTContractOrderByIds(ids));
    }
}
