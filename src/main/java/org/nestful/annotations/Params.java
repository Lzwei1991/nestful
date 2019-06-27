package org.nestful.annotations;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.internal.Annotations;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Lzw
 * @date 2019/5/23
 * @since JDK 1.8
 */
public class Params {

    public static class PathParamImpl implements PathParam, Serializable {

        private static final long serialVersionUID = -3192454276606618806L;
        private final String value;

        private PathParamImpl(String value) {
            this.value = checkNotNull(value, "name");
        }

        @Override
        public String value() {
            return this.value;
        }

        @Override
        public int hashCode() {
            // This is specified in java.lang.Annotation.
            return (127 * "value".hashCode()) ^ value.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PathParam)) {
                return false;
            }

            PathParam other = (PathParam) o;
            return value.equals(other.value());
        }

        @Override
        public String toString() {
            return "@" + PathParam.class.getName() + "(value=" + Annotations.memberValueString(value) + ")";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return PathParam.class;
        }

    }


    private Params() {
    }

    /**
     * Creates a {@link Named} annotation with {@code name} as the value.
     */
    public static PathParam param(String name) {
        return new PathParamImpl(name);
    }

    /**
     * Creates a constant binding to {@code @Named(key)} for each entry in {@code properties}.
     */
    public static void bindProperties(Binder binder, Map<String, String> properties) {
        binder = binder.skipSources(Names.class);
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            binder.bind(Key.get(String.class, new PathParamImpl(key))).toInstance(value);
        }
    }

    /**
     * Creates a constant binding to {@code @Named(key)} for each property. This method binds all
     * properties including those inherited from {@link Properties defaults}.
     */
    public static void bindProperties(Binder binder, Properties properties) {
        binder = binder.skipSources(Names.class);

        // use enumeration to include the default properties
        for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements(); ) {
            String propertyName = (String) e.nextElement();
            String value = properties.getProperty(propertyName);
            binder.bind(Key.get(String.class, new PathParamImpl(propertyName))).toInstance(value);
        }
    }
}


