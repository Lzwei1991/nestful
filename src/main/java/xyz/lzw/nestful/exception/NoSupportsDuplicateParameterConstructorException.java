package xyz.lzw.nestful.exception;

import java.lang.reflect.Constructor;

/**
 * TODO
 *
 * @author liangzhuowei
 * @date 2022/3/26
 * @since open-jdk 11
 */
public class NoSupportsDuplicateParameterConstructorException extends RuntimeException {
    public NoSupportsDuplicateParameterConstructorException(Class<?> clazz, Constructor<?> constructor) {
        super(clazz + "'s constructor has duplicate parameter: " + constructor);
    }
}
