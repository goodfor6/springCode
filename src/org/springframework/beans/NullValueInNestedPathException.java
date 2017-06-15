package org.springframework.beans;

@SuppressWarnings("serial")
public class NullValueInNestedPathException extends InvalidPropertyException {

	public  NullValueInNestedPathException(Class<?>beanClass,String propertyName){
		super(beanClass,propertyName,"Vlaue of nested property '"+propertyName+"'is null");
	}
	
	public  NullValueInNestedPathException(Class<?>beanClass,String propertyName,String msg){
		super(beanClass,propertyName,msg);
	}
	
	
}
