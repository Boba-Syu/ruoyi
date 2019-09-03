package com.ruoyi.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder

class ChatServer {
    /**
     * 监听线程组，监听客户端请求
     */
    private var bossGroup: EventLoopGroup = NioEventLoopGroup()
    /**
     * 处理客户段相关线程组，负责处理与客户端的数据通信
     */
    private var workerGroup: EventLoopGroup = NioEventLoopGroup()
    /**
     * 服务器相关配置信息
     */
    private var bootstrap: ServerBootstrap = ServerBootstrap()


    /**
     * 初始化bootstrap
     */
    init {
        // 绑定线程组
        this.bootstrap.group(bossGroup, workerGroup)
        // 设置通讯模式为nio
        this.bootstrap.channel(NioServerSocketChannel::class.java as Class<out ServerChannel>?)
        // 绑定缓冲区大小, 以字节为单位
    }

    /**
     * 处理监听逻辑
     *
     * @param port            监听端口
     * @param channelHandlers 服务端处理类
     * @return
     */
    @Throws(InterruptedException::class)
    fun doAccept(port: Int, vararg channelHandlers: ChannelHandler): ChannelFuture {
        // childHandler用于提供处理对象，增加多个处理逻辑，为责任链模式
        this.bootstrap.childHandler(object : ChannelInitializer<SocketChannel>() {
            // 服务端只初始化一次处理逻辑
            override fun initChannel(socketChannel: SocketChannel) {
                val pipeline = socketChannel.pipeline()
                pipeline.addLast("decoder", StringDecoder())
                pipeline.addLast(*channelHandlers)
                pipeline.addLast("encoder", StringEncoder())
            }
        })
        // 绑定监听端口， 可绑定多个
        // sync()为启动, 返回启动成功后的future
        return bootstrap.bind(port).sync()
    }

    /**
     * 优雅的关闭
     */
    fun release() {
        this.bossGroup.shutdownGracefully()
        this.workerGroup.shutdownGracefully()
    }

    companion object {
        @JvmStatic
        fun serverStart(port: Int) {
            var chatServer: ChatServer? = null
            var future: ChannelFuture? = null
            try {
                chatServer = ChatServer()
                future = chatServer.doAccept(port, ChatServerHandler())
                println("服务端开启，端口号为$port")
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    future?.channel()?.closeFuture()?.sync()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                chatServer?.release()
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            ChatServer.serverStart(8888)
        }
    }
}
