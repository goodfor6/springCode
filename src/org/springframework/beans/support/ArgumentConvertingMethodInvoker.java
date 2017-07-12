package org.springframework.beans.support;

import java.beans.PropertyEditor;
import java.lang.reflect.Method;

import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.util.MethodInvoker;
import org.springframework.util.ReflectionUtils;

public class ArgumentConvertingMethodInvoker extends MethodInvoker {
	
	private TypeConverter typeConverter;
	
	private boolean useDefaultConverter=true;
	
	public void setTypeConverter(TypeConverter typeConverter){
		this.typeConverter=typeConverter;
		this.useDefaultConverter=false;
	}
	
	public TypeConverter getTypeConverter(){
		if(this.typeConverter==null && this.useDefaultConverter){
			this.typeConverter=getDefaultTypeConverter();
		}
		return this.typeConverter;
	}
	
	protected TypeConverter getDefaultTypeConverter(){
		return new SimpleTypeConverter();
	}

	public void registerCustomEditor(Class<?> requiredType,PropertyEditor propertyEditor){
		TypeConverter converter=getTypeConverter();
		if(!(converter instanceof PropertyEditorRegistry)){
			throw new  IllegalStateException(
					"TypeConverter does not implements propertyEditorRegistry interface: "+converter);
		}
		((PropertyEditorRegistry)converter).registerCustomEditor(requiredType, propertyEditor);
	}
	
	@Override
	protected Method findMatchingMethod(){
		Method matchingMethod =super.findMatchingMethod();
		if(matchingMethod ==null){
			matchingMethod=doFindMatchingMethod(getArguments());
		}
		if(matchingMethod==null){
			matchingMethod=doFindMatchingMethod(new Object[]{getArguments()});
		}
		return matchingMethod;
	}
	
	protected Method doFindMatchingMethod(Object[] arguments){
		TypeConverter converter=getTypeConverter();
		if(converter!=null){
			String targetMethod=getTargetMethod();
			Method matchingMethod=null;
			int argCount=arguments.length;
			Method []candidates=ReflectionUtils.getAllDeclaredMethods(getTargetClass());
			int minTypeDiffWeight=Integer.MAX_VALUE;
			Object [] argumentsToUse=null;
			for(Method candidate:candidates){
				if(candidate.getName().equals(targetMethod)){
					Class<?>[]paramTypes=candidate.getParameterTypes();
					if(paramTypes.length==argCount){
						Object[] convertedArguments=new Object[argCount];
						boolean match=true;
						for(int j=0;j<argCount&&match;j++){
							try{
								convertedArguments[j]=converter.convertIfNecessary(arguments[j], paramTypes[j]);
							}
							catch(TypeMismatchException ex){
								match=false;
							}
						}
						if(match){
							int typeDiffWeight=getTypeDifferenceWeight(paramTypes,convertedArguments);
							if(typeDiffWeight<minTypeDiffWeight){
								minTypeDiffWeight=typeDiffWeight;
								matchingMethod=candidate;
								argumentsToUse=convertedArguments;
							}
						}
					}
				}
			}
			if(matchingMethod!=null){
				setArguments(argumentsToUse);
				return matchingMethod;
			}
		}
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
}
