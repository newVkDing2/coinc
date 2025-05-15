package com.ruoyi.web.controller.bussiness;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bussiness.domain.TNftProduct;
import com.ruoyi.bussiness.service.ITNftProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
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
import com.ruoyi.bussiness.domain.TNftSeries;
import com.ruoyi.bussiness.service.ITNftSeriesService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * nft合计Controller
 * 
 * @author ruoyi
 * @date 2023-09-01
 */
@RestController
@RequestMapping("/bussiness/series")
public class TNftSeriesController extends BaseController
{
    @Autowired
    private ITNftSeriesService tNftSeriesService;
    @Resource
    private ITNftProductService tNftProductService;

    /**
     * 查询nft合计列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:series:list')")
    @GetMapping("/list")
    public TableDataInfo list(TNftSeries tNftSeries)
    {
        startPage();
        List<TNftSeries> list = tNftSeriesService.selectTNftSeriesList(tNftSeries);
        return getDataTable(list);
    }

    @PostMapping("/addProSeries")
    public AjaxResult addProSeries()
    {
        List<TNftSeries> list = tNftSeriesService.selectTNftSeriesList(new TNftSeries());
        return AjaxResult.success(list);
    }
    /**
     * 获取nft合计详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:series:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tNftSeriesService.selectTNftSeriesById(id));
    }

    /**
     * 新增nft合计
     */
    @PreAuthorize("@ss.hasPermi('bussiness:series:add')")
    @Log(title = "nft合计", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TNftSeries tNftSeries)
    {
        return toAjax(tNftSeriesService.insertTNftSeries(tNftSeries));
    }

    /**
     * 修改nft合计
     */
    @PreAuthorize("@ss.hasPermi('bussiness:series:edit')")
    @Log(title = "nft合计", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TNftSeries tNftSeries)
    {

        List<TNftProduct> list = tNftProductService.list(new LambdaQueryWrapper<TNftProduct>().eq(TNftProduct::getSeriesId, tNftSeries.getId()));
        if (!CollectionUtils.isEmpty(list)){
            return AjaxResult.error("该合集已被使用，不能修改");
        }
        return toAjax(tNftSeriesService.updateTNftSeries(tNftSeries));
    }

    /**
     * 删除nft合计
     */
    @PreAuthorize("@ss.hasPermi('bussiness:series:remove')")
    @Log(title = "nft合计", businessType = BusinessType.DELETE)
	@DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        List<TNftProduct> list = tNftProductService.list(new LambdaQueryWrapper<TNftProduct>().eq(TNftProduct::getSeriesId, id));
        if (!CollectionUtils.isEmpty(list)){
            return AjaxResult.error("该合集已被使用，不能删除");
        }
        return toAjax(tNftSeriesService.deleteTNftSeriesById(id));
    }
}
