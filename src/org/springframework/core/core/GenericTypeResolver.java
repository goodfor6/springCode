package org.springframework.core.core;



import org.springframework.core.ResolvableType;
import org.springframework.core.util.Assert;
import org.springframework.core.util.ConcurrentReferenceHashMap;

import javax.lang.model.type.*;
import java.lang.reflect.*;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/9/29 0029.
 */
public abstract  class GenericTypeResolver {

    private static final Map<Class<?>,Map<TypeVariable,Type>>typeVariableCache = new ConcurrentReferenceHashMap<>();

    public static Type getTargetType(MethodParameter methodParam){
        Assert.notNull(methodParam,"MethodParameter must not bte null");
        return methodParam.getGenericParameterType();
    }

    public static Class<?> resolverParameterType(MethodParameter methodParam,Class<?>clazz){
        Assert.notNull(methodParam,"MethodParameter must not be null");
        Assert.notNull(clazz,"Class must not be null");
        methodParam.setContainingClass(clazz);
        methodParam.setParameterType(ResolvableType.forMethodParameter(methodParam).resolve());
        return methodParam.getParameterType();
    }

    public static Class<?> resolverReturnType(Method method,Class<?>clazz){
        Assert.notNull(method,"Method must not be null");
        Assert.notNull(clazz,"Class must no be null");
        return ResolvableType.forMethodReturnType(method,clazz).resolve(method.getReturnType());
    }

    public static Class<?>resolveReturnTypeForGenricMethod(Method method,Object[] args,ClassLoader classLoader){
        Assert.notNull(method,"Method must not be null");
        Assert.notNull(args,"Argument array must not be null");

        TypeVariable<Method>[] declaredTypeVariables = method.getTypeParameters();
        Type genericReturnType = method.getGenericReturnType();
        Type [] methodArgumentTypes = method.getGenericParameterTypes();

        if(declaredTypeVariables.length == 0){
            return method.getReturnType();
        }

        if(args.length < methodArgumentTypes.length){
            return null;
        }

        boolean locallyDeclaredTypeVariableMatchesReturnType = false;
        for(TypeVariable<Method> currentTypeVariable : declaredTypeVariables){
            if(currentTypeVariable.equals(genericReturnType)){
                locallyDeclaredTypeVariableMatchesReturnType = true;
                break;
            }
        }

        if(locallyDeclaredTypeVariableMatchesReturnType){
            for(int i = 0 ; i < methodArgumentTypes.length; i++){
                Type currentMethodArgumentType = methodArgumentTypes[i];
                if(currentMethodArgumentType.equals(genericReturnType)){
                    return args[i].getClass();
                }
                if(currentMethodArgumentType instanceof ParameterizedType){
                    ParameterizedTypeType parameterizedType = (ParameterizedType)currentMethodArgumentType;
                    Type [] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    for(Type typeArg : actualTypeArguments){
                        if(typeArg.equals(genericReturnType)){
                            Object arg =args[i];
                            if(arg instanceof  Class){
                                return (Class<?>)arg;
                            }
                            else if(arg instanceof String &&  classLoader != null){
                                try{
                                    return classLoader.loadClass((String) arg);
                                }catch( ClassNotFoundException ex){
                                    throw new IllegalStateException("Clould not resolve spefific class name argument["+arg+"]",ex);
                                }
                            }
                            else{
                                return method.getReturnType();
                            }
                        }
                    }
                }
            }
        }
        return method.getReturnType();
    }

    public static Class<?> resolveReturnTypeArgument(Method method, Class<?>genericIfc){
        Assert.notNull(method,"method must no be null");
        ResolvableType resolvableType = ResolvableType.forMethodReturnType(method).as(genericIfc);
        if(!resolvableType.hasGenerics() || resolvableType.getType() instanceof  WildcardType){
            return null;
        }
        return getSingleGeneric(resolvableType);
    }

    public static Class<?> resolveTypeArgument(Class<?> clazz,Class<?>genericIfc){
        ResolvableType resolvableType = ResolvableType.forClass(clazz).as(genericIfc);
        if(!resolvableType.hasGenerics()){
            return null;
        }
        return getSingleGeneric(resolvableType);
    }

    private static Class<?> getSingleGeneric(ResolvableType resolvableType){
        if(resolvableType.getGenerics().length > 1){
            throw new IllegalArgumentException("Expected 1 type argument on generic interface ["+resolvableType+
                    "] but found "+resolvableType.getGenerics().length);
        }
        return resolvableType.getGeneric().resolve();
    }

    public static Class<?>[] resolveTypeArguments(Class<?> clazz,Class<?>genericIfc){
        ResolvableType type = ResolvableType.forClass(clazz).as(genericIfc);
        if(!type.hasGenerics() || type.isEntirelyUnresolvable()){
            return null;
        }
        return type.resolveGeneric(Object.class);
    }

    public static Class<?> resolveType(Type genericType,Map<TypeVariable,Type>map){
        return ResolvableType.forType(genericType,new TypeVariableMapVariableResolver(map).resolve(Object.class));
    }

    public static Map<TypeVariable,Type>getTypeVariableMap(Class<?> clazz){
        Map<TypeVariable,Type>typeVariableMap = typeVariableCache.get(clazz);
        if(typeVariableMap == null ){
            typeVariableMap = new HashMap<TypeVariable,Type>();
            buildTypeVariableMap(ResolvableType.forClass(clazz),typeVariableMap);
            typeVariableCache.put(clazz, Collections.unmodifiableMap(typeVariableMap));
        }
        return typeVariableMap;
    }

    private static void buildTypeVariableMap(ResolvableType type,Map<TypeVariable,Type> typeVariableMap){
        if(type != ResolvableType.NONE){
            if(type.getType() instanceof ParameterizedType){
                TypeVariable<?>[] variables = type.resolve().getTypeParameters();
                for(int i = 0; i < variables.length; i++){
                    ResolvableType generic = type.getGeneric(i);
                    while(generic.getType() instanceof TypeVariable<?>){
                        generic = generic.resolveType();
                    }
                    if(generic != ResolvableType.NONE){
                        typeVariableMap.put(variables[i],generic.getType());
                    }
                }
            }
            buildTypeVariableMap(type.getSuperType(),typeVariableMap);
            for(ResolvableType interfaceType : type.getInterfaces()){
                buildTypeVariableMap(interfaceType,typeVariableMap);
            }
            if(type.resolve().isMemberClass()){
                buildTypeVariableMap(ResolvableType.forClass(type.resolve().getEnclosingClass()),typeVariableMap);
            }
        }
    }

    private static class TypeVariableMapVariableResolver implements ResolvableType.VariableResolver{

        private final Map<TypeVariable,Type>typeVariableTypeMap;

        public TypeVariableMapVariableResolver(Map<TypeVariable,Type>typeVariableMap){
            this.typeVariableTypeMap = typeVariableMap;
        }

        public ResolvableType resolvableType(TypeVariable<?> variable){
            Type type = this.typeVariableMap.get(variable);
            return (type != null? ResolvableType.forType(type):null);
        }

        public Object getSource(){return this.typeVariableMap;}

    }

}
