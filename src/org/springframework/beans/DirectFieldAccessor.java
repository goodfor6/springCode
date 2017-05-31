package org.springframework.beans;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

public class DirectFieldAccessor extends AbstractPropertyAccessor {
	
	private  Object rootObject;
	
	private  Map<String,FieldAccessor>fieldMap=new HashMap<String,FieldAccessor>();
	
	public DirectFieldAccessor(final Object rootObject){
		Assert.notNull(rootObject,"Root object must not be null ");
	    this.rootObject=rootObject;
	    this.typeConverterDelegate=new TypeConverterDelegate(this,rootObject);
	    registerDefaultEditors();
	    setExtractOldValueForEditor(true);
	}
	
	public final Class<?> getRootClass() {
		return (this.rootObject != null ? this.rootObject.getClass() : null);
	}
	public final Object getRootInstance() {
		return this.rootObject;
	}
	@Override
	public boolean isReadableProperty(String propertyName) throws BeansException {
		return hasProperty(propertyName);
	}

	@Override
	public boolean isWritableProperty(String propertyName) throws BeansException {
		return hasProperty(propertyName);
	}

	private boolean hasProperty(String propertyPath){
		Assert.notNull(propertyPath,"PropertyPath must not be null ");
		return getFieldAccessor(propertyPath)!=null;
	}
	private FieldAccessor getFieldAccessor(String propertyPath){
		FieldAccessor fieldAccessor=this.fieldMap.get(propertyPath);
		if(fieldAccessor==null){
			fieldAccessor=doGetFieldAccessor(propertyPath,getRootClass());
			this.fieldMap.put(propertyPath, fieldAccessor);
		}
		return fieldAccessor;
	}
	
	private FieldAccessor doGetFieldAccessor(String propertyPath,Class<?>targetClass){
		StringTokenizer st=new StringTokenizer(propertyPath,".");
		FieldAccessor accessor=null;
		Class<?>parentType=targetClass;
		while(st.hasMoreTokens()){
			String localProperty=st.nextToken();
			Field field =ReflectionUtils.findField(parentType, localProperty);
			if(field==null){
				return null;
			}
			if(accessor==null){
				accessor=root(propertyPath,localProperty,field);
			}else{
				accessor=accessor.child(localProperty, field);
			}
			parentType=field.getType();
		}
		return accessor;
		
	}
	
	private FieldAccessor root(String canonicalName,String actualName,Field field){
		return new FieldAccessor(null,canonicalName,actualName,field);
	}
	
	//---------------------------------------------------------------------
	// 内部类
	//---------------------------------------------------------------------
	private class FieldAccessor{
	
	private final  List<FieldAccessor> parents;
	
	private final  String canonicalName;
	
	private final String actualName;
	
	private final Field field;
	
	public FieldAccessor(FieldAccessor parent, String canonicalName, String actualName, Field field){
		Assert.notNull(canonicalName, "Expression must no be null");
		Assert.notNull(field, "Field must no be null");
		this.parents = buildParents(parent);
		this.canonicalName = canonicalName;
		this.actualName=actualName;
		this.field=field;
	}
	
	public FieldAccessor child(String actualName,Field field){
		return new FieldAccessor(this,this.canonicalName,this.actualName+"."+actualName,field);
	} 
	
	public Field getField(){
		return this.field;
	}
	
	public Object getValue(){
		Object localTarget=getLocalTarget(getRootInstance());
		return getParentValue(localTarget);
	}
	
	public void setValue(Object value){
		Object localTarget=getLocalTarget(getRootInstance());
		try{
			this.field.set(localTarget, value);
		}catch(IllegalAccessException ex){
			throw new InvalidPropertyException(localTarget.getClass(),canonicalName,"Field is not accessible",ex);
		}
	}
	
	private Object getParentValue(Object target){
		try{
			ReflectionUtils.makeAccessible(this.field);
			return this.field.get(target);
		}catch(IllegalAccessException ex){
			throw new InvalidPropertyException(target.getClass(),this.canonicalName,"Field is not accessible " ,ex);
		}
		
	}
	
	private Object getLocalTarget(Object rootTarget){
		Object localTarget=rootTarget;
		for(FieldAccessor parent:parents){
			localTarget=autoGrowIfNecessary(parent,parent.getParentValue(localTarget));
			if(localTarget==null){
				throw new NullValueInNestedPathException(getRootClass(),parent.actualName,"Cannot access indexed value of property "
						+ " referenced in indexed property path'"+getField().getName()+"':return null ");
			}
		}
		return localTarget;
	}
	
	private Object newValue(){
		Class<?>type=getField().getType();
		try{
			return type.newInstance();
		}catch(Exception ex){
			throw new NullValueInNestedPathException(getRootClass(),this.actualName,
					"Could not instantiate property type ["+type.getName()+"] to auto-grow nested property path "+ex);
		}
		
	}
	
	private Object autoGrowIfNecessary(FieldAccessor accessor,Object value){
		if(value==null && isAutoGrowNestedPaths()){
			Object defaultValue=accessor.newValue();
			accessor.setValue(defaultValue);
			return defaultValue;
		}
		return value;
	}
	
	private List<FieldAccessor>buildParents(FieldAccessor parent){
		List<FieldAccessor>parents=new ArrayList<FieldAccessor>();
		if(parent!=null){
			parents.addAll(parent.parents);
			parents.add(parent);
		}
		return parents;
	}
	
  }

	@Override
	public TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws BeansException {
		FieldAccessor fieldAccessor = getFieldAccessor(propertyName);
		if (fieldAccessor != null) {
			return new TypeDescriptor(fieldAccessor.getField());
		}
		return null;
	}

	@Override
	public Object getPropertyValue(String propertyName) throws BeansException {
		FieldAccessor fieldAccessor = getFieldAccessor(propertyName);
		if (fieldAccessor == null) {
			throw new NotReadablePropertyException(
					getRootClass(), propertyName, "Field '" + propertyName + "' does not exist");
		}
		return fieldAccessor.getValue();
	}

	@Override
	public void setPropertyValue(String propertyName, Object newValue) throws BeansException {
		FieldAccessor fieldAccessor=getFieldAccessor(propertyName);
		if(fieldAccessor==null){
			throw new NotWritablePropertyException(getRootClass(),propertyName,"Field '"+propertyName+"' does not exist ");
		}
		Field field =fieldAccessor.getField();
		Object oldValue=null;
		try{
			oldValue=fieldAccessor.getField();
			Object convertedValue=this.typeConverterDelegate.convertIfNecessary(
					field.getName(), oldValue,newValue,field.getType(),new TypeDescriptor(field)); 
			fieldAccessor.setValue(convertedValue);
		}catch (ConverterNotFoundException ex) {
			PropertyChangeEvent pce = new PropertyChangeEvent(getRootInstance(), propertyName, oldValue, newValue);
			throw new ConversionNotSupportedException(pce, field.getType(), ex);
		}
		catch (ConversionException ex) {
			PropertyChangeEvent pce = new PropertyChangeEvent(getRootInstance(), propertyName, oldValue, newValue);
			throw new TypeMismatchException(pce, field.getType(), ex);
		}
		catch (IllegalStateException ex) {
			PropertyChangeEvent pce = new PropertyChangeEvent(getRootInstance(), propertyName, oldValue, newValue);
			throw new ConversionNotSupportedException(pce, field.getType(), ex);
		}
		catch (IllegalArgumentException ex) {
			PropertyChangeEvent pce = new PropertyChangeEvent(getRootInstance(), propertyName, oldValue, newValue);
			throw new TypeMismatchException(pce, field.getType(), ex);
		}
	}
	
}
