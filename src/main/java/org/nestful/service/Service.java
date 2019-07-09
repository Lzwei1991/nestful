package org.nestful.service;

import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;

import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.*;

public abstract class Service<T> {
    private ChannelHandlerContext ctx;
    private FullHttpRequest req;

    private T entity;

    @Inject
    public Service(ChannelHandlerContext ctx, FullHttpRequest req) {
        this.ctx = ctx;
        this.req = req;
    }

    public abstract T getEntity(String id, Class<T> clazz);

}