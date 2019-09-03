package com.ruoyi.web.controller.chat;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chat")
public class Chat {
    private String prefix = "chat";

    @RequestMapping("chat")
    public String chat() {
        return prefix + "/chat";
    }
}
