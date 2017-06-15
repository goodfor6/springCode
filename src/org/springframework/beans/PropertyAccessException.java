package org.springframework.beans;

import java.beans.PropertyChangeEvent;

import org.springframework.core.ErrorCoded;

@SuppressWarnings("serial")
public class PropertyAccessException extends BeansException implements ErrorCoded {

	private transient PropertyChangeEvent propertyChangeEvent;
	
	public PropertyAccessException(PropertyChangeEvent propertyChangeEvent,String msg,Throwable cause){
		super(msg,cause);
		this.propertyChangeEvent=propertyChangeEvent;
	 }
	
	public PropertyAccessException(String msg,Throwable cause){
		super(msg,cause);
	}
	
	public String getPropertyName(){
		return (this.propertyChangeEvent!=null? this.propertyChangeEvent.getPropertyName():null);
	}
	
	public Object getValue(){
		return (this.propertyChangeEvent!=null?this.propertyChangeEvent.getNewValue():null);
	}

	@Override
	public String getErrorCode() {
		// TODO Auto-generated method stub
		return null;
	}
	public PropertyChangeEvent getPropertyChangeEvent() {
		return this.propertyChangeEvent;
	}
	
}