package org.springframework.beans;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

final class GenericTypeAwarePropertyDescriptor extends PropertyDescriptor{
 
	private final Class<?>beanClass;
	
	private final Method readMethod;

    private final Method writeMethod;
    
    private volatile Set<Method>ambiguousWriteMethods;
    
    private MethodParameter writeMethodParameter;
    
    private Class<?>propertyType;
    
    private final Class<?> propertyEditorClass;
    
    public GenericTypeAwarePropertyDescriptor(Class<?>beanClass,String propertyName,
    		Method readMethod,Method writeMethod,Class<?>propertyEditorClass)throws IntrospectionException{
    	super(propertyName,null,null);
    	
    	if(beanClass==null){
    		throw new IntrospectionException("Bean class must not be null");
    	}
    	this.beanClass=beanClass;
    	
    	Method readMethodToUse=BridgeMethodResolver.findBridgedMethod(readMethod);
    	Method writeMethodToUse=BridgeMethodResolver.findBridgedMethod(writeMethod);
    	if(writeMethodToUse==null&& readMethodToUse!=null){
    	   Method candidate=ClassUtils.getMethodIfAvailable(this.beanClass, "set"+StringUtils.capitalize(getName()),(Class<?>[])null);
    	   if(candidate!=null && candidate.getParameterTypes().length==1){
    		   writeMethodToUse=candidate;
    	   }
    	}
    	this.readMethod=readMethodToUse;
    	this.writeMethod=writeMethodToUse;
    	
    	if(this.writeMethod != null){
    		if(this.readMethod == null){
    			Set<Method>ambiguousCandidates=new HashSet<Method>();
    			for(Method method : beanClass.getMethods()){
    				if(method.getName().equals(writeMethodToUse.getName()) &&
    				     !method.equals(writeMethodToUse)&&!method.isBridge()){
    				    	 ambiguousCandidates.add(method);
    				     }
    			}
    		    if(!ambiguousCandidates.isEmpty()){
    		    	this.ambiguousWriteMethods=ambiguousCandidates;
    		    }	
    		}
    		this.writeMethodParameter=new MethodParameter(this.writeMethod,0);
    		GenericTypeResolver.resolveParameterType(this.writeMethodParameter, this.beanClass);
    	  }
    	if(this.readMethod!=null){
    		this.propertyType=GenericTypeResolver.resolveReturnType(this.readMethod, this.beanClass);
    	}
    	else if(this.writeMethodParameter!=null){
    		this.propertyType=this.writeMethodParameter.getParameterType();
    	}
    	this.propertyEditorClass=propertyEditorClass;
    	}
    	
    public Class<?> getBeanClass() {
		return this.beanClass;
	}

	@Override
	public Method getReadMethod() {
		return this.readMethod;
	}

	@Override
	public Method getWriteMethod() {
		return this.writeMethod;
	}
	
	public Method getWriteMethodForActualAccess(){
		Set<Method>ambiguousCandidates=this.ambiguousWriteMethods;
		if(ambiguousCandidates!=null){
			this.ambiguousWriteMethods=null;
			LogFactory.getLog(GenericTypeAwarePropertyDescriptor.class).warn(" Invalid javaBean property '"+
			getName()+"'beging accessed! Ambiguous write methods found next to acutally user["+
					this.writeMethod+"]:"+ambiguousCandidates);
		}
		return this.writeMethod;
	}
    	
}
