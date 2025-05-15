package com.ruoyi.web.controller.bussiness;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.bussiness.domain.TAppUser;
import com.ruoyi.bussiness.service.ITAppUserService;
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
import com.ruoyi.bussiness.domain.TAppMail;
import com.ruoyi.bussiness.service.ITAppMailService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 1v1站内信Controller
 * 
 * @author ruoyi
 * @date 2023-07-18
 */
@RestController
@RequestMapping("/bussiness/mail")
public class TAppMailController extends BaseController
{
    @Autowired
    private ITAppMailService tAppMailService;
    @Resource
    private ITAppUserService tAppUserService;

    /**
     * 查询1v1站内信列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:mail:list')")
    @GetMapping("/list")
    public TableDataInfo list(TAppMail tAppMail)
    {
        startPage();
        List<TAppMail> list = tAppMailService.selectTAppMailList(tAppMail);
        return getDataTable(list);
    }



    /**
     * 新增1v1站内信
     */
    @PreAuthorize("@ss.hasPermi('bussiness:mail:add')")
    @Log(title = "1v1站内信", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TAppMail tAppMail)
    {
        String msg = "";
        if (tAppMail.getType().equals("1")){
            String[] userIds = tAppMail.getUserIds().split(",");
            for (int i = 0; i < userIds.length; i++) {
                TAppUser appUser = tAppUserService.getById(userIds[i]);
                if (Objects.isNull(appUser)){
                    msg+=userIds[i]+",";
                }
            }
        }
        if (StringUtils.isNotBlank(msg)){
            return AjaxResult.error("暂无此用户"+msg.substring(0,msg.length()-1));
        }
        return toAjax(tAppMailService.insertTAppMail(tAppMail));
    }


    /**
     * 删除1v1站内信
     */
    @PreAuthorize("@ss.hasPermi('bussiness:mail:remove')")
    @Log(title = "1v1站内信", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long ids)
    {
        return toAjax(tAppMailService.deleteTAppMailById(ids));
    }
}
