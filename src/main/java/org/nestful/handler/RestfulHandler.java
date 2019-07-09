package org.nestful.handler;

import com.alibaba.fastjson.JSON;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.StringUtil;
import org.nestful.annotations.*;
import org.nestful.common.Utils;
import org.nestful.model.ref.RestfulMethods;
import org.nestful.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.*;

/**
 * @author Lzw
 * @date 2019/5/5
 * @since JDK 1.8
 */
@SuppressWarnings("unchecked")
public class RestfulHandler extends ChannelInboundHandlerAdapter {
    private static Logger log = LoggerFactory.getLogger(RestfulHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof HttpRequest)) {
            ReferenceCountUtil.release(msg);
            return;
        }

        try {
            FullHttpRequest req = (FullHttpRequest) msg;
            String uri = req.uri();
            if (uri.contains("?")) {
                uri = uri.substring(0, uri.indexOf("?"));
            }

            HttpMethod httpMethod = req.method();
            Map.Entry<Pattern, Set<Method>> entry = RestfulMethods.requests(uri);
            if (entry == null) {
                status(ctx, HttpResponseStatus.NOT_FOUND);
                return;
            }

            Set<Method> methodSet = entry.getValue();
            Pattern pattern = entry.getKey();
            Matcher matcher = pattern.matcher(uri);
            if (matcher.matches()) {

                Method o = null;
                for (Method m : methodSet) {
                    if (HttpMethod.POST.equals(httpMethod) && m.getAnnotation(POST.class) != null) {
                        o = m;
                    } else if (HttpMethod.GET.equals(httpMethod) && m.getAnnotation(GET.class) != null) {
                        o = m;
                    } else if (HttpMethod.PUT.equals(httpMethod) && m.getAnnotation(PUT.class) != null) {
                        o = m;
                    } else if (HttpMethod.DELETE.equals(httpMethod) && m.getAnnotation(DELETE.class) != null) {
                        o = m;
                    }
                }

                if (o == null) {
                    status(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
                    return;
                }

                Method method = o;

                Injector injector = Guice.createInjector(binder -> {
                    for (String key : Utils.getNamedGroupCandidates(pattern.pattern())) {
                        binder.bind(String.class).annotatedWith(Params.param(key)).toInstance(matcher.group(key));
                    }
                    binder.bind(ChannelHandlerContext.class).toInstance(ctx);
                    binder.bind(FullHttpRequest.class).toInstance(req);
                    binder.bind(Service.class).to((Class<? extends Service>) method.getDeclaringClass());

                    for (Parameter parameter : method.getParameters()) {
                        if (parameter.getAnnotation(PathParam.class) != null) {
                            // PathParam pathParam = parameter.getAnnotation(PathParam.class);
                            // binder.bind(parameter.getType())
                            //         .annotatedWith(Params.param(pathParam.value()))
                            //         .toInstance(matcher.group(pathParam.value()));
                            continue;
                        }

                        Class type = parameter.getType();
                        String body = req.content().toString(Charset.forName("UTF-8"));
                        if (!StringUtil.isNullOrEmpty(body)) {
                            if (req.headers().get(CONTENT_TYPE).toLowerCase().contains("json")) {
                                binder.bind(type).toInstance(JSON.toJavaObject(JSON.parseObject(body), type));
                            } else {
                                binder.bind(type).toInstance(JAXB.unmarshal(body, type));
                            }
                        } else {
                            try {
                                binder.bind(type).toInstance(type.newInstance());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }


                    // binder.bindInterceptor(any(), any(), invocation -> {
                    //     if (invocation.getMethod().equals(method))
                    //         return invocation.proceed();
                    //     return null;
                    // });
                });
                Service service = injector.getInstance(Service.class);
                Object[] objects = Stream.of(method.getParameters())
                        .map(parameter -> {
                            if (parameter.getAnnotation(PathParam.class) != null) {
                                return injector.getBinding(String.class);
                            }
                            return injector.getInstance(parameter.getType());
                        })
                        .toArray();
                // method.invoke(service, objects);
                Object result = method.invoke(service, objects);
                FullHttpResponse response = this.createResponse(req, result);
                ctx.writeAndFlush(response);
            } else {
                status(ctx, HttpResponseStatus.NOT_FOUND);
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
     * create response
     *
     * @param result result value
     * @return FullHttpResponse
     */
    public FullHttpResponse createResponse(HttpRequest req, Object result) {
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
}