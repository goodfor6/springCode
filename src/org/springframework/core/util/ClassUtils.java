package org.springframework.core.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ClassUtils {

	public static final String ARRAY_SUFFIX="[]";
	
	private static final String INTERNAL_ARRAY_PREFIX="[";

	private static final String NON_PRIMITIVE_ARRAY_PREFIX="[L";
	
	private static final char PACKAGE_SEPARATOR = '.';
	
	private static final char PATH_SEPARATOR = '/';
	
	private static final char INNER_CLASS_SEPARATOR = '$';
	
	private static final String CGLIB_CLASS_SEPARATOR = "$$";
	
	public static final String CLASS_FILE_SUFFIX = ".class";
	
	public static final Map<Class<?>,Class<?>> primitiveWrapperTypeMap = new HashMap<Class<?>,Class<?>>(8);
	
	private static final Map<Class<?>,Class<?>> primitiveTypeToWrapperMap = new HashMap<Class<?>,Class<?>>(8);
	
	private static final Map<String,Class<?>> primitiveTypeNameMap = new HashMap<String,Class<?>>(32);
	
	private static final Map<String,Class<?>> commonClassCache = new HashMap<String,Class<?>>(32);
	
	static {
		primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
		primitiveWrapperTypeMap.put(Byte.class, byte.class);
		primitiveWrapperTypeMap.put(Character.class, char.class);
		primitiveWrapperTypeMap.put(Double.class, double.class);
		primitiveWrapperTypeMap.put(Float.class, float.class);
		primitiveWrapperTypeMap.put(Integer.class, int.class);
		primitiveWrapperTypeMap.put(Long.class, long.class);
		primitiveWrapperTypeMap.put(Short.class, short.class);
		
		for(Map.Entry<Class<?>,Class<?>>entry:primitiveWrapperTypeMap.entrySet()){
			primitiveTypeToWrapperMap.put(entry.getValue(), entry.getKey());
			registerCommonClasses(entry.getKey());
		}
		Set<Class<?>>primitiveTypes = new HashSet<Class<?>>(32);
		primitiveTypes.addAll(primitiveWrapperTypeMap.values());
		primitiveTypes.addAll(Arrays.asList(new Class<?>[]{
			boolean[].class, byte[].class, char[].class, double[].class,
			float[].class, int[].class, long[].class, short[].class
		}));
		primitiveTypes.add(void.class);
		for(Class<?>primitiveType : primitiveTypes){
			primitiveTypeNameMap.put(primitiveType.getName(), primitiveType);
		}
		
		registerCommonClasses(Boolean[].class,Byte[].class,Character[].class,Double[].class,
				Float[].class,Integer[].class,Long[].class,Short[].class);
		registerCommonClasses(Number.class,Number[].class,String.class,String[].class,
				Object.class,Object[].class,Class.class,Class[].class);
		registerCommonClasses(Throwable.class,Exception.class,RuntimeException.class,
				Error.class,StackTraceElement.class,StackTraceElement[].class);
	}
	private static void registerCommonClasses(Class<?>...  commonClasses){
		for(Class<?>clazz:commonClasses){
			commonClassCache.put(clazz.getName(), clazz);
		}
	}
	
	
	public static ClassLoader getDefaultClassLoader(){
		ClassLoader cl =null;
		try{
			cl= Thread.currentThread().getContextClassLoader();
		}
		catch(Throwable ex){
			
		}
		if(cl == null){
			cl = ClassUtils.class.getClassLoader();
			if(cl == null){
				try{
					cl=ClassLoader.getSystemClassLoader();
				}
				catch (Throwable ex){
					
				}
			}
		}
		return cl;
	}
	
	public static ClassLoader overrideThreadContextClassLoader(ClassLoader classLoaderToUse){
		Thread currentThread = Thread.currentThread();
		ClassLoader threadContextClassLoader = currentThread.getContextClassLoader();
		if(classLoaderToUse != null && !classLoaderToUse.equals(threadContextClassLoader)){
			currentThread.setContextClassLoader(classLoaderToUse);
			return threadContextClassLoader;
		}
		else {
			return null;
		}
	}
	
	public static Class<?> forName(String name,ClassLoader classLoader) throws ClassNotFoundException,LinkageError{
		Assert.notNull(name,"Name must not be null");
		
		Class<?>clazz = resolvePrimitiveClassName(name);
		if(clazz == null){
			clazz = commonClassCache.get(name);
		}
		if(clazz != null){
			return clazz;
		}
		
		if(name.endsWith(ARRAY_SUFFIX)){
			String elementClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
			Class<?>elementClass = forName(elementClassName,classLoader);
			return Array.newInstance(elementClass,0).getClass();
		}
		
		if(name.startsWith(NON_PRIMITIVE_ARRAY_PREFIX)&& name.endsWith(";")){
			String elementName = name.substring(NON_PRIMITIVE_ARRAY_PREFIX.length(),name.length()-1);
			Class<?> elementClass = forName(elementName,classLoader);
			return Array.newInstance(elementClass, 0).getClass();
		}
		
		if(name.startsWith(INTERNAL_ARRAY_PREFIX)){
			String elementName = name.substring(INTERNAL_ARRAY_PREFIX.length());
			Class<?>elementClass  = forName(elementName,classLoader);
			return Array.newInstance(elementClass, 0).getClass();
		}
		
		ClassLoader clToUse = classLoader;
		if(clToUse == null){
			clToUse = getDefaultClassLoader();
		}
		try{
			return (clToUse != null ? clToUse.loadClass(name):Class.forName(name));
		}
		catch(ClassNotFoundException ex){
			int lastDotIndex = name.lastIndexOf(PACKAGE_SEPARATOR);
			if(lastDotIndex != -1){
				String innerClassName =
						 name.substring(0,lastDotIndex)+ INNER_CLASS_SEPARATOR + name.substring(lastDotIndex + 1);
				try{
					return (clToUse != null ? clToUse.loadClass(innerClassName): Class.forName(innerClassName));
				}
				catch(ClassNotFoundException ex2){
					
				}
			}
				throw ex;
			}
	}
	
	public static Class<?> resolveClassName(String className,ClassLoader classLoader)throws IllegalArgumentException{
		try {
			return forName(className,classLoader);
		}
		catch(ClassNotFoundException ex){
			throw new IllegalArgumentException("Cannot find class ["+className+"]",ex);
		}
		catch(LinkageError ex){
			throw new IllegalArgumentException("Error loading class ["+className+"]: problem with class file or dependent class.",ex);
		}
	}
	
	public static Class<?> resolvePrimitiveClassName(String name){
		Class<?> result = null;
		if(name !=null && name.length() <=8){
			result =primitiveTypeNameMap.get(name);
		}
		return result;
	}
	
	public static boolean isPresent(String className,ClassLoader classLoader){
		try{
			forName(className,classLoader);
			return true;
		}
		catch(Throwable ex){
			return false;
		}
	}
	
	public static Class<?> getUserClass(Object instance){
		Assert.notNull(instance,"Instance must bot be null ");
		return getUserClass(instance.getClass());
	}
	
	public static Class<?> getUserClass(Class<?>clazz){
		if(clazz != null && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)){
			Class<?> superClass = clazz.getSuperclass();
			if(superClass != null && !Object.class.equals(superClass)){
				return superClass;
			}
		}
		return clazz;
	}
	
	public static boolean isCacheSafe(Class<?>clazz,ClassLoader classLoader){
		Assert.notNull(clazz, "Class must not be null ");
		try{
			ClassLoader target = clazz.getClassLoader();
			if(target == null){
				return true;
			}
			ClassLoader cur = classLoader;
			if(cur == target){
				return true;
			}
			while (cur != null){
				cur = cur.getParent();
				if(cur == target){
					return true;
				}
			}
			return false;
		}
		catch(SecurityException ex){
			return true;
		}
	}
	
	public static String getShortName (String className){
		Assert.hasLength(className," Class name must not be empty");
		int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
		int nameEndIndex = className.indexOf(CGLIB_CLASS_SEPARATOR);
		if( nameEndIndex == -1){
			nameEndIndex = className.length();
		}
		String shortName = className.substring(lastDotIndex+1,nameEndIndex);
		shortName = shortName.replace(INNER_CLASS_SEPARATOR, PACKAGE_SEPARATOR);
		return shortName;
	}
	
	public static String getShortName(Class<?>clazz){
		return getShortName(getQualifiedName(clazz));
	}
	
	public static String getQualifiedName(Class<?>clazz){
		Assert.notNull(clazz,"Class must not be null");
		if(clazz.isArray()){
			return getQualifiedNameForArray(clazz);
		}
		else{
			return clazz.getName();
		}
	}
	
	private static String getQualifiedNameForArray(Class<?>clazz){
		StringBuffer result = new StringBuffer();
		while(clazz.isArray()){
			clazz = clazz.getComponentType();
			result.append(ClassUtils.ARRAY_SUFFIX);
		}
		result.insert(0, clazz.getName());
		return result.toString();
	}
	
}
