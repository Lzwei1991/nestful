package org.wei.restful.model.ref;

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
                Optional.ofNullable(m.getAnnotation(POST.class)).ifPresent(post -> RestfulMethods.addPostMethod(uri, m));
                Optional.ofNullable(m.getAnnotation(PUT.class)).ifPresent(put -> RestfulMethods.addPutMethod(uri, m));
                Optional.ofNullable(m.getAnnotation(GET.class)).ifPresent(get -> RestfulMethods.addGetMethod(uri, m));
                Optional.ofNullable(m.getAnnotation(DELETE.class)).ifPresent(delete -> RestfulMethods.addDeleteMethod(uri, m));
            }
        }
    }

}
