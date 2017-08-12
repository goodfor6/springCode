package org.springframework.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
	
	
}
