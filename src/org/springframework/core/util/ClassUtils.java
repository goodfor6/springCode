package org.springframework.core.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.cglib.proxy.Proxy;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

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
		if(clazz != null && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)){////
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
	
	public static String getDescriptiveType(Object value){
		if(value == null ){
			return null;
		}
		
		Class<?> clazz = value.getClass();
		if(Proxy.isProxyClass(clazz)){
			StringBuilder result = new StringBuilder(clazz.getName());
			result.append(" implmenting ");
			Class<?>[] ifcs = clazz.getInterfaces();
			for (int i=0 ;i<ifcs.length;i++){
				result.append(ifcs[i].getName());
				if(i< ifcs.length - 1){
					result.append(',');
				}
			}
			return result.toString();
		}
		else if (clazz.isArray()){
			return getQualifiedNameForArray(clazz);
		}
		else{
			return clazz.getName();
		}
	}
	
	public static boolean matchesTypeName(Class<?>clazz,String typeName){
		return ( typeName != null && 
				( typeName.equals(clazz.getName()) || typeName.equals(clazz.getSimpleName()) ||
						(clazz.isArray() && typeName.equals(getQualifiedNameForArray(clazz)))));
	}
	public static boolean hasConstructor(Class <?> clazz,Class<?>... paramTypes){
		return (getConstructorIfAvailable(clazz,paramTypes)!= null);
	}
	
	public static <T> Constructor<T> getConstructorIfAvailable(Class<T> clazz,Class<?>... paramTypes){
		Assert.notNull(clazz," Class must not be null ");
		try{
			return clazz.getConstructor(paramTypes);
		}
		catch(NoSuchMethodException ex){
			return null;
		}
	}
	
	public static boolean hasMethod(Class<?>clazz,String methodName,Class<?>... paramTypes){
		return (getMethodIfAvailable(clazz,methodName,paramTypes)!=null);
	}
	
	public static Method getMethod(Class<?>clazz,String methodName,Class<?>... paramTypes){
		Assert.notNull(clazz,"Class must not be null ");
		Assert.notNull(methodName,"Method name must not be null ");
		if(paramTypes != null){
			try{
				return clazz.getMethod(methodName, paramTypes);
			}
			catch(NoSuchMethodException ex){
				throw new IllegalStateException("Expected method not found : "+ ex);
			}
		}
		else {
			Set<Method>candidates = new HashSet<Method>(1);
			Method[] methods = clazz.getMethods();
			for (Method method : methods){
				if(methodName.equals(method.getName())){
					candidates.add(method);
				}
			}
			if(candidates.size() == 1){
				return candidates.iterator().next();
			}
			else if(candidates.isEmpty()){
				throw new IllegalStateException("Expected method not found : "+clazz+"."+methodName);
			}
			else{
				throw new IllegalStateException("No unique method found:"+clazz+"."+methodName);
			}
		}
	}
	
	public static Method getMethodIfAvailable(Class<?>clazz,String methodName,Class<?>...paramTypes){
		Assert.notNull(clazz,"Class must not be null ");
		Assert.notNull(methodName,"Method name must not be null");
		if(paramTypes != null){
			try{
				return clazz.getMethod(methodName, paramTypes);
			}
			catch(NoSuchMethodException ex){
				return null;
			}
		}
		else{
			Set<Method>candidates = new HashSet<Method>(1);
			Method[] methods = clazz.getMethods();
			for(Method method : methods){
				if(methodName.equals(method.getName())){
					candidates.add(method);
				}
			}
			if(candidates.size() == 1){
				return candidates.iterator().next();
			}
			return null;
		}
	}
	 
	
	public static int getMethodCountForName(Class<?>clazz,String methodName){
		Assert.notNull(clazz," Class must not be null ");
		Assert.notNull(methodName," Method bame must not be null ");
		int count = 0;
		Method[] declaredMethods = clazz.getDeclaredMethods();
		for(Method method : declaredMethods){
			if(methodName.equals(method.getName())){
				count++;
			}
		}
		Class<?>[] ifcs = clazz.getInterfaces();
		for(Class<?> ifc : ifcs){
			count += getMethodCountForName(ifc,methodName);
		}
		if(clazz.getSuperclass() != null){
			count += getMethodCountForName(clazz.getSuperclass(),methodName);
		}
		return count;
	}
	
	
	public static boolean hasAtLeastOneMethodWithName(Class<?> clazz,String methodName){
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodName, "Method name must not be null");
		Method[] declaredMethods = clazz.getDeclaredMethods();
		for (Method method : declaredMethods) {
			if (method.getName().equals(methodName)) {
				return true;
			}
		}
		Class<?>[] ifcs = clazz.getInterfaces();
		for (Class<?> ifc : ifcs){
			if(hasAtLeastOneMethodWithName(ifc,methodName)){
				return true;
			}
		}
		return (clazz.getSuperclass()!= null && hasAtLeastOneMethodWithName(clazz.getSuperclass(),methodName));
	}
	
	public static Method getMostSpecificMethod(Method method,Class<?> targetClass){
		if(method != null && isOverridable(method,targetClass) &&
				targetClass != null && !targetClass.equals(method.getDeclaringClass())){
			try{
				if(Modifier.isPublic(method.getModifiers())){
					try{
						return targetClass.getMethod(method.getName(), method.getParameterTypes());
					}
					catch(NoSuchMethodException ex){
						return method;
					}
				}
				else{
					Method specificMethod =
							ReflectionUtils.findMethod(targetClass, method.getName(),method.getParameterTypes());
					return (specificMethod != null ? specificMethod : method);
				}
			}
			catch(SecurityException ex){
				
			}
		}
		return method;
	}
	
	public static boolean isUserLevelMethod(Method method){
		Assert.notNull(method,"Method must not be null");
		return (method.isBridge() || (!method.isSynthetic() && !isGroovyObjectMethod(method)));
	}
	
	public static boolean isGroovyObjectMethod(Method method){
		return method.getDeclaringClass().getName().equals("groovy.lang.GroovyObject");
	}
	
	private static boolean isOverridable(Method method,Class<?>targetClass){
		if(Modifier.isPrivate(method.getModifiers())){
			return false;
		}
		if(Modifier.isPublic(method.getModifiers()) || Modifier.isProtected(method.getModifiers())){
			return true;
		}
		return getPackageName(method.getDeclaringClass()).equals(getPackageName(targetClass));
	}
	
	public static Method getStaticMethod(Class<?>clazz,String methodName,Class<?>... args){
		Assert.notNull(clazz,"Class must not be null ");
		Assert.notNull(methodName,"Method name must not be null ");
		try{
			Method method = clazz.getMethod(methodName, args);
			return Modifier.isStatic(method.getModifiers())? method : null;
		}
		catch(NoSuchMethodException ex){
			return null;
		}
	}
	
	public static boolean isPrimitiveWrapper(Class<?> clazz){
		Assert.notNull(clazz,"Class must not be null ");
		return primitiveWrapperTypeMap.containsKey(clazz);
	}
	
	public static boolean isPrimitiveOrWrapper(Class<?> clazz){
		Assert.notNull(clazz,"Class must not be null");
		return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
	}
	
	public static boolean isPritiveArray(Class<?>clazz){
		Assert.notNull(clazz,"Class must not be null ");
		return (clazz.isArray() && clazz.getComponentType().isPrimitive());
	}
	
	public static boolean isPritiveWrapperArray(Class<?> clazz){
		Assert.notNull(clazz,"Class must not be null ");
		return (clazz.isArray() && isPrimitiveWrapper(clazz.getComponentType()));
	}
	
	public static Class<?> resolvePrimitiveIfNecessary(Class<?> clazz){
		Assert.notNull(clazz,"Class must not be null ");
		return (clazz.isPrimitive() && clazz != void.class ? primitiveTypeToWrapperMap.get(clazz): clazz);
	}
	
	public static boolean isAssignable(Class<?> lhsType,Class<?>rhsType){
		Assert.notNull(lhsType,"Lefg-hand side type must not be null ");
		Assert.notNull(rhsType,"Right-hand siede type must not be null ");
		if(lhsType.isAssignableFrom(rhsType)){
			return true;
		}
		if(lhsType.isPrimitive()){
			Class<?> resolvedPrimitive = primitiveWrapperTypeMap.get(rhsType);
			if( resolvedPrimitive != null && lhsType.equals(resolvedPrimitive)){
				return true;
			}
		}
		else {
			Class<?> resolvedWrapper = primitiveTypeToWrapperMap.get(rhsType);
			if(resolvedWrapper != null && lhsType.isAssignableFrom(resolvedWrapper)){
				return true;
			}
		}
		return false;
	}
	
	public static boolean isAssignableValue(Class<?>type,Object value){
		Assert.notNull(type,"Type must not be null ");
		return (value != null ? isAssignable(type,value.getClass()):!type.isPrimitive());
	}
	
	public static String convertResourcePathToClassName(String resourcePath){
		Assert.notNull(resourcePath," Resource path must not be null ");
		return resourcePath.replace(PACKAGE_SEPARATOR, PACKAGE_SEPARATOR);
	}
	
	public static String convertClassNameToResourcepath(String className){
		Assert.notNull(className,"Class name must not be null ");
		return className.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
	}
	
	public static String addResourcePathToPackagePath(Class <?> clazz,String resourceName){
		Assert.notNull(resourceName,"Resource name must not be null");
		if(!resourceName.startsWith("/")){
			return classPackageAsResourcePath(clazz)+"/"+resourceName;
		}
		return classPackageAsResourcePath(clazz) + resourceName;
	}
	
	
	public static String getPackageName(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return getPackageName(clazz.getName());
	}
	
	public static String classPackageAsResourcePath(Class<?>clazz){
		if(clazz == null ){
			return "";
		}
		String className = clazz.getName();
		int packageEndIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
		if(packageEndIndex == -1){
			return "";
		}
		String packageName = className.substring(0,packageEndIndex);
		return packageName.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
	}
	
	public static String classNamesToString (Class<?>...classes ){
		return classNamesToString(Arrays.asList(classes));
	}
	
	public static String classNamesToString(Collection<Class<?>>classes){
		if(CollectionUtils.isEmpty(classes)){
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		for(Iterator<Class<?>>it = classes.iterator();it.hasNext();){
			Class<?>clazz=it.next();
			sb.append(clazz.getName());
			if(it.hasNext()){
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}
	
	public static Class<?>[] toClassArray(Collection<Class<?>> collection){
		if(collection == null){
			return null;
		}
		return collection.toArray(new Class<?>[collection.size()]);
	}
	
	public static Class<?>[]getAllInterfaces(Object instance){
		Assert.notNull(instance,"Instance must not be null");
		return getAllInterfacesForClass(instance.getClass());
	}
	
	public static Class<?>[] getAllInterfacesForClass(Class<?>clazz){
		return getAllInterfacesForClass(clazz,null);
	}
	
	public static Class<?>[] getAllInterfacesForClass(Class<?>clazz,ClassLoader classLoader){
		Set<Class<?>>ifcs = getAllInterfacesForClassAsSet(clazz,classLoader);
		return ifcs.toArray(new Class<?>[ifcs.size()]);
	}
	
	public static Set<Class<?>>getAllInterfacesForClassAsSet(Class<?> clazz,ClassLoader classLoader){
		Assert.notNull(clazz,"Class must not be null ");
		if(clazz.isInterface() && isVisible(clazz,classLoader)){
			return Collections.<Class<?>>singleton(clazz);
		}
		Set<Class<?>>interfaces = new LinkedHashSet<Class<?>>();
		while (clazz != null){
			Class<?> [] ifcs = clazz.getInterfaces();
			for( Class<?> ifc : ifcs){
				interfaces.addAll(getAllInterfacesForClassAsSet(ifc,classLoader));
			}
			clazz = clazz.getSuperclass();
		}
		return interfaces;
	}
	
	public static Class<?> createCompositeInterface(Class<?>[] interfaces,ClassLoader classLoader){
		Assert.notEmpty(interfaces,"Interfaces must not be empty");
		Assert.notNull(classLoader,"ClassLoader must not be null ");
		return Proxy.getProxyClass(classLoader, interfaces);
	}
	
	public static Class<?>determineCommonAncestor(Class<?>clazz1,Class<?>clazz2){
		if(clazz1 == null){
			return clazz2;
		}
		if(clazz2 == null){
			return clazz1;
		}
		if(clazz1.isAssignableFrom(clazz2)){
			return clazz1;
		}
		if(clazz2.isAssignableFrom(clazz1)){
			return clazz2;
		}
		Class<?> ancestor = clazz1;
		do{
			ancestor = ancestor.getSuperclass();
			if(ancestor == null || Object.class.equals(ancestor)){
				return null;
			}
		}
		while(!ancestor.isAssignableFrom(clazz2));
		return ancestor;
	}
	
	public static boolean isVisible(Class<?>clazz,ClassLoader classLoader){
		if(classLoader == null){
			return true;
		}
		try{
			Class<?> actualClass = classLoader.loadClass(clazz.getName());
			return (clazz == actualClass);
		}
		catch(ClassNotFoundException ex){
			return false;
		}
	}
	
	public static boolean isCglibProxy(Object object){
		return ClassUtils.isCglibProxy(object.getClass());
	}
	
	public static boolean isCglibProxyClass(Class<?>clazz){
		return (clazz != null && isCglibProxyClassName(clazz.getName()));
	}
	
	public static boolean isCglibProxyClassName(String className){
		return (className != null && className.contains(CGLIB_CLASS_SEPARATOR));
	}
	
	public static String getPackageName(String fqClassName){
		Assert.notNull(fqClassName,"Class name must not be null ");
		int lastDotIndex = fqClassName.lastIndexOf(PACKAGE_SEPARATOR);
		return (lastDotIndex != -1 ? fqClassName.substring(0,lastDotIndex): "");
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
