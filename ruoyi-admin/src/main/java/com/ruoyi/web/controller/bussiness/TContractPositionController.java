package com.ruoyi.web.controller.bussiness;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.bussiness.domain.TContractLoss;
import com.ruoyi.bussiness.service.ITContractLossService;
import com.ruoyi.bussiness.service.ITWithdrawService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.CachePrefix;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ucontract.ContractComputerUtil;
import com.ruoyi.socket.socketserver.WebSocketNotice;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.bussiness.domain.TContractPosition;
import com.ruoyi.bussiness.service.ITContractPositionService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * U本位持仓表Controller
 *
 * @author michael
 * @date 2023-07-20
 */
@RestController
@RequestMapping("/bussiness/position")
public class TContractPositionController extends BaseController {
    @Autowired
    private ITContractPositionService tContractPositionService;

    @Autowired
    private ITContractLossService contractLossService;

    @Resource
    private RedisCache redisCache;
    /**
     * 查询U本位持仓表列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:position:list')")
    @GetMapping("/list")
    public TableDataInfo list(TContractPosition tContractPosition) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        if (!user.isAdmin()) {
            if (StringUtils.isNotBlank(user.getUserType()) && !user.getUserType().equals("0")) {
                tContractPosition.setAdminParentIds(String.valueOf(user.getUserId()));
            }
        }
        startPage();
        List<TContractPosition> list = tContractPositionService.selectTContractPositionList(tContractPosition);
            for (TContractPosition t : list) {
                BigDecimal earnRate = Objects.isNull(t.getEarnRate()) ? BigDecimal.ZERO : t.getEarnRate();
                BigDecimal adjustAmount=t.getAdjustAmount();
                //rxce
                BigDecimal bigDecimal =adjustAmount.add((adjustAmount.multiply(t.getLeverage()).multiply(earnRate).setScale(4, RoundingMode.UP)));
                //需要补多少仓
                BigDecimal subzs=bigDecimal.subtract(t.getRemainMargin());
                if(subzs.compareTo(BigDecimal.ZERO)<0){
                    subzs=BigDecimal.ZERO;
                }
                t.setSubAmount(bigDecimal);
                Map<String, Object> params = new HashMap<>();

                Long days= Objects.nonNull(t.getDeliveryDays())?t.getDeliveryDays()*3600*24*1000:0L;
                params.put("subTime", Objects.nonNull(t.getSubTime())?t.getSubTime().getTime()+days:0L);
                Long sub=System.currentTimeMillis()-t.getCreateTime().getTime();
                Long result=t.getDeliveryDays()*3600*24*1000-sub;
                if(result<0){
                    result=0L;
                }
                params.put("deliveryDays", Objects.nonNull(t.getDeliveryDays())?result:0L);
                t.setParams(params);
                BigDecimal currentlyPrice = redisCache.getCacheObject(CachePrefix.CURRENCY_PRICE.getPrefix() + t.getSymbol().toLowerCase());
                if (Objects.nonNull(currentlyPrice)) {
                    BigDecimal earn = ContractComputerUtil.getPositionEarn(t.getOpenPrice(), t.getOpenNum(), currentlyPrice, t.getType());
                    Integer status = t.getStatus();
                    t.setUreate(earn);
                    if (1 == status) {
                        t.setUreate(t.getEarn());
                    }
                }
            }
        return getDataTable(list);
    }

    /**
     * 导出U本位持仓表列表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:position:export')")
    @Log(title = "U本位持仓表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TContractPosition tContractPosition) {
        List<TContractPosition> list = tContractPositionService.selectTContractPositionList(tContractPosition);
        ExcelUtil<TContractPosition> util = new ExcelUtil<TContractPosition>(TContractPosition.class);
        util.exportExcel(response, list, "U本位持仓表数据");
    }

    /**
     * 获取U本位持仓表详细信息
     */
    @PreAuthorize("@ss.hasPermi('bussiness:position:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(tContractPositionService.selectTContractPositionById(id));
    }

    /**
     * 新增U本位持仓表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:position:add')")
    @Log(title = "U本位持仓表", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TContractPosition tContractPosition) {
        return toAjax(tContractPositionService.insertTContractPosition(tContractPosition));
    }

    /**
     * 修改U本位持仓表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:position:edit')")
    @Log(title = "U本位持仓表", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TContractPosition tContractPosition) {
        int count=tContractPositionService.updateTContractPosition(tContractPosition);
        return toAjax(count);
    }

    /**
     * 删除U本位持仓表
     */
    @PreAuthorize("@ss.hasPermi('bussiness:position:remove')")
    @Log(title = "U本位持仓表", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(tContractPositionService.deleteTContractPositionByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('bussiness:position:query')")
    @PostMapping("contractLoss/{id}")
    public TableDataInfo contractLoss(@PathVariable Long id) {
        TContractLoss tContractLoss = new TContractLoss();
        tContractLoss.setPositionId(id);
        startPage();
        List<TContractLoss> list = contractLossService.selectTContractLossList(tContractLoss);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('bussiness:position:pass')")
    @Log(title = "平仓审核", businessType = BusinessType.UPDATE)
    @PutMapping("/pass")
    public AjaxResult pass(@RequestBody  TContractPosition tContractPosition) {
           String result=   tContractPositionService.pass(tContractPosition);
           if(!"success".equals(result)){
               return AjaxResult.error(result);
        }
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('bussiness:position:reject')")
    @Log(title = "平仓审核", businessType = BusinessType.UPDATE)
    @PutMapping("/reject")
    public AjaxResult reject(@RequestBody  TContractPosition tContractPosition) {
        String result=   tContractPositionService.reject(tContractPosition);
        if(!"success".equals(result)){
            return AjaxResult.error(result);
        }
        return AjaxResult.success();
    }
    @PreAuthorize("@ss.hasPermi('bussiness:position:stopPosition')")
    @Log(title = "平仓", businessType = BusinessType.UPDATE)
    @PostMapping("/stopPositon")
    public AjaxResult stopPositon(@RequestBody TContractPosition tContractPosition) {
        String result=   tContractPositionService.stopPosition(tContractPosition.getId());
        if(!"success".equals(result)){
            return AjaxResult.error(result);
        }
        return AjaxResult.success();
    }
    @PreAuthorize("@ss.hasPermi('bussiness:position:stopAll')")
    @Log(title = "一键爆仓", businessType = BusinessType.UPDATE)
    @PostMapping("/stopAllPositon")
    public AjaxResult stopAllPositon(@RequestBody TContractPosition tContractPosition) {
        String result=   tContractPositionService.stopAllPosition(tContractPosition.getId());
        if(!"success".equals(result)){
            return AjaxResult.error(result);
        }
        return AjaxResult.success();
    }
}
