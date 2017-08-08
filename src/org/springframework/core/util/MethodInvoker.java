package org.springframework.core.util;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by Administrator on 2017/8/3 0003.
 */
public class MethodInvoker  {
    private Class<?> targetClass;
    private Object targetObject;
    private String targetMethod;
    private String staticMethod;
    private Object [] arguments = new Object[0];
    private Method methodObject;

    public MethodInvoker(){

    }

    public void setTargetClass(Class<?> targetClass){this.targetClass = targetClass;}

    public Class<?> getTargetClass(){return this.targetClass;}

    public void setTargetObject(Object targetObject){
        this.targetObject = targetObject;
        if(targetObject != null){
            this.targetClass =  targetObject.getClass();
        }
    }

    public Object getTargetObject() {
        return targetObject;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public String getStaticMethod() {
        return staticMethod;
    }

    public void setStaticMethod(String staticMethod) {
        this.staticMethod = staticMethod;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public Method getMethodObject() {
        return methodObject;
    }

    public void setMethodObject(Method methodObject) {
        this.methodObject = methodObject;
    }

    public void prepare() throws ClassNotFoundException,NoSuchMethodException{
        String targetMethod;
        if(this.staticMethod != null ){
            int lastDotIndex = this.staticMethod.lastIndexOf(46);
            if(lastDotIndex == -1 || lastDotIndex == this.staticMethod.length()){
                throw new IllegalArgumentException("staticMethod must be afully qualified class plus method name: e.g 'example.MyExampleClass.myExampleMethod'");
            }

            targetMethod = this.staticMethod.substring(0,lastDotIndex);
            String methodName = this.staticMethod.substring(lastDotIndex+1);
            this.targetClass = this.resolveClassName(targetMethod);
            this.targetMethod = methodName;
        }

        Class<?> targetClass = this.getTargetClass();
        targetMethod = this.getTargetMethod();
        if(targetClass == null){
            throw new IllegalArgumentException("Either 'targetClass' or 'targetObject' is required");
        }
        else if(targetMethod == null){
            throw new IllegalArgumentException("Property 'targetMethod' is required ");
        }
        else{
            Object [] arguments = this.getArguments();
            Class<?>[] argTypes = new Class[arguments.length];

            for(int i= 0;i < arguments.length;++i){
                argTypes[i] = arguments[i] != null? arguments[i].getClass(): Object.class;
            }

            try{
                this.methodObject = targetClass.getMethod(targetMethod,argTypes);
            }catch(NoSuchMethodException var6){
                this.methodObject = this.findMatchingMethod();
                if(this.methodObject == null){
                    throw var6;
                }
            }
        }
    }

    protected Class<?> resolveClassName(String className)throws ClassNotFoundException{
        return ClassUtils.forName(className,ClassUtils.getDefaultClassLoader());
    }

    protected Method findMatchingMethod(){
        String targetMethod = this.getTargetMethod();
        Object[] arguments = this.getArguments();
        int argCount = arguments.length;
        Method [] candidates = ReflectionUtils.getAllDeclaredMethods(this.getTargetClass());
        int minTypeDiffWeight = 2147483647;
        Method matchingMethod = null;
        Method[] var7 = candidates;
        int var8 = candidates.length;

        for(int var9=0 ;var9 < var8;++var9){
            Method candidate = var7[var9];
            if(candidate.getName().equals(targetMethod)){
                Class<?> [] paramTypes = candidate.getParameterTypes();
                if(paramTypes.length == argCount){
                    int typeDiffWeight = getTypeDifferenceWeight(paramTypes,arguments);
                    if(typeDiffWeight < minTypeDiffWeight){
                        minTypeDiffWeight = typeDiffWeight;
                        matchingMethod = candidate;
                    }
                }
            }
        }
        return matchingMethod;
    }

    public Method getPrepareMethod()throws IllegalStateException{
        if(this.methodObject == null){
            throw new IllegalStateException("prepare() must be called prior to invoke() on MethodInvole");
        }
        else{
            return this.methodObject;
        }
    }

    public boolean isPrepared(){return this.methodObject != null;}

    public Object invoke() throws InvocationTargetException,IllegalAccessException{
        Object targetObject = this.getTargetObject();
        Method preparedMethod = this.getPrepareMethod();
        if(targetObject == null && Modifier.isStatic(preparedMethod.getModifiers())){
            throw new IllegalArgumentException("Target method must not be non-static without a target");
        }
        else{
            ReflectionUtils.makeAccessible(preparedMethod);
            return preparedMethod.invoke(targetObject,this.getArguments());
        }
    }

    public static int getTypeDifferenceWeight(Class<?>[] paramTypes, Object[] args){
        int result = 0;
        for(int i = 0;i < paramTypes.length; ++i){
            if(!ClassUtils.isAssignableValue(paramTypes[i],args[i])){
                return 2147483647;
            }

            if(args[i] != null){
                Class<?> paramType = paramTypes[i];
                Class superClass = args[i].getClass().getSuperclass();

                while(superClass != null){
                    if(paramType.equals(superClass)){
                        result += 2;
                        superClass = null;
                    }
                    else if(ClassUtils.isAssignable(paramType, superClass)){
                        result += 2;
                        superClass = superClass.getSuperclass();
                    }
                    else{
                        superClass = null;
                    }
                }
                if(paramType.isInterface()){
                    ++result;
                }
            }
        }
        return result;
    }
}
