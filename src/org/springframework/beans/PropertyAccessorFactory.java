package org.springframework.beans;

public class PropertyAccessorFactory {
	
	public static BeanWrapper forBeanPropertyAccess(Object target){
		return new BeanWrapperImpl(target);
	}
	
	public static ConfigurablePropertyAccessor forDirectFieldAccess(Object target){
		return new DirectFieldAccessor(target);
	}
}
