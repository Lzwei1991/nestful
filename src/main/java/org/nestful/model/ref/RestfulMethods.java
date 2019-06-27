package org.nestful.model.ref;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.quote;

/**
 * @author Lzw
 * @date 2019/4/30
 * @since JDK 1.8
 */
public class RestfulMethods {
    private static Logger log = LoggerFactory.getLogger(RestfulMethods.class);

    private static Map<Pattern, Set<Method>> restful_mapping = new ConcurrentHashMap<>();

    /**
     * registration restful_mapping url
     *
     * @param url    post request uniform resource locator
     * @param method method
     */
    public static void addMapping(String url, Method method) {
        restful_mapping.computeIfAbsent(transform(url), key -> new HashSet<>()).add(method);
        log.info("Add [restful_mapping] request uri: {}", url);
    }

    /**
     * restful_mapping request
     *
     * @param url request url
     * @return matcher case
     */
    public static Map.Entry<Pattern, Set<Method>> requests(String url) {

        for (Map.Entry<Pattern, Set<Method>> entry : restful_mapping.entrySet()) {
            Matcher m = entry.getKey().matcher(url);
            if (m.matches()) {
                return entry;
            }
        }
        return null;
    }


    /**
     * transform string url to regular expression
     * <p>
     * get the url params
     *
     * @param url registration url
     * @return regular expression(Pattern)
     */
    private static Pattern transform(String url) {
        Pattern p = Pattern.compile("\\{.+?}");
        Matcher matcher = p.matcher(url);
        while (matcher.find()) {
            String val = matcher.group();
            url = url.replaceAll(quote(val), "(?" + val.replace("{", "<").replace("}", ">") + "[^/]+?" + ")");
        }
        return Pattern.compile("^" + url + "$");
    }
}
