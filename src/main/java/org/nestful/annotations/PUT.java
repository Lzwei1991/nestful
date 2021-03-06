package org.nestful.annotations;

import java.lang.annotation.*;

/**
 * @author Lzw
 * @date 2019/4/30
 * @since JDK 1.8
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PUT {
    String value() default "";
}
