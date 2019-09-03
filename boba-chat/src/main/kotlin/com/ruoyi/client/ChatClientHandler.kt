package com.ruoyi.client

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ChatClientHandler : SimpleChannelInboundHandler<String>() {
    /**
     * 当客户端和服务端 TCP 链路建立成功之后，Netty 的 NIO 线程会调用 channelActive 方法
     */
    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        println("与服务端连接成功")
        super.channelActive(ctx)
    }

    /**
     * 当服务端返回应答消息时，channelRead 方法被调用，从 Netty 的 ByteBuf 中读取并打印应答消息
     */
    public override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
        println(msg)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }

}