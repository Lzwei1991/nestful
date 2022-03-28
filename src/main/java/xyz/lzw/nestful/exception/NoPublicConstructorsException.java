package xyz.lzw.nestful.exception;

/**
 * TODO
 *
 * @author liangzhuowei
 * @date 2022/3/26
 * @since open-jdk 11
 */
public class NoPublicConstructorsException extends RuntimeException {
    public NoPublicConstructorsException(Class<?> clazz) {
        super(clazz + " does not have public constructor!");
    }
}
