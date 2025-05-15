package com.ruoyi.web.controller.bussiness;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bussiness.domain.TNftOrder;
import com.ruoyi.bussiness.domain.TNftSeries;
import com.ruoyi.bussiness.service.ITNftOrderService;
import com.ruoyi.bussiness.service.ITNftSeriesService;
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
import com.ruoyi.bussiness.domain.TNftProduct;
import com.ruoyi.bussiness.service.ITNftProductService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * nft详情Controller
 * 
 * @author ruoyi
 * @date 2023-09-01
 */
@RestController
@RequestMapping("/bussiness/nftProduct")
public class TNftProductController extends BaseController
{
    @Autowired
    private ITNftProductService tNftProductService;
    @Resource
    private ITNftSeriesService tNftSeriesService;
    @Resource
    private ITNftOrderService tNftOrderService;

    /**
     * 查询nft详情列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:nftProduct:list')")
    @GetMapping("/list")
    public TableDataInfo list(TNftProduct tNftProduct)
    {
        startPage();
        List<TNftProduct> list = tNftProductService.selectTNftProductList(tNftProduct);
        return getDataTable(list);
    }

    /**
     * 获取nft详情详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:nftProduct:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tNftProductService.selectTNftProductById(id));
    }

    /**
     * 新增nft详情
     */
    @PreAuthorize("@ss.hasPermi('bussiness:nftProduct:add')")
    @Log(title = "nft详情", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TNftProduct tNftProduct)
    {
        if (tNftProduct.getSeriesId()!=null){
            TNftSeries series = tNftSeriesService.getById(tNftProduct.getSeriesId());
            tNftProduct.setChainType(series.getChainType());
        }else{
            return AjaxResult.error("合集不能为空");
        }
        return toAjax(tNftProductService.insertTNftProduct(tNftProduct));
    }

    /**
     * 修改nft详情
     */
    @PreAuthorize("@ss.hasPermi('bussiness:nftProduct:edit')")
    @Log(title = "nft详情", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TNftProduct tNftProduct)
    {
        TNftProduct oldProduct = tNftProductService.getById(tNftProduct.getId());
        List<TNftOrder> list = tNftOrderService.list(new LambdaQueryWrapper<TNftOrder>().eq(TNftOrder::getProductId, tNftProduct.getId()));
        if ("2".equals(oldProduct.getStatus()) || CollectionUtils.isEmpty(list)){
            return AjaxResult.error("该藏品已上架/正在交易，不能修改!");
        }
        return toAjax(tNftProductService.updateTNftProduct(tNftProduct));
    }

    @PreAuthorize("@ss.hasPermi('bussiness:nftProduct:upOrDown')")
    @Log(title = "NFT藏品上下架", businessType = BusinessType.UPDATE)
    @PostMapping("/upOrDownPro")
    public AjaxResult upOrDownPro(@RequestBody TNftProduct tNftProduct)
    {
        return toAjax(tNftProductService.updateTNftProduct(tNftProduct));
    }

    /**
     * 删除nft详情
     */
    @PreAuthorize("@ss.hasPermi('bussiness:nftProduct:remove')")
    @Log(title = "nft详情", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long id)
    {
        TNftProduct oldProduct = tNftProductService.getById(id);
        List<TNftOrder> list = tNftOrderService.list(new LambdaQueryWrapper<TNftOrder>().eq(TNftOrder::getProductId, id));
        if ("2".equals(oldProduct.getStatus()) || CollectionUtils.isEmpty(list)){
            return AjaxResult.error("该藏品已上架/正在交易，不能删除!");
        }
        return toAjax(tNftProductService.deleteTNftProductById(id));
    }
}
