package org.springframework.beans.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.util.StringUtils;

public class PropertyComparator<T> implements Comparator<T> {
	
	protected final Log logger=LogFactory.getLog(getClass());
	
	private final SortDefinition sortDefinition;
	
	private final BeanWrapperImpl beanWrapper=new BeanWrapperImpl(false);
	
	public PropertyComparator (SortDefinition sortDefinition){
		this.sortDefinition=sortDefinition;
	}
	
	public PropertyComparator(String property,boolean ignoreCase,boolean ascending){
		this.sortDefinition=new MutableSortDefinition(property,ignoreCase,ascending);
	}
	
	public final SortDefinition getSortDefinition(){
		return this.sortDefinition;
	}
	
	public int compare(T o1,T o2){
		Object v1=getPropertyValue(o1);
		Object v2=getPropertyValue(o2);
		if(this.sortDefinition.isIgnoreCase()&& (v1 instanceof String)&& (v2 instanceof String)){
			v1=((String)v1).toLowerCase();
			v2=((String) v2).toLowerCase();
		}
		int result;
		
		try{
			if(v1!=null){
				result=(v2!=null ? ((Comparable<Object>)v1).compareTo(v2):-1);
			}
			else{
				result=(v2!=null?1:0);
			}
		}catch(RuntimeException ex){
			if(logger.isWarnEnabled()){
				logger.warn("Coubld not sort objects ["+o1+"] and ["+o2+"]",ex);
			}
			return 0;
		}
		return (this.sortDefinition.isAscending()? result:-result);
	}
	
	private Object getPropertyValue(Object obj){
		try{
			this.beanWrapper.setWrappedInstance(obj);
			return this.beanWrapper.getPropertyValue(this.sortDefinition.getProperty());
		}
		catch(BeansException ex){
			logger.info("PropertyComparator could not access property - treating as null for sorting",ex);
			return null;
		}
	}
	
	public static void sort(List<?>source, SortDefinition sortDefinition) throws BeansException{
		if(StringUtils.hasText(sortDefinition.getProperty())){
			Collections.sort(source,new PropertyComparator<Object>(sortDefinition));
		}
	}
	
	public static void sort (Object[] source,SortDefinition sortDefinition )throws BeansException{
		if(StringUtils.hasText(sortDefinition.getProperty())){
			Arrays.sort(source,new PropertyComparator<Object>(sortDefinition));
		}
	}

}
