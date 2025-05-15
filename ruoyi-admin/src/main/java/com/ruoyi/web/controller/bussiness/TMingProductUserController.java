package com.ruoyi.web.controller.bussiness;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.ruoyi.bussiness.domain.TMingProductUser;
import com.ruoyi.bussiness.service.ITMingProductUserService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 用户购买质押限制Controller
 * 
 * @author ruoyi
 * @date 2023-10-11
 */
@RestController
@RequestMapping("/bussiness/productUser")
public class TMingProductUserController extends BaseController
{
    @Autowired
    private ITMingProductUserService tMingProductUserService;

    /**
     * 查询用户购买质押限制列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:productUser:list')")
    @GetMapping("/list")
    public TableDataInfo list(TMingProductUser tMingProductUser)
    {
        startPage();
        List<TMingProductUser> list = tMingProductUserService.selectTMingProductUserList(tMingProductUser);
        return getDataTable(list);
    }

    /**
     * 获取用户购买质押限制详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:productUser:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tMingProductUserService.selectTMingProductUserById(id));
    }

    /**
     * 新增用户购买质押限制
     */
    @PreAuthorize("@ss.hasPermi('bussiness:productUser:add')")
    @Log(title = "用户购买质押限制", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TMingProductUser tMingProductUser)
    {
        return toAjax(tMingProductUserService.insertTMingProductUser(tMingProductUser));
    }

    /**
     * 修改用户购买质押限制
     */
    @PreAuthorize("@ss.hasPermi('bussiness:productUser:edit')")
    @Log(title = "用户购买质押限制", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TMingProductUser tMingProductUser)
    {
        return toAjax(tMingProductUserService.updateTMingProductUser(tMingProductUser));
    }

    /**
     * 删除用户购买质押限制
     */
    @PreAuthorize("@ss.hasPermi('bussiness:productUser:remove')")
    @Log(title = "用户购买质押限制", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tMingProductUserService.deleteTMingProductUserByIds(ids));
    }
}
