package xyz.lzw.nestful.service.controller;

import com.google.inject.Inject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import xyz.lzw.nestful.annotations.*;
import xyz.lzw.nestful.service.Service;
import xyz.lzw.nestful.service.TestEntity;

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

    @GET
    public Object get() {
        System.out.println("get");
        return "hello restful.";
    }

    @POST
    public Object post(TestEntity entity) {
        System.out.println("post");
        return "hello " + entity.name;
    }

    @DELETE("/{id}")
    public Object delete(@Path("id") String id) {
        System.out.println("delete");
        return "ok";
    }

    @PUT("/{id}")
    public Object put(@Path("id") String id, TestEntity entity) {
        System.out.println(entity.name);
        System.out.println(entity.age);
        return "ok";
    }

    @Override
    public TestEntity getForm(String id, Class<TestEntity> clazz) {
        System.out.println(id);
        return null;
    }
}
