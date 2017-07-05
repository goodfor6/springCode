/**
 * 
 */
package org.springframework.beans;

import java.beans.PropertyChangeEvent;

import org.springframework.util.ClassUtils;

/**
 * @author luolianhuan
 *
 */
public class TypeMismatchException extends PropertyAccessException{

	public static final String ERROR_CODE="typeMismatch";
	
	private transient Object value;
	
	private Class<?> requiredType;
	
	public TypeMismatchException(PropertyChangeEvent propertyChangeEvent,Class<?>requiredType){
		this(propertyChangeEvent,requiredType,null);
	}
	
	public TypeMismatchException(PropertyChangeEvent propertyChangeEvent,Class<?>requiredType,Throwable cause){
		super(propertyChangeEvent,"Failed to convert property value of type '"+
	           ClassUtils.getDescriptiveType(propertyChangeEvent.getNewValue())+"'"+
			  (requiredType!=null ? "to required type '"+ClassUtils.getQualifiedName(requiredType)+"'":"")+	
			  (propertyChangeEvent.getPropertyName()!=null? "for property'"+propertyChangeEvent.getPropertyName()+"'":""),
			  cause);
		this.value=propertyChangeEvent.getNewValue();
		this.requiredType=requiredType;
	}
	
	public TypeMismatchException(Object value,Class<?>requiredType){
		this(value,requiredType,null);
	}
	
	public TypeMismatchException(Object vlaue,Class<?>requiredType,Throwable cause){
		super("Failed to convert value of type '"+ClassUtils.getDescriptiveType(vlaue)+"'"+
				(requiredType!=null?"to required type '"+ClassUtils.getQualifiedName(requiredType)+"'":""),
				cause);
		this.value=value;
		this.requiredType=requiredType;
	}
	
	/**
	 * Return the offending value (may be {@code null})
	 */
	@Override
	public Object getValue() {
		return this.value;
	}

	/**
	 * Return the required target type, if any.
	 */
	public Class<?> getRequiredType() {
		return this.requiredType;
	}

	@Override
	public String getErrorCode() {
		return ERROR_CODE;
	}
}
