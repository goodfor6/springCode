package org.springframework.beans;

import java.beans.PropertyEditor;
import java.util.Collection;
import java.util.Map;

import org.springframework.core.convert.ConversionService;
import org.springframework.util.ClassUtils;

public class PropertyEditorRegistrySupport implements PropertyEditorRegistry{
	
	private static Class<?>zoneIdClass;
	
	static {
		try {
			zoneIdClass=ClassUtils.forName("java.time.ZoneId",PropertyEditorRegistrySupport.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			zoneIdClass=null;
		}
	}
	
	private ConversionService conversionService;
	
	private boolean defaultEditorsActive=false;
	
	private boolean configValueEditorsActive=false;
	
	private Map<Class<?>,PropertyEditor> defaultEditors;
	
	private Map<Class<?>,PropertyEditor> overridenDefaultEditors;
	
	private Map<Class<?>,PropertyEditor>customEditors;
	
	private Map<String,CustomEditorHolder>customEditorsForPath;
	
	private Map<Class<?>,PropertyEditor>customEditorCache;
	
	
	private static class CustomEditorHolder{
		
		private final PropertyEditor propertyEditor;
		
		private final Class<?> registeredType;
		
		private CustomEditorHolder(PropertyEditor propertyEditor,Class<?>registeredType){
			this.propertyEditor=propertyEditor;
			this.registeredType=registeredType;
		}
		private PropertyEditor getPropertyEditor(){
			return this.propertyEditor;
		}
		private Class<?>getRegisteredType(){
			return this.registeredType;
		}
		private PropertyEditor getPropertyEditor(Class<?> requiredType){
			if(this.registeredType==null ||
				(requiredType!=null && 
					(ClassUtils.isAssignable(this.registeredType, requiredType)||
					 ClassUtils.isAssignable(requiredType, this.registeredType)))
				||
				(requiredType==null && 
				(!Collection.class.isAssignableFrom(this.registeredType)&& !this.registeredType.isArray()))){
					return this.propertyEditor;
				}
			else{
				return null;
			}
			}
		}
	
	
	
	
	
	
	

}
