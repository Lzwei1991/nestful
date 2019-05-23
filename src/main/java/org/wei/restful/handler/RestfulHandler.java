package org.wei.restful.handler;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wei.restful.annotations.Params;
import org.wei.restful.common.Utils;
import org.wei.restful.model.ref.RestfulMethods;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.inject.matcher.Matchers.any;

/**
 * @author Lzw
 * @date 2019/5/5
 * @since JDK 1.8
 */
public class RestfulHandler extends ChannelInboundHandlerAdapter {
    private static Logger log = LoggerFactory.getLogger(RestfulHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (!(msg instanceof HttpRequest))
            return;

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
                            binder.bind(String.class).annotatedWith(Params.param(key)).toInstance(matcher.group(key));
                        }
                        binder.bind(ChannelHandlerContext.class).toInstance(ctx);
                        binder.bind(FullHttpRequest.class).toInstance(req);
                        binder.bind(Object.class).to(method.getDeclaringClass());

                        binder.bindInterceptor(any(), any(), invocation -> {
                            if (invocation.getMethod().equals(method))
                                return invocation.proceed();
                            return null;
                        });
                    });
                    // Object service =
                    injector.getInstance(method.getDeclaringClass());
                    // Object[] objects = Stream.of(method.getParameters())
                    //         .map(parameter -> injector.getInstance(parameter.getType()))
                    //         .toArray();
                    // Object result = method.invoke(service, objects);
                    // FullHttpResponse response = this.response(req, result);
                    // ctx.writeAndFlush(response);
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
}