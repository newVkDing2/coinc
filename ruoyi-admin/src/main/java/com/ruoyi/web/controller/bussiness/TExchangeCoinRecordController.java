package com.ruoyi.web.controller.bussiness;

import com.ruoyi.bussiness.domain.TExchangeCoinRecord;
import com.ruoyi.bussiness.service.ITExchangeCoinRecordService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 币种兑换记录Controller
 * 
 * @author ruoyi
 * @date 2023-07-07
 */
@RestController
@RequestMapping("/bussiness/texchange")
public class TExchangeCoinRecordController extends BaseController
{
    @Autowired
    private ITExchangeCoinRecordService tExchangeCoinRecordService;

    /**
     * 查询币种兑换记录列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:texchange:list')")
    @GetMapping("/list")
    public TableDataInfo list(TExchangeCoinRecord tExchangeCoinRecord)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if(!user.isAdmin()){
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")){
                tExchangeCoinRecord.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        startPage();
        List<TExchangeCoinRecord> list = tExchangeCoinRecordService.selectTExchangeCoinRecordList(tExchangeCoinRecord);
        return getDataTable(list);
    }
}
