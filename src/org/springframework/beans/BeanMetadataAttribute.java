package org.springframework.beans;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public class BeanMetadataAttribute
implements BeanMetadataElement
{
	 private final String name;
	 private final Object value;
	 private Object source;
	 
	 public BeanMetadataAttribute(String name,Object value)
	 {
		 Assert.notNull(name,"Name must not be null");
		 this.name=name;
		 this.value=value;
	 }

	public Object getSource() {
		return source;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}
	 
	public boolean equals(Object other)
	{
		if(this==other){
			return true;
		}
		if(!(other instanceof BeanMetadataAttribute)){
			return false;
		}
		BeanMetadataAttribute otherMa=(BeanMetadataAttribute)other;
		return (this.name.equals(otherMa.name))&&(ObjectUtils.nullSafeEquals(this.value, otherMa.value))&&(ObjectUtils.nullSafeEquals(this.source, otherMa.source));
	}
   
	public int hashCode()
	{
		return this.name.hashCode()*29+ObjectUtils.nullSafeHashCode(this.value);
	}
	
	public String toString()
	{
		return "metadata attribute '"+this.name+"'";
	}
}
