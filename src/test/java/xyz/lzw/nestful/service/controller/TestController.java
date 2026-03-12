package xyz.lzw.nestful.service.controller;

import com.google.inject.Inject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import xyz.lzw.nestful.annotations.*;
import xyz.lzw.nestful.service.Service;
import xyz.lzw.nestful.service.TestEntity;

import java.util.Map;

/**
 * TODO
 *
 * @author liangzhuowei
 * @date 2022/3/24
 * @since open-jdk 11
 */
@Restful("/api/test")
public class TestController extends Service<TestEntity> {

    @Inject
    public TestController(ChannelHandlerContext ctx, FullHttpRequest req) {
        super(ctx, req);
    }

    @GET("/get")
    public Object get() {
        return new TestEntity("lzw", 20);
    }

    @POST
    public Object post(TestEntity entity) {

        return Map.of("success", true, "message", "ok", "data", entity);
    }

    @DELETE("/{id}")
    public Object delete(@Path("id") String id) {
        return Map.of("success", true, "message", "ok");
    }

    @PUT("/{id}")
    public Object put(@Path("id") String id, TestEntity entity) {
        return Map.of("success", true, "message", "ok");
    }

    @Override
    public TestEntity getForm(String id, Class<TestEntity> clazz) {
        System.out.println(id);
        return null;
    }
}
