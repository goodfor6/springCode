package org.springframework.beans;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public class PropertyBatchUpdateException  extends BeansException{

	private PropertyAccessException[] propertyAccessExceptions;
	
	public PropertyBatchUpdateException(PropertyAccessException[] propertyAccessExceptions){
		super(null);
		Assert.notEmpty(propertyAccessExceptions,"At least 1 PropertyAccesssException required");
	    this.propertyAccessExceptions=propertyAccessExceptions;
	}
	
	public final int getExceptionCount(){
		return this.propertyAccessExceptions.length;
	}
	
	public final PropertyAccessException[] getPropertyAccessException(){
		return this.propertyAccessExceptions;
	}
	
	public PropertyAccessException getPropertyAccessException(String propertyName){
		for(PropertyAccessException pae:this.propertyAccessExceptions){
			if(ObjectUtils.nullSafeEquals(propertyName, pae.getPropertyName())){
				return pae;
			}
		}
		return null;
	}
	
	public String getMessage(){
		StringBuilder sb=new StringBuilder(" Failed properties: ");
		for(int i=0;i<this.propertyAccessExceptions.length;i++){
			sb.append(this.propertyAccessExceptions[i].getMessage());
			if(i<this.propertyAccessExceptions.length-1){
				sb.append(";");
			}
		}
		return sb.toString();
	}
}
