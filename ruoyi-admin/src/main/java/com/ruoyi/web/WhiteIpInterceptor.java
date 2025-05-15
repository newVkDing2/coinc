package com.ruoyi.web;

import cn.hutool.json.JSONUtil;
import com.ruoyi.bussiness.domain.setting.LoginRegisSetting;
import com.ruoyi.bussiness.domain.setting.Setting;
import com.ruoyi.bussiness.domain.setting.WhiteIpSetting;
import com.ruoyi.bussiness.service.SettingService;
import com.ruoyi.common.enums.SettingEnum;
import com.ruoyi.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
public class WhiteIpInterceptor implements HandlerInterceptor {

    public static final String LOCALHOST = "127.0.0.1";
    @Autowired
    private SettingService settingService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Setting setting = settingService.get(SettingEnum.WHITE_IP_SETTING.name());
        if (Objects.isNull(setting)){
            return true;
        }else{
            List<WhiteIpSetting> list = JSONUtil.toList(JSONUtil.parseArray(setting.getSettingValue()), WhiteIpSetting.class);
            if(CollectionUtils.isEmpty(list)){
                return true;
            }else{
                try {
                    String ip = getIpAddr(request);
                    for (WhiteIpSetting whiteIp:list) {
                        if (StringUtils.isNotEmpty(whiteIp.getIp()) && whiteIp.getIp().equals(ip)){
                            return true;
                        }
                    }
                    //拦截跳转
                    log.debug("ip:{} is not allowed.",ip);
                    return this.failInterceptor(response,ip);
                }catch(Exception e){
                    log.error("Error occurred while checking the white ip ", e);
                }
            }
        }
        return false;
    }

    /**
     * 拦截器过滤失败
     *
     * @param response
     * @return
     * @throws IOException
     */
    public boolean failInterceptor(HttpServletResponse response,String ip) throws IOException {
        try {
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/html; charset=utf-8");
            response.setStatus(403);
            response.getWriter().println("Access denied: "+ip + ".登录其他账号请刷新页面");
            response.getWriter().flush();
            response.getWriter().close();
            return false;
        } catch (Exception e) {
            log.error("Error occurred while checking the white ip ", e);
            return false;
        }
    }


    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip.equals("0:0:0:0:0:0:0:1")) {
            ip = LOCALHOST;
        }
        if (ip.split(",").length > 1) {
            ip = ip.split(",")[0];
        }
        return ip;
    }
}
