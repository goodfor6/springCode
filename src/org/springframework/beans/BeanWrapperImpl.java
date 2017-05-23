package org.springframework.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.springframework.core.CollectionFactory;
import org.springframework.core.GenericCollectionTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.Property;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public class BeanWrapperImpl extends AbstractPropertyAccessor implements BeanWrapper { 
	//测试update****
	private static final Log logger=LogFactory.getLog(BeanWrapperImpl.class);
	private Object object;
	private String nestedPath="";
	private Object rootObject;
	private TypeConverterDelegate typeConverterDelegate;
	private AccessControlContext acc;
	private CachedIntrospectionResults cachedIntrospectionResults;
	private Map<String ,BeanWrapperImpl>nestedBeanWrappers;
	private boolean autoGrowNestedPaths=false;
	
	private  int autoGrowCollectionLimit=2147483647;
	
	public BeanWrapperImpl()
	{
		this(true);
	}
	public BeanWrapperImpl(boolean registerDefaultEditors)
	{
		if(registerDefaultEditors){
			registerDefaultEditors();
		}
		this.typeConverterDelegate=new TypeConverterDelegate(this);
	}
	
	public BeanWrapperImpl(Object object){
		registerDefaultEditors();
		setWrappedInstance(object);
	}
	
	public BeanWrapperImpl(Class<?>clazz)
	{
		registerDefaultEditors();
		setWrappedInstance(BeanUtils.instantiateClass(clazz);
	}
	
	public BeanWrapperImpl(Object object ,String nestedPath,Object rootObject)
	{
		registerDefaultEditors();
		setWrappedInstance(object,nestedPath,rootObject);
	}
	
	private BeanWrapperImpl(Object object ,String nestedPath,BeanWrapperImpl superBw)
	{
		setWrappedInstance(object,nestedPath,superBw.getWrappedInstance());
		setExtractOldValueForEditor(superBw.isExtractOldValueForEditor());
		setAutoGrowNestedPaths(superBw.isAutoGrowNestedPaths());
		setAutoGrowCollectionLimit(superBw.getAutoGrowCollectionLImit());
		setSecurityContext(superBw.acc);
	}
	
	public void setWrappedInstance(Object object)
	{
		setWrappedInstance(object,"",null);
	}
	
	public void setWrappedInstance(Object object,String nestedPath,Object rootObject)
	{
		Assert.notNull(object,"Bean object must not be null");
		this.object=object;
		this.nestedPath=(nestedPath!=null?nestedPath:"");
		this.rootObject=(!"".equals(this.nestedPath)?rootObject:object);
		this.nestedBeanWrappers=null;
		this.typeConverterDelegate=new TypeConverterDelegate(this,object);
		setIntrospectionClass(object.getClass());
	}
	public final Object getWrappedInstance() {
	   return this.object;
	  }
	
	public final Class getWrappedClass(){
		return this.object!=null?this.object.getClass():null;
	} 
	
	public final String getNestedPath()
	{
		return this.nestedPath;
	}
	
	public final Object getRootInstance()
	{
		return this.rootObject;
	}
	
	public final Class getRootClass()
	{
       return this.rootObject!=null?this.rootObject.getClass():null; 		
	}
	
	public void setAutoGrowNestedPaths(boolean autoGrowNestedPaths)
	{
		this.autoGrowNestedPaths=autoGrowNestedPaths;
	}

	public boolean isAutoGrowNestedPaths()
	{
		return this.autoGrowNestedPaths;
	}
	
	public void setAtuoGrowCollectionLImit(int autoGrowCollectionLimit)
	{
		this.autoGrowCollectionLimit=autoGrowCollectionLimit;
	}

	public int getAutoGrowCollectionLimit()
	{
		return this.autoGrowCollectionLimit;
	}
	
	public void setSecurityContext(AccessControlContext acc)
	{
		this.acc=acc;
	}
	
	public void setIntrospectionClass(Class clazz)
	{
		if((this.cachedIntrospectionResults!=null)&&(!clazz.equals(this.cachedIntrospectionResults.getBeanClass())))
		{
			this.cachedIntrospectionResults=null;
		}
	}
	
	private CachedIntrospectionResults getCachedIntrospectionResults()
	{
		return getCachedIntrospectionResults().getPropertyDescriptor();
	}
	
	public PropertyDescriptor getPropertyDescriptor(String propertyName)throws BeansException{
		PropertyDescriptor pd=getPropertyDescriptorInternal(propertyName);
		if(pd==null){
			throw new InvalidPropertyException(getRootClass(),this.nestedPath+propertyName," No rpoperty' "+propertyName+" 'found ");
		}
		return pd;
	}
	
	protected PropertyDescriptor getPropertyDescriptorInternal(String propertyName)
	throws BeansException
	{
		Assert.notNull(propertyName,"Property name must not be null");
		BeanWrapperImpl nestedBw=getBeanWrapperForPropertyPath(propertyName);
		return nestedBw.getCachedIntrospectionResults().getPropertyDescriptor(getGFinalPath(nestedBw,propertyName));
	}
	
	public Class getPropertyType(String propertyName)throws BeansException
	{
		try{
			PropertyDescriptor pd=getPropertyDescriptorInternal(propertyName);
			if(pd!=null){
				return pd.getPropertyType();
			}
			Object value=getPropertyValue(propertyName);
			if(value!=null){
				return value.getClass();
			}
			Class editorType=guessPropertyTypeFormEditors(propertyName);
			if(editorType!=null){
				return editorType;
			}
		}catch(InvalidPropertyException ex){}
		return null;
	}
	
	public TypeDescriptor getPropertyTypeDescriptor(String propertyName)
	throws BeansException{
		try{
		BeanWrapperImpl nestedBw=getBeanWrapperForPropertyPath(propertyName);
		String finalPath=getFinalPath(nestedBw,propertyName);
		PropertyTokenHolder tokens=getPropertyNameTokens(finalPath);
		PropertyDescriptor pd=nestedBw.getCachedIntrospectionResults().getPropertyDescriptor(tokens.actualName);
		if(pd!=null){
			if(tokens.keys!=null){
				if((pd.getReadMethod()!=null)||(pd.getWriteMethod()!=null)){
					return TypeDescriptor.nested(property(pd), tokens.keys.length);
				}
			}
			else if((pd.getReadMethod()!=null)||(pd.getWriteMethod()!=null)){
				return new TypeDescriptor(property(pd));
			}
		}
		
	}catch(InvalidPropertyException ex){
		
	}
	
	return null;
	}
	
	public boolean isReadableProperty(String propertyName)
	{
		try{
			PropertyDescriptor pd=getPropertyDescriptorInternal(propertyName);
			if(pd!=null){
				if(pd.getReadMethod()!=null){
					return true;
				}
			}else{
				getPropertyValue(propertyName);
				return true;
			}
		}catch(InvalidPropertyException ex){}
		return false;
	}
	
	public boolean isWritableProperty(String propertyName)
	{
		try{
			PropertyDescriptor pd=getPropertyDescriptorInternal(propertyName);
			if(pd!=null){
				if(pd.getWriteMethod()!=null){
					return true;
				}
			}else{
				getPropertyValue(propertyName);
				return true;
			}
		}catch(InvalidPropertyException ex){
			ex.printStackTrace();
		}
		return false;
	}

	public <T> T convertIfNecessary(Object value,Class<T>requiredType,MethodParameter methodParam)throws TypeMismatchException
	{
		try{
			return this.typeConverterDelegate.convertIfNecessary(value, requiredType, methodParam);
		}catch(ConverterNotFoundException ex){
			throw new ConversionNotSupportedException(value,requiredType,ex);
		}catch(ConversionException ex){
			throw new TypeMismatchException(value,requiredType,ex);
		}catch(IllegalStateException ex){
			throw new ConversionNotSupportedException(value,requiredType,ex);
		}catch(IllegalArgumentException ex){
			throw new TypeMismatchException(value,requiredType,ex);
		}
	}
	
	private Object convertIfNecessary(String propertyName,Object oldValue,Object newValue,Class<?>requiredType,TypeDescriptor td)
	throws TypeMismatchException
	{
		PropertyChangeEvent pce;
		try{
			return this.typeConverterDelegate.convertIfNecessary(propertyName, oldValue, newValue,requiredType,td);
		}catch(ConverterNotFoundException ex){
			 pce=new PropertyChangeEvent(this.rootObject,this.nestedPath+propertyName,oldValue,newValue);
			throw new ConversionNotSupportedException(pce,td.getType(),ex);
		}catch(ConversionException ex){
			 pce=new PropertyChangeEvent(this.rootObject,this.nestedPath+propertyName,oldValue,newValue);
			throw new TypeMismatchException(pce,requiredType,ex);
		}catch(IllegalStateException ex){
			 pce = new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, newValue);
			 throw new TypeMismatchException(pce,requiredType,ex);
		}
	}
	
	public Object convertForProperty(Object value,String propertyName)
	throws TypeMismatchException
	{
		PropertyDescriptor pd=getCachedIntrospectionResults().getPropertyDescriptor(propertyName);
		if(pd==null){
			throw new InvalidPropertyException(getRootClass(),this.nestedPath+propertyName,"No property'"+propertyName+"'found");
		}
		return convertForProperty(propertyName,null,value,pd);
	}
	
	private Object convertForProperty(String propertyName,Object oldValue,Object newValue,PropertyDescriptor pd)
	throws TypeMismatchException
	{
		return convertIfNecessary(propertyName,oldValue,newValue,pd.getPropertyType(),new TypeDescriptor(property(pd)));
	}
	
	private Property property(PropertyDescriptor pd){
		GenericTypeAwarePropertyDescriptor typeAware=(GenericTypeAwarePropertyDescriptor)pd;
		return new Property(typeAware.getBeanClass(),typeAware.getReadMethod(),typeAware.getWriteMethod());
	}
	
	private String getFinalPath(BeanWrapper bw,String nestedPath)
	{
		if(bw==this){
			return nestedPath;
		}
		return nestedPath.substring(PropertyAccessorUtils.getLastNestedPropertySeparatorIndex(nestedPath)+1);
	}
	
	protected BeanWrapperImpl getBeanWrapperForPropertyPath(String propertyPath)
	{
		int pos=PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(propertyPath);
		if(pos>-1){
			String nestedProperty=propertyPath.substring(0,pos);
			String nestedPath=propertyPath.substring(pos+1);
			BeanWrapperImpl nestedBw=getNestedBeanWrapper(nestedProperty);
			return nestedBw.getBeanWrapperForPropertyPath(nestedPath);
		}
		return this;
	}
	
	private BeanWrapperImpl getNestedBeanWrapper(String nestedProperty)
	{
	   if(this.nestedBeanWrappers==null){
		   this.nestedBeanWrappers=new HashMap();
	   }
	   
	   PropertyTokenHolder tokens=getpropertyNameTokens(nestedProperty);
	   String canonicalName=tokens.canonicalName;
	   Object propertyValue=getPropertyValue(tokens);
	   if(propertyValue==null){
		   if(this.autoGrowNestedPaths){
			   propertyValue=setDefaultValue(tokens);
		   }else{
			   throw new NullValueInNestedPathException(getRootClass(),this.nestedPath+canonicalName);
		   }
	   }
	   
	   BeanWrapperImpl nestedBw=(BeanWrapperImpl)this.nestedBeanWrappers.get(canonicalName);
	   if((nestedBw==null)||(nestedBw.getWrappedInstance()!=propertyValue)){
		   if(logger.isTraceEnabled()){
			   logger.trace("Creanting new nested BwanWrapper for property ' "+canonicalName+"'");
		   }
		   nestedBw=newNestedBeanWrapper(propertyValue,this.nestedPath+canonicalName+".");
	       copyDefaultEditorsTo(nestedBw);
	       copyCustomEditorsTo(nestedBw,canonicalName);
	       this.nestedBeanWrappers.put(canonicalName,nestedBw);
	   }
	   else if(logger.isTraceEnabled()){
		   logger.trace("Using cached nested BeanWrapper for property '"+canonicalName+"'");
	   }
	   return nestedBw;
	}
	
	private Object setDefaultValue(String propertyName){
		PropertyTokenHolder tokens=new PropertyTokenHolder(null);
		tokens.actualName=propertyName;
		tokens.canonicalName=propertyName;
		return setDefaultValue(tokens);
	}
	
	private Object setDefaultValue(PropertyTokenHolder tokens){
		PropertyValue pv=createDefaultPropertyValue(tokens);
		setPropertyValue(tokens,pv);
		return getPropertyValue(tokens);
	}
	
	private PropertyValue createDefaultPropertyValue(PropertyTokenHolder tokens){
		Class type=getPropertyTypeDescriptor(tokens.canonicalName).getType();
		if(type==null){
			throw new NullValueInNestedPathException(getRootClass(),this.nestedPath+tokens.canonicalName,"Could not determine property type for auto-growing a dfault value");
		}
		Object defaultValue=newValue(type,tokens.canonicalName);
		return new PropertyValue(tokens.canonicalName,defaultValue);
	}
	
	
	private Object newValue(Class<?>type,String name){
		try{
			if(type.isArray()){
				Class componentType=type.getComponentType();
				if(componentType.isArray()){
					Object array=Array.newInstance(componentType, 1);
					Array.set(array, 0, Array.newInstance(componentType.getComponentType(), 0));
				    return array;
				}
				return Array.newInstance(componentType, 0);
			}
			if(Collection.class.isAssignableFrom(type)){
				return CollectionFactory.createMap(type, 16);
			}
			return type.newInstance();
		}catch(Exception ex){
			throw new NullValueInNestedPathException(getRootClass(),this.nestedPath+name,"Could not instantiate property type["+type.getName()+"]to auto-grow nested property path:"+ex);
		}
	}
	
	protected BeanWrapperImpl newNestedBeanWrapper(Object object,String nestedPath)
	{
		return new BeanWrapperImpl(object,nestedPath,this);
	}
	
	private PropertyTokenHolder getPropertyNameTokens(Stirng propertyName)
	{
		PropertyTokenHolder tokens=new PropertyTokenHolder(null);
		String actualName=null;
		List keys=new ArrayList(2);
		int searchIndex=0;
		while(searchIndex!=-1){
			int keyStart =propertyName.indexOf("[",searchIndex);
			searchIndex=-1;
			if(keyStart!=-1){
				int keyEnd=propertyName.indexOf("]",keyStart+"[".length());
				if(keyEnd!=-1){
					if(actualName==null){
						actualName=propertyName.subString(0,keyStart);
					}
					String key=propertyName.subString(keyStart+"[".length(),keyEnd);
					if(((key.startsWith("'"))&&(key.endsWith("'")))||((key.startsWith("\""))&&(key.endsWith("\"")))){
						key=key.substring(1,key.length()-1);
					}
					keys.add(key);
					searchIndex=keyEnd+"]".length();
				}
			}
		}
		tokens.actualName=(actualName!=null?actualName:propertyName);
		tokens.canonicalName=tokens.actualName;
		if(!keys.isEmpty()){
			PropertyTokenHolder tem216_215=tokens;
			tem216_215.canonicalName=(tem216_215.canonicalName+"["+StringUtils.collectionToDelimitedString(keys, "][")+"]");
			tokens.keys=StringUtils.toStringArray(keys);
		}
		return tokens;
	}
	
	public Object getPropertyValue(String propertyName) throws BeansException
	{
		BeanWrapperImpl nestedBw=getBeanWrapperForPropertyPath(propertyName);
		PropertyTokenHolder tokens=getPropertyNameTokens(getFinalPath(nestedBw,propertyName));
		return nestedBw.getPropertyValue(tokens);
	}
	
	
	
	private Object getPropertyValue(PropertyTokenHolder tokens)throws BeansException{
		String propertyName=tokens.canonicalName;
		String actualName=tokens.actualName;
		PropertyDescriptor pd=getCachedIntrospectionResults().getPropertyDescriptor(actualName);
		if((pd==null)||(pd.getReadMethod()==null)){
			throw new NotReadablePropertyException(getRootClass(),this.nestedPath+propertyName);
		}
		Method readMethod =pd.getReadMethod();
		try{
			if((!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers()))&&(!readMethod.isAccessible()))
				if(System.getSecurityManager()!=null)
					AccessController.doPrivileged(new PrivilegedAction(readMethod){
					public Object run(){
						this.val$readMethod.setAccessible(true);
						return null;
					}
				});
		else  readMethod.setAccessible(true);
			Object value;
			if(System.getSecurityManager()!=null){
				try{
					value=AccessController.doPrivileged(new PrivilegedExceptionAction(readMethod)){
						public Object run() throws Exception{
							return this.val$readMethod.invoke(BeanWrapperImpl.this.object,(Object[])null);
						}
					}
					,this.acc);
				}
				catch(PrivilegedActionException pae)
				{
					throw pae.getException();
				}
			}
			else{
				value=readMethod.invoke(this.object,(Object[])null);
			}
			
			if(tokens.keys!=null){
				if(value==null){
					if(this.autoGrowNestedPaths){
					   value=setDefaultValue(tokens.actualName);
				}
				else{
					throw new NullValueInNestedPathException(getRootClass(),this.nestedPath+propertyName,"cannot access indexed value of propety referenced in indexed property path '"+propertyName+"':returned null");
				}
			}
			String indexedPropertyName=tokens.actualName;
			for(int i=0;i<tokens.keys.length;i++){
				String key =tokens.keys[i];
				if(value==null){
					throw new NullValueInNestedPathException (getRootClass(),this.nestedPath+propertyName,"Cannot access indexed of property referenceed in indexed property path'"+propertyName+"':returned null");
				}
				if(value.getClass().isArray()){
					int index=Integer.parseInt(key);
					value =growArrayIfNecessary(value,index,indexedPropertyName);
					value=Array.get(value,index);
				}
				 else if ((value instanceof List)) {
				           int index = Integer.parseInt(key);
				           List list = (List)value;
				           growCollectionIfNecessary(list, index, indexedPropertyName, pd, i + 1);
				           value = list.get(index);
					   }
				else if((value instanceof Set))
				{
					Set set=(Set)value;
					int index=Integer.parseInt(key);
					if((index<0)||(index>=set.size())){
						throw new InvalidPropertyException(getRootClass(),this.nestedPath+propertyName,"Cannot get element with index " + index + " from Set of size " + set.size() + ", accessed using property path '" + propertyName + "'");
					}
					Iterator it=set.iterator();
					for(int j=0;it.hasNext();j++){
						Object elem=it.next();
						if(j==index){
							value=elem;
							break;
						}
					}
				}
				else if((value instanceof Map)){
					Map map=(Map)value;
					Class mapKeyType=GenericCollectionTypeResolver.getMapKeyReturnType(pd.getReadMethod(),i+1);
					TypeDescriptor typeDescriptor=mapKeyType!=null?TypeDescriptor.valueOf(mapKeyType):TypeDescriptor.valueOf(Object.class);
					Object convertedMapKey=convertIfNecessary(null,null,key,mapKeyType,typeDescriptor);
					value=map.get(convertedMapKey);
				}
				else{
					throw new InvalidPropertyException(getRootClass(),this.nestedPath+propertyName,"Property referenced in indexed property path ' is neither an array not a List nor a Set nor a Map;returned value was ["+value+"]");
				}
			indexedPropertyName=indexedPropertyName+"["+key+"]";
		}
	}
	return value;
	}
	catch(IndexOutOfBoundsException ex){
		throw new InvalidPropertyException(getRootClass(),this.nestedPath+propertyName,"Index of out of bounds in property path '"+propertyName+"'",ex);
	}catch(NumberFromatException ex){
		throw new InvaildPropertyException(getRootClass(),this.nestedPath+propertyName,"Invalid index in property path '"+propertyName+"'",ex);
	}catch (TypeMismatchException ex){
		throw new InvalidPropertyException (getRootClass(),this.nestedPath+propertyName,"Invalid index in property path '"+propertyName+"'",ex);
	}catch (InvocationTargetException ex){
		throw new InvalidPropertyException(getRootClass(),this.nestedPath+propertyName,"Getter for property '"+actualName+"'threw exception ",ex);
	}catch(Exception ex){
		throw new InvalidPropertyException (getRootClass(),this.nestedPath+propertyName,"Illegal attegal to get property '"+acturalName+"'threw exception",ex);
	}
  }
	
	private Object growArrayIfNecessary(Object array,int index,String name){
		if(!this.autoGrowNestedPaths){
			return array;
		}
		int length=Array.getLength(array);
		if((index>=length)&& (index<this.autoGrowCollectionLimit)){
			Class componentType=array.getClass().getComponentType();
			Object newArray=Array.newInstance(componentType, index+1);
			System.arraycopy(array, 0,newArray, 0, length);
            for(int i=length;i<Array.getLength(newArray);i++){
            }
            setPropertyValue(name,newArray);
            return getPropertyValue(name);
		}
		return array;
	}
	
	private void growCollectionIfNecessary(Collection collection,int index,String name,PropertyDecriptor pd,int nestingLevel)
	{
		if(!this.autoGrowNestedPaths){
			return ;
		}
		int size=collection.size();
		if((index>=size)&& (index<this.autoGrowCollectionLimit)){
			Class elementType=GenericCollectionTypeResolver.getCollectionReturnType(pd.getReadMethod(),nestingLevel);
			if(elementType!=null){
				for(int i=collection.size();i<index+1;i++){
					collection.add(newValue(elementType,name));
				}
			}
		}
	}
	
	public void setPropertyValue(String propertyName,Object value)throws BeansException
	{
		BeanWrapperImpl nestedBw;
		try{
			nestedBw=getBeanWrapperForPropertyPath(propertyName);
		}catch(NotReadablePropertyException ez){
			throw new NotWritablePropertyException(getRootClass(),this.nestedPath+propertyName,"Nested Property in path '"+propertyName+"' does not exist",ex);
		}
		PropertyTokenHolder tokens=getPropertyNameTokens(getFinalPath(nestedBw,propertyName));
		nestedBw.setPropertyValue(tokens,new PropertyValue(propertyName,value));
	}
	
	public void setPropertyValue(PropertyValue pv)throws BeansException
	{
		PropertyTokenHolder tokens=(PropertyTokenHolder)pv.resolvedTokens;
		String propertyName;
		if(tokens==null){
			 propertyName=pv.getName();
		BeanWrapperImpl nestedBw;
		try{
			nestedBw=getBeanWrapperForPropertyPath(propertyName);
		}catch(NotReadablePropertyException ex){
			throw new NotWritablePropertyException(getRootClass(),this.nestedPath+propertyName,"Nested property in path '"+propertyName+"'does not exist",ex);
		}
		tokens=getPropertyNameTokens(getFinalPath(nestedBw,propertyName));
		if(nestedBw==this){
			pv.getOriginalPropertyValue().resolvedTokens=tokens;
		}
		nestedBw.setPropertyValue(tokens,pv);
	   }else
	   {
		   setPropertyValue(tokens,pv);
	   }
   }
	
	private void setPropertyValue(PropertyTokenHolder tokens,PropertyValue pv)throws BeansException
	{
		String propertyName=tokens.canonicalName;
		String actualName=tokens.actualName;
		Object propValue;
	    if(tokens.keys!=null){
           PropertyTokenHolder getterTokens=new PropertyTokenHolder(null);
           getterTokens.canonicalName=tokens.canonicalName;
           getterTokens.actualName=tokens.actualName;
           getterTokens.keys=new String[tokens.keys.length-1];
           System.arraycopy(tokens.keys, 0, getterTokens.keys,0,tokens.keys.length-1);
           try{
        	   propValue=getPropertyValue(getterTokens);
           }catch(NotReadablePropertyException ex){
        	   throw new NotWritablePropertyException(getRootClass(),this.nestedPath+propertyName,"Cannot access indexed value in proeprty referenced in indexed property  path '"+propertyName+"'",ex);
           }
           String key =tokens.keys[tokens.keys.length-1];
           if(propValue==null){
        	   if(this.autoGrowNestedPaths){
        		   int lastKeyIndex=tokens.canonicalName.lastIndexOf('[');
        		   getterTokens.canonicalName=tokens.canonicalName.substring(0,lastKeyIndex);
        		   propValue=setDefaultValue(getterTokens);
        	   }
        	   else{
        		 throw new NullValueInNestedPathException(getRootClass(),this.nestedPath+propertyName,"Cannot access indexed value in property referenced in indexed property path'"+propertyName+"':returned null");
        	   }
           }
           if(propValue.getClass().isArray()){
        	   PropertyDescriptor pd=getCachedIntrospectionResults().getPropertyDescriptor(actualName);
        	   Class requiredType=propValue.getClass().getComponentType();
        	   int arrayIndex=Integer.parseInt(key);
        	   Object oldValue=null;
        	   try{
        		   if((isExtractOldVlaueForEditor())&& (arrayIndex<Array.getLength(propValue))){
        			   oldValue=Array.get(propValue, arrayIndex);
        		   }
        		   Object convertedValue=convertIfNecessary(propertyName,oldValue,pv.getValue(),requiredType,TypeDescriptor.nested(property(pd), tokens.keys.length));
        		   Array.set(propValue, arrayIndex, convertedValue);
        	   }catch(IndexOutOfBoundsException ex){
        		   throw new InvalidPropertyException(getRootClass(),this.nestedPath+propertyName,"Invalid array index in proeprty path'"+propertyName+"'",ex);
        	   }
           }else if((propValue instanceof List)){
        	   PropertyDescriptor pd=getCachedIntrospectionResults().getPropertyDescriptor(actualName);
        	   Class requiredType=GenericCollectionTyepResolver.getCollectionReturnType(pd.getReadMethod(),tokens.keys.length);
        	   List list=(List)propValue;
        	   int index=Integer.parseInt(key);
        	   Object oldValue=null;
        	   if((isExtractOldValueForEditor())&& (index<list.size())){
        		   oldValue=list.get(index);
        	   }
        	   Object convertedValue=ConvertIfNecessary(propertyName,oldValue,pv.getValue(),requiredType,TypeDescriptor.nested(property(pd), tokens.keys.length));
        	   int size=list.size();
        	   if((index>=size)&&(index<this.autoGrowCollectionLimit)){
        		   for(int i=size;i<index;i++){
        			   try{
        				   list.add(null);
        			   }catch (NullPointerException ex){
        				   throw new InvalidPropertyException(getRootClass(),this.nestedPath+propertyName,"Cannot set element with index "+index+"in List of size"+size+",accessed using property path'"+propertyName+"':List does not support filling up gaps with null elements");
        				   
        			   }
        		   }
        		   list.add(convertedValue);
        	   }
        	   try{
        		   list.set(index, convertedValue);
        	   }catch(IndexOutOfBoundsException ex){
        		   throw new InvalidPropertyException(getRootClass(),this.nestedPath+propertyName,"Invalid list index in property prth'"+propertyName+"'",ex);
        	   }
           }
	    }else if((propValue instanceof Map)){
	    	
	    }
	
	}
	
	
	
	
	
	
	
	
	
  private static class PropertyTokenHolder
   {
	   public String  canonicalName;
	   public String   actualName;
	   public String[] keys ;
   }
}	
	
	
	
