package org.wei.restful.model.ref;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
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

    private static Map<Pattern, Method> POST = new ConcurrentHashMap<>();
    private static Map<Pattern, Method> GET = new ConcurrentHashMap<>();
    private static Map<Pattern, Method> PUT = new ConcurrentHashMap<>();
    private static Map<Pattern, Method> DELETE = new ConcurrentHashMap<>();


    /**
     * registration POST url
     *
     * @param url    post request uniform resource locator
     * @param method method
     */
    public static void addPostMethod(String url, Method method) {
        POST.put(transform(url), method);
        log.info("Add [POST] request uri: {}", url);
    }

    /**
     * POST request
     *
     * @param url request url
     * @return matcher case
     */
    public static Map.Entry<Pattern, Method> POST(String url) {

        for (Map.Entry<Pattern, Method> entry : POST.entrySet()) {
            Matcher m = entry.getKey().matcher(url);
            if (m.matches()) {
                return entry;
            }
        }
        return null;
    }

    /**
     * registration GET url
     *
     * @param url    post request uniform resource locator
     * @param method method
     */
    public static void addGetMethod(String url, Method method) {
        GET.put(transform(url), method);
        log.info("Add [GET] request uri: {}", url);
    }

    /**
     * GET request
     *
     * @param url request url
     * @return matcher case
     */
    public static Map.Entry<Pattern, Method> GET(String url) {

        for (Map.Entry<Pattern, Method> entry : GET.entrySet()) {
            Matcher m = entry.getKey().matcher(url);
            if (m.matches()) {
                return entry;
            }
        }
        return null;
    }

    /**
     * registration PUT url
     *
     * @param url    post request uniform resource locator
     * @param method method
     */
    public static void addPutMethod(String url, Method method) {
        PUT.put(transform(url), method);
        log.info("Add [PUT] request uri: {}", url);
    }

    /**
     * PUT request
     *
     * @param url request url
     * @return matcher case
     */
    public static Map.Entry<Pattern, Method> PUT(String url) {

        for (Map.Entry<Pattern, Method> entry : PUT.entrySet()) {
            Matcher m = entry.getKey().matcher(url);
            if (m.matches()) {
                return entry;
            }
        }
        return null;
    }

    /**
     * registration DELETE url
     *
     * @param url    post request uniform resource locator
     * @param method method
     */
    public static void addDeleteMethod(String url, Method method) {
        DELETE.put(transform(url), method);
        log.info("Add [DELETE] request uri: {}", url);
    }

    /**
     * DELETE request
     *
     * @param url request url
     * @return matcher case
     */
    public static Map.Entry<Pattern, Method> DELETE(String url) {

        for (Map.Entry<Pattern, Method> entry : DELETE.entrySet()) {
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
