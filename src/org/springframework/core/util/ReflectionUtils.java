package org.springframework.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

public abstract class ReflectionUtils {
	
	private static final String CGLIB_RENAMED_METHOD_PREFIX = "CGLIB$";
	
	private static final Map<Class<?>,Method[]>declaredMethodsCache =
			new ConcurrentReferenceHashMap<Class<?>,Method[]>(256);
	
	public static Field findField(Class<?> clazz,String name){
		return findField(clazz,name,null);
	}
	
	public static Field findField(Class<?>clazz,String name,Class<?>type){
		Assert.notNull(clazz,"Class must not be null");
		Assert.notNull(name != null || type != null,"Either name or type of Field must not be  specified");
		Class<?>searchType = clazz;
		while(!Object.class.equals(searchType) && searchType != null){
			Field [] fields = searchType.getDeclaredFields();
			for(Field field : fields){
				if((name == null || name.equals(field.getName())) && (type == null || type.equals(field.getType()))){
					return field;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}
	
	
	public static void setField(Field field,Object target,Object value){
		try{
			field.set(target, value);
		}
		catch(IllegalAccessException ex){
			handleReflectionException(ex);
			throw new IllegalStateException(
					"Unexception reflection exception - "+ ex.getClass().getName() + ":" +ex.getMessage());
		}
	}
	
	public static Object getField(Field field,Object target){
		try{
			return field.get(target);
		}
		catch(IllegalAccessException ex){
			handleReflectionException(ex);
			throw new IllegalStateException(
					"Unexpected feflection exception - "+ex.getClass().getName()+":"+ex.getMessage());
		}
	}

	public static Method findMessage(Class<?> clazz,String name){
		return findMethod(clazz ,name,new Class<?>[0] );
	}
	
	public static Method findMethod(Class<?>clazz,String name,Class<?>... paramTypes){
		Assert.notNull(clazz,"Class must not be null");
		Assert.notNull(name,"Method name must not be null");
		Class<?> searchType = clazz;
		while(searchType != null){
			Method[] methods = (searchType.isInterface() ? searchType.getMethods() : getDeclaredMethods(searchType));	
			for(Method method : methods){
				if(name.equals(method.getName()) && 
						(paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))){
					return method;
				}
			}
			return null;
		}
	}
	

	public static Object invokeMethod(Method method,Object target){
		return invokeMethod(method,target,new Object[0]);
	}
	
	public static Object invokeMethod(Method method, Object target, Object...args){
		try{
			return method.invoke(target, args);
		}
		catch(Exception ex){
			handleReflectionExcption(ex);
		}
		throw new IllegalStateException("Should never get here");
	}
	
	public static Object invokeJdbcMethod(Method method, Object target) throws SQLException{
		return invokeJdbcMethod(method, target, new Object[0]);
	}
	
	public static Object invokeJdbcMethod(Method method, Object target, Object...args)throws SQLException{
		try{
			return method.invoke(target, args);
		}
		catch(IllegalAccessException ex){
			handleReflectionException(ex);
		}
		catch(InvocationTargetException ex){
			if(ex.getTargetException() instanceof SQLException ){
				throw (SQLException)ex.getTargetException();
			}
			handleInvocationTargetException(ex);
		}
		throw new IllegalStateException("Should never get here");
	}
	 
	public static void handleReflectionException(Exception ex){
		if(ex instanceof NoSuchMethodException){
			throw new IllegalStateException("Method not found "+ ex.getMessage());
		}
		if(ex instanceof IllegalAccessException){
			throw new IllegalStateException("Could not access method "+ ex.getMessage());
		}
		if(ex instanceof InvocationTargetException){
			handleInvovationTargetException((InvocationTargetException)ex);
		}
		if(ex instanceof RuntimeException){
			throw (RuntimeException)ex;
		}
		throw new UndeclaredThrowableException(ex);
	}

	public static void handleInvocationTargetException(InvocationTargetException ex){
		rethrowRuntimeException(ex.getTargetException());
	}
	
	public static void rethrowRuntimeException(Throwable ex){
		if(ex instanceof RuntimeException){
			throw (RuntimeException)ex;
		}
		if(ex instanceof Error){
			throw (Error) ex;
		}
		throw new UndeclaredThrowableException(ex);
	}

	public static void rethrowException(Throwable ex) throws Exception{
		if(ex instanceof Exception){
			throw (Exception)ex;
		}
		else if(ex instanceof Error){
			throw (Error)ex;
		}
		else {
			throw new UndeclaredThrowableException(ex);
		}
	}

	public static boolean declaresException(Method method, Class<?>exceptionType){
		Assert.notNull(method, "Method must not be null");
		Class<?>[] declaredExceptions = method.getExceptionTypes();
		for(Class<?>declaredException : declaredExceptions){
			if(declaredException.isAssignableFrom(exceptionType)){
				return true;
			}
		}
		return false;
	}
	
	public static boolean isPublicStaticFinal(Field field){
		int modifiers = field.getModifiers();
		return (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers));
	}
	
	public static boolean isEqualsMethod(Method method){
		if(method == null || !method.getName().equals("equals")){
			return false;
		}
		Class<?>[] paramTypes = method.getParameterTypes();
		return (paramTypes.length == 1 && paramTypes[0] == Object.class);
	}
	
	public static boolean isHashCodeMethod(Method method){
		return (method != null && method.getName().equals("hashCode") && method.getParameterTypes().length == 0);
	}
	
	public static boolean isToStringMethod(Method method){
		return (method != null && method.getName().equals("toString") && method.getParameterTypes().length == 0);
	}
	
	public static boolean isObjectMethod(Method method){
		if(method == null){
			return false;
		}
		try{
			Object.class.getDeclaredMethod(method.getName(),method.getParameterTypes());
			return true;
		}
		catch(Exception ex){
			return false;
		}
	}
	
	public static boolean isCglibRenamedMethod(Method renamedMethod){
		String name = renamedMethod.getName();
		if(name.startsWith(CGLIB_RENAMED_METHOD_PREFIX)){
			int i = name.length() - 1;
			while(i >= 0 && Character.isDigit(name.charAt(i))){
				i--;
			}
			return ((i > CGLIB_RENAMED_METHOD_PREFIX.length())&&
					(i < name.length() - 1) &&
					(name.charAt(i) == '$'));
		}
		return false;
	}
	
	public static void makeAccessible(Field field){
		if((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
				Modifier.isFinal(field.getModifiers())) && !field.isAccessible()){
			field.setAccessible(true);
		}
	}
	
}