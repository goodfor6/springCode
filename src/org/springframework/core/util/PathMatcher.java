package org.springframework.core.util;

import java.util.Comparator;

/**
 * Created by Administrator on 2017/8/10 0010.
 */
public interface PathMatcher {
    boolean isPattern(String var1);

    boolean match(String var1,String var2);

    boolean matchStart(String var1,String var2);

    String extractPathWithinPattern(String var1,String var2);
    Comparator<String> getPatternComparator(String var1);
    String combine(String var1,String var2);

}
