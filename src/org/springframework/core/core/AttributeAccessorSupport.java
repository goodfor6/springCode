package org.springframework.core.core;

import org.springframework.core.util.Assert;

import javax.management.Attribute;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/9/20 0020.
 */
public  abstract class AttributeAccessorSupport implements AttributeAccessor,Serializable {

    private final Map<String,Object> attributes = new LinkedHashMap<String,Object>(0);

    public void setAttributes(String name,Object value){
        Assert.notNull(name,"Name must not be null");
        if(value != null){
            this.attributes.put(name,value);
        }
        else{
            removeAttribute(name);
        }
    }

    public Object getAttribute(String name){
        Assert.notNull(name,"name must not be null");
        return this.attributes.get(name);
    }

    public Object removeAttribute(String name){
        Assert.notNull(name,"Name must not be null");
        return this.attributes.remove(name);
    }

    public boolean hasAttribute(String name){
        Assert.notNull(name,"Name must not be null");
        return this.attributes.containsKey(name);
    }

    public String[] attributeNames(){return this.attributes.keySet().toArray(new String[this.attributes.size()]);}

    protected void copyAttributeFrom(AttributeAccessor source){
        Assert.notNull(source,"Source must not be null");
        String[] attributeNames = source.attributeNames();
        for(String attributeName : attributeNames){
            setAttribute(attributeName,source.getAttribute(attributeName));
        }
    }

    protected void copyAttributeForm(AttributeAccessor source){
        Assert.notNull(source,"Source must not be null");
        String [] attributeNames = source.attributeNames();
        for(String attributeName : attributeNames){
            setAttribute(attributeName,source.getAttribute(attributeName));
        }
    }

    public boolean equals(Object other){
        if(this == other){
            return true;
        }
        if(!(other instanceof AttributeAccessorSupport)){
            return false;
        }
        AttributeAccessorSupport that = (AttributeAccessorSupport)other;
        return this.attributes.equals(that.attributes);
    }

    public int hashCode(){return this.attributes.hashCode();}


}
