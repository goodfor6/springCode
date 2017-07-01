package org.springframework.beans;

import java.beans.PropertyEditor;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.CollectionFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public class TypeConverterDelegate {
	
	private static final Log logger=LogFactory.getLog(TypeConverterDelegate.class);
	
	private static Object javaUtilOptionalEmpty=null;
	
	static {
		try{
			Class<?>clazz=ClassUtils.forName("java.util.optional", TypeConverterDelegate.class.getClassLoader());
			javaUtilOptionalEmpty=ClassUtils.getMethod(clazz, "empty").invoke(null);
		}
		catch(Exception ex){
			
		}
	}
	
	private final PropertyEditorRegistrySupport propertyEditorRegistry;
	
	private final Object targetObject;
	
	public TypeConverterDelegate(PropertyEditorRegistrySupport propertyEditorRegistry){
		this(propertyEditorRegistry,null);
	}

	public TypeConverterDelegate(PropertyEditorRegistrySupport propertyEditorRegistry, Object targetObject) {
		// TODO Auto-generated constructor stub
		this.propertyEditorRegistry=propertyEditorRegistry;
		this.targetObject=targetObject;
	}
	
	public <T> T convertIfNecessary(Object newValue,Class<T>requiredType,MethodParameter methodParam)throws IllegalArgumentException{
		return converterIfNecessary(null,null,newValue,requiredType,
				(methodParam!=null?new TypeDescriptor(methodParam):TypeDescriptor.valueOf(requiredType)));
	}
	
	public <T>T convertIfNecessary(Object newValue,Class<T>requiredType,Field field)throws IllegalArgumentException{
	return convertIfNecessary(null,null,newValue,requiredType,
			(field!=null?new TypeDescriptor(field):TypeDescriptor.valueOf(requiredType)));
	}
	
	public <T> T convertIfNecessary(
			String propertyName,Object oldValue,Object newValue,Class<T>requiredType)
	 		throws IllegalArgumentException{
		return convertIfNecessary(propertyName,oldValue,newValue,requiredType,TypeDescriptor.valueOf(requiredType));
	}
	
	public <T> T convertIfNecessary(String propertyName,Object oldValue,Object newValue,
			Class<T>requiredType,TypeDescriptor typeDescriptor)throws IllegalArgumentException{
		Object convertedValue=newValue;
		
		PropertyEditor editor=this.propertyEditorRegistry.findCustomEditor(requiredType, propertyName);
		
		ConversionFailedException firstAttemptEx=null;
		
		ConversionService conversionService=this.propertyEditorRegistry.getConversionService();
		
		if(editor==null&&conversionService!=null&& convertedValue!=null&&typeDescriptor!=null){
			TypeDescriptor sourceTypeDesc=TypeDescriptor.forObject(newValue);
			TypeDescriptor targetTypeDesc=typeDescriptor;
			if(conversionService.canConvert(sourceTypeDesc,targetTypeDesc)){
				try{
					return (T)conversionService.convert(convertedValue, sourceTypeDesc, targetTypeDesc);
				}catch(ConversionFailedException ex){
					firstAttemptEx=ex;
				}
			}
		}
		
		if(editor!=null || (requiredType!=null&&!ClassUtils.isAssignableValue(requiredType,convertedValue))){
			if(requiredType !=null&& Collection.class.isAssignableFrom(requiredType)&& convertedValue instanceof String){
				TypeDescriptor elementType=typeDescriptor.getElementTypeDescriptor();
				if(elementType!=null && Enum.class.isAssignableFrom(elementType.getType())){
					convertedValue=StringUtils.commaDelimitedListToStringArray((String)convertedValue);
				}
			}
			if(editor==null){
				editor=findDefaultEditor(requiredType);
			}
			convertedValue=doConvertValue(oldValue,convertedValue,requiredType,editor);
		}
		
	}
	
	private PropertyEditor findDefaultEditor(Class<?>requiredType){
		PropertyEditor editor=null;
		if(requiredType!=null){
			editor=this.propertyEditorRegistry.getDefaultEditor(requiredType);
			if(editor==null&&!String.class.equals(requiredType)){
				editor=BeanUtils.findEditorByConvention(requiredType);
			}
		}
		return editor;
	}
	
	private Object doConvertValue(Object oldValue,Object newValue,Class<?>requiredType,PropertyEditor editor){
		Object convertedValue=newValue;
		if(editor!=null&&!(convertedValue instanceof String)){
			try{
				editor.setValue(convertedValue);
				Object newConvertedValue=editor.getValue();
				if(newConvertedValue!=convertedValue){
					convertedValue=newConvertedValue;
					editor=null;
				}
			}catch(Exception ex){
				if(logger.isDebugEnabled()){
					logger.debug("PropertyEditor["+editor.getClass().getName()+"] does not support setValue call",ex);
				}
			}
		}
		Object returnValue=convertedValue;
		if(requiredType!=null && !requiredType.isArray()&& convertedValue instanceof String[]){
            if(logger.isTraceEnabled()){
            	logger.trace("Converting String array to comma-delimited String ["+convertedValue+"]");
              }
            convertedValue=StringUtils.arrayToCommaDelimitedString((String[])convertedValue);
          }
		
		if(convertedValue instanceof String){
			if(editor!=null){
				if(logger.isTraceEnabled()){
					logger.trace("Converting String to ["+requiredType+"] using property editor ["+editor+"]");
				}
				String newTextValue=(String)convertedValue;
				return doConvertTextValue(oldValue,newTextValue,editor);
			}
			else if(String.class.equals(requiredType)){
				returnValue=convertedValue;
			}
		}
		return returnValue;	
	}
	
	private Object doConvertTextValue (Object oldValue,String newTextValue,PropertyEditor editor){
		try{
			editor.setValue(oldValue);
		}catch(Exception ex){
			if(logger.isDebugEnabled()){
				logger.debug("propertyEditor ["+editor.getClass().getName()+"] does not support setValue call ",ex);
			}
		}
		editor.setAsText(newTextValue);
		return editor.getValue();
	}
	
	private Object convertToTypeArray(Object input,String propertyName,Class<?>componentType){
		if(input instanceof Collection){
			Collection<?>coll=(Collection<?>)input;
			Object result =Array.newInstance(componentType, coll.size());
			int i=0;
			for(Iterator<?>it=coll.iterator();it.hasNext();i++){
				Object value=convertIfNecessary(buildIndexedPropertyName(propertyName,i),null,it.next(),componentType);
				Array.set(result, i, value);
			}
			return result;
		}
		else if(input.getClass().isArray()){
			if(componentType.equals(input.getClass().getComponentType())&&
					!this.propertyEditorRegistry.hasCustomEditorForElement(componentType, propertyName)){
				return input;
			}
		    int arrayLength=Array.getLength(input);
		    Object result=Array.newInstance(componentType, arrayLength);
		    for(int i=0;i<arrayLength;i++){
		    	Object value=convertIfNecessary(
		    			buildIndexedPropertyName(propertyName,i),null,Array.get(input, i),componentType);
		    	Array.set(result, i, value);
		    }
		    return result;
		}
		else{
			Object result=Array.newInstance(componentType, 1);
			Object value=convertIfNecessary(
					buildIndexedPropertyName(propertyName,0),null,input),componentType);
			Array.set(result, 0, value);
			return result;
		}
	}
	
	private String buildIndexedPropertyName(String propertyName,int index){
		return (propertyName!=null?propertyName+PropertyAccessor.PROPERTY_KEY_PREFIX+index+PropertyAccessor.PROPERTY_KEY_SUFFIX:null);
	}
	
	public <T> T convertIfNecessary(String propertyName,Object oldValue,Object newValue,
			    Class<T> requiredType,TypeDescriptor typeDescriptor)throws IllegalArgumentExceptionP{
		Object convertedValue=newValue;
		PropertyEditor editor=this.propertyEditorRegistry.findCustomEditor(requiredType, propertyName);
		ConversionFailedException firstAttemptEx=null;
		ConversionService conversionService=this.propertyEditorRegistry.getConversionService();
		if(editor==null&&conversionService!=null&&convertedValue!=null&&typeDescriptor!=null){
			TypeDescriptor sourceTypeDesc=TypeDescriptor.forObject(newValue);
			TypeDescriptor targetTypeDesc=typeDescriptor;
			if(conversionService.canConvert(sourceTypeDesc, targetTypeDesc)){
				try{
					return (T)conversionService.convert(conversionService, sourceTypeDesc, targetTypeDesc);
				}catch(ConversionFailedException ex){
					firstAttemptEx=ex;
				}
			}
		}
		if(editor!=null||(requiredType!=null&&!ClassUtils.isAssignableValue(requiredType, convertedValue))){
			if(requiredType!=null && Collection.class.isAssignableFrom(requiredType)&&convertedValue instanceof String){
				TypeDescriptor elementType=typeDescriptor.getElementTypeDescriptor();
				if(elementType!=null&&Enum.class.isAssignableFrom(elementType.getType())){
					convertedValue=StringUtils.commaDelimitedListToStringArray((String)convertedValue);
				}
			}
			if(editor==null){
				editor=findDefaultEditor(requiredType);
			}
			convertedValue=doConvertValue(oldValue,convertedValue,requiredType,editor);
		}
		boolean standardConversion=false;
		
		if(requiredType!=null){
			if(convertedValue!=null){
				if(Object.class.equals(requiredType)){
					return (T)convertedValue;
				}
				if(requiredType.isArray()){
					if(convertedValue instanceof String&&Enum.class.isAssignableFrom(requiredType.getComponentType())){
						convertedValue=StringUtils.commaDelimitedListToStringArray((String)convertedValue);
					}
					return (T)convertToTypeArray(convertedValue,propertyName,requiredType.getComponentType());
				}
				else if(convertedValue instanceof Collection){
					convertedValue=convertToTypedCollection(
							(Collection<?>)convertedValue,propertyName,requiredType,typeDescriptor);
					standardConversion=true;
				}
				else if(convertedValue instanceof Map){
					convertedValue=convertToTypeMap(
							(Map<?,?>)convertedValue,propertyName,requiredType,typeDescriptor);
					standardConversion=true;
				}
				if(convertedValue.getClass().isArray()&& Array.getLength(convertedValue)==1){
					convertedValue=Array.get(conversionService, 0);
					standardConversion=true;
				}
				if(String.class.equals(requiredType)&& ClassUtils.isPrimitiveOrWrapper(convertedValue.getClass())){
					return (T)convertedValue.toString();
				}
				else if(convertedValue instanceof String && !requiredType.isInstance(convertedValue)){
					if(firstAttemptEx==null&&!requiredType.isInterface()&&!requiredType.isEnum()){
						try{
							Constructor<T> strCtor=requiredType.getConstructor(String.class);
							return BeanUtils.instantiateClass(strCtor,convertedValue);
						}catch(NoSuchMethodException ex){
							if(logger.isTraceEnabled()){
								logger.trace("No String constructor found on type ["+requiredType.getName()+"]",ex);
							}
						}
						catch(Exception ex){
							if(logger.isDebugEnabled()){
								logger.debug("Construction via String faild for type ["+requiredType.getName()+"]");
							}
						}
					}
					String trimmedValue=((String)convertedValue).trim();
					if(requiredType.isEnum()&&"".equals(trimmedValue)){
						return null;
					}
					convertedValue=attemptToConvertStringToEnum(requiredType,trimmedValue,convertedValue);
					standardConversion=true;
				}
				
			}
			else{
				if(javaUtilOptionalEmpty!=null&&requiredType.equals(javaUtilOptionalEmpty.getClass())){
					convertedValue=javaUtilOptionalEmpty;
				}
			}
			if(!ClassUtils.isAssignableValue(requiredType, convertedValue)){
				if(firstAttemptEx !=null){
					throw firstAttemptEx;
				}
				StringBuilder msg=new StringBuilder();
				msg.append("Cannot convert value of type [").append(ClassUtils.getDescriptiveType(newValue));
				msg.append("] to required type [").append(ClassUtils.getQualifiedName(requiredType)).append("]");
				if(propertyName!=null){
					msg.append("for property '").append(propertyName).append("'");
				}
				if(editor!=null){
					msg.append(":PropertyEditor [").append(editor.getClass().getName()).append("]"
							+ " returned inappropriate value of type [").append(
									ClassUtils.getDescriptiveType(convertedValue)).append("]");
					throw new IllegalArgumentException(msg.toString());
				}
				else{
					msg.append(":no matching editors or conversion strategy found");
					throw new IllegalStateException(msg.toString());
				}
			}
		}
		if(firstAttemptEx !=null){
			if(editor==null && !standardConversion && requiredType!=null&&!Object.class.equals(requiredType)){
				throw firstAttemptEx;
			}
			logger.debug("Original ConversionService attempt failed -ignored since "+
			    "PropertyEditor based conversion eventually successed",firstAttemptEx);
		}
		return (T)convertedValue;
	}
	
	private Collection<?>convertToTypeCollection(
			Collection<?>original,String propertyName,Class<?>requiredType,TypeDescriptor typeDescriptor){
		if(!Collection.class.isAssignableFrom(requiredType)){
			return original;
		}
		boolean approximable =CollectionFactory.isApproximableCollectionType(requiredType);
		if(!approximable&& ! canCreateCopy(requiredType)){
			if(logger.isDebugEnabled()){
				logger.debug("Custom Collection type ["+original.getClass().getName()+"] does not allow for creating "
						+ " a copy - injecting original Collection as-is ");
			}
			return original;
		}
		boolean originalAllowed=requiredType.isInstance(original);
		typeDescriptor =typeDescriptor.narrow(original);
		TypeDescriptor elementType=typeDescriptor.getElementTypeDescriptor();
		if(elementType==null && originalAllowed &&
				!this.propertyEditorRegistry.hasCustomEditorForElement(null, propertyName)){
			return original;
		}
	    Iterator<?>it;
	    try{
	    	it=original.iterator();
	    	if(it==null){
	    		if(logger.isDebugEnabled()){
	    			logger.debug("Collection of type ["+original.getClass().getName()+
	    					"] returned null iterator - injecting original Collection as-is ");
	    		}
	    		return original;
	    	}
	    }catch(Throwable ex){
	    	if(logger.isDebugEnabled()){
	    		logger.debug("Collection of type ["+original.getClass().getName()+
	    				"] injecting original Collection as-is:"+ex);
	    	}
	    	return original;
	    }
	    
	    Collection<Object>convertedCopy;
	    try{
	    	if(approximable){
	    		convertedCopy=CollectionFactory.createApproximateCollection(original, original.size());
	    	}else{
	    		convertedCopy=(Collection<Object>)requiredType.newInstance();
	    	}
	    }
	    catch(Throwable ex){
	    	if(logger.isDebugEnabled()){
	    		logger.debug("Cannot create copy of Collction type["+original.getClass().getName()+
	    				"] - injecting original Collection as-is:"+ex);
	    	}
	    	return original;
	    }
	    int i=0;
	    for(;it.hasNext();i++){
	    	Object element=it.next();
	    	String indexedPropertyName=buildIndexedPropertyName(propertyName,i);
	    	Object convertedElement=convertIfNecessary(indexedPropertyName,null,element,
	    			(elementType!=null?elementType.getType():null),elementType);
	    	try{
	    		convertedCopy.add(convertedElement);
	    	}catch(Throwable ex){
	    		if(logger.isDebugEnabled()){
	    			logger.debug("Collection type ["+original.getClass().getName()+
	    					"] seems to be read - only - injecting original Collection as-is"+ex);
	    		}
	    		return original;
	    	}
	    	originalAllowed=originalAllowed && (element==convertedElement);
	    }
	    return (originalAllowed?original:convertedCopy);
	}
	

	private boolean canCreateCopy(Class<?>requiredType){
		return(!requiredType.isInterface()&&!Modifier.isAbstract(requiredType.getModifiers())&&
				Modifier.isPublic(requiredType.getModifiers())&&ClassUtils.hasConstructor(requiredType));
	}
	
	private Object attemptToConvertStringToEnum(Class<?>requiredType,String trimmedValue,Object currentConvertedValue){
		Object convertedValue =currentConvertedValue;
		if(Enum.class.equals(requiredType)){
			int index=trimmedValue.lastIndexOf(".");
			if(index>-1){
				String enumType=trimmedValue.substring(0,index);
				String fieldName=trimmedValue.substring(index+1);
				ClassLoader c1=this.targetObject.getClass().getClassLoader();
				try{
					Class<?>enumValueType=ClassUtils.forName(enumType, c1);
					Field enumField=enumValueType.getField(fieldName);
					convertedValue=enumField.get(null);
				}catch(ClassNotFoundException ex){
					if(logger.isTraceEnabled()){
						logger.trace("Enum class["+enumType+"] cannot be loaded",ex);
					}
				}
				catch(Throwable ex){
					if(logger.isTraceEnabled()){
						logger.trace("Field [ "+fieldName+"] isn't an enum value for type ["+enumType+"]",ex);
					}
				}
			}
		}
		if(convertedValue==currentConvertedValue){
			try{
				Field enumField=requiredType.getField(trimmedValue);
				convertedValue=enumField.get(null);
			}catch(Throwable ex){
				if(logger.isTraceEnabled()){
					logger.trace("Field ["+convertedValue+"] isn't an enum value",ex);
				}
			}
		}
		return convertedValue;
	}
}
