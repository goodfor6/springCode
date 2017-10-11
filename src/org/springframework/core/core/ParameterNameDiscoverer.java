package org.springframework.core.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/10/11 0011.
 */
public interface ParameterNameDiscoverer {

    String[] getParameterNames(Method method);

    String [] getParameterNames(Constructor<?> ctor);
}
