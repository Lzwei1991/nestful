package org.wei.restful.model.ref;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lzw
 * @date 2019/4/30
 * @since JDK 1.8
 */
public class RestfulMethods {
    private static Map<Pattern, Method> POST = new ConcurrentHashMap<>();

    public static Map.Entry<Pattern, Method> POST(String uri) {

        for (Map.Entry<Pattern, Method> entry : POST.entrySet()) {
            Matcher m = entry.getKey().matcher(uri);
            if (m.matches()) {
                return entry;
            }
        }
        return null;
    }
}
