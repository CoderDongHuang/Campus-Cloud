package com.sharecampus.im.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ImWebSocketServer {

    @Value("${im.websocket.port:9090}")
    private int port;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    public static final ConcurrentHashMap<Long, Channel> USER_CHANNELS = new ConcurrentHashMap<>();

    @PostConstruct
    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        new Thread(() -> {
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ch.pipeline()
                                        .addLast(new HttpServerCodec())
                                        .addLast(new ChunkedWriteHandler())
                                        .addLast(new HttpObjectAggregator(65536))
                                        .addLast(new WebSocketServerProtocolHandler("/ws"))
                                        .addLast(new ImMessageHandler());
                            }
                        });
                ChannelFuture future = bootstrap.bind(port).sync();
                log.info("IM WebSocket 启动: port={}", port);
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @PreDestroy
    public void stop() {
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
    }

    /** 消息处理器 */
    @ChannelHandler.Sharable
    public static class ImMessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
            // 简化版：收到消息 → 回显
            ctx.writeAndFlush(new TextWebSocketFrame("{\"echo\":\"" + frame.text() + "\"}"));
        }
        @Override
        public void handlerAdded(ChannelHandlerContext ctx) { log.debug("连接建立: {}", ctx.channel().id()); }
        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) { log.debug("连接断开: {}", ctx.channel().id()); }
    }
}
