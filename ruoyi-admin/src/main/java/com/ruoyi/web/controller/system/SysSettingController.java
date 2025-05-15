package com.ruoyi.web.controller.system;

import cn.hutool.json.JSONUtil;
import com.ruoyi.bussiness.domain.setting.*;
import com.ruoyi.bussiness.service.SettingService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.enums.SettingEnum;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setting")
public class SysSettingController extends BaseController {

    @Resource
    private SettingService settingService;



    @PutMapping(value = "/put/{key}")
    public AjaxResult saveConfig(@PathVariable String key, @RequestBody String configValue) {
        SettingEnum settingEnum = SettingEnum.valueOf(key);
        if(settingEnum.equals(SettingEnum.WITHDRAWAL_CHANNEL_SETTING) || settingEnum.equals(SettingEnum.ASSET_COIN)){
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if(!loginUser.getUser().isAdmin()){
            return  AjaxResult.success("您没有操作权限，请联系管理员！");
            }
        }

        //获取系统配置
        Setting setting = settingService.getById(settingEnum.name());
        if (setting == null) {
            setting = new Setting();
            setting.setId(settingEnum.name());
        }

        //特殊配置过滤
        configValue = filter(settingEnum, configValue);
        setting.setSettingValue(configValue);
        settingService.saveUpdate(setting);
        return AjaxResult.success();
    }



    @ApiOperation(value = "查看配置")
    @GetMapping(value = "/get/{key}")

    public AjaxResult settingGet(@PathVariable String key) {
        return createSetting(key);
    }


    /**
     * 对配置进行过滤
     *
     * @param settingEnum
     * @param configValue
     */
    private String filter(SettingEnum settingEnum, String configValue) {
        if (settingEnum.equals(SettingEnum.APP_SIDEBAR_SETTING)) {
            Setting setting = settingService.get(SettingEnum.APP_SIDEBAR_SETTING.name());
            AppSidebarSetting appSidebar = JSONUtil.toBean(configValue, AppSidebarSetting.class);
            List<AppSidebarSetting> list;
            if (Objects.nonNull(setting)){
                list = JSONUtil.toList(JSONUtil.parseArray(setting.getSettingValue()), AppSidebarSetting.class);
            }else{
                list = new ArrayList<>();
            }
            List<AppSidebarSetting> copyList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(list)){
                copyList.addAll(list);
                Boolean flag = true;
                for (AppSidebarSetting a:copyList) {
                    if (appSidebar.getKey().equals(a.getKey())){
                        flag = false;
                    }
                }
                if (flag){
                    list.add(appSidebar);
                    copyList.add(appSidebar);
                }
                //修改还是删除
                for (AppSidebarSetting a:copyList) {
                    if (StringUtils.isNotEmpty(appSidebar.getName()) && appSidebar.getKey().equals(a.getKey())){
                        list.remove(a);
                        list.add(appSidebar);
                        break;
                    }
                    if (StringUtils.isEmpty(appSidebar.getName()) && appSidebar.getKey().equals(a.getKey())){
                        list.remove(a);
                        break;
                    }
                }
            }else{
                list.add(appSidebar);
            }
            configValue = JSONUtil.toJsonStr(list);
        }

        return configValue;
    }

    /**
     * 获取表单
     * 这里主要包含一个配置对象为空，导致转换异常问题的处理，解决配置项增加减少，带来的系统异常，无法直接配置
     *
     * @param key
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private AjaxResult createSetting(String key) {
        SettingEnum settingEnum = SettingEnum.valueOf(key);
        Setting setting = settingService.get(key);
        switch (settingEnum) {
            case BASE_SETTING:
                return setting == null ?
                        AjaxResult.success(new BaseSetting()) :
                        AjaxResult.success(JSONUtil.toBean(setting.getSettingValue(), BaseSetting.class));
            case APP_SIDEBAR_SETTING:
                return setting == null ?
                        AjaxResult.success(new ArrayList<AppSidebarSetting>()) :
                        AjaxResult.success(JSONUtil.toList(JSONUtil.parseArray(setting.getSettingValue()), AppSidebarSetting.class)
                                .stream().sorted(Comparator.comparing(AppSidebarSetting::getSort)).collect(Collectors.toList()));

            case EMAIL_SETTING:
                return setting == null ?
                        AjaxResult.success(new EmailSetting()) :
                        AjaxResult.success(JSONUtil.toBean(setting.getSettingValue(), EmailSetting.class));

            case OSS_SETTING:
                return setting == null ?
                        AjaxResult.success(new OssSetting()) :
                        AjaxResult.success(JSONUtil.toBean(setting.getSettingValue(), OssSetting.class));
            case SMS_SETTING:
                return setting == null ?
                        AjaxResult.success(new SmsSetting()) :
                        AjaxResult.success(JSONUtil.toBean(setting.getSettingValue(), SmsSetting.class));
            case ASSET_COIN:
                return setting == null ?
                        AjaxResult.success(new AssetCoinSetting()) :
                        AjaxResult.success(JSONUtil.toList(JSONUtil.parseArray(setting.getSettingValue()), AssetCoinSetting.class));
            case MARKET_URL:
                return setting == null ?
                        AjaxResult.success(new MarketUrlSetting()) :
                        AjaxResult.success(JSONUtil.toBean(setting.getSettingValue(), MarketUrlSetting.class));
            case LOGIN_REGIS_SETTING:
                return setting == null ?
                        AjaxResult.success(new LoginRegisSetting()) :
                        AjaxResult.success(JSONUtil.toBean(setting.getSettingValue(), LoginRegisSetting.class));
            case LOAD_SETTING:
                return setting == null ?
                        AjaxResult.success(new LoadSetting()) :
                        AjaxResult.success(JSONUtil.toBean(setting.getSettingValue(), LoadSetting.class));
            case WITHDRAWAL_CHANNEL_SETTING:
                return setting == null ?
                        AjaxResult.success(new TRechargeChannelSetting()) :
                        AjaxResult.success(JSONUtil.toList(JSONUtil.parseArray(setting.getSettingValue()), TRechargeChannelSetting.class));
            case FINANCIAL_SETTLEMENT_SETTING:
                return setting == null ?
                        AjaxResult.success(new FinancialSettlementSetting()) :
                        AjaxResult.success(JSONUtil.toBean(setting.getSettingValue(), FinancialSettlementSetting.class));
            case PLATFORM_SETTING:
                return setting == null ?
                        AjaxResult.success(new PlatformSetting()) :
                        AjaxResult.success(JSONUtil.toBean(setting.getSettingValue(), PlatformSetting.class));
            case WHITE_IP_SETTING:
                return setting == null ?
                        AjaxResult.success(new ArrayList<WhiteIpSetting>()) :
                        AjaxResult.success(JSONUtil.toList(JSONUtil.parseArray(setting.getSettingValue()), WhiteIpSetting.class));
            case SUPPORT_STAFF_SETTING:
                return setting == null ?
                        AjaxResult.success(new ArrayList<SupportStaffSetting>()) :
                        AjaxResult.success(JSONUtil.toList(JSONUtil.parseArray(setting.getSettingValue()), SupportStaffSetting.class));
            case RECHARGE_REBATE_SETTING:
                return setting == null ?
                        AjaxResult.success(new RechargeRebateSetting()) :
                        AjaxResult.success(JSONUtil.toBean(setting.getSettingValue(), RechargeRebateSetting.class));
            case FINANCIAL_REBATE_SETTING:
                return setting == null ?
                        AjaxResult.success(new FinancialRebateSetting()) :
                        AjaxResult.success(JSONUtil.toBean(setting.getSettingValue(), FinancialRebateSetting.class));
            case MING_SETTLEMENT_SETTING:
                return setting == null ?
                        AjaxResult.success(new MingSettlementSetting()) :
                        AjaxResult.success(JSONUtil.toBean(setting.getSettingValue(), MingSettlementSetting.class));
            case WITHDRAWAL_RECHARGE_VOICE:
                return setting == null ?
                        AjaxResult.success(new VoiceSetting()) :
                        AjaxResult.success(JSONUtil.toBean(setting.getSettingValue(), VoiceSetting.class));
            case WHITE_PAPER_SETTING:
                return setting == null ?
                        AjaxResult.success(new WhitePaperSetting()) :
                        AjaxResult.success(JSONUtil.toBean(setting.getSettingValue(), WhitePaperSetting.class));
            case DEFI_INCOME_SETTING:
                return setting == null ?
                        AjaxResult.success(new DefiIncomeSetting()) :
                        AjaxResult.success(JSONUtil.toBean(setting.getSettingValue(), DefiIncomeSetting.class));
            case TAB_SETTING:
                return setting == null ?
                        AjaxResult.success(new TabSetting()) :
                        AjaxResult.success(JSONUtil.toList(JSONUtil.parseArray(setting.getSettingValue()), TabSetting.class));
            case PLAYING_SETTING:
                return setting == null ?
                    AjaxResult.success(new ArrayList<PlayingSetting>()) :
                    AjaxResult.success(JSONUtil.toList(JSONUtil.parseArray(setting.getSettingValue()), PlayingSetting.class));
            case BOTTOM_MENU_SETTING:
                return setting == null ?
                        AjaxResult.success(new ArrayList<BottomMenuSetting>()) :
                        AjaxResult.success(JSONUtil.toList(JSONUtil.parseArray(setting.getSettingValue()), BottomMenuSetting.class));
            case MIDDLE_MENU_SETTING:
                return setting == null ?
                        AjaxResult.success(new ArrayList<MiddleMenuSetting>()) :
                        AjaxResult.success(JSONUtil.toList(JSONUtil.parseArray(setting.getSettingValue()), MiddleMenuSetting.class));
            case LOGO_SETTING:
                return setting == null ?
                        AjaxResult.success(new LogoSetting()) :
                        AjaxResult.success(JSONUtil.toBean(setting.getSettingValue(), LogoSetting.class));
            case HOME_COIN_SETTING:
                return setting == null ?
                        AjaxResult.success(new ArrayList<HomeCoinSetting>()) :
                        AjaxResult.success(JSONUtil.toList(JSONUtil.parseArray(setting.getSettingValue()), HomeCoinSetting.class));
            case DOWNLOAD_SETTING:
                return setting == null ?
                        AjaxResult.success(new ArrayList<DownloadSetting>()) :
                        AjaxResult.success(JSONUtil.toList(JSONUtil.parseArray(setting.getSettingValue()), DownloadSetting.class));
            case TG_BOT_SETTING:
                return setting == null ?
                        AjaxResult.success(new HomeCoinSetting()) :
                        AjaxResult.success( JSONUtil.toBean(setting.getSettingValue(), TgBotSetting.class));
            case AUTH_LIMIT:
                return setting == null ?
                        AjaxResult.success(new AuthLimitSetting ()) :
                        AjaxResult.success( JSONUtil.toBean(setting.getSettingValue(), AuthLimitSetting.class));
            case THIRD_CHANNL:
                return setting == null ?
                        AjaxResult.success(new ArrayList<ThirdPaySetting> ()) :
                        AjaxResult.success( (JSONUtil.toList(JSONUtil.parseArray(setting.getSettingValue()), ThirdPaySetting.class)));
            case VIP_LEVEL_SETTING:
                return setting == null ?
                        AjaxResult.success(new VipLevelSetting ()) :
                        AjaxResult.success( JSONUtil.toBean(setting.getSettingValue(), VipLevelSetting.class));
            case VIP_DIRECTIONS_SETTING:
                return setting == null ?
                        AjaxResult.success(new ArrayList<VipDirectionsSetting> ()) :
                        AjaxResult.success( (JSONUtil.toList(JSONUtil.parseArray(setting.getSettingValue()), VipDirectionsSetting.class)));
            case ADD_MOSAIC_SETTING:
                return setting == null ?
                        AjaxResult.success(new AddMosaicSetting ()) :
                        AjaxResult.success( JSONUtil.toBean(setting.getSettingValue(), AddMosaicSetting.class));
            default:
                return AjaxResult.success();
        }
    }
}
