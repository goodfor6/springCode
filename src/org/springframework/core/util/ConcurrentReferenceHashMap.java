package org.springframework.core.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.util.ConcurrentReferenceHashMap.ReferenceType;
import org.springframework.util.ObjectUtils;



/**
 * Created by Administrator on 2017/7/27 0027.
 */
public class ConcurrentReferenceHashMap<K,V>extends AbstractMap<K,V> implements ConcurrentMap<K,V> {
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75F;
    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;
    private static final ReferenceType DEFAULT_REFERENCE_TYPE = ReferenceType.SOFT;
    private static final int MAXIMUM_CONCURRENCY_LEVEL = 1 << 16;
    private static final int MAXIMUM_SEGMENT_SIZE = 1 << 30;
    private final Segment[] segments;
    private final float loadFactor;
    private final ReferenceType referenceType;
    private final int shift;
    private Set<Map.Entry<K,V>> entrySet;

    public ConcurrentReferenceHashMap(){
    	this(DEFAULT_INITIAL_CAPACITY,DEFAULT_LOAD_FACTOR,DEFAULT_CONCURRENCY_LEVEL,DEFAULT_REFERENCE_TYPE);
    }

    public ConcurrentReferenceHashMap(int initialCapacity){this(initialCapacity,DEFAULT_LOAD_FACTOR,DEFAULT_CONCURRENCY_LEVEL,DEFAULT_REFERENCE_TYPE);}

    public ConcurrentReferenceHashMap(int initialCapacity,float loadFactor){
        this(initialCapacity,loadFactor,DEFAULT_CONCURRENCY_LEVEL,DEFAULT_REFERENCE_TYPE);
    }

    public ConcurrentReferenceHashMap(int initialCapacity,int concurrencyLevel){
        this(initialCapacity,DEFAULT_LOAD_FACTOR,concurrencyLevel,DEFAULT_REFERENCE_TYPE);
    }

    public ConcurrentReferenceHashMap(int initialCapacity, org.springframework.util.ConcurrentReferenceHashMap.ReferenceType referenceType) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, referenceType);
    }

    public ConcurrentReferenceHashMap(int initialCapacity,float loadFactor,int concurrencyLevel){
        this(initialCapacity, loadFactor,concurrencyLevel,DEFAULT_REFERENCE_TYPE);
    }
    							
    public ConcurrentReferenceHashMap(int initialCapacity, float loadFactor, int concurrencyLevel, org.springframework.util.ConcurrentReferenceHashMap.ReferenceType referenceType){
        Assert.isTrue(initialCapacity >= 0,"initial capacity must not be negative");
        Assert.isTrue(loadFactor > 0.0F, "Load factor must be positive");
        Assert.isTrue(concurrencyLevel > 0 ,"Concurrency level must be positive");
        Assert.notNull(referenceType,"Reference type must not be null");
        this.loadFactor = loadFactor;
        this.shift = calculateShift(concurrencyLevel,MAXIMUM_CONCURRENCY_LEVEL);
        int size =1 << this.shift;
        this.referenceType = referenceType;
        int roundedUpSegmentCapacity = (int) (((long)(initialCapacity + size )-1L)/(long) size);
        this.segments = (Segment[])((Segment[]) Array.newInstance(Segment.class,size));

        for(int i=0;i< this.segments.length;++i){
            this.segments[i] = new Segment(roundedUpSegmentCapacity);
        }
    }
    
    protected static int calculateShift(int minimumValue,int maximumValue){
    	int shift = 0;
    	int value = 1;
    	while(value < minimumValue && value < maximumValue){
    		value <<= 1;
    		shift++;
    	}
    	return shift;
    }

	protected final float getLoadFactor() {
		return this.loadFactor;
	}

	protected final int getSegmentsSize() {
		return this.segments.length;
	}

	protected final Segment getSegment(int index) {
		return this.segments[index];
	}
	protected ReferenceManager createReferenceManager() {
		return new ReferenceManager();
	}
	
	protected int getHash(Object o){
		int hash = o ==null ? 0 : o.hashCode();
		hash += (hash << 15)^0xffffcd7d;
		hash += (hash >> 10);
		hash += (hash << 3);
		hash ^= (hash >>> 6);
		hash += (hash <<2)+ (hash<<14);
		hash ^= (hash >>> 16);
		return hash;
	}
	
	public V get(Object key){
		Reference<K,V> reference = getReference(key,Restructure.WHEN_NECESSARY);
		Entry<K,V> entry = (reference != null ? reference.get(): null);
		return (entry != null ? entry.getValue():null);
	}
	
	public boolean containskey(Object key){
		Reference<K,V> reference = getReference(key,Restructure.WHEN_NECESSARY);
		Entry<K,V> entry = (reference != null ? reference.get(): null);
		return (entry != null && ObjectUtils.nullSafeEquals(entry.getKey(),key));
	}
	
	protected final Reference<K,V> getReference(Object key,Restructure restructure){
		int hash = getHash(key);
		return getSegmentForHash(hash).getReference(key,hash,restructure);
	}
	
	public V put(K key,V value){
		return put(key,value,true);
	}
	
	public V putIfAbsent(K key,V value){
		return put(key,value,false);
	}
	
	private <T> T doTask(Object key,Task<T>task){
		int hash = getHash(key);
		return getSegmentForHash(hash).doTask(hash,key,task);
	}
	
	private V put(final K key,final V value,final boolean overwriteExisting){
		return doTask(key,new Task<V>(TaskOption.RESTRUCTURE_BEFORE,TaskOption.RESIZE){
			@Override
			protected V execute(Reference<K,V> reference, Entry<K,V> entry, Entries entries){
				if(entry != null){
					V previousValue = entry.getValue();
					if(overwriteExisting){
						entry.setValue(value);
					}
					return previousValue;
				}
				entries.add(value);
				return null;
			}
		});
	}
	
	public boolean remove(Object key,final Object value){
		return doTask(key,new Task<Boolean>(TaskOption.RESTRUCTURE_AFTER,TaskOption.SKIP_IF_EMPTY){
			protected Boolean execute(Reference<K,V> reference,Entry<K,V> entry){
				if(entry != null && ObjectUtils.nullSafeEquals(entry.getValue(), value)){
					reference.release();
					return true;
				}
				return false;
			}
		});
	}
	
	protected static final class Entry <K,V> implements Map.Entry<K, V>{
		private final K key;
		
		private volatile V value;
		
		public Entry(K key ,V value){
			this.key = key;
			this.value = value;
		}
		
		public K getKey(){
			return this.key;
		}
		
		@Override
		public V getValue(){
			return this.value;
		}
		
		public V setValue(V value){
			V previous = this.value;
			this.value = value;
			return previous;
		}
		
		public String toString(){
			return (this.key + "=" + this.value);
		}
		
		@SuppressWarnings("rawtypes")
		public final boolean equals(Object other){
			if (this == other){
				return true;
			}
			if(!(other instanceof Map.Entry)){
				return false;
			}
			Map.Entry otherEntry = (Map.Entry) other;
			return (ObjectUtils.nullSafeEquals(getKey(), otherEntry.getKey()) &&
					ObjectUtils.nullSafeEquals(getValue(),otherEntry.getValue()));
		}
		public final int hashCode(){
			return (ObjectUtils.nullSafeHashCode(this.key)^ ObjectUtils.nullSafeHashCode(this.value));
		}

	}
	
	public boolean replace(K key,final V oldValue,final V newValue){
		return doTask(key,new Task<Boolean>(TaskOption.RESTRUCTURE_BEFORE,TaskOption.SKIP_IF_EMPTY){
			protected Boolean execute(Reference<K,V> reference,Entry<K,V>entry){
				if(entry != null && ObjectUtils.nullSafeEquals(entry.getValue(), oldValue)){
					entry.setValue(newValue);
					return true;
				}
				return false;
			}
		});
	}
	
	public V replace(K key,final V value){
		return doTask(key,new Task<V>(TaskOption.RESTRUCTURE_BEFORE,TaskOption.SKIP_IF_EMPTY){
			protected V execute(Reference<K,V>reference,Entry<K,V>entry){
				if(entry != null){
					V previousValue = entry.getValue();
					entry.setValue(value);
					return previousValue;
				}
				return null;
			}
		});
	}
	
	public void clear(){
		for(Segment segment : this.segments){
			segment.clear();
		}
	}
	
	public void purgeUnreferenceEntries(){
		for(Segment segment : this.segments){
			segment.restructureIfNecessary(false);
		}
	}
	
	public int size(){
		int size = 0;
		for(Segment segment : this.segments){
			size += segment.getCount();
		}
		return size;
	}
	
	public Set<java.util.Map.Entry<K,V>>entrySet(){
		if(this.entrySet == null){
			this.entrySet = new EntrySet();
		}
		return this.entrySet();
	}
	
	private abstract class Task<T>{
		private final EnumSet<TaskOption>options;
		
		public Task(TaskOption... options){
			this.options = (options.length == 0 ? EnumSet.noneOf(TaskOption.class):EnumSet.of(options[0],options));
		}
		
		public boolean hasOption(TaskOption option){
			return this.options.contains(option);
		}
		
		protected T execute(Reference<K,V>reference,Entry<K,V>entry,Entries entries){
			return execute(reference,entry);
		}
		protected T execute(Reference<K,V>reference,Entry<K,V>entry){
			return null;
		}
	}
	
	private static enum TaskOption{
		RESTRUCTURE_BEFORE,RESTRUCTURE_AFTER,SKIP_IF_EMPTY,RESIZE
	}
	private Segment getSegmentForHash(int hash){
		return this.segments[(hash >>> (32 -this.shift))& (this.segments.length-1)];
	}
	
	protected class ReferenceManager
	{
	private final ReferenceQueue<Entry<K, V>> queue = new ReferenceQueue<Entry<K, V>>();

	public Reference<K, V> createReference(Entry<K, V> entry, int hash, Reference<K, V> next) {
		if (ConcurrentReferenceHashMap.this.referenceType == ReferenceType.WEAK) {
		}
		return new SoftEntryReference<K, V>(entry, hash, next, this.queue);
	}

	@SuppressWarnings("unchecked")
	public Reference<K,V> pollForPurge(){
			return (Reference<K,V>)this.queue.poll();	
	  }
	}
	
	private static final class SoftEntryReference<K,V> extends SoftReference<Entry<K,V>> implements Reference<K,V>{
		private final int hash;
		private final Reference<K,V> nextReference;
		public SoftEntryReference(Entry<K,V>entry,int hash,Reference<K,V>next,ReferenceQueue<Entry<K,V>>queue){
			super(entry,queue);
			this.hash = hash;
			this.nextReference = next;
		}
		@Override
		public int getHash() {
			return this.hash;
		}

		@Override
		public Reference<K, V> getNext() {
			return this.nextReference;
		}

		@Override
		public void release() {
			enqueue();
			clear();
		}
	}

	private abstract class Entries{
		public abstract void add(V value);
	}
	
	private class EntrySet extends AbstractSet<Map.Entry<K,V>>{
		@Override
		public Iterator<Map.Entry<K,V>>iterator(){
			return new EntryIterator();
		}
		@Override
		public boolean contains(Object o){
			if(o != null && o instanceof Map.Entry<?, ?>){
				Map.Entry<?,?> entry = (java.util.Map.Entry<?, ?>)o;
				Reference<K,V> reference = ConcurrentReferenceHashMap.this.getReference(entry.getKey(),Restructure.NEVER);
				Entry<K,V> other = (reference != null ? reference.get() : null);
				if(other != null){
					return ObjectUtils.nullSafeEquals(entry.getValue(), other.getValue());
				}
			}
			return false;
		}
		
		public boolean remove(Object o){
			if(o instanceof Map.Entry<?, ?>){
				Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
				return ConcurrentReferenceHashMap.this.remove(entry.getKey(), entry.getValue());
			}
			return false;
		}
		
		public int size(){
			return ConcurrentReferenceHashMap.this.size();
		}
		
		public void clear(){
			ConcurrentReferenceHashMap.this.clear();
		}
		
	}
	
	private class EntryIterator implements Iterator<Map.Entry<K, V>>{
		
		private int segmentIdex;
		
		private int referenceIndex;
		
		private Reference<K,V> [] references;
		
		private Reference<K,V> reference;
		
		private Entry<K,V> next;
		
		private Entry<K,V> last;
		
		public EntryIterator(){
			moveToNextSegment();
		}
		
		public boolean hasNext() {
			getNextIfNecessary();
			return (this.next != null);
		}
		
		public Entry<K,V> next(){
			getNextIfNecessary();
			if( this.next == null){
				throw new NoSuchElementException();
			}
			this.last = this.next;
			this.next = null;
			return this.last;
		}
		
		private void getNextIfNecessary(){
			while (this.next == null){
				moveToNextReference();
				if(this.reference == null){
					return ;
				}
				this.next = this.reference.get();
			}
		}
		
		private void moveToNextReference(){
			if(this.reference != null){
				this.reference = this.reference.getNext();
			}
			while(this.reference == null && this.reference != null){
				if(this.referenceIndex >= this.references.length){
					moveToNextSegment();
					this.referenceIndex = 0;
				}
				else{
					this.reference = this.references[this.referenceIndex];
					this.referenceIndex++;
				}
			}
		}
		
		private void moveToNextSegment(){
			this.reference = null;
			this.references = null;
			if(this.segmentIdex < ConcurrentReferenceHashMap.this.segments.length){
				this.references = ConcurrentReferenceHashMap.this.segments[this.segmentIdex].references;
				this.segmentIdex++;
			}
		}
		
		public void remove(){
			Assert.state(this.last != null);
			ConcurrentReferenceHashMap.this.remove(this.last.getKey());
		}
		
	}
	
    protected final class Segment extends ReentrantLock {
        private final ConcurrentReferenceHashMap<K,V>.ReferenceManager referenceManager =ConcurrentReferenceHashMap.this.createReferenceManager();
        private final int initialSize;
        private volatile Reference<K,V>[] references;
        private volatile int count = 0;
        private int resizeThreshold;

        public Segment(int initialCapacity) {
            this.initialSize = 1 << ConcurrentReferenceHashMap.calculateShift(initialCapacity,1073741824);
            this.setReferences(this.createReferenceArray(this.initialSize));
        }
        
        public Reference<K, V> getReference(Object key, int hash, Restructure restructure) {
			return null;
		}

		private Reference<K,V>[] createReferenceArray(int size){
        	return (Reference<K,V>[]) Array.newInstance(Reference.class, size);
        }
        
        private void setReferences(Reference<K,V>[] references){
        	this.references = references;
        	this.resizeThreshold = (int)(references.length*getLoadFactor());
        }
        
        public <T> T doTask(final int hash,final Object key,final Task <T> task){
        	boolean resize = task.hasOption(TaskOption.RESIZE);
        	if(task.hasOption(TaskOption.RESIZE)){
        		restructureIfNecessary(resize);
        	}
        	if(task.hasOption(TaskOption.SKIP_IF_EMPTY)&& this.count == 0){
        		return task.execute(null, null,null);
        	}
        	lock();
        	try{
        		final int index = getIndex(hash,this.references);
        		final Reference<K,V> head = this.references[index];
        		Reference<K, V> reference = findInChain(head,key,hash);
        		Entry<K,V> entry = (reference!= null ?reference.get():null);
        		Entries entries = new Entries(){
        			public void add(V value){
        				@SuppressWarnings("unchecked")
        				Entry<K,V> newEntry = new Entry<K,V>((K)key,value);
        				Reference<K,V> newReference = Segment.this.referenceManager.createReference(newEntry,hash,head);
        				Segment.this.references[index] = newReference;
        				Segment.this.count++;
        			}
        		};
        		return task.execute(reference, entry,entries);
        	}
        	finally{
        		unlock();
        		if(task.hasOption(TaskOption.RESTRUCTURE_AFTER)){
        			restructureIfNecessary(resize);
        		}
        	}
        }
        
        
        private Reference<K,V>findInChain(Reference<K,V> reference,Object key,int hash){
        	while (reference != null){
        		if(reference.getHash() == hash){
        			Entry<K,V> entry = reference.get();
        			if(entry != null){
        				K entryKey = entry.getKey();
        				if(entryKey == key || entryKey.equals(key)){
        					return reference;
        				}
        			}
        		}
        		reference = reference.getNext();
        	}
        	return null;
        }
        
        public void clear(){
        	if(this.count == 0){
        		return;
        	}
        	lock();
        	try{
        		setReferences(createReferenceArray(this.initialSize));
        		this.count = 0;
        	}
        	finally{
        		unlock();
        	}
        }
        
        protected final void restructureIfNecessary(boolean allowResize){
        	boolean needsResize = ((this.count > 0) && (this.count >= this.resizeThreshold));
        	Reference<K,V> reference = this.referenceManager.pollForPurge();
        	if((reference != null) || (needsResize && allowResize)){
        		lock();
        		try{
        			int countAfterRestructure = this.count;
        			Set<Reference<K,V>> toPurge = Collections.emptySet();
        			if(reference != null){
        				toPurge = new HashSet<Reference<K,V>>();
        				while(reference != null){
        					toPurge.add(reference);
        					reference = this.referenceManager.pollForPurge();
        				}
        			}
        			countAfterRestructure -=toPurge.size();
        			needsResize = (countAfterRestructure > 0 && countAfterRestructure >= this.resizeThreshold);
        			boolean resizing = false;
        			int restructureSize = this.references.length;
        			if(allowResize && needsResize && restructureSize < MAXIMUM_SEGMENT_SIZE){
        				restructureSize <<= 1;
        				resizing = true;
        			}
        			                                            
        			Reference<K,V>[] restructured = (resizing ? createReferenceArray(restructureSize): this.references); 
        			for( int i = 0; i< this.references.length; i++){
        				reference = this.references[i];
        				if(!resizing){
        					restructured[i] = null;
        				}
        				while(reference != null){
        					if(!toPurge.contains(reference) && (reference.get()!=null)){
        						int index = getIndex(reference.getHash(),restructured);
        						restructured[index] = this.referenceManager.createReference(
        								reference.get(), reference.getHash(),restructured[index]); 
        					}
        					reference = reference.getNext();
        				}
        			}
        			if(resizing){
        				setReferences(restructured);
        			}
        			this.count = Math.max(countAfterRestructure, 0);
        			
        		}finally{
        			unlock();
        		}
        	}
        }
       
        public final int getSize(){
        	return this.references.length;
        }
        
        public final int getCount(){
        	return this.count;
        }
        
    }
    
    @SuppressWarnings("unchecked")
	private Reference<K,V>[]createReferenceArray(int size){
    	return (Reference<K,V>[])Array.newInstance(Reference.class,size);
    }
    
    private int getIndex(int hash,Reference<K,V>[] references){
    	return (hash & (references.length - 1));
    }

    protected static interface Reference<K,V>{
    	Entry<K,V>get();
    	int getHash();
    	Reference<K,V>getNext();
    	void release();
    }
   
    protected static enum Restructure{
    	WHEN_NECESSARY,NEVER
    }
}
