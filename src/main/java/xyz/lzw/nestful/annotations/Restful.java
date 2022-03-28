package xyz.lzw.nestful.annotations;

import java.lang.annotation.*;

/**
 * request uri
 *
 * @author Lzw
 * @date 2022-03-24
 * @since JDK 11
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Restful {
    String value();
}
