# nestful
本项目基于 netty、 guice ，是一个 IOT http 框架，适合用于构建 restful 的 api。\
与其他基于 netty 的 http 框架不一样的是，其他框架都是将 netty 封装起来然后再作处理。|
本项目只是利用反射调用，inject 注入的方式去处理。\
使用者只需要在 netty 的 channel pipe 中加入 RestfulHandler 即可使用。\
\
\
template: 
```
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
            ch.pipeline().addLast(new RestfulHandler());
        }
    });
ChannelFuture f = bootstrapHttp.bind(8080).sync();
System.out.println("HTTP[" + 8080 + "], Http Server start...");
f.channel().closeFuture().sync();
```
