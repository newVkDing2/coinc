package com.ruoyi.web.controller.bussiness;

import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.ruoyi.bussiness.domain.TUserBank;
import com.ruoyi.bussiness.service.ITUserBankService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 银行卡Controller
 * 
 * @author ruoyi
 * @date 2023-08-21
 */
@RestController
@RequestMapping("/bussiness/userBank")
public class TUserBankController extends BaseController
{
    @Autowired
    private ITUserBankService tUserBankService;

    /**
     * 查询银行卡列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:userBank:list')")
    @GetMapping("/list")
    public TableDataInfo list(TUserBank tUserBank)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if(!user.isAdmin()){
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")){
                tUserBank.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        startPage();
        List<TUserBank> list = tUserBankService.selectTUserBankList(tUserBank);
        return getDataTable(list);
    }

    /**
     * 获取银行卡详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:userBank:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tUserBankService.selectTUserBankById(id));
    }

    /**
     * 修改银行卡
     */
    @PreAuthorize("@ss.hasPermi('bussiness:userBank:edit')")
    @Log(title = "银行卡", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TUserBank tUserBank)
    {
        TUserBank oldBack = tUserBankService.getOne(new LambdaQueryWrapper<TUserBank>().eq(TUserBank::getCardNumber, tUserBank.getCardNumber()));
        if (Objects.nonNull(oldBack) && oldBack.getId()!=tUserBank.getId()){
            return AjaxResult.error(tUserBank.getCardNumber()+"该银行卡已经存在");
        }
        return toAjax(tUserBankService.updateTUserBank(tUserBank));
    }

    /**
     * 删除银行卡
     */
    @PreAuthorize("@ss.hasPermi('bussiness:userBank:remove')")
    @Log(title = "银行卡", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tUserBankService.deleteTUserBankByIds(ids));
    }
}
