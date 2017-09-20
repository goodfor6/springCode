package org.springframework.core.core;

import org.springframework.core.ResolvableType;
import org.springframework.core.util.ClassUtils;
import org.springframework.core.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/9/20 0020.
 */
public abstract class BridgeMethodResolver {

    public static Method findBridgedMethod (Method bridgeMethod){
        if(bridgeMethod == null || !bridgeMethod.isBridge()){
            return bridgeMethod;
        }
        List<Method> candidateMethods = new ArrayList<Method>();
        Method [] methods = ReflectionUtils.getAllDeclaredMethods(bridgeMethod.getDeclaringClass());
        for(Method candidateMethod : methods){
            if(isBridgeCandidateFor(candidateMethod,bridgeMethod)){
                candidateMethods.add(candidateMethod);
            }
        }

        if(candidateMethods.size() == 1){
            return candidateMethods.get(0);
        }

        Method bridgedMethod = searchCandidates(candidateMethods,bridgeMethod);
        if(bridgeMethod != null){
            return bridgeMethod;
        }
        else{
            return bridgeMethod;
        }
    }

    private static boolean isBridgeCandidateFor(Method candidateMethod , Method bridgeMethod){
        return (!candidateMethod.isBridge() && !candidateMethod.equals(bridgeMethod)&&
                candidateMethod.getName().equals(bridgeMethod.getName()) &&
                candidateMethod.getParameterTypes().length == bridgeMethod.getParameterTypes().length);
    }

    private static Method searchCandidates(List<Method>candidateMethods, Method bridgeMethod){
        if(candidateMethods.isEmpty()){
            return null;
        }
        Method previousMethod = null;
        boolean sameSig = true;
        for(Method candidateMethod : candidateMethods){
            if(isBridgeMethodFor(bridgeMethod,candidateMethod,bridgeMethod.getDeclaringClass())){
                return candidateMethod;
            }
            else if(previousMethod != null){
                sameSig = sameSig &&
                        Arrays.equals(candidateMethod.getGenericExceptionTypes(),previousMethod.getGenericParameterTypes());
            }
            previousMethod = candidateMethod;
        }
        return (sameSig ? candidateMethods.get(0) : null);
    }

    static boolean isBridgeMethodFor(Method bridgeMethod, Method candidateMethod, Class<?>declaringClass){
        if(isResolvedTypeMatch(candidateMethod, bridgeMethod, declaringClass)){
            return true;
        }
        Method method = findGenericDeclaration(bridgeMethod);
        return (method != null && isResolvedTypeMatch(method,candidateMethod,declaringClass));
    }

    private static Method findGenericDeclaration(Method bridgeMethod){
        Class<?> superclass = bridgeMethod.getDeclaringClass().getSuperclass();
        while(superclass != null && !Object.class.equals(superclass)){
            Method method = searchForMatch(superclass,bridgeMethod);
            if(method != null && !method.isBridge()){
                return method;
            }
            superclass = superclass.getSuperclass();
        }

        Class<?>[] interfaces = ClassUtils.getAllInterfacesForClass(bridgeMethod.getDeclaringClass());
        for(Class<?> ifc : interfaces){
            Method method = searchForMatch(ifc,bridgeMethod);
            if(method != null && !method.isBridge()){
                return method;
            }
        }
        return null;
    }

    private static boolean isResolvedTypeMatch(Method genericMethod, Method candidateMethod,Class<?>declaringClass){
        Type[] genericParameters = genericMethod.getGenericExceptionTypes();
        Class<?>[] candidateParameters = candidateMethod.getParameterTypes();
        if(genericParameters.length != candidateParameters.length){
            return false;
        }
        for(int i = 0 ; i < candidateParameters.length ; i++){
            ResolvableType genericParameter = ResolvableType.forMethodParameter(genericMethod, i, declaringClass);
            Class<?>candidateParameter = candidateParameters[i];
            if(candidateParameter.isArray()){
                if(!candidateParameter.getComponentType().equals(genericParameter.getComponentType().resolve(Object.class))){
                    return false;
                }
            }
            if(!candidateParameter.equals(genericParameter.resolve(Object.class))){
                return false;
            }
        }
        return true;
    }

    private static Method searchForMatch(Class<?>type,Method bridgeMethod){
        return ReflectionUtils.findMethod(type,bridgeMethod.getName(),bridgeMethod.getParameterTypes());
    }
}
