package org.springframework.core.core;

import org.springframework.core.util.Assert;
import org.springframework.core.util.ConcurrentReferenceHashMap;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by Administrator on 2017/10/11 0011.
 */
public final  class ResolvableType  implements Serializable {

    public static final ResolvableType NONE = new ResolvableType(null,null,null, null);

    private static final ResolvableType[] EMPTY_TYPES_ARRAY = new ResolvableType[0];

    private static final ConcurrentReferenceHashMap<ResolvableType,ResolvableType>cache = new ConcurrentReferenceHashMap<>(256);

    private final Type type ;

    private final TypeProvider typeProvider;

    private final VariableResolver variableResolver;

    private final ResolvableType componentType;

    private final Class<?> resolved;

    private ResolvableType superType;

    private ResolvableType [] interfaces;

    private ResolvableType [] generics;

    private ResolvableType(Type type,TypeProvider typeProvider, VariableResolver variableResolver,ResolvableType componentType){
        this.type =type;
        this.typeProvider =typeProvider;
        this.variableResolver =variableResolver;
        this.componentType = componentType;
        this.resolved = resolveClass();
    }

    private ResolvableType (Type type,TypeProvider typeProvider,VariableResolver variableResolver){
        this.type = type;
        this.typeProvider =typeProvider;
        this.variableResolver = variableResolver;
        this.componentType = null;
        this.resolved = null;
    }

    public Type getType(){return SeriabliableTypeWrapper.unwrap(this.type);}

    public Class<?> getRawClass(){
        Type rawType = this.type;
        if(rawType instanceof ParameterizedType){
            rawType = ((ParameterizedType)rawType).getRawType();
        }
        return (rawType instanceof  Class ? (Class<?>)rawType : null);
    }

    public Object getSource(){
        Object source = (this.typeProvider != null ? this.typeProvider.getSource() : null);
        return (source != null ? source : this.type);
    }

    public boolean isAssignableFrom(ResolvableType other){return isAssignableFrom(other,null);}

    private boolean isAssignableFrom(ResolvableType other,Map<Type,Type> matchedBefore){
        Assert.notNull(other,"RsolvableType must not be null");

        if(this == NONE || other == NONE){
            return false;
        }

        if(isArray()){
            return (other.isArray() && getComponentType().isAssignableFrom(other.getComponentType()));
        }
        if(matchedBefore != null && matchedBefore.get(this.type) == other.type){
            return true;
        }
        org.springframework.core.ResolvableType.WildcardBounds ourBounds = org.springframework.core.ResolvableType.WildcardBounds.get(this);
        org.springframework.core.ResolvableType.WildcardBounds typeBounds = org.springframework.core.ResolvableType.WildcardBounds.get(other);
    }
}
