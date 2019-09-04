package com.ruoyi.server

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.group.ChannelGroup
import io.netty.channel.group.DefaultChannelGroup
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import io.netty.handler.codec.http.websocketx.*
import io.netty.util.CharsetUtil
import io.netty.util.concurrent.GlobalEventExecutor
import org.slf4j.LoggerFactory

class ChatSocketHandler : SimpleChannelInboundHandler<Any>() {
    private var handshaker: WebSocketServerHandshaker? = null
    private val port = 8888
    private val url = "ws://47.97.90.166:$port/websocket"

    companion object {
        var group: ChannelGroup = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
        private val log = LoggerFactory.getLogger(ChatSocketHandler::class.java)
    }

    /**
     * 处理客户端向服务端发起http握手请求的业务
     */
    private fun handHttpRequest(ctx: ChannelHandlerContext, req: FullHttpRequest) {
        if (!req.decoderResult.isSuccess || "websocket" != req.headers().get("Upgrade")) {
            sendHttpResponse(ctx, req,
                    DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST))
            return
        }
        val wsFactory = WebSocketServerHandshakerFactory(url, null, false)
        handshaker = wsFactory.newHandshaker(req)
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel())
        } else {
            handshaker!!.handshake(ctx.channel(), req)
        }
    }

    /**
     * 服务端向客户端响应消息
     */
    @Suppress("deprecation")
    private fun sendHttpResponse(ctx: ChannelHandlerContext, req: FullHttpRequest, res: DefaultFullHttpResponse) {
        if (res.status.code() != 200) {
            val buf = Unpooled.copiedBuffer(res.status.toString(), CharsetUtil.UTF_8)
            res.content().writeBytes(buf)
            buf.release()
        }
        // 服务端向客户端发送数据
        val f = ctx.channel().writeAndFlush(res)
        if (res.status.code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE)
        }
    }

    /**
     * 处理客户端与服务端之前的websocket业务
     */
    private fun handWebSocketFrame(ctx: ChannelHandlerContext, frame: WebSocketFrame) {
        // 判断是否是关闭webSocket的指令
        if (frame is CloseWebSocketFrame) {
            handshaker!!.close(ctx.channel(), frame.retain() as CloseWebSocketFrame)
        }
        // 判断是否ping消息
        if (frame is PingWebSocketFrame) {
            ctx.channel().write(PongWebSocketFrame(frame.content().retain()))
            return
        }
        // 每当从服务端读到客户端写入信息时，将信息转发给其他客户端的 Channel
        if (frame is TextWebSocketFrame) {
            val incoming = ctx.channel()
            for (channel in group) {
                if (channel !== incoming) {
                    channel.writeAndFlush(TextWebSocketFrame(frame.text()))
                } else {
                    channel.writeAndFlush(TextWebSocketFrame(frame.text()))
                }
            }
        }
    }

    /**
     * 对客户端websocket请求的核心方法
     */
    @Throws(Exception::class)
    override fun channelRead0(context: ChannelHandlerContext, msg: Any) {
        if (msg is FullHttpRequest) {
            handHttpRequest(context, msg)
        } else if (msg is WebSocketFrame) {// 处理websocket连接业务
            handWebSocketFrame(context, msg)
        }
    }

    /**
     * 新用户加入
     */
    @Throws(Exception::class)
    override fun handlerAdded(ctx: ChannelHandlerContext) {
        val incoming = ctx.channel()
        for (channel in group) {
            channel.writeAndFlush("[SERVER] - " + incoming.remoteAddress() + " 加入\n")
        }
        group.add(ctx.channel())

    }

    /**
     * 有用户退出时
     */

    @Throws(Exception::class)
    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        val incoming = ctx.channel()
        for (channel in group) {
            channel.writeAndFlush("[SERVER] - " + incoming.remoteAddress() + " 离开\n")
        }
        group.remove(ctx.channel())
    }

    /**
     * 服务端监听到客户端活动
     */
    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        log.info("Client:" + channel.remoteAddress() + "在线")
    }

    /**
     * 有用户断开连接时
     */
    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        log.info("Client:" + channel.remoteAddress() + "掉线")
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        val channel = ctx.channel()
        log.error("Client:" + channel.remoteAddress() + "异常")
        cause.printStackTrace()
        ctx.close()
    }

    /**
     * 用户发送过来的数据结束之后
     */
    @Throws(Exception::class)
    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

}
