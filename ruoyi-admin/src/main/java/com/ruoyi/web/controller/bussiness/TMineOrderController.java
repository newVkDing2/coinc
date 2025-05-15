package com.ruoyi.web.controller.bussiness;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.bussiness.domain.TMineOrder;
import com.ruoyi.bussiness.service.ITMineFinancialService;
import com.ruoyi.bussiness.service.ITMineOrderService;
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
 * 理财订单Controller
 * 
 * @author ruoyi
 * @date 2023-07-17
 */
@RestController
@RequestMapping("/bussiness/order")
public class TMineOrderController extends BaseController
{
    @Autowired
    private ITMineOrderService tMineOrderService;
    @Resource
    private ITMineFinancialService tMineFinancialService;

    /**
     * 查询理财订单列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(TMineOrder tMineOrder)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if(!user.isAdmin()){
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")){
                tMineOrder.setAdminUserIds(String.valueOf(user.getUserId()));
            }
        }
        startPage();
        List<TMineOrder> list = tMineOrderService.selectTMineOrderList(tMineOrder);
        return getDataTable(list);
    }

    /**
     * 导出理财订单列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:order:export')")
    @Log(title = "理财订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TMineOrder tMineOrder)
    {
        List<TMineOrder> list = tMineOrderService.selectTMineOrderList(tMineOrder);
        ExcelUtil<TMineOrder> util = new ExcelUtil<TMineOrder>(TMineOrder.class);
        util.exportExcel(response, list, "理财订单数据");
    }

    /**
     * 获取理财订单详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:order:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tMineOrderService.selectTMineOrderById(id));
    }

    /**
     * 新增理财订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:order:add')")
    @Log(title = "理财订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TMineOrder tMineOrder)
    {
        return toAjax(tMineOrderService.insertTMineOrder(tMineOrder));
    }

    /**
     * 修改理财订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:order:edit')")
    @Log(title = "理财订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TMineOrder tMineOrder)
    {
        return toAjax(tMineOrderService.updateTMineOrder(tMineOrder));
    }

    /**
     * 删除理财订单
     */
    @PreAuthorize("@ss.hasPermi('bussiness:order:remove')")
    @Log(title = "理财订单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tMineOrderService.deleteTMineOrderByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('bussiness:order:reCall')")
    @Log(title = "理财赎回", businessType = BusinessType.UPDATE)
    @PutMapping("/reCall")
    public AjaxResult reCall(String id) {
        String msg = tMineFinancialService.reCall(id);
        if(StringUtils.isNotBlank(msg)){
            return AjaxResult.error(msg);
        }
        return  AjaxResult.success();
    }
}
