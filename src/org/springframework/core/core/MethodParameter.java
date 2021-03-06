package org.springframework.core.core;

import org.springframework.core.util.Assert;

/**
 * Created by Administrator on 2017/9/29 0029.
 */
public class MethodParameter<T> extends ThreadLocal<T> {

    private final String name;

    public NamedThreadLocal(String name){
        Assert.hasText(name,"Name must not be empty");
        this.name = name;
    }

    public String toString(){return this.name;}

}
