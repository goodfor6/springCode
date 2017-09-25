package org.springframework.core.core;

/**
 * Created by Administrator on 2017/9/25 0025.
 */
public interface ControlFlow {

    boolean under(Class<?> clazz);

    boolean under(Class<?> clazz,String methodName);

    boolean underToken(String token);

}

