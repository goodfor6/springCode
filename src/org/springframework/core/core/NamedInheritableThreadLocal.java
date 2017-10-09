package org.springframework.core.core;

import org.springframework.core.util.Assert;

/**
 * Created by Administrator on 2017/10/9 0009.
 */
public class NamedInheritableThreadLocal<T> extends InheritableThreadLocal<T> {
    private final String name;

    public NamedInheritableThreadLocal(String name){
        Assert.hasText(name,"Name must not be empty");
        this.name = name;
    }

    public String toString(){return this.name;}
}
