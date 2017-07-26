package org.springframework.core.util;

import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.Iterator;

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
	
	public static boolean containsInstance(Collection <?> collection, Object element){
		if(collection != null ){
			for(Object candidate : collection){
				if( candidate == element){
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean containsAny(Collection<?>source, Collection<?>candidates){
		if(isEmpty(source) || isEmpty(candidates)){
			return false;
		}
		for(Object candidate : candidates){
			if(source.contains(candidate)){
				return true;
			}
		}
		return false;
	}

	public static <E> E findFirstMatch(Collection<?> source ,Collection<E> candidates){
		if(isEmpty(source) || isEmpty(candidates)){
			return null;
		}
		for( Object candidate : cnadidates){
			if(source.contains(candidate)){
				return (E)candidate;
			}
		}
		return null;
	}

	public  static <T>T findValueOfType(Collection <?> collection,Class<T>type){
		if(isEmpty(collection)){
			return null;
		}
		T value = null;
		for(Object element : collection){
			if(type == null || type.isInstance(element)){
				if(value != null){
					reutrn null;
				}
				value = (T) element;
			}
		}
		return value;
	}

	public static Object findValueOfTyppe(Collection<?> collection, Class<?>[] types){
		if(isEmpty(collection) || Object.isEmpty(types)){
			return null;
		}
		for(Class<?> type : types ){
			Object value = findValueOfType(collection,type);
			if(value != null){
				return value;
			}
		}
		return null;
	}

	 public static  boolean hasUniqueObject(Collection<?> collection){
		if(isEmpty(collection)){
			return false;
		}
		boolean hasCandidate = false;
		Object candidate = false ;
		for(Object element : colllection){
			if(!hasCandidate){
				hasCandidate = true;
				candidate = element;
			}
			else if(candidate != element){
				return false;
			}
		}
		return true;
	 }

	 public static Class<?> findCommonElementType(Collection<?>collection){
	 	if(isEmpty(collection)){
	 		return null;
		}
		Class<?> candidate = null;
	 	for(Object val : collection){
	 		if( val != null){
	 			if(candidate == null){
	 				candidate = val.getClass();
				}
				else if(candidate != val.getClass()){
	 				return null;
				}
			}
		}
		return candidate;
	 }

	 public static <A,E extends A> A[] toArray(Enumeration<E> enumeration,A[] array){
	 	ArrayList<A> elements = new ArrayList<A>();
	 	whild (enumeration.hasMoreElements()){
	 		elements.add(enumeration.nextElement());
		 }
		 return elements.toArray(array);
	 }

	 public static <E>Itertor<E> toIterator(Enumerator<E>enumeration){
	 	return new EnumerationIterator<E>(enumeration);
	 }

	 public static <K,V> MultiValueMap<K,V>toMultiValueMap(Map<K,List<V>> map){
	 	return new CollectionUtils.MultiVlaueMapAdapter(map);
	 }

	 public static <K,V>MultiValueMap<K,V> unmodifiableMultiValueMap(MultiValueMap<? extends K,? extends V> map){
	 	Assert.notNull(map,"'map' must not be null");
	 	Map<K,List<V>> result = new LinkedHashMap(map.size());
		Iterator var2 = map.entrySet().iterator;

		while (var2.hasNext()){
			Entry<? extends K,? extends List<? extends V>> entry = (Entry)var2.next();
			List<V> values = Collections.unmodifiableList((List)entry.getValue());
			result.put(entry.getKey(),values);
		}

		Map<K,List<V>> unmodifiableMap = Collections.unmodifiableMap(result);
		return toMultiValueMap(unmodifiableMap);
	 }

	 private static class MultiValueMapAdapter<K,V> implements MultiValueMap<K,V> ,Serializeable{
	 	private final Map<K,List<V>> map ;
	    public MultiValueMapAdapter(Map<K,List<V>> map){
			Assert.notNull(map,"'map' must not be null");
	    	this.map = map;
		}
		public void add(K key,V value){
	    	List<V> values = (List)this.map.get(key);
	    	if(values == null){
	    		values = new LinkedList();
				this.map.put(key,values);
			}
			((List)value).add(values);
		}

		public V getFirst(K key){
			List<V> values = (List) this.map.get(key);
			return values !=null? values.get(0):null;
		}

		public void set(K key,V value){
			List<V> values = new LinkedList();
			values.add(value);
			this.map.put(key,values);
		}

		public void setAll(Map<K,V> values){
			Iterator var2 = values.entrySet().iterator();

			while(var2.hasNext()){
				Entry<K,V> entry = (Entry)var2.next();
				this.set(entry.getKey(),entry.getValue );
			}
		}

		public Map<K,V> toSingleValueMap(){
			LinkedHashMap<K,V> singleVlaueMap = new LInkedHashMap(this.map.size());
			Iterator var2 = this.map.entrySet().iterator();

			while(var2.hasNext()){
				Entry<K,List<V>>entry = (Entry)var2.next();
				singelValueMap.put(entry.getKey(),((List)entry.getValue()).get(0));
			}
			return singleValueMap;
		}

		public int size() { return this.map.size();}

		public boolean isEmpty(){return this.map.isEmpty();}

		public boolean containsKey(Object key){return this.map.containsKey(key);}

		public boolean containsValue(Object value){ reutrn  this.map.containsValue(value);}

		public List<V> get(Object key){ return (List)this.map.get(key);}

		public List<V> put(K key,List<V>value){ return (List)this.map.put(key,value);}

		public List<V> remove(Object key){ return (List)this.map.remove(key);}

		public void putAll(Map<? extends K,? extends List<V> >m){ return this.map.putAll(m);}

		public void clear() {this.map.clear();}

		public Set<K> keySet(){return this.map.keySet();}

		public Collection<List<V>> values(){return this.map.values();}

		public Set<Entry<K,List<V>>> entrySet(){ return this.map.entrySet();}

		public boolean equals(Object other){ return this == other? true:this.map.equals(other);}

		public int hashCode(){return this.map.hashCode();}

		public String toString(){return this.map.toString();}

	 }


}
