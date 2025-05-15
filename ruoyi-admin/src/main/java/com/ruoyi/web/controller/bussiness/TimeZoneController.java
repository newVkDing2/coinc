package com.ruoyi.web.controller.bussiness;

import com.ruoyi.common.core.domain.entity.TimeZone;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author:michael
 * @createDate: 2022/9/19 16:29
 */
@RestController
@RequestMapping("/bussiness/timezone")
public class TimeZoneController extends BaseController {

    @GetMapping("/list")
    public AjaxResult list() {

        List<TimeZone> list = DateUtils.getZoneTimeList();
        for (TimeZone t : list) {
            String offSet = t.getOffSet();
            t.setOffSetValue(offSet.replaceAll(":",".").replaceAll("\\+0","")
                    .replaceAll("\\+","").replaceAll("\\-0","").replaceAll(".00",""));
            t.setOffSet("GMT" + t.getOffSet());
        }
        return AjaxResult.success(list);
    }
}
