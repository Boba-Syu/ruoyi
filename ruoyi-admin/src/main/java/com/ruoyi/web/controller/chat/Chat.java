package com.ruoyi.web.controller.chat;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import com.ruoyi.redis.ChatRedisFactory;

import java.util.List;

@Controller
@RequestMapping("/chat")
public class Chat {
    private String prefix = "chat";

    @RequestMapping("chat")
    public String chat() {
        return prefix + "/chat";
    }

    @RequestMapping("chatList")
    @ResponseBody
    public List<String> chatList() {
        Jedis jedis = ChatRedisFactory.INSTANCE.getJedis();
        int len = new Integer(jedis.get(ChatRedisFactory.INSTANCE.getCHAT_LIST_LENGTH()));
        return jedis.lrange(ChatRedisFactory.INSTANCE.getCHAT_LIST(), 0, len);
    }
}
