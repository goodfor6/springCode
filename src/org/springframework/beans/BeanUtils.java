package org.springframework.beans;

import java.awt.List;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public abstract class BeanUtils {
    
	private static final Log logger=LogFactory.getLog(BeanUtils.class);

    @SuppressWarnings("unchecked")
	private static final Map<Class<?>,Boolean> unknownEditorTypes=Collections.synchronizedMap(new WeakHashMap());
    
    public static <T> T instantiate(Class<T>clazz)
    throws BeanInstantiationException
    {
    	Assert.notNull(clazz,"Calss must not be null     ");
    	if(clazz.isInterface())
    		throw new BeanInstantiationException(clazz," Sepecified class is an interface");
    	try
    	{
    		return clazz.newInstance();
    	}
    	catch(InstantiationException ex)
    	{
    		throw new BeanInstantiationException(clazz,"It is an abstract class",ex);
    	}
    	catch(IllegalAccessException ex)
    	{
    		throw new BeanInstantiationException(clazz,"Is the constructor accessible?",ex);
    	}
    }
    
    public static <T> T instantiateClass(Class<T> clazz)
    throws BeanInstantiationException
    {
    	Assert.notNull(clazz,"Class must be null");
    	if(clazz.isInstance(clazz)){
    		throw new BeanInstantiationException(clazz,"Sepecified class is an interface");
    	}
    	try
    	{
    		return instantiateClass(clazz.getDeclaredConstructor(new Class[0]),new Object[0]);
    	}catch(NoSuchMethodException ex){
    	   ex.printStackTrace();
    	   throw new BeanInstantiationException(clazz ,"no default constructor found",ex);
    	}
    }
    
    public static <T>T instantiateClass(Class<?>clazz,Class<T>assignableTo)
    throws BeanInstantiationException
    {
       Assert.isAssignable(assignableTo,clazz);
       return (T) instantiateClass(clazz);
    }
    
    public static <T> T instantiateClass(Constructor<T> ctor,Object [] args)
    throws BeanInstantiationException
    {
    	Assert.notNull(ctor,"constructor must be null");
    	try{
    		ReflectionUtils.makeAccessible(ctor);
    		return ctor.newInstance(args);
    	}
    	catch(InstantiationException  ex){
    		throw new BeanInstantiationException(ctor.getDeclaringClass(),"Is it an abstract class",ex);
    	}
    	catch(IllegalAccessException ex)
    	{
    		throw new BeanInstantiationException(ctor.getDeclaringClass(),"is the constructor accessible?",ex);
    	}
    	catch(IllegalArgumentException ex)
    	{
    		throw new BeanInstantiationException(ctor.getDeclaringClass(),"Illegal arguments for constructor",ex);
    	}
    	catch(InvocationTargetException ex)
    	{
    		throw new BeanInstantiationException(ctor.getDeclaringClass(),"Constructor threw exception",ex.getTargetException());
    	}
    }
    
    public static Method findMethod (Class<?>clazz,String methodName,Class<?>[]paramTypes)
    {
    	try 
    	{
    		return clazz.getMethod(methodName, paramTypes);
    	}
    	catch(NoSuchMethodException ex)
    	{
    		return findDeclaredMethod(clazz,methodName,paramTypes);
    	}
    }
    
    public static Method findDeclaredMethod(Class<?>clazz,String methodName,Class<?>[] paramTypes)
    {
       try
       {
    	   return clazz.getDeclaredMethod(methodName, paramTypes);
       }
       catch(NoSuchMethodException ex){
    	   if(clazz.getSuperclass()!=null)
    		   return findDeclaredMethod(clazz.getSuperclass(),methodName,paramTypes);
       }
       return null;
    }
    
    public static Method findMethodWithMinimalParameters(Class<?>clazz,String methodName)
    throws IllegalArgumentException
    {
    	Method targetMethod=findMethodWithMinimalParameters(clazz.getMethods(),methodName);
    	if(targetMethod==null){
    		targetMethod=findMethodWithMinimalParameters(clazz,methodName);
    	}                
    	return targetMethod;
    }
    
    public static Method findDeclaredMethodWithMinimalParameters(Class<?>clazz,String methodName)
    throws IllegalArgumentException
    {
    	Method targetMethod=findMethodWithMinimalParameters(clazz.getDeclaredMethods(),methodName);
    	if((targetMethod==null)&&(clazz.getSuperclass()!=null)){
    		targetMethod=findDeclaredMethodWithMinimalParameters(clazz.getSuperclass(),methodName);
    	}
		return targetMethod;
    } 
    					 
    public static Method findMethodWithMinimalParameters(Method[] methods,String methodName)
    throws IllegalArgumentException
    {
    	Method targetMethod =null;
    	int numMethodsFoundWithCurrentMininumArgs=0;
    	for(Method method:methods){
    	 if(method.getName().equals(methodName)){
    		int numParams=method.getParameterTypes().length;
    		if((targetMethod==null)||(numParams<targetMethod.getParameterTypes().length)){
    			targetMethod=method;
    			numMethodsFoundWithCurrentMininumArgs=1;
    		}
    		else{
    			if(targetMethod.getParameterTypes().length!=numParams)
    				continue;
    			numMethodsFoundWithCurrentMininumArgs++;
    			}
    		}
    	}
    	if(numMethodsFoundWithCurrentMininumArgs>0){
    		throw new IllegalArgumentException("cannot resolve method'"+methodName+" 'to a unique method. Attributed to resolve to overloaded method with "+"the least number of parameters,but there were "+numMethodsFoundWithCurrentMininumArgs+" candidates.");
    	}
    	return targetMethod;
    }
    
    public static Method resolveSignature(String signature,Class<?>clazz)
    {
    	Assert.hasText(signature,"' signature' must not be empty");
    	Assert.notNull(clazz,"Class must not be null");
    	int firstParen=signature.indexOf("(");
    	int lastParen=signature.indexOf(")");
    	if((firstParen>-1)&&(lastParen==-1)){
    		throw new IllegalArgumentException("Invalid method signatrue '"+signature+"':expected closing ')' for args list");
    	}
    	if((lastParen>-1)&&(firstParen==-1)){
    		throw new IllegalArgumentException("Invalid method signature '" + signature + "': expected opening '(' for args list");
    	}
    	if((firstParen==-1)&& (lastParen==-1)){
    		return findMethodWithMinimalParameters(clazz,signature);
    	}
    	String methodName=signature.substring(0,firstParen);
    	String []parameterTypeNames=StringUtils.commaDelimitedListToStringArray(signature.substring(firstParen+1,lastParen));
    	Class[] parameterTypes=new Class[parameterTypeNames.length];
    	for(int i=0;i<parameterTypeNames.length;i++){
    		String parameterTypeName=parameterTypeNames[i].trim();
    		try{
    			parameterTypes[i]=ClassUtils.forName(parameterTypeName,clazz.getClassLoader());
    		}
    		catch(Throwable ex){
    			throw new IllegalArgumentException("Invalid method signature:unable to resolve type ["+parameterTypeName+"] for argument "+i+" .Root cause:"+ex);
    		}
    	}
    	return findMethod(clazz,methodName,parameterTypes);
    }
    
    public static PropertyDescriptor[]getPropertyDescriptors(Class<?> clazz)
    throws BeansException
    {
    	CachedIntrospectionResults cr=CachedIntrospectionResults.forClass(clazz);
    	return cr.getPropertyDescriptors();
    }
    
    public static PropertyDescriptor getPropertyDescriptor(Class<?>clazz,String propertyName)
    throws BeansException
    {
    	CachedIntrospectionResults cr=CachedIntrospectionResults.forClass(clazz);
    	return cr.getPropertyDescriptor(propertyName);
    }
    
    public static PropertyDescriptor findPropertyForMethod(Method method)
    throws BeansException
    {
    	Assert.notNull(method,"Method must not be null ");
    	PropertyDescriptor[] pds=getPropertyDescriptors(method.getDeclaringClass());
    	for(PropertyDescriptor pd:pds){
    		if((method.equals(pd.getReadMethod()))||(method.equals(pd.getWriteMethod())))
    			return pd;
    	}
    	return null;
    }
    
    public static PropertyEditor findEditorByConvention(Class<?>targetType)
    {
    	if((targetType==null)|| (targetType.isArray())||(unknownEditorTypes.containsKey(targetType))){
    		return null;
    	}
    	ClassLoader cl=targetType.getClassLoader();
    	if(cl==null){
    		try{
    			cl=ClassLoader.getSystemClassLoader();
    			if(cl==null){
    				return null;
    			}
    		}catch(Throwable ex){
    			if(logger.isDebugEnabled()){
    				logger.debug("Could not access system ClassLoader:"+ex);
    			}
    			return null;
    		}
    	}
    	String editorName=targetType.getName()+"Editor";
    	try{
    		Class editorClass=cl.loadClass(editorName);
    		if(!PropertyEditor.class.isAssignableFrom(editorClass)){
    			if(logger.isWarnEnabled()){
    				logger.warn("Editor class ["+editorName+"] does note implement [java.beans.PropertyEditor] interface");
    			}
    			unknownEditorTypes.put(targetType, Boolean.TRUE);
    			return null;
    		}
    		return (PropertyEditor)instantiateClass(editorClass);
    	}
    	catch(ClassNotFoundException ex)
    	{
    		if(logger.isDebugEnabled()){
    			logger.debug(" No property editor["+editorName+"] found for type "+targetType.getName()+" according to 'editor' suffix convention");
    		}
    		unknownEditorTypes.put(targetType,Boolean.TRUE);
    	}
    	return null;
    	
    }
    
    public static Class<?>findPropertyType(String propertyName,Class<?>[]beanClasses)
    {
    	if(beanClasses!=null){
    		for(Class beanClass:beanClasses){
    			PropertyDescriptor pd=getPropertyDescriptor(beanClass,propertyName);
    			if(pd!=null){
    				return pd.getPropertyType();
    			}
    		}
    	}
    	
    	return Object.class;
    }
    
    public static MethodParameter getWriteMethodParameter(PropertyDescriptor pd)
    {
    	if((pd instanceof GenericTypeAwarePropertyDescriptor)){
    		return new MethodParameter(((GenericTypeAwarePropertyDescriptor)pd).getWriteMethodParameter());
    	}
    	return new MethodParameter(pd.getWriteMethod(),0);
    }
    
    public static boolean isSimpleProperty(Class<?>clazz)
    {
    	Assert.notNull(clazz,"Class must not be null");
        return (isSimpleValueType(clazz)||((clazz.isArray())&&(isSimpleValueType(clazz.getComponentType()))));
    }
    
    public static boolean isSimpleValueType(Class<?>clazz)
    {
    	return (ClassUtils.isPrimitiveOrWrapper(clazz)||(clazz.isEnum())||(CharSequence.class.isAssignableFrom(clazz)||(Number.class.isAssignableFrom(clazz))||(Date.class.isAssignableFrom(clazz)||(clazz.equals(URI.class))||(clazz.equals(URL.class))||(clazz.equals(Locale.class)||(clazz.equals(Class.class))))));
    }
    
    public static void copyProperties(Object source,Object target)
    throws BeansException
    {
    	copyProperties(source,target,null,null);
    }
    
    public static void copyProperties(Object source,Object target,Class<?>editable)
    throws BeansException
    {
    	copyProperties(source,target,editable,null);
    }
    
    public static void copyProperties(Object source,Object target,String[]ignoreProperties)
    throws BeansException
    {
    	copyProperties(source,target,null,ignoreProperties);
    }
    
    public static void copyProperties(Object source,Object target,Class<?>editable,String[]ignoreProperties)
    throws BeansException
    {
    	Assert.notNull(source,"Source must not be null");
    	Assert.notNull(target,"Target must not be null");
    	
    	Class actualEditable=target.getClass();
    	if(editable!=null){
    		if(!editable.isInstance(target)){
    			throw new IllegalArgumentException("Target class ["+ target.getClass().getName()+"] not assignable to Editable class ["+editable.getName()+"]");
    		}
    		actualEditable=editable;
    	}
    	PropertyDescriptor[] targetPds=getPropertyDescriptors(actualEditable); 
    	ArrayList ignoreList=ignoreProperties!=null?new ArrayList(Arrays.asList(ignoreProperties)):null;
    	for (PropertyDescriptor targetPd:targetPds){
    		if((targetPd.getWriteMethod()==null)||((ignoreProperties!=null)&&(ignoreList.contains(targetPd.getName()))))
    			continue;
    		PropertyDescriptor sourcePd=getPropertyDescriptor(source.getClass(),targetPd.getName());
    		if((sourcePd==null)||(sourcePd.getReadMethod()==null)) continue;
    		try{
    			Method readMethod=sourcePd.getReadMethod();
    			if(!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())){
    				readMethod.setAccessible(true);
    			}
    			Object value=readMethod.invoke(source, new Object[0]);
    			Method writeMethod=targetPd.getWriteMethod();
    			writeMethod.invoke(target,new Object[]{value});
    		}catch(Throwable ex){
    			throw new FatalBeanException("Could not copy properties from source to target",ex);
    		}
    	}
    }
    
    
}
