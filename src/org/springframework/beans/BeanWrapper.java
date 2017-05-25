package org.springframework.beans;

import java.beans.PropertyDescriptor;

import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.InvalidPropertyException;

public abstract interface BeanWrapper extends ConfigurablePropertyAccessor
{
  public abstract Object getWrappedInstance();
  
  public abstract Class getWrappedClass();
  
  public abstract PropertyDescriptor[] getPropertyDesciptors();

  public abstract PropertyDescriptor getPropertyDescriptor(String paramsString)
  throws InvalidPropertyException;
  
  public abstract void setAutoGrowNestedPaths(boolean paramBoolean);
  
  public abstract boolean isAutoGrowNestedPaths(); 
  
  public abstract void setAutoGrowCollectionLimit(int paramInt);
  
  public abstract int getAutoGrowCollectionLImit();
}
