package org.wei.restful.model.ref;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.wei.restful.annotations.Path;
import org.wei.restful.service.Service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

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
    public static Set<Class<?>> scannerServiceChild(String packagePath) {
        Reflections reflections = new Reflections(packagePath);
        return reflections.getTypesAnnotatedWith(Path.class);
    }

    public static void addRouttingPath(Set<Class<? extends Service>> classes) {
        for (Class<? extends Service> clazz : classes) {
            Path path = clazz.getAnnotation(Path.class);
            String uri = path.value();
            Method[] methods = clazz.getMethods();
            for (Method m : methods) {
                Annotation[] annotations = m.getAnnotations();

            }
        }
    }

}
