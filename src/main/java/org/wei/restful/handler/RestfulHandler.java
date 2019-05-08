package org.wei.restful.handler;

import com.alibaba.fastjson.JSON;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wei.restful.common.Utils;
import org.wei.restful.model.ref.RestfulMethods;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.*;

/**
 * @author Lzw
 * @date 2019/5/5
 * @since JDK 1.8
 */
public class RestfulHandler extends ChannelInboundHandlerAdapter {
    private static Logger log = LoggerFactory.getLogger(RestfulHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (!(msg instanceof HttpRequest)) {
            return;
        }

        try {
            FullHttpRequest req = (FullHttpRequest) msg;
            String uri = req.uri();
            HttpMethod httpMethod = req.method();
            Map.Entry<Pattern, Method> entry;
            if (HttpMethod.POST.equals(httpMethod)) {
                entry = RestfulMethods.POST(uri);
            } else if (HttpMethod.GET.equals(httpMethod)) {
                entry = RestfulMethods.GET(uri);
            } else if (HttpMethod.PUT.equals(httpMethod)) {
                entry = RestfulMethods.PUT(uri);
            } else if (HttpMethod.DELETE.equals(httpMethod)) {
                entry = RestfulMethods.DELETE(uri);
            } else {
                status(ctx, HttpResponseStatus.OK);
                return;
            }

            if (entry == null) {
                status(ctx, HttpResponseStatus.NOT_FOUND);
            } else {
                Method method = entry.getValue();
                Pattern pattern = entry.getKey();
                Matcher matcher = pattern.matcher(uri);
                if (matcher.matches()) {
                    Injector injector = Guice.createInjector(binder -> {
                        for (String key : Utils.getNamedGroupCandidates(pattern.pattern())) {
                            binder.bind(String.class).annotatedWith(Names.named(key)).toInstance(matcher.group(key));
                        }
                        binder.bind(ChannelHandlerContext.class).toInstance(ctx);
                        binder.bind(FullHttpRequest.class).toInstance(req);
                        binder.bind(Object.class).to(method.getDeclaringClass());
                        for (Parameter parameter : method.getParameters()) {
                            if (APPLICATION_JSON.toString().equals(req.headers().get(CONTENT_TYPE))) {
                                Class type = parameter.getType();
                                binder.bind(type).toInstance(
                                        JSON.toJavaObject(
                                                JSON.parseObject(req.content().toString(Charset.forName("UTF-8"))),
                                                type
                                        )
                                );
                            } else {

                            }
                        }
                    });
                    Object service = injector.getInstance(method.getDeclaringClass());
                    Object[] objects = Stream.of(method.getParameters())
                            .map(parameter -> injector.getInstance(parameter.getType()))
                            .toArray();
                    Object result = method.invoke(service, objects);
                    this.response(req, result);
                } else {
                    status(ctx, HttpResponseStatus.NOT_FOUND);
                }
            }

        } catch (Exception e) {
            log.error("", e);
            status(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * response http status
     *
     * @param ctx    socket channel
     * @param status http status
     */
    private void status(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                status, Unpooled.copiedBuffer("Failure: " + status.toString()
                + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


    /**
     * response result
     *
     * @param result result
     * @return
     */
    private FullHttpResponse response(FullHttpRequest req, Object result) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

        if (req.headers().get(ACCEPT).contains("json")) {
            response.headers().set(CONTENT_TYPE, APPLICATION_JSON + ";charset=UTF-8");
        } else if (req.headers().get(ACCEPT).contains("text")) {
            response.headers().set(CONTENT_TYPE, TEXT_PLAIN + ";charset=UTF-8");
        } else if (req.headers().get(ACCEPT).contains("xml")) {
            response.headers().set(CONTENT_TYPE, "application/xml;charset=UTF-8");
        } else {
            response.headers().set(CONTENT_TYPE, APPLICATION_JSON + ";charset=UTF-8");
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
            return response;
        }
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS, "x-requested-with,content-type");

        return response;
    }
}