package com.ruoyi.web.controller.bussiness;

import com.ruoyi.bussiness.domain.TAppWalletRecord;
import com.ruoyi.bussiness.domain.vo.AgencyAppUserDataVo;
import com.ruoyi.bussiness.domain.vo.AgencyDataVo;
import com.ruoyi.bussiness.domain.vo.DailyDataVO;
import com.ruoyi.bussiness.domain.vo.UserDataVO;
import com.ruoyi.bussiness.service.ITAppWalletRecordService;
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
 * 数据源Controller
 * 
 * @author ruoyi
 * @date 2023-07-10
 */
@RestController
@RequestMapping("/bussiness/userStatistics")
public class UserStatisticsController extends BaseController
{
    @Autowired
    private ITAppWalletRecordService appWalletRecordService;

    /**
     * 查询数据源列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:userStatistics:list')")
    @GetMapping("/list")
    public TableDataInfo list(TAppWalletRecord appWalletRecord)
    {
        startPage();
        List<UserDataVO> list = appWalletRecordService.selectUserDataList(appWalletRecord);
        return getDataTable(list);
    }

    /**
     * 查询代理数据源列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:userStatistics:agencyList')")
    @GetMapping("/agencyList")
    public TableDataInfo agencyList(TAppWalletRecord appWalletRecord)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if(!user.isAdmin()){
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")){
                appWalletRecord.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        startPage();
        List<AgencyDataVo> list = appWalletRecordService.selectAgencyList(appWalletRecord);
        return getDataTable(list);
    }


    /**
     * 查询代理下级用户数据源列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:userStatistics:agencyAppUserList')")
    @GetMapping("/agencyAppUserList")
    public TableDataInfo agencyAppUserList(TAppWalletRecord appWalletRecord)
    {
        startPage();
        List<AgencyAppUserDataVo> list = appWalletRecordService.selectAgencyAppUserList(appWalletRecord);
        return getDataTable(list);
    }

    /**
     * 查询数据源列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:userStatistics:dailyData')")
    @GetMapping("/dailyData")
    public TableDataInfo dailyData(TAppWalletRecord appWalletRecord)
    {
        startPage();
        List<DailyDataVO> list = appWalletRecordService.dailyData(appWalletRecord);
        return getDataTable(list);
    }


}
