package org.springframework.core.core;

import org.springframework.core.util.Assert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by Administrator on 2017/10/11 0011.
 */
public abstract class ParameterizedTypeReference<T> {

    private final Type type;

    protected ParameterizedTypeReference(){
        Class<?>parameterizedTypeReferenceSubclass = findParameterizedTypeReferenceSubclass(getClass());
        Type type = parameterizedTypeReferenceSubclass.getGenericSuperclass();
        Assert.isInstanceOf(ParameterizedType.class,type);
        ParameterizedType parameterizedType = (ParameterizedType)type;
        Assert.isTrue(parameterizedType.getActualTypeArguments().length == 1);
        this.type = parameterizedType.getActualTypeArguments()[0];
    }

    public Type getType(){return this.type;}

    public boolean equals(Object obj){
        return (this == obj || (obj instanceof  ParameterizedTypeReference && this.type.equals(((ParameterizedTypeReference<?>)obj).type)));
    }

    public int hasCode(){return this.type.hashCode();}

    public String toString(){return "ParameterizedTypeReference<"+this.type+">";}

    private static Class<?> findParameterizedTypeReferenceSubclass(Class<?> child){
        Class<?> parent = child.getSuperclass();
        if(Object.class.equals(parent)){
            throw new IllegalStateException("Expected ParameterizedTypeReference superclass");
        }
        else if(ParameterizedTypeReference.class.equals(parent)){
            return child;
        }
        else{
            return findParameterizedTypeReferenceSubclass(parent);
        }
    }

}
