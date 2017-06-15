package org.springframework.beans;

public class NotWritablePropertyException  extends InvalidPropertyException{

	private String[] possibleMatches=null;
	
	public NotWritablePropertyException(Class<?>beanClass,String propertyName){
		super(beanClass,propertyName,"Bean property '"+propertyName+"'is not writable or has an invalid setter method: "+
	                                 "Does the reutrn type of the getter match the parameter type of the setter? ");
	}
	
	public NotWritablePropertyException(Class<?>beanClass,String propertyName,String msg){
		super(beanClass,propertyName,msg);
	}
	
	public NotWritablePropertyException (Class<?>beanClass,String propertyName,String msg,Throwable cause){
		super(beanClass,propertyName,msg,cause);
	}
	
	public NotWritablePropertyException(Class<?>beanClass,String propertyName,String msg,String[]possibleMathes){
		super(beanClass,propertyName,msg);
		this.possibleMatches=possibleMathes;
	}
	
    public String[]getPossibleMatches(){
    	return this.possibleMatches;
    }
	
}
