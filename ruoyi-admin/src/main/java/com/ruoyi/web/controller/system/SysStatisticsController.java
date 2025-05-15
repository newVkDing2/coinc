package com.ruoyi.web.controller.system;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.ISysStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 参数配置 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/statistics")
public class SysStatisticsController extends BaseController {

    @Autowired
    private ISysStatisticsService statisticsService;

    /**
     * 获取首页数据统计列表
     */
    @GetMapping("/dataList")
    public AjaxResult dataList() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if (user != null) {
            String userType = user.getUserType();
            String parentId = "";
            if (user.isAdmin() || ("0").equals(userType)) {
                parentId = null;
            } else {
                parentId = user.getUserId().toString();
            }
            return success(statisticsService.getDataList(parentId));
        }
        return error();
    }
}
