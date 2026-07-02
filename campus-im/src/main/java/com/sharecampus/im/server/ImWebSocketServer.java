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

    /** 消息处理器 — JSON 格式: {"receiverId":10001,"content":"hello"} */
    @ChannelHandler.Sharable
    public static class ImMessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
        private static final com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
            try {
                String payload = frame.text();
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> msg = mapper.readValue(payload, java.util.Map.class);
                Long receiverId = msg.get("receiverId") != null ? ((Number) msg.get("receiverId")).longValue() : null;
                if (receiverId == null) { ctx.writeAndFlush(new TextWebSocketFrame("{\"error\":\"缺少receiverId\"}")); return; }

                Channel receiverChannel = USER_CHANNELS.get(receiverId);
                if (receiverChannel != null && receiverChannel.isActive()) {
                    receiverChannel.writeAndFlush(new TextWebSocketFrame(payload));
                } else {
                    ImMessageRouter.saveOffline(receiverId, payload);
                }
                ctx.writeAndFlush(new TextWebSocketFrame("{\"status\":\"ok\"}"));
            } catch (Exception e) {
                ctx.writeAndFlush(new TextWebSocketFrame("{\"error\":\"消息格式错误\"}"));
            }
        }
        @Override public void handlerAdded(ChannelHandlerContext ctx) { log.debug("连接建立: {}", ctx.channel().id()); }
        @Override public void handlerRemoved(ChannelHandlerContext ctx) { log.debug("连接断开: {}", ctx.channel().id()); }
    }
}
