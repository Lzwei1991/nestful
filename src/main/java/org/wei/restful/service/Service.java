package org.wei.restful.service;

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

public abstract class Service {
    private ChannelHandlerContext ctx;
    private FullHttpRequest req;

    @Inject
    public Service(ChannelHandlerContext ctx, FullHttpRequest req) {
        this.ctx = ctx;
        this.req = req;
    }

    /**
     * create response
     *
     * @param result result value
     * @return FullHttpResponse
     */
    public FullHttpResponse createResponse(Object result) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

        response.headers().set(CONTENT_TYPE, TEXT_PLAIN + ";charset=UTF-8");
        String accept = req.headers().get(ACCEPT);
        if (!StringUtil.isNullOrEmpty(accept)) {
            if (accept.contains("json")) {
                response.headers().set(CONTENT_TYPE, APPLICATION_JSON + ";charset=UTF-8");
            } else if (accept.contains("xml")) {
                response.headers().set(CONTENT_TYPE, "application/xml;charset=UTF-8");
            }
        }

        if (result instanceof String || result instanceof Map) {
            // body content
            response.content().clear().writeBytes(Unpooled.copiedBuffer(result.toString(), HttpConstants.DEFAULT_CHARSET));
        } else if (result instanceof ByteBuf || result instanceof byte[]) {
            byte[] buf = result instanceof ByteBuf ? ((ByteBuf) result).array() : (byte[]) result;
            // body content
            response.content().clear().writeBytes(buf);
            response.headers().set(CONTENT_TYPE, APPLICATION_OCTET_STREAM);
        } else {
            response.content().clear().writeBytes(Unpooled.copiedBuffer(result.toString(), HttpConstants.DEFAULT_CHARSET));
            return response;
        }
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS, "x-requested-with,content-type");

        return response;
    }

    /**
     * @param response
     */
    public void response(FullHttpResponse response) {
        ctx.writeAndFlush(response);
    }

    /**
     * @param result
     */
    public void response(Object result) {
        response(createResponse(result));
    }

    public FullHttpResponse notFound() {
        return status(HttpResponseStatus.NOT_FOUND);
    }

    public FullHttpResponse status(HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        return response;
    }
}