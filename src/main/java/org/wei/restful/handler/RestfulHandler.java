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
import org.wei.restful.annotations.PathParam;
import org.wei.restful.model.ref.Reflection;
import org.wei.restful.model.ref.RestfulMethods;
import org.wei.restful.service.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Lzw
 * @date 2019/5/5
 * @since JDK 1.8
 */
public class RestfulHandler extends ChannelInboundHandlerAdapter {
    static {
        Set<Class<? extends Service>> classes = Reflection.scannerServiceChild("");
        Reflection.addRoutePath(classes);
    }

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
                sendError(ctx, HttpResponseStatus.OK);
                return;
            }

            if (entry == null) {
                sendError(ctx, HttpResponseStatus.NOT_FOUND);
            } else {
                Method method = entry.getValue();
                Class<?> clazz = method.getDeclaringClass();
                Constructor<?> constructor = clazz.getConstructor(ChannelHandlerContext.class, FullHttpRequest.class);
                Service service = (Service) constructor.newInstance(ctx, req);

                Injector injector = Guice.createInjector(binder -> {
                    Parameter[] parameters = method.getParameters();
                    for (Parameter parameter : parameters) {
                        PathParam param = parameter.getAnnotation(PathParam.class);
                        binder.bind(String.class).annotatedWith(PathParam.class).to()
                    }
                })


                System.out.println(method);
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                status, Unpooled.copiedBuffer("Failure: " + status.toString()
                + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

}
