package com.ruoyi;

import com.ruoyi.server.ChatServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.net.InetSocketAddress;

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
            InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8888);
            ChatServer chatServer =  new ChatServer();
            chatServer.run(address);
            System.out.println("聊天室端口8888启动成功");
        }catch (Exception e) {
            System.out.println("聊天室端口8888启动失败");

        }
    }
}