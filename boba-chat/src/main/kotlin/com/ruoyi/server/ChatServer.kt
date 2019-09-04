package com.ruoyi.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.stream.ChunkedWriteHandler
import java.net.InetSocketAddress

class ChatServer {
    private val port = 8888

    fun run(address: InetSocketAddress) {
        val bossGroup = NioEventLoopGroup()
        val workGroup = NioEventLoopGroup()
        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(CheckSocketHandler())
            val channel = bootstrap.bind(address).sync().channel()
            channel.closeFuture().sync()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bossGroup.shutdownGracefully()
            workGroup.shutdownGracefully()
        }
    }

    private inner class CheckSocketHandler : ChannelInitializer<SocketChannel>() {

        @Throws(Exception::class)
        override fun initChannel(socketChannel: SocketChannel) {
            socketChannel.pipeline().addLast("http-codec", HttpServerCodec())
            socketChannel.pipeline().addLast("aggregator", HttpObjectAggregator(65536))
            socketChannel.pipeline().addLast("http-chunket", ChunkedWriteHandler())
            socketChannel.pipeline().addLast("handler", ChatSocketHandler())
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val address = InetSocketAddress("47.97.90.166", 8888)
            ChatServer().run(address)
        }
    }
}
