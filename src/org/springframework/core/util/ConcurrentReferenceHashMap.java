package org.springframework.core.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.util.ConcurrentReferenceHashMap.ReferenceType;
import org.springframework.util.ObjectUtils;

import com.sun.javafx.css.CalculatedValue;


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
    private Set<Map<K,V>> entrySet;

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
	
	private V put(final K key,final V value,final boolean overwriteExisting){
		return doTask(key,new Task<V>(TaskOption.RESTRUCTURE_BEFORE,TaskOption.RESIZE)){
			protected V execute(Reference<k,V>reference,Entry<K,V>entry,Entries entries){
				if(entry != null){
					V previousValue = entry.getValue();
					if(overwriteexisting){
						entry.setValue(value);
					}
					return previousValue;
				}
				entries.add(value);
				return null;
			}
		});
	}
	private <T>T doTask(Object key,Task<T>task){
		int hash = getHash(key);
		return getSegmentForHash(hash).doTask(hash,key,task);
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
			// TODO Auto-generated method stub
			return null;
		}

		private Reference<K,V>[] createReferenceArray(int size){
        	return (Reference<K,V>[]) Array.newInstance(Reference.class, size);
        }
        
        private void setReferences(Reference<K,V>[] references){
        	this.references = references;
        	this.resizeThreshold = (int)(references.length*getLoadFactor());
        }
        
        public <T>doTask(final int hash,final Object key,final Task<T>task){
        	boolean resize = task.hasOption(TaskOption.RESIZE);
        	if(task.hasOption(TaskOption.RESIZE))){
        		restructureIfNecessary(resize);
        	}
        	if(task.hasOption(TaskOption.SKIP_IF_EMPTY)&& this.count == 0){
        		return task.execute(null, null,null);
        	}
        	lock();
        	try{
        		final int index = getIndex(hash,this.references);
        		final Reference<K,V> head = this.references[index];
        		Entry<K,V> reference = findInChain(head,key,hash);
        		Entry<K,V> entry = (reference!= null ?reference.get():null);
        		Entries entries = new Entries(){
        			public void add(V value){
        				Entry<K,V> newEntry = new Entry<K,V>((K)key,value);
        				Reference<K,V> newReference = segment.this.referenceManager.createReference(newEntry,hash,head);
        				Segment.this.references[index] = newReference;
        				Segment.this.count++;
        			}
        		};
        		return task.execute(reference, entry,entries);
        	}
        	finally{
        		unlock();
        		if(task.hasOption(TaskOption.RESTRUCTURE_AFTER)){
        			restructrueIfNecessary(resize);
        		}
        	}
        	
        	
        }
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
