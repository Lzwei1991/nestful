package xyz.lzw.nestful.ref.params;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.internal.Annotations;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import xyz.lzw.nestful.annotations.Param;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO
 *
 * @author liangzhuowei
 * @date 2022/3/26
 * @since open-jdk 11
 */
public class RequestParams {


    public static class ParamImpl implements Param, Serializable {

        private static final long serialVersionUID = -3192454276606618806L;
        private final String value;

        private ParamImpl(String value) {
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
            if (!(o instanceof Param)) {
                return false;
            }

            Param other = (Param) o;
            return value.equals(other.value());
        }

        @Override
        public String toString() {
            return "@" + Param.class.getName() + "(value=" + Annotations.memberValueString(value) + ")";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Param.class;
        }

    }


    private RequestParams() {
    }

    /**
     * Creates a {@link Named} annotation with {@code name} as the value.
     */
    public static Param param(String name) {
        return new RequestParams.ParamImpl(name);
    }

    /**
     * Creates a constant binding to {@code @Named(key)} for each entry in {@code properties}.
     */
    public static void bindProperties(Binder binder, Map<String, String> properties) {
        binder = binder.skipSources(Names.class);
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            binder.bind(Key.get(String.class, new RequestParams.ParamImpl(key))).toInstance(value);
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
            binder.bind(Key.get(String.class, new RequestParams.ParamImpl(propertyName))).toInstance(value);
        }
    }

}
