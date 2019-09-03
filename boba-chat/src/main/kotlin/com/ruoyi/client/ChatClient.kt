package com.ruoyi.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import java.util.*
import java.util.concurrent.TimeUnit

class ChatClient {
    /**
     * 处理请求和处理服务端响应的线程组
     */
    private var group: EventLoopGroup = NioEventLoopGroup()
    /**
     * 服务启动相关信息
     */
    private var bootstrap: Bootstrap = Bootstrap()

    init {
        init()
    }

    /**
     * 初始化
     */
    private fun init() {
        // 绑定线程组
        bootstrap.group(group)
        // 设定通讯模式为NIO
        bootstrap.channel(NioSocketChannel::class.java)
    }

    /**
     * 建立与服务端的连接
     *
     * @param host            服务端地址
     * @param port            服务端端口号
     * @param channelHandlers 客户端处理类
     * @return
     * @throws InterruptedException
     */
    @Throws(InterruptedException::class)
    fun doRequest(host: String, port: Int, vararg channelHandlers: ChannelHandler): ChannelFuture {
        this.bootstrap.handler(object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {
                val pipeline = ch.pipeline()
                pipeline.addLast("decoder", StringDecoder())
                pipeline.addLast(*channelHandlers)
                pipeline.addLast("encoder", StringEncoder())
            }
        })
        return bootstrap.connect(host, port).sync()
    }

    /**
     * 优雅的退出
     */
    fun release() {
        this.group.shutdownGracefully()
    }

    companion object {
        @JvmStatic
        fun start(host: String, port: Int) {
            var chatClient: ChatClient? = null
            var future: ChannelFuture? = null
            try {
                chatClient = ChatClient()
                future = chatClient.doRequest(host, port, ChatClientHandler())
                val scanner = Scanner(System.`in`)
                while (true) {
                    val msg = scanner.nextLine()
                    if ("exit" == msg) {
                        // 关闭监听器
                        future.channel().writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE)
                        break
                    }
                    future.channel().writeAndFlush(msg)
                    TimeUnit.SECONDS.sleep(1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        @JvmStatic
        fun main(args: Array<String>) {
            ChatClient.start("127.0.0.1", 8888)
        }
    }
}
