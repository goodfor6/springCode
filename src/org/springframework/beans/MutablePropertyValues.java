package org.springframework.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MutablePropertyValues implements PropertyValues,Serializable {
 
	private final List<PropertyValue>propertyValueList;
	
	private Set<String>processedProperties;
	
	private volatile boolean converted=false;
	
	public MutablePropertyValues(){
		this.propertyValueList=new ArrayList<PropertyValue>(0);
	}
	
	public MutablePropertyValues(PropertyValues original){
		if(original!=null){
			PropertyValue[]pvs=original.getPropertyValues();
			this.propertyValueList=new ArrayList<PropertyValue>(pvs.length);
			for(PropertyValue pv:pvs){
				this.propertyValueList.add(pv);
			}
		}
		else{
			this.propertyValueList=new ArrayList<PropertyValue>(0);
		}
	}
	
	public MutablePropertyValues(Map<?,?>original){
		if(original!=null){
			this.propertyValueList=new ArrayList<PropertyValue>(original.size());
			for(Map.Entry<?, ?> entry:original.entrySet()){
				this.propertyValueList.add(new PropertyValue(entry.getKey().toString(),entry.getValue()));
			}
		}
		
		else{
			this.propertyValueList=new ArrayList<PropertyValue>(0);
		}
	}
	
	public MutablePropertyValues(List<PropertyValue>propertyValueList){
		this.propertyValueList=(propertyValueList!=null?propertyValueList:new ArrayList<PropertyValue>());
	}

	public List<PropertyValue> getPropertyValueList() {
		return this.propertyValueList;
	}
	
	public int size(){
		return this.propertyValueList.size();
	}
	
	public MutablePropertyValues addPropertyValues(PropertyValues other){
		if(other!=null){
			PropertyValue[]pvs=other.getPropertyValues();
			for(PropertyValue pv:pvs){
				addPropertyValue(new PropertyValue(pv));
			}
		}
		return this;
	}
	
	public MutablePropertyValues addPropertyValues(Map<?,?>other){
		if(other!=null){
			for(Map.Entry<?, ?>entry:other.entrySet()){
				addPropertyValue(new PropertyValue(entry.getKey().toString(),entry.getValue()));
			}
		}
		return this;
	}
	
	public MutablePropertyValues addPropertyValue(PropertyValue pv){
		for(int i=0;i<this.propertyValueList.size();i++){
			PropertyValue currentPv=this.propertyValueList.get(i);
			if(currentPv.getName().equals(pv.getName())){
				pv=mergeIfRequired(pv,currentPv);
				setPropertyValueAt(pv,i);
				return this;
			}
		}
		this.propertyValueList.add(pv);
		return this;
	}
	
	public void setPropertyValueAt(PropertyValue pv, int i) {
		this.propertyValueList.set(i, pv);
	}
	
	private PropertyValue mergeIfRequired(PropertyValue newPv,PropertyValue currentPv){
		Object value=newPv.getValue();
		if(value instanceof Mergeable){
			Mergeable mergeable=(Mergeable)value;
			if(mergeable.isMergeEnabled()){
				Object merged=mergeable.mege(currentPv.getValue());
				return new PropertyValue(newPv.getName(),merged);
			}
		}
		return newPv;
	}
}
