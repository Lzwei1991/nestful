package org.wei.restful.annotations;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.*;

/**
 * @author Lzw
 * @date 2019/5/5
 * @since JDK 1.8
 */
@BindingAnnotation
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PathParam {
    String value();
}
