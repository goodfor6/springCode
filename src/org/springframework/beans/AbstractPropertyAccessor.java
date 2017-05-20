package org.springframework.beans;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractPropertyAccessor extends PropertyEditorRegistrySupport
implements ConfigurablePropertyAccessor{
	
	private boolean extractOldValueForEditor=false;

	public boolean isExtractOldValueForEditor() {
		return extractOldValueForEditor;
	}

	public void setExtractOldValueForEditor(boolean extractOldValueForEditor) {
		this.extractOldValueForEditor = extractOldValueForEditor;
	}
   
	public void setPropertyValue(PropertyValue pv)throws BeansException{
		setPropertyValue(pv.getName(),pv.getValue());
	}
	
	public void setPropertyValue(Map<?,?>map)throws BeansException
	{
		setPropertyValues(new MutablePropertyValues(map));
	}
	
	public void setPropertyValues(PropertyValues pvs,boolean ignoreUnknown)throws BeansException
	{
		setPropertyValues(pvs,ignoreUnknown,false);
	}
	
	public void setPropertyValues(PropertyValues pvs,boolean ignoreUnknown,boolean ignoreInvalid, boolean PropertyAccessException)throws BeansException
	{
		List propertyAccessExceptions=null;
		List<PropertyValue> propertyValues=(pvs instanceof MutablePropertyValues)?((MutablePropertyValues)pvs).getPropertyValueList():Arrays.asList(pvs.getPropertyValues());
	    for(PropertyValue  pv:propertyValues){
	    	try
	    	{
	    		setPropertyValue(pv);
	    	}
	    	catch(NotWritablePropertyException ex)
	    	{
	    		if(!ignoreUnknown){
	    			throw ex;
	    		}
	    	}catch(NullValueInNestedPathException ex){
	    		if(!ignoreInvalid){
	    			throw ex;
	    		}
	    	}
	    	catch(PropertyAccessException ex)
	    	{
	    		if(propertyAccessExceptions==null ){
	    			propertyAccessExceptions=new LinkedList();
	    		}
	    		propertyAccessExceptions.add(ex);
	    	}
	    	
	    }
	    if(propertyAccessExceptions!=null){
	    	PropertyAccessException[] paeArray=(PropertyAccessException[])propertyAccessExceptions.toArray(new PropertyAccessException[propertyAccessExceptions.size()]);
	       throw new PropertyBatchUpdateException(paeArray);
	    }
	}
	
	public <T>T convertIfNecessary(Object value,Class<T> requiredType)throws TypeMismatchException{
		return convertIfNecessary(value,requiredType,null);
	}
	
	public Class getPropertyType(String propertyPath)
	{
		return null;
	}
	
	public abstract Object getPropertyValue(String paramString,Object paramObject)throws BeansException;
}

 