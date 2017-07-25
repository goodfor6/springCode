package org.springframework.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.util.ObjectUtils;

public abstract class CollectionUtils {

	public static boolean isEmpty(Collection<?> collection ){
		return (collection == null || collection.isEmpty());
	}
	
	public static boolean isEmpty(Map<?,?>map){
		return (map == null || map.isEmpty());
	}
	
	@SuppressWarnings("rawtypes")
	public static List arrayToList(Object source){
		return Arrays.asList(ObjectUtils.toObjectArray(source));
	}
	
	@SuppressWarnings("unchecked")
	public static <E> void megeArrayIntoCollection(Object array,Collection<E> collection){
		if(collection == null){
			throw new IllegalArgumentException("Collection must not be null");
		}
		Object [] arr =ObjectUtils.toObjectArray(array);
		for(Object elem : arr){
			collection.add((E)elem);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <K,V> void megePropertiesIntoMap(Properties props,Map<K,V>map){
		if(map == null){
			throw new IllegalArgumentException("Map must not be null ");
		}
		if(props != null){
			for(Enumeration<?> en = props.propertyNames();en.hasMoreElements();){
				String key = (String) en.nextElement(); 
				Object value = props.getProperty(key);
				if( value == null){
					value = props.get(key);
				}
				map.put((K)key, (V)value);
			}
		}
	}
	
	public static boolean contains(Iterator<?>iterator, Object element){
		if(iterator != null){
			while (iterator.hasNext()){
				Object candidate = iterator.next();
				if (ObjectUtils.nullSafeEquals(candidate, element)){
					return true;
				}//
			}
		} 
		return false;     
	}
	
	public static boolean contains(Enumeration<?> enumeration,Object element){
		if (enumeration != null){
			while (enumeration.hasMoreElements()){
				Object candidate = enumeration.nextElement();
				if (ObjectUtils.nullSafeEquals(candidate, element)){
					return true;
				}
			}
		}
		return false;
	}
	

}
