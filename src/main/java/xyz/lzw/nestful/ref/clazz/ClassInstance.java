package xyz.lzw.nestful.ref.clazz;

import xyz.lzw.nestful.exception.NoPublicConstructorsException;
import xyz.lzw.nestful.exception.NoSupportsDuplicateParameterConstructorException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author liangzhuowei
 * @date 2022/3/26
 * @since open-jdk 11
 */
public class ClassInstance {
    // instance the object
    public static <T> T instance(Class<T> clazz, Object... obj) {
        Constructor<?>[] constructors = clazz.getConstructors();
        if (constructors.length == 0) {
            throw new NoPublicConstructorsException(clazz);
        }
        List<Constructor<?>> collect = Arrays.stream(constructors)
                .filter(constructor -> constructor.getParameters().length == obj.length)
                .collect(Collectors.toList());
        if (collect.size() == 0) {
            try {
                return (T) constructors[0].newInstance((Object) null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            List<? extends Class<?>> objectClassList = Arrays.stream(obj).map(Object::getClass).collect(Collectors.toList());
            Constructor<?> c = collect.stream()
                    .filter(constructor -> Arrays.stream(constructor.getParameters()).allMatch(parameter -> objectClassList.contains(parameter.getType())))
                    .findAny().orElse(null);
            if (c == null) {
                throw new NoPublicConstructorsException(clazz);
            }

            Object[] objs = new Object[obj.length];
            Parameter[] parameters = c.getParameters();

            Set<Class<?>> classSet = Arrays.stream(parameters).map(Parameter::getClass).collect(Collectors.toSet());
            if (classSet.size() != parameters.length) {
                throw new NoSupportsDuplicateParameterConstructorException(clazz, c);
            }

            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                objs[i] = Arrays.stream(obj).filter(o -> o.getClass().equals(parameter.getType())).findAny().orElseThrow();
            }
            try {
                return (T) c.newInstance(objs);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
