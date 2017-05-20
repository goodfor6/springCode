package org.springframework.beans;

import org.springframework.core.AttributeAccessorSupport;

public class BeanMetadataAttributeAccessor extends AttributeAccessorSupport implements BeanMetadataElement {
	private Object source;

	public Object getSource() {
		return source;
	}

	public void setSource(Object source) {
		this.source = source;
	}
	
	public void addMetadataAttribute(BeanMetadataAttribute attribute)
	{
		super.setAttribute(attribute.getName(),attribute);
	}
	
	public BeanMetadataAttribute getMetadataAttribute(String name)
	{
       return (BeanMetadataAttribute)super.getAttribute(name);
	}
	
	public void setAttribute(String name,Object value)
	{
		super.setAttribute(name, new BeanMetadataAttribute(name,value));
	}
	
	public Object getAttribute(String name)
	{
		BeanMetadataAttribute attribute =(BeanMetadataAttribute)super.getAttribute(name);
		return attribute!=null?attribute.getValue():null;
	}
	
	public Object removeAttribute(String name)
	{
		BeanMetadataAttribute attribute=(BeanMetadataAttribute)super.removeAttribute(name);
		return attribute!=null ? attribute.getValue():null;
	}
}
