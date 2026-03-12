package xyz.lzw.nestful.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.lzw.nestful.annotations.*;
import xyz.lzw.nestful.ref.model.Reflection;
import xyz.lzw.nestful.ref.model.RestfulMethods;
import xyz.lzw.nestful.ref.params.PathParams;
import xyz.lzw.nestful.service.Service;
import xyz.lzw.nestful.ref.model.ParamMeta;
import xyz.lzw.nestful.ref.model.ParamMeta.ParamSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.*;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author Lzw
 * @date 2022-03-24
 * @since JDK 11
 */
@SuppressWarnings("unchecked")
public class RestfulHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(RestfulHandler.class);

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    /**
     * 缓存 Service 子类的构造函数，避免每次请求都通过反射查找。
     */
    private static final ConcurrentHashMap<Class<? extends Service>, Constructor<? extends Service>>
            SERVICE_CTORS = new ConcurrentHashMap<>();

    /**
     * 缓存每个 Controller 方法的参数元数据，避免重复反射和注解解析。
     */
    private static final ConcurrentHashMap<Method, ParamMeta[]> METHOD_PARAM_METAS = new ConcurrentHashMap<>();

    public RestfulHandler(String packagePath) {
        Reflection.addRoutePath(Reflection.scannerServiceChild(packagePath));
    }


    /**
     * channel reader
     * 数据交互
     *
     * @param ctx socket
     * @param msg body
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof HttpRequest)) {
            ReferenceCountUtil.release(msg);
            return;
        }

        try {
            FullHttpRequest req = (FullHttpRequest) msg;
            String uri = req.uri();
            // without url query params
            if (uri.contains("?")) {
                uri = uri.substring(0, uri.indexOf("?"));
            }

            HttpMethod httpMethod = req.method();
            Map.Entry<Pattern, Set<Method>> entry = RestfulMethods.requests(uri);
            if (entry == null) {
                // url not found
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
                        break;
                    } else if (HttpMethod.GET.equals(httpMethod) && m.getAnnotation(GET.class) != null) {
                        o = m;
                        break;
                    } else if (HttpMethod.PUT.equals(httpMethod) && m.getAnnotation(PUT.class) != null) {
                        o = m;
                        break;
                    } else if (HttpMethod.DELETE.equals(httpMethod) && m.getAnnotation(DELETE.class) != null) {
                        o = m;
                        break;
                    }
                }

                if (o == null) {
                    // options request support access-control-allow-origin
                    if (HttpMethod.OPTIONS.equals(httpMethod)) {
                        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(new byte[]{}));
                        response.headers().set(CONTENT_TYPE, "application/json;charset=UTF-8");
                        String host = req.headers().get("Host");
                        log.info("host:" + host);
                        // access allow origin
                        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                        response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS, "*");//允许headers自定义
                        response.headers().set(ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE");
                        response.headers().set(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                        ctx.writeAndFlush(response);
                    } else {
                        status(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
                    }
                    return;
                }

                Method method = o;

                // 创建 Service 实例（Controller）
                Class<? extends Service> serviceClass = (Class<? extends Service>) method.getDeclaringClass();
                Service service = createServiceInstance(serviceClass, ctx, req);

                // 构造方法参数
                Object[] args = buildMethodArguments(method, matcher, req, ctx);

                Object result = method.invoke(service, args);
                FullHttpResponse response = this.createResponse(req, result);
                ctx.writeAndFlush(response);
            } else {
                status(ctx, HttpResponseStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
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
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);

        // 默认 text/plain
        response.headers().set(CONTENT_TYPE, TEXT_PLAIN + ";charset=UTF-8");
        String accept = req.headers().get(ACCEPT);
        String lowerAccept = accept == null ? "" : accept.toLowerCase();
        boolean prefersJson = lowerAccept.contains("json") || lowerAccept.contains("*/*");

        if (result == null) {
            response.content().clear().writeBytes(Unpooled.copiedBuffer("", HttpConstants.DEFAULT_CHARSET));
        } else if (result instanceof String) {
            // 明确返回 String，则按字符串处理
            response.content().clear().writeBytes(Unpooled.copiedBuffer((String) result, HttpConstants.DEFAULT_CHARSET));
        } else if (result instanceof Map) {
            // Map 默认 JSON
            String json = GSON.toJson(result);
            response.content().clear().writeBytes(Unpooled.copiedBuffer(json, HttpConstants.DEFAULT_CHARSET));
            response.headers().set(CONTENT_TYPE, APPLICATION_JSON + ";charset=UTF-8");
        } else if (result instanceof ByteBuf || result instanceof byte[]) {
            byte[] buf = result instanceof ByteBuf ? ((ByteBuf) result).array() : (byte[]) result;
            response.content().clear().writeBytes(buf);
            response.headers().set(CONTENT_TYPE, APPLICATION_OCTET_STREAM);
        } else {
            // 其他对象：默认也按 JSON 返回
            if (prefersJson || StringUtil.isNullOrEmpty(lowerAccept)) {
                String json = GSON.toJson(result);
                response.content().clear().writeBytes(Unpooled.copiedBuffer(json, HttpConstants.DEFAULT_CHARSET));
                response.headers().set(CONTENT_TYPE, APPLICATION_JSON + ";charset=UTF-8");
            } else {
                // 客户端显式只接受非 JSON，再退回 toString
                response.content().clear().writeBytes(Unpooled.copiedBuffer(result.toString(), HttpConstants.DEFAULT_CHARSET));
            }
        }

        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(ACCESS_CONTROL_ALLOW_METHODS, "GET,PUT,POST,DELETE,OPTIONS,HEAD,PATCH,TRACE");
        response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS, "Accept,Origin,X-Requested-With,Content-Type,Last-Modified,device,token");

        return response;
    }

    /**
     * 为指定的 Service 子类创建实例。
     * 要求 Service 子类有形如 (ChannelHandlerContext, FullHttpRequest) 的构造函数。
     */
    private Service createServiceInstance(Class<? extends Service> serviceClass,
                                          ChannelHandlerContext ctx,
                                          FullHttpRequest req) throws Exception {
        try {
            Constructor<? extends Service> ctor = SERVICE_CTORS.computeIfAbsent(serviceClass, clazz -> {
                try {
                    Constructor<? extends Service> c =
                            clazz.getConstructor(ChannelHandlerContext.class, FullHttpRequest.class);
                    c.setAccessible(true);
                    return c;
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException(
                            "Service class " + clazz.getName()
                                    + " must provide a constructor (ChannelHandlerContext, FullHttpRequest)", e);
                }
            });
            return ctor.newInstance(ctx, req);
        } catch (IllegalStateException e) {
            // 构造函数找不到等配置错误
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 构造 Controller 方法参数数组：
     * - @Path 参数：从 matcher 的命名分组中取
     * - ChannelHandlerContext / FullHttpRequest：直接注入
     * - 其他参数：根据 body 用 JSON 反序列化到对应类型（已在 2/3 节中做好 Content-Type 判断）
     */
    private Object[] buildMethodArguments(Method method,
                                          Matcher matcher,
                                          FullHttpRequest req,
                                          ChannelHandlerContext ctx) {
        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return new Object[0];
        }

        // 只解析一次 body，避免重复转换
        String body = req.content().toString(StandardCharsets.UTF_8);
        String contentType = req.headers().get(CONTENT_TYPE);
        String lowerContentType = contentType == null ? "" : contentType.toLowerCase();

        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            // 1. 路径参数
            Path pathAnn = parameter.getAnnotation(Path.class);
            if (pathAnn != null) {
                String groupName = pathAnn.value();
                args[i] = matcher.group(groupName);
                continue;
            }

            Class<?> type = parameter.getType();

            // 2. 特殊类型直接注入
            if (ChannelHandlerContext.class.isAssignableFrom(type)) {
                args[i] = ctx;
                continue;
            }
            if (FullHttpRequest.class.isAssignableFrom(type)) {
                args[i] = req;
                continue;
            }

            // 3. 业务对象：从 body 反序列化
            if (!StringUtil.isNullOrEmpty(body)) {
                if (lowerContentType.contains("application/json")) {
                    // application/json
                    args[i] = GSON.fromJson(body, type);
                } else if (lowerContentType.contains("application/x-www-form-urlencoded")) {
                    // application/x-www-form-urlencoded
                    String decoded = URLDecoder.decode(body, StandardCharsets.UTF_8);
                    args[i] = GSON.fromJson(decoded, type);
                } else if (!lowerContentType.isEmpty()) {
                    throw new RuntimeException("not support Content-Type：" + contentType);
                } else {
                    // 没有 Content-Type，则按默认无参构造处理
                    args[i] = newInstanceOrNull(type);
                }
            } else {
                // 空 body，尝试调用无参构造
                args[i] = newInstanceOrNull(type);
            }
        }

        return args;
    }

    private ParamMeta[] getOrCreateParamMetas(Method method) {
        return METHOD_PARAM_METAS.computeIfAbsent(method, m -> {
            Parameter[] parameters = m.getParameters();
            ParamMeta[] metas = new ParamMeta[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                Parameter p = parameters[i];
                Class<?> type = p.getType();

                Path pathAnn = p.getAnnotation(Path.class);
                if (pathAnn != null) {
                    metas[i] = new ParamMeta(ParamSource.PATH, String.class, pathAnn.value());
                } else if (ChannelHandlerContext.class.isAssignableFrom(type)) {
                    metas[i] = new ParamMeta(ParamSource.CTX, type, null);
                } else if (FullHttpRequest.class.isAssignableFrom(type)) {
                    metas[i] = new ParamMeta(ParamSource.REQ, type, null);
                } else {
                    metas[i] = new ParamMeta(ParamSource.BODY_OR_DEFAULT, type, null);
                }
            }
            return metas;
        });
    }

    private Object newInstanceOrNull(Class<?> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("Failed to create instance for type: {}", type, e);
            return null;
        }
    }
}