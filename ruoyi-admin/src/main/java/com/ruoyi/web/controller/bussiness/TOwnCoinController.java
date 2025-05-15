package com.ruoyi.web.controller.bussiness;

import java.util.List;
import java.util.Objects;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bussiness.domain.KlineSymbol;
import com.ruoyi.bussiness.domain.TOwnCoinSubscribeOrder;
import com.ruoyi.bussiness.domain.TSpontaneousCoin;
import com.ruoyi.bussiness.service.IKlineSymbolService;
import com.ruoyi.bussiness.service.ITSpontaneousCoinService;
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
import com.ruoyi.bussiness.domain.TOwnCoin;
import com.ruoyi.bussiness.service.ITOwnCoinService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 发币Controller
 * 
 * @author ruoyi
 * @date 2023-09-18
 */
@RestController
@RequestMapping("/bussiness/ownCoin")
public class TOwnCoinController extends BaseController
{
    @Autowired
    private ITOwnCoinService tOwnCoinService;
    @Resource
    private ITSpontaneousCoinService tSpontaneousCoinService;
    @Resource
    private IKlineSymbolService klineSymbolService;

    /**
     * 查询发币列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:list')")
    @GetMapping("/list")
    public TableDataInfo list(TOwnCoin tOwnCoin)
    {
        startPage();
        List<TOwnCoin> list = tOwnCoinService.selectTOwnCoinList(tOwnCoin);
        return getDataTable(list);
    }

    /**
     * 导出发币列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:export')")
    @Log(title = "发币", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TOwnCoin tOwnCoin)
    {
        List<TOwnCoin> list = tOwnCoinService.selectTOwnCoinList(tOwnCoin);
        ExcelUtil<TOwnCoin> util = new ExcelUtil<TOwnCoin>(TOwnCoin.class);
        util.exportExcel(response, list, "发币数据");
    }

    /**
     * 获取发币详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tOwnCoinService.selectTOwnCoinById(id));
    }

    /**
     * 新增发币
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:add')")
    @Log(title = "发币", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TOwnCoin tOwnCoin)
    {
        tOwnCoin.setCoin(tOwnCoin.getCoin().toLowerCase());
        TSpontaneousCoin oldSpontaneousCoin = tSpontaneousCoinService.getOne(new LambdaQueryWrapper<TSpontaneousCoin>().eq(TSpontaneousCoin::getCoin, tOwnCoin.getCoin()));
        TOwnCoin oldTOwnCoin = tOwnCoinService.getOne(new LambdaQueryWrapper<TOwnCoin>().eq(TOwnCoin::getCoin, tOwnCoin.getCoin()));
        KlineSymbol oldklineSymbol = klineSymbolService.getOne(new LambdaQueryWrapper<KlineSymbol>()
                .eq(KlineSymbol::getSymbol, tOwnCoin.getCoin())
                .and(k->k.eq(KlineSymbol::getMarket,"binance").or().eq(KlineSymbol::getMarket,"echo")));
        if (Objects.nonNull(oldSpontaneousCoin) || Objects.nonNull(oldTOwnCoin) || Objects.nonNull(oldklineSymbol)){
            return AjaxResult.error(tOwnCoin.getCoin()+"已经存在");
        }
        return toAjax(tOwnCoinService.insertTOwnCoin(tOwnCoin));
    }

    /**
     * 修改发币
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:edit')")
    @Log(title = "发币", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TOwnCoin tOwnCoin)
    {
        return toAjax(tOwnCoinService.updateTOwnCoin(tOwnCoin));
    }

    /**
     * 删除发币
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:remove')")
    @Log(title = "发币", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tOwnCoinService.deleteTOwnCoinByIds(ids));
    }

    /**
     * 发布新币
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:edit')")
    @Log(title = "发布", businessType = BusinessType.UPDATE)
    @GetMapping("/editStatus/{id}")
    public AjaxResult editStatus(@PathVariable Long id)
    {
        return toAjax(tOwnCoinService.editStatus(id));
    }

    /**
     * 新币上线中，发币结束 申购资产发送
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:edit')")
    @Log(title = "发布", businessType = BusinessType.UPDATE)
    @GetMapping("/editReleaseStatus/{id}")
    public AjaxResult editReleaseStatus(@PathVariable Long id)
    {
        return toAjax(tOwnCoinService.editReleaseStatus(id));
    }

    /**
     * 查询发币订阅列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:list')")
    @Log(title = "订阅")
    @GetMapping("/subscribeList")
    public TableDataInfo subscribeList(TOwnCoinSubscribeOrder tOwnCoinSubscribeOrder)
    {
        startPage();
        List<TOwnCoinSubscribeOrder> list = tOwnCoinService.selectTOwnCoinSubscribeOrderList(tOwnCoinSubscribeOrder);
        return getDataTable(list);
    }

    /**
     * 查询订阅订单详情
     *
     * @param id
     * @return
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:edit')")
    @Log(title = "订阅")
    @GetMapping("/subOrder/{id}")
    public AjaxResult subOrder(@PathVariable Long id)
    {
        return success(tOwnCoinService.getTOwnCoinSubscribeOrder(id));
    }

    /**
     * 修改(审批)发币订阅
     */
    @PreAuthorize("@ss.hasPermi('bussiness:coin:edit')")
    @Log(title = "订阅", businessType = BusinessType.UPDATE)
    @PostMapping("/editSubscribe")
    public AjaxResult editSubscribe(@RequestBody TOwnCoinSubscribeOrder tOwnCoinSubscribeOrder)
    {
        return toAjax(tOwnCoinService.updateTOwnCoinSubscribeOrder(tOwnCoinSubscribeOrder));
    }
}
