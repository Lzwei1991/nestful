package xyz.lzw.nestful.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import xyz.lzw.nestful.handler.RestfulHandler;

/**
 * TODO
 *
 * @author liangzhuowei
 * @date 2022/3/24
 * @since open-jdk 11
 */
public class TestHttpServer {

    public static void main(String[] args) {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // http 服务
            ServerBootstrap bootstrapHttp = new ServerBootstrap();
            bootstrapHttp.group(bossGroup, workerGroup);
            bootstrapHttp.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrapHttp.channel(NioServerSocketChannel.class);
            bootstrapHttp.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    //ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                    ch.pipeline().addLast(new IdleStateHandler(60, 600, 600));
                    ch.pipeline().addLast(new HttpRequestDecoder());
                    ch.pipeline().addLast(new ChunkedWriteHandler());
                    ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024 * 8));//8k
                    ch.pipeline().addLast(new HttpResponseEncoder());
                    ch.pipeline().addLast(new HttpContentCompressor());
                    ch.pipeline().addLast(new RestfulHandler("org.nestful.service.controller"));
                }
            });
            int port = 8001;
            ChannelFuture f = bootstrapHttp.bind(port).sync();
            System.out.println("HTTP[" + port + "], Http Server start...");
            f.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
