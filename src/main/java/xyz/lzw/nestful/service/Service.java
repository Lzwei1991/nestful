package xyz.lzw.nestful.service;

import com.google.inject.Inject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public abstract class Service<T> {
    private ChannelHandlerContext ctx;
    private FullHttpRequest req;

    private T form;

    @Inject
    public Service(ChannelHandlerContext ctx, FullHttpRequest req) {
        this.ctx = ctx;
        this.req = req;
    }

    public abstract T getForm(String id, Class<T> clazz);

}