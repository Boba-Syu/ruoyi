package com.ruoyi.redis

import redis.clients.jedis.Jedis;

object ChatRedisFactory {
    //var jedis: Jedis = Jedis("localhost")
    var jedis: Jedis = Jedis("47.97.90.166")
    init {
        jedis.auth("123456")
    }
    var CHAT_LIST = "chatList"
    var CHAT_LIST_LENGTH = "chatListLength"
}

