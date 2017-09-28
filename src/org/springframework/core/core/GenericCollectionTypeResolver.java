package org.springframework.core.core;

import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Administrator on 2017/9/28 0028.
 */
public abstract class GenericCollectionTypeResolver {

    public static Class<?> getCollectionType(Class<? extends Collection>collectionClass){
        return ResolvableType.forClass(collectionClass).asCollection().resolveGeneric();
    }

    public static Class<?> getMapKeyType(Class<? extends Map>mapClass){
        return ResolvableType.forClass(mapClass).asMap().resolveGeneric(0);
    }

    public static Class<?>getMapValueType(Class<? extends Map>mapClass){
        return ResolvableType.forClass(mapClass).asMap().resolveGeneric(1);
    }

    public static Class<?>getCollectionFieldType(Field collectionField){
        return ResolvableType.forField(collectionField).asCollection().resolveGeneric();
    }

    public static Class<?>getCollectionFieldType(Field collectionField, int nestinglevel){
        return ResolvableType.forField(collectionField).getNested(nestinglevel).asCollection().resolveGeneric();
    }

    public static Class<?>getCollectionFieldType(Field collectionField, int nestingLevel,Map<Integer,Integer>typeIndexPerLevel){
        return ResolvableType.forField(collectionField).getNested(nestingLevel,typeIndexPerLevel).asCollection().resolveGeneric();
    }

    public static Class<?>getMapKeyFieldType(Field mapField){
        return ResolvableType.forField(mapField).asMap().resolveGeneric(0);
    }

    public static Class<?> getMapKeyFieldType(Field mapField,int nestingLevel){
        return ResolvableType.forField(mapField).getNested(nestingLevel).asMap().resolveGeneric(0);
    }

    public static Class<?>getMapKeyFieldType(Field mapField,int nestingLevel,Map<Integer,Integer>typeIndexesPerLevel){
        return ResolvableType.forField(mapField).getNested(nestingLevel,typeIndexesPerLevel).asMap().resolveGeneric(0);
    }

    public static Class<?>getMapValueFieldType(Field mapField){
        return ResolvableType.forField(mapField).asMap().resolveGeneric(1);
    }

    public static Class<?>getMapValueFieldType(Field mapField,int nestingLevel){
        return ResolvableType.forField(mapField).getNested(nestingLevel).asMap().resolveGeneric(1);
    }

    public static Class<?>getMapValueFieldType(Field mapField, int nestingLevel,Map<Integer,Integer>typeIndexesPerLevel){
        return ResolvableType.forField(mapField).getNested(nestingLevel,typeIndexesPerLevel).asMap().resolveGeneric(1);
    }

    public static Class<?> getCollectionParameterType(MethodParameter methodParam){
        return ResolvableType.forMethodParameter(methodParam).asCollection().resolveGeneric();
    }

    public static Class<?>getMapKeyParameterType(MethodParameter methodParam){
        return ResolvableType.forMethodParameter(methodParam).asMap().resolveGeneric(0);
    }

    public static Class<?>getMapValueParameterType(MethodParameter methodParam){
        return ResolvableType.forMethodParameter(methodParam).asMap().resolveGeneric(1);
    }

    public static Class<?> getCollectionReturnType(Method method){
        return ResolvableType.forMethodReturnType(method).asCollection().resolveGeneric();
    }

    public static Class<?>getCollectionReturnType(Method method, int nestingLevel){
        return ResolvableType.forMethodReturnType(method).getNested(nestingLevel).asCollection().resolveGeneric();
    }

    public static Class<?> getMapKeyReturnType(Method method){
        return ResolvableType.forMethodReturnType(method).asMap().resolveGeneric(0);
    }

    public static Class<?> getMapkeyReturnType(Method method,int nestingLevel){
        return ResolvableType.forMethodReturnType(method).getNested(nestingLevel).asMap().resolveGeneric(0);
    }

    public static Class<?> getMapValueReturnType(Method method){
        return ResolvableType.forMethodReturnType(method).asMap().resolveGeneric(1);
    }

    public static Class<?> getMapValueReturnType(Method method, int nestingLevel){
        return ResolvableType.forMethodReturnType(method).getNested(nestingLevel).asMap().resolveGeneric(1);
    }

}
