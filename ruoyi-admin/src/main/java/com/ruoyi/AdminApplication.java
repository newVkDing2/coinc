package com.ruoyi;

import com.ruoyi.framework.ethws.WebSocketClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author ruoyi
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class AdminApplication //implements ApplicationRunner
{
 /*   @Autowired
    private WebSocketClientFactory webSocketClientFactory;*/
   /* @Override
    public void run(ApplicationArguments args) {
        // 项目启动的时候打开websocket连接
        webSocketClientFactory.retryOutCallWebSocketClient();
    }*/
    public static void main(String[] args)
    {
        SpringApplication.run(AdminApplication.class, args);
        System.out.println("启动成功\n");
    }
}
