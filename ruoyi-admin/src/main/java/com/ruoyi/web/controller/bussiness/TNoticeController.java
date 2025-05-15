package com.ruoyi.web.controller.bussiness;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.enums.HttpMethod;
import com.ruoyi.common.enums.NoticeTypeEnum;
import com.ruoyi.common.enums.OptionRulesEnum;
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
import com.ruoyi.bussiness.domain.TNotice;
import com.ruoyi.bussiness.service.ITNoticeService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 通知公告Controller
 * 
 * @author ruoyi
 * @date 2023-07-20
 */
@RestController
@RequestMapping("/bussiness/notice")
public class TNoticeController extends BaseController
{
    @Autowired
    private ITNoticeService tNoticeService;

    /**
     * 查询通知公告列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:notice:list')")
    @GetMapping("/list")
    public TableDataInfo list(TNotice tNotice)
    {
        tNotice.setNoticeType(NoticeTypeEnum.valueOf(tNotice.getKey()).getCode());
        if (StringUtils.isNotBlank(tNotice.getModelKey())){
            tNotice.setModelType(NoticeTypeEnum.ChildrenEnum.valueOf(tNotice.getModelKey()).getCode());
        }
        startPage();
        List<TNotice> list = tNoticeService.selectTNoticeList(tNotice);
        NoticeTypeEnum[] typeEnumList = NoticeTypeEnum.values();
        NoticeTypeEnum.ChildrenEnum[] childrenEnumList = NoticeTypeEnum.ChildrenEnum.values();
        for (TNotice notice:list) {
            if (notice.getModelType()!=null){
                for (int i = 0; i < childrenEnumList.length; i++) {
                    if (notice.getNoticeType().equals(childrenEnumList[i].getPrent().getCode()) && childrenEnumList[i].getCode().equals(notice.getModelType())){
                        notice.setModelType(childrenEnumList[i].getValue());
                        notice.setModelKey(childrenEnumList[i].name());
                    }
                }
            }
            for (NoticeTypeEnum typeEnum:typeEnumList) {
                if (typeEnum.getCode().equals(notice.getNoticeType())){
                    notice.setNoticeType(typeEnum.getValue());
                    notice.setKey(typeEnum.name());
                }
            }
        }
        return getDataTable(list);
    }

    /**
     * 导出通知公告列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:notice:export')")
    @Log(title = "通知公告", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TNotice tNotice)
    {
        List<TNotice> list = tNoticeService.selectTNoticeList(tNotice);
        ExcelUtil<TNotice> util = new ExcelUtil<TNotice>(TNotice.class);
        util.exportExcel(response, list, "通知公告数据");
    }

    /**
     * 获取通知公告详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@PathVariable("noticeId") Long noticeId)
    {
        TNotice tNotice = tNoticeService.selectTNoticeByNoticeId(noticeId);
        NoticeTypeEnum[] typeEnumList = NoticeTypeEnum.values();
        NoticeTypeEnum.ChildrenEnum[] childrenEnumList = NoticeTypeEnum.ChildrenEnum.values();
        if (tNotice.getModelType()!=null){
            for (int i = 0; i < childrenEnumList.length; i++) {
                if (tNotice.getNoticeType().equals(childrenEnumList[i].getPrent().getCode()) && childrenEnumList[i].getCode().equals(tNotice.getModelType())){
//                    tNotice.setModelType(childrenEnumList[i].getValue());
                    tNotice.setModelKey(childrenEnumList[i].name());
                }
            }
        }
        for (NoticeTypeEnum typeEnum:typeEnumList) {
            if (typeEnum.getCode().equals(tNotice.getNoticeType())){
//                tNotice.setNoticeType(typeEnum.getValue());
                tNotice.setKey(typeEnum.name());
            }
        }
        return success(tNotice);
    }

    /**
     * 新增通知公告
     */
    @PreAuthorize("@ss.hasPermi('bussiness:notice:add')")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TNotice tNotice)
    {
        return toAjax(tNoticeService.insertTNotice(tNotice));
    }

    /**
     * 修改通知公告
     */
    @PreAuthorize("@ss.hasPermi('bussiness:notice:edit')")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TNotice tNotice)
    {
        return toAjax(tNoticeService.updateTNotice(tNotice));
    }

    /**
     * 删除通知公告
     */
    @PreAuthorize("@ss.hasPermi('bussiness:notice:remove')")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
	@DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds)
    {
        return toAjax(tNoticeService.deleteTNoticeByNoticeIds(noticeIds));
    }

    /**
     * 获取公告管理枚举list
     * @return
     */
    @PreAuthorize("@ss.hasPermi('bussiness:notice:NoticeTypeList')")
    @GetMapping("/noticeTypeList")
    public TableDataInfo noticeTypeList()
    {
        return getDataTable(NoticeTypeEnum.getEnum());
    }
}
