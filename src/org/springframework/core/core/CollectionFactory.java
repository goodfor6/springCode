package org.springframework.core.core;

import org.springframework.core.util.Assert;
import org.springframework.core.util.LinkedMultiValueMap;
import org.springframework.core.util.MultiValueMap;

import java.util.*;

/**
 * Created by Administrator on 2017/9/21 0021.
 */
public abstract class CollectionFactory {

    private static final Set<Class<?>>approximableCollectionTypes = new HashSet<>(11);

    private static final Set<Class<?>>approximableMapTypes = new HashSet<>(7);

    static{

        approximableCollectionTypes.add(Collection.class);
        approximableCollectionTypes.add(List.class);
        approximableCollectionTypes.add(Set.class);
        approximableCollectionTypes.add(SortedSet.class);
        approximableCollectionTypes.add(NavigableSet.class);
        approximableMapTypes.add(Map.class);
        approximableMapTypes.add(SortedMap.class);
        approximableMapTypes.add(NavigableMap.class);

        approximableCollectionTypes.add(ArrayList.class);
        approximableCollectionTypes.add(LinkedList.class);
        approximableCollectionTypes.add(HashSet.class);
        approximableCollectionTypes.add(LinkedHashSet.class);
        approximableCollectionTypes.add(TreeSet.class);
        approximableCollectionTypes.add(EnumSet.class);
        approximableMapTypes.add(HashMap.class);
        approximableMapTypes.add(LinkedHashMap.class);
        approximableMapTypes.add(TreeMap.class);
        approximableMapTypes.add(EnumMap.class);
    }

    public static boolean isApproximableCollectionType(Class<?>collectionType){
        return (collectionType != null && approximableCollectionTypes.contains(collectionType));
    }

    public static <E>Collection<E>createApproximateCollection(Object collection,int capacity){
        if(collection instanceof LinkedList){
            return new LinkedList<E>();
        }
        else if(collection instanceof List){
            return new ArrayList<E>(capacity);
        }
        else if(collection instanceof  EnumSet){
            Collection<E> enumSet = (Collection<E>)EnumSet.copyOf((EnumSet)collection);
            enumSet.clear();
            return enumSet;
        }
        else if(collection instanceof SortedSet){
            return new TreeSet<E>(((SortedSet<E>)collection).comparator());
        }
        else{
            return new LinkedHashSet<E>(capacity);
        }
    }

    public static <E> Collection<E> createCollection(Class<?>collectionType, int capacity){
        return createCollection(collectionType,null,capacity);
    }

    public static <E> Collection<E> createCollection(Class<?>collectionType,Class<?>elementType,int capacity){
        Assert.notNull(collectionType,"Collection type must not be null");
        if(collectionType.isInterface()){
            if(Set.class.equals(collectionType)|| Collection.class.equals(collectionType)){
                return new LinkedHashSet<E>(capacity);
            }
            else if(List.class.equals(collectionType)){
                return new ArrayList<E>();
            }
            else if(SortedSet.class.equals(collectionType)|| NavigableSet.class.equals(collectionType)){
                return new TreeSet<>();
            }
            else{
                throw new IllegalArgumentException("Unsupported Collection interface:"+collectionType.getName());
            }
        }
        else if(EnumSet.class.equals(collectionType)){
            Assert.notNull(elementType,"Cannot create EnumSet for unknown element type");
            return (Collection<E>)EnumSet.noneOf(asEnumType(elementType));
        }
        else{
            if(!Collection.class.isAssignableFrom(collectionType)){
                throw new IllegalArgumentException("Unsupported Collection type "+ collectionType.getName());
            }
            try{
                return (Collection<E>)collectionType.newInstance();
            }catch (Exception ex){
                throw new IllegalArgumentException(
                        "Could not instantiate Collection type:"+ collectionType.getName(),ex
                );
            }
        }
    }

    public static boolean isApproximableMapType(Class<?>mapType){
        return (mapType != null && approximableMapTypes.contains(mapType));
    }

    public static<K,V>Map<K,V>createApproximateMap(Object map,int capacity){
        if(map instanceof  EnumMap){
            EnumMap enumMap = new EnumMap((EnumMap)map);
            enumMap.clear();
            return enumMap;
        }
        else if(map instanceof SortedMap){
            return new TreeMap<K,V>(((SortedMap<K,V>)map).comparator());
        }
        else {
            return new LinkedHashMap<K,V>(capacity);
        }
    }

    public static <K,V>Map<K,V> createMap(Class<?>mapType,int capacity){return createMap(mapType,null,capacity);}

    public static <K,V>Map<K,V> createMap(Class<?>mapType,Class<?>keyType,int capacity){
        Assert.notNull(mapType,"Map type must not be null");
        if(mapType.isInterface()){
            if(Map.class.equals(mapType)){
                return new LinkedHashMap<K,V>(capacity);
            }
            else if(SortedMap.class.equals(mapType)|| NavigableMap.class.equals(mapType)){
                return new TreeMap<K,V>();
            }
            else if(MultiValueMap.class.equals(mapType)){
                return new LinkedMultiValueMap();
            }
            else{
                throw new IllegalArgumentException("Unsupported Map interface:"+mapType.getName());
            }
        }
        else if(EnumMap.class.equals(mapType)){
            Assert.notNull(keyType,"Cannot create EnumMap for unkown key type");
            return new EnumMap(asEnumType(keyType));
        }
        else{
            if(!Map.class.isAssignableFrom(mapType)){
                throw new IllegalArgumentException("Unsupported Map type :"+mapType.getName());
            }
            try{
                return (Map<K,V>) mapType.newInstance();
            }
            catch (Exception ex){
                throw new IllegalArgumentException("Could not instantiate Map type:"+mapType.getName(),ex);
            }
        }
    }

    private static Class<? extends Enum> asEnumType(Class<?>enumType){
        Assert.notNull(enumType,"Enum type must not be null");
        if(!Enum.class.isAssignableFrom(enumType)){
            throw new IllegalArgumentException("Supplied type is not an enum:"+enumType.getName());
        }
        return enumType.asSubclass(Enum.class);
    }

}
