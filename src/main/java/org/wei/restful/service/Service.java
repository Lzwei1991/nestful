package org.wei.restful.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * base controller class
 *
 * @author Lzw
 * @date 2019/4/30
 * @since JDK 1.8
 */
public abstract class Service {
    private ChannelHandlerContext ctx;
    private FullHttpRequest req;

    public Service(ChannelHandlerContext ctx, FullHttpRequest req) {
        this.ctx = ctx;
        this.req = req;
    }
}
