package org.wei.restful.model.ref;

import org.reflections.Reflections;
import org.wei.restful.annotations.*;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

/**
 * @author Lzw
 * @date 2019/4/30
 * @since JDK 1.8
 */
public class Reflection {

    /**
     * getting child class in package path
     *
     * @param packagePath package path
     * @return set for child service class
     */
    public static Set<Class<?>> scannerServiceChild(String packagePath) {
        Reflections reflections = new Reflections(packagePath);
        return reflections.getTypesAnnotatedWith(Path.class);
    }

    /**
     * loading requests routing path
     *
     * @param classes classes for annotation with {@link Path}
     */
    public static void addRoutePath(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            Path path = clazz.getAnnotation(Path.class);
            String uri = path.value();
            Method[] methods = clazz.getMethods();
            for (Method m : methods) {
                Optional.ofNullable(m.getAnnotation(POST.class))
                        .ifPresent(post -> RestfulMethods.addPostMethod(uri + post.value(), m));
                Optional.ofNullable(m.getAnnotation(PUT.class))
                        .ifPresent(put -> RestfulMethods.addPutMethod(uri + put.value(), m));
                Optional.ofNullable(m.getAnnotation(GET.class))
                        .ifPresent(get -> RestfulMethods.addGetMethod(uri + get.value(), m));
                Optional.ofNullable(m.getAnnotation(DELETE.class))
                        .ifPresent(delete -> RestfulMethods.addDeleteMethod(uri + delete.value(), m));
            }
        }
    }

}
