package com.ruoyi.web.controller.common;

import cn.hutool.json.JSONUtil;
import com.ruoyi.bussiness.domain.setting.*;
import com.ruoyi.bussiness.domain.vo.RecoedEnumVO;
import com.ruoyi.bussiness.service.SettingService;
import com.ruoyi.bussiness.service.impl.FileServiceImpl;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.CachePrefix;
import com.ruoyi.common.enums.RecordEnum;
import com.ruoyi.common.enums.SettingEnum;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.framework.config.ServerConfig;
import com.ruoyi.telegrambot.TelegramBotConfig;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 通用请求处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/common")
public class CommonController
{
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    @Resource
    private ServerConfig serverConfig;
    @Resource
    private RedisCache redisCache;
    @Resource
    private FileServiceImpl fileService;
    @Resource
    private TelegramBotConfig telegramBotConfig;
    @Resource
    private SettingService settingService;

    private static final String FILE_DELIMETER = ",";

    /**
     * 通用下载请求
     * 
     * @param fileName 文件名称
     * @param delete 是否删除
     */
    @GetMapping("/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request)
    {
        try
        {
            if (!FileUtils.checkAllowDownload(fileName))
            {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = RuoYiConfig.getDownloadPath() + fileName;

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete)
            {
                FileUtils.deleteFile(filePath);
            }
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求（单个）
     */
    @PostMapping("/upload")
    public AjaxResult uploadFile(MultipartFile file) throws Exception
    {

        try {
            String filename = file.getResource().getFilename();
            //这里文件名用了uuid 防止重复，可以根据自己的需要来写
            String name = UUID.randomUUID() + filename.substring(filename.lastIndexOf("."), filename.length());
            name = name.replace("-", "");
            String url = fileService.uploadFileOSS(file,name);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", name);
            ajax.put("newFileName", FileUtils.getName(name));
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        } catch (Exception e) {
            e.getMessage();
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 通用上传请求（多个）
     */
    @PostMapping("/uploads")
    public AjaxResult uploadFiles(List<MultipartFile> files) throws Exception
    {
        try
        {
            // 上传文件路径
            String filePath = RuoYiConfig.getUploadPath();
            List<String> urls = new ArrayList<String>();
            List<String> fileNames = new ArrayList<String>();
            List<String> newFileNames = new ArrayList<String>();
            List<String> originalFilenames = new ArrayList<String>();
            for (MultipartFile file : files)
            {
                // 上传并返回新文件名称
                String fileName = FileUploadUtils.upload(filePath, file);
                String url = serverConfig.getUrl() + fileName;
                urls.add(url);
                fileNames.add(fileName);
                newFileNames.add(FileUtils.getName(fileName));
                originalFilenames.add(file.getOriginalFilename());
            }
            AjaxResult ajax = AjaxResult.success();
            ajax.put("urls", StringUtils.join(urls, FILE_DELIMETER));
            ajax.put("fileNames", StringUtils.join(fileNames, FILE_DELIMETER));
            ajax.put("newFileNames", StringUtils.join(newFileNames, FILE_DELIMETER));
            ajax.put("originalFilenames", StringUtils.join(originalFilenames, FILE_DELIMETER));
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 本地资源通用下载
     */
    @GetMapping("/download/resource")
    public void resourceDownload(String resource, HttpServletRequest request, HttpServletResponse response)
            throws Exception
    {
        try
        {
            if (!FileUtils.checkAllowDownload(resource))
            {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载。 ", resource));
            }
            // 本地资源路径
            String localPath = RuoYiConfig.getProfile();
            // 数据库资源地址
            String downloadPath = localPath + StringUtils.substringAfter(resource, Constants.RESOURCE_PREFIX);
            // 下载名称
            String downloadName = StringUtils.substringAfterLast(downloadPath, "/");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, downloadName);
            FileUtils.writeBytes(downloadPath, response.getOutputStream());
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }

    @PostMapping("/upload/OSS")
    public AjaxResult uploadFileOSS(MultipartFile file, String remark) {
        try {
            String filename = file.getResource().getFilename();
            //这里文件名用了uuid 防止重复，可以根据自己的需要来写
            String name = UUID.randomUUID() + filename.substring(filename.lastIndexOf("."), filename.length());
            name = name.replace("-", "");
            String url = fileService.uploadFileOSS(file,name);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("fileName", name);
            ajax.put("url", url);
            return ajax;
        } catch (Exception e) {
            e.getMessage();
            return AjaxResult.error(e.getMessage());
        }
    }


    @PostMapping("/recordType")
    public AjaxResult recordType() {
        List<RecoedEnumVO> recoedEnumVOS = new ArrayList<>();
        LinkedHashMap<Integer, String> map = RecordEnum.getMap();
        for (Integer s : map.keySet()) {
            RecoedEnumVO recoedEnumVO = new RecoedEnumVO();
            recoedEnumVO.setKey(s);
            recoedEnumVO.setValue(map.get(s));
            recoedEnumVOS.add(recoedEnumVO);
        }
       System.out.println(map);
        return AjaxResult.success(recoedEnumVOS);
    }

    @PostMapping("/reStartBot")
    public AjaxResult reStartBot() {
        try {
            telegramBotConfig.start();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return AjaxResult.success();
    }
    @PostMapping("/getCoinPrice")
    public AjaxResult getCoinPrice(@RequestBody String coin) {
        return AjaxResult.success(redisCache.getCacheObject(CachePrefix.CURRENCY_CLOSE_PRICE.getPrefix() + coin.toLowerCase()));
    }

    @ApiOperation(value = "获取所有配置")
    @PostMapping("/getAllSetting")
    public AjaxResult getAllSetting() {
        //提现
        Setting setting = settingService.get(SettingEnum.WITHDRAWAL_CHANNEL_SETTING.name());
        HashMap<String, Object> map = new HashMap<>();
        //图形验证码
        setting = settingService.get(SettingEnum.MARKET_URL.name());
        map.put("MARKET_URL",setting == null ? new MarketUrlSetting() :
                JSONUtil.toBean(setting.getSettingValue(), MarketUrlSetting.class));

        return AjaxResult.success(map);
    }
}
