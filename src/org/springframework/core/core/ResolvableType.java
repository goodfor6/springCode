package org.springframework.core.core;

import org.springframework.core.util.Assert;
import org.springframework.core.util.ClassUtils;
import org.springframework.core.util.ConcurrentReferenceHashMap;
import org.springframework.core.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.IdentityHashMap;
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
        WildcardBounds ourBounds = WildcardBounds.get(this);
        WildcardBounds typeBounds = WildcardBounds.get(other);

        if(typeBounds != null){
            return (ourBounds != null && ourBounds.isSameKind(typeBounds) && ourBounds.isAssignableForm(typeBounds.getBounds()));
        }
        if(ourBounds != null){
            return ourBounds.isAssignableForm(other);
        }

        boolean exactMatch = (matchedBefore != null);
        boolean checkedGenerics = true;
        Class<?> ourResolved = null;
        if(this.type instanceof TypeVariable){
            TypeVariable<?> variable = (TypeVariable<?>)this.type;
            if(this.variableResolver != null){
                ResolvableType resolved = this.variableResolver.resolveVariable(variable);
                if(resolved != null){
                    ourResolved = resolved.resolve();
                }
            }
            if(ourResolved == null){
                if(other.variableResolver != null){
                    ResolvableType resolved = other.variableResolver.resolveVariable(variable);
                    if(resolved != null){
                        ourResolved = resolved.resolve();
                        checkGenerics = false;
                    }
                }
            }
            if(ourResolved == null){
                exactMatch = false;
            }
        }
        if(ourResolved == null){
            ourResolved = resolve(Object.class);
        }
        Class<?> otherResolved = other.resolve(Object.class);

        if(exactMatch ? !ourResolved.equals(otherResolved): !ClassUtils.isAssignable(ourResolved.otherResolved)){
            return false;
        }

        if(checkGenerics){
            ResolvableType[] ourGnerics = getGenerics();
            ResolvableType[] typeGenerics = other.as(ourResolved).getGenerics();
            if(ourGenerics.length != typeGenerics.length){
                return false;
            }
            if(matchedBefor == null){
                matchedBefore = new IdentityHashMap<Type,Type>(1);
            }
            matchedBefore.put(this.type,other.type);
            for(int i = 0;i < ourGnerics.length;i++){
                if(!ourGnerics[i].isAssignableFrom(typeGenerics[i],matchedBefore)){
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isArray(){
        if(this == NONE){
            return false;
        }
        return (((this.type instanceof  Class && ((Class<?>)this.type).isArray())) || this.type instanceof GenericArrayType || resolveType().isArray));
    }


    public ResolvableType getComponentType(){
        if(this == NONE){
            return NONE;
        }
        if(this.componentType != null){
            return this.componentType;
        }
        if(this.type instanceof  Class){
            Class<?> componentType = ((Class<?>)this.type).getComponentType();
            return ForType(componentType, this.variableResolver);
        }
        if(this.type instanceof  GenericArrayType){
            return forType(((GenericArrayType)this.type).getGenericComponentType(),this.variableResolver);
        }
        return resolveType().getComponentType();
    }

    public ResolvableType asCollection(){return as(Collection.class);}

    public ResolvableType asMap(){return asMap(Map.class);}

    public ResolvableType as(Class<?> type){
        if(this == NONE){
            return NONE;
        }
        if(ObjectUtils.nullSafeEquals(resolve(),type)){ return this;}

        for(ResolvableType interfaceType : getInterfaces()){
            ResolvableType interfaceAsType = interfaceType.as(type);
            if(interfaceAsType != NONE){
                return interfaceAsType;
            }
        }
        return getSuperType().as(type);
    }

}
