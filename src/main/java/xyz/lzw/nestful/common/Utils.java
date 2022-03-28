package xyz.lzw.nestful.common;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lzw
 * @date 2022-03-24
 * @since JDK 11
 */
public class Utils {
    /**
     * get regex named groups
     *
     * @param regex regex string
     * @return set(string) for regex named groups
     */
    public static Set<String> getNamedGroupCandidates(String regex) {
        Set<String> namedGroups = new TreeSet<>();

        Matcher m = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(regex);

        while (m.find()) {
            namedGroups.add(m.group(1));
        }

        return namedGroups;
    }
}
