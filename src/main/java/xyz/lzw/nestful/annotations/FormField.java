package xyz.lzw.nestful.annotations;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.*;

/**
 * request form field
 *
 * @author Lzw
 * @date 2022-03-24
 * @since JDK 11
 */
@BindingAnnotation
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FormField {
    String value();
}
