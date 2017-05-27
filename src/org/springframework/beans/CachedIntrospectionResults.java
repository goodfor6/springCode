package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.SpringProperties;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

public class CachedIntrospectionResults {
   public static final String IGNORE_EBANINFO_PROPERTY_NAME="spring.beaninfo.ignore";
   
   private static final boolean shouldIntrospectorIgnoreBeaninfoClasses=SpringProperties.getFlag(IGNORE_EBANINFO_PROPERTY_NAME);
   
   private static List<BeanInfoFactory> beanInfoFactories=SpringFactoriesLoader.loadFactories(
		   BeanInfoFactory.class, CachedIntrospectionResults.class.getClassLoader());
   
   private static  final Log logger=LogFactory.getLog(CachedIntrospectionResults.class);
   
   static final Set<ClassLoader>acceptedClassLoaders=Collections.newSetFromMap(new ConcurrentHashMap<ClassLoader,Boolean>(16));
   
   static final ConcurrentMap<Class<?>,CachedIntrospectionResults>strongClassCache=new ConcurrentHashMap<Class<?>,CachedIntrospectionResults>(64);
   
   static final ConcurrentMap<Class<?>,CachedIntrospectionResults>softClassCache=new ConcurrentReferenceHashMap<Class<?>,CachedIntrospectionResults>(64);
   
   public static void acceptClassLoader(ClassLoader classLoader){
	   if(classLoader!=null){
		   acceptedClassLoaders.add(classLoader);
	   }
   }
   
   public static void clearClassLoader(ClassLoader classLoader){
	   for(Iterator <ClassLoader>it=acceptedClassLoaders.iterator();it.hasNext();){
		   ClassLoader registeredLoader=it.next();
		   if(isUnderneathClassLoader(registeredLoader,classLoader)){
			   it.remove();
		   }
	   }
	   for(Iterator<Class<?>>it=strongClassCache.keySet().iterator();it.hasNext();){
		   Class<?>beanClass=it.next();
		   if(isUnderneathClassLoader(beanClass.getClassLoader(),classLoader)){
			   it.remove();
		   }
	   }
	   for(Iterator<Class<?>>it=softClassCache.keySet().iterator();it.hasNext();){
		   Class<?>beanClass=it.next();
		   if(isUnderneathClassLoader(beanClass.getClassLoader(),classLoader)){
			   it.remove();
		   }
	   }
	   
   }
   
   private final BeanInfo beanInfo;
   
   private final Map<String,PropertyDescriptor>propertyDescriptorCache;
   
   private final ConcurrentMap<PropertyDescriptor,TypeDescriptor>typeDescriptorCache;
   
   private CachedIntrospectionResults(Class<?>beanClass)throws BeansException{
	   try{
		   if(logger.isTraceEnabled()){
			   logger.trace("Getting BeanInfo for class ["+beanClass.getName()+"]");
		   }
		   BeanInfo beanInfo=null;            
		   for(BeanInfoFactory beanInfoFactory:beanInfoFactories){
			   if(beanInfo!=null){
				   break;
			   }
		   }
		   if(beanInfo ==null){
			   beanInfo=(shouldIntrospectorIgnoreBeaninfoClasses?
					   Introspector.getBeanInfo(beanClass,Introspector.IGNORE_ALL_BEANINFO):
					   Introspector.getBeanInfo(beanClass));
		   }
		   this.beanInfo=beanInfo;
		   if(logger.isTraceEnabled()){
			   logger.trace("Caching PropertyDescriptors for class ["+beanClass.getName()+"]");
		   }
		   this.propertyDescriptorCache=new LinkedHashMap<String,PropertyDescriptor>();
		   PropertyDescriptor[]pds=this.beanInfo.getPropertyDescriptors();
		   for(PropertyDescriptor pd:pds){
			   if(Class.class.equals(beanClass)&&
					   ("classLoader".equals(pd.getName()) || "propertuionDomain".equals(pd.getName()))){
						   continue;
					   }
			   if(logger.isTraceEnabled()){
				   logger.trace("Found bean property '"+pd.getName()+"'"+
			        (pd.getPropertyType()!=null?"of type ["+pd.getPropertyType().getName()+"]":"")+
			        (pd.getPropertyEditorClass()!=null?";editor ["+pd.getPropertyEditorClass().getName()+"]":""));
			   }
			   pd=buildGenericTypeAwarePropertyDescriptor(beanClass,pd);
			   this.propertyDescriptorCache.put(pd.getName(), pd);
		   }
		   this.typeDescriptorCache=new ConcurrentReferenceHashMap<PropertyDescriptor,TypeDescriptor>();
	   }catch(IntrospectionException ex){
		   throw new FatalBeanException("Failed to obtain BeanInfo for class["+beanClass.getName()+"]",ex);
	   }
   }
   
   BeanInfo getBeanInfo(){return this.beanInfo;}
   
   Class<?>getBeanClass(){
	 return this.beanInfo.getBeanDescriptor().getBeanClass();   
   }
   
   PropertyDescriptor getPropertyDescriptor(String name){
	   PropertyDescriptor pd=this.propertyDescriptorCache.get(name);
       if(pd==null && StringUtils.hasLength(name)){
    	   pd=this.propertyDescriptorCache.get(name.substring(0,1).toLowerCase()+name.substring(1));
    	   if(pd==null){
    		   pd=this.propertyDescriptorCache.get(name.substring(0,1).toUpperCase()+name.substring(1));
    	   }
       }
      return (pd==null||pd instanceof GenericTypeAwarePropertyDescriptor ? pd:buildGenericTypeAwarePropertyDescriptor(getBeanClass(), pd));
   }
   
   PropertyDescriptor[] getPropertyDescriptors(){
	   PropertyDescriptor[]pds=new PropertyDescriptor[this.propertyDescriptorCache.size()];
	   int i=0;
	   for(PropertyDescriptor pd:this.propertyDescriptorCache.values()){
		   pds[i]=(pd instanceof GenericTypeAwarePropertyDescriptor ? pd:buildGenericTypeAwarePropertyDescriptor(getBeanClass(), pd));
		   i++;
	   }
	   return pds;
   }
   
   private PropertyDescriptor buildGenericTypeAwarePropertyDescriptor(Class<?>beanClass,PropertyDescriptor pd){
	   try{
		   return new GenericTypeAwarePropertyDescriptor(beanClass,pd.getName(),pd.getReadMethod(),pd.getWriteMethod(), pd.getPropertyEditorClass());
	   }catch(IntrospectionException ex){
		   throw new FatalBeanException("Failed to re-introspect class ["+beanClass.getName()+"]",ex);
	   }
   }
  TypeDescriptor addTypeDescriptor(PropertyDescriptor pd,TypeDescriptor td){
	  TypeDescriptor existing =this.typeDescriptorCache.putIfAbsent(pd, td);
	  return (existing!=null? existing:td);
  }
  
  TypeDescriptor getTypeDescriptor(PropertyDescriptor pd){
	  return this.typeDescriptorCache.get(pd);
  }
   
   private static boolean isUnderneathClassLoader(ClassLoader candidate,ClassLoader parent){
	   if(candidate==parent){
		   return true;
	   }
	   if(candidate ==null){
		   return false;
	   }
	   ClassLoader classLoaderToCheck=candidate;
	   while(classLoaderToCheck!=null){
		   classLoaderToCheck=classLoaderToCheck.getParent();
		   if(classLoaderToCheck==parent){
			   return true;
		   }
	   }
	   return false;
   }
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
}
