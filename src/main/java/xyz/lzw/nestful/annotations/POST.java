package xyz.lzw.nestful.annotations;

import java.lang.annotation.*;

/**
 * @author Lzw
 * @date 2022-03-24
 * @since JDK 11
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface POST {
    String value() default "";
}
