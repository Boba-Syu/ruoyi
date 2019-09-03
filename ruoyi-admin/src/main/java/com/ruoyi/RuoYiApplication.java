package com.ruoyi;

import com.ruoyi.server.ChatServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author ruoyi
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class RuoYiApplication
{
    public static void main(String[] args) {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(RuoYiApplication.class, args);
        try {
            ChatServer.serverStart(8888);
            System.out.println("聊天室端口8888启动成功");
        }catch (Exception e) {
            System.out.println("聊天室端口8888启动失败");

        }
    }
}