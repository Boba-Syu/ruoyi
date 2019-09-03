package com.ruoyi.server

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import java.util.ArrayList

class ChatServerHandler : SimpleChannelInboundHandler<String>() {
    private var list: MutableList<Channel> = ArrayList()

    /**
     * 当有新客户端加入时
     *
     * @param ctx
     * @throws Exception
     */
    override fun handlerAdded(ctx: ChannelHandlerContext) {
        println("一位新用户加入")
        list.add(ctx.channel())
        ctx.channel().writeAndFlush("hello")
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        println("有一位用户离开")
        list.remove(ctx.channel())
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
        println(msg)
        ctx.writeAndFlush(msg)
        for (channel in list) {
            if (channel !== ctx.channel()) {
                channel.writeAndFlush(msg)
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        //cause.printStackTrace()
        ctx.close()
    }
}