package xyz.lzw.nestful.ref.model;

import org.reflections.Reflections;
import xyz.lzw.nestful.annotations.*;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

/**
 * @author Lzw
 * @date 2022-03-24
 * @since JDK 11
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
        return reflections.getTypesAnnotatedWith(Restful.class);
    }

    /**
     * loading requests routing path
     *
     * @param classes classes for annotation with {@link Restful}
     */
    public static void addRoutePath(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            Restful path = clazz.getAnnotation(Restful.class);
            String uri = path.value();
            Method[] methods = clazz.getMethods();
            for (Method m : methods) {
                Optional.ofNullable(m.getAnnotation(POST.class))
                        .ifPresent(post -> RestfulMethods.addMapping(uri + post.value(), m));
                Optional.ofNullable(m.getAnnotation(PUT.class))
                        .ifPresent(put -> RestfulMethods.addMapping(uri + put.value(), m));
                Optional.ofNullable(m.getAnnotation(GET.class))
                        .ifPresent(get -> RestfulMethods.addMapping(uri + get.value(), m));
                Optional.ofNullable(m.getAnnotation(DELETE.class))
                        .ifPresent(delete -> RestfulMethods.addMapping(uri + delete.value(), m));
            }
        }
    }

}
