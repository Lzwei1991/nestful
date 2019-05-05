package org.wei.restful.model.ref;

import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.wei.restful.annotations.*;
import org.wei.restful.service.Service;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

/**
 * @author Lzw
 * @date 2019/4/30
 * @since JDK 1.8
 */
@Slf4j
public class Reflection {

    /**
     * getting child class in package path
     *
     * @param packagePath package path
     * @return set for child service class
     */
    public static Set<Class<? extends Service>> scannerServiceChild(String packagePath) {
        Reflections reflections = new Reflections(packagePath);
        return reflections.getSubTypesOf(Service.class);
    }

    /**
     * loading requests routing path
     *
     * @param classes child classes for {@link Service}
     */
    public static void addRoutePath(Set<Class<? extends Service>> classes) {
        for (Class<? extends Service> clazz : classes) {
            Path path = clazz.getAnnotation(Path.class);
            String uri = path.value();
            Method[] methods = clazz.getMethods();
            for (Method m : methods) {
                POST post = m.getAnnotation(POST.class);
                if (post != null) {
                    concat(uri, post.value()).ifPresent(postUrl -> RestfulMethods.addPostMethod(postUrl, m));
                }

                GET get = m.getAnnotation(GET.class);
                if (get != null) {
                    concat(uri, get.value()).ifPresent(getUrl -> RestfulMethods.addGetMethod(getUrl, m));
                }

                PUT put = m.getAnnotation(PUT.class);
                if (put != null) {
                    concat(uri, put.value()).ifPresent(putUrl -> RestfulMethods.addPutMethod(putUrl, m));
                }

                DELETE delete = m.getAnnotation(DELETE.class);
                if (delete != null) {
                    concat(uri, delete.value()).ifPresent(deleteUrl -> RestfulMethods.addDeleteMethod(deleteUrl, m));
                }
            }
        }
    }

    /**
     * @param path
     * @param methodPath
     * @return
     */
    private static Optional<String> concat(String path, String methodPath) {
        if (path != null) {
            String url;
            if (StringUtil.isNullOrEmpty(methodPath)) {
                if (methodPath.startsWith("/")) {
                    url = path + methodPath;
                } else if (path.endsWith("/")) {
                    url = path + methodPath;
                } else {
                    url = path + "/" + methodPath;
                }
            } else {
                url = path;
            }
            url = url.replace("//", "/");
            if (!url.equals("/") && url.endsWith("/")) {
                url = url.substring(0, url.lastIndexOf("/"));
            }
            return Optional.of(url);
        }
        return Optional.empty();
    }

}
