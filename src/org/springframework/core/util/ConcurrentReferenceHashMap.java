package org.springframework.core.util;

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
//import org.springframework.util.ConcurrentReferenceHashMap;


/**
 * Created by Administrator on 2017/7/27 0027.
 */
public class ConcurrentReferenceHashMap<K,V>extends AbstractMap<K,V> implements ConcurrencyMap<K,V> {
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_fACTOR = 0.75F;
    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;
    private static final ConcurrentReferenceHashMap.ReferenceType DEFAULT_REFERENCE_TYPE;
    private static final int MAXIMUM_CONCURRENCY_LEVEL = 65535;
    private static final int MAXIMUM_SEGMENT_SIZE = 1073741824;
    private final ConcurrentReferenceHashMap<K,V>.Segment[] segments;
    private final float loadFactory;
    private final int shift;
    private Set<Map<K,V>> entrySet;

    public ConcurrentReferenceHashMap(){this(16,0.75F,16,DEFAULT_REFERENCE_TYPE);}

    public ConcurrentReferenceHashMap(int initialCapacity){this(initialCapacity,0.75F,16,DEFAULT_REFERENCE_TYPE);}

    public ConcurrentReferenceHashMap(int initialCapacity,float loadFactor){
        this(initialCapacity,loadFactor,16,DEFAULT_REFERENCE_TYPE);
    }

    public ConcurrentReferenceHashMap(int initialCapacity,int concurrencyLevel){
        this(initialCapacity,0.75F,concurrencyLevel,DEFAULT_REFERENCE_TYPE);
    }

    public ConcurrentReferenceHashMap(int initialCapacity, org.springframework.util.ConcurrentReferenceHashMap.ReferenceType referenceType) {
        this(initialCapacity, 0.75F, 16, referenceType);
    }

    public ConcurrentReferenceHashMap(int initialCapacity,float loadFactor,int concurrency){
        this(initialCapacity, loadFactor,concurrencyLevel,DEFAULT_REFERENCE_TYPE);
    }

    public ConcurrentReerenceHashMap(int initialCapacity, float loadFactor, int concurrencyLevel, org.springframework.util.ConcurrentReferenceHashMap.ReferenceType referenceType){
        Assert.isTrue(initialCapacity >= 0,"initial capacity must not be negative");
        Assert.isTrue(loadFactor > 0.0F, "Load factor must be positive");
        Assert.isTrue(concurrencyLevel > 0 ,"Concurrency level must be positive");
        Assert.notNull(referenceType,"Reference type must not be null");
        this.loadFactory = loadFactor;
        this.shift =  calculateShift(concurrencyLevel,65535);
        int size =1 << this.shift;
        this.referenceType = referenceType;
        int roundedUpSegmentCapacity = (int) (((long)(initialCapacity + size )-1L)/(long) size);
        this.segments = (ConcurrentReferenceHashMap.Segment[])((ConcurrentReferenceHashMap.Segment[]) Array.newInstance(ConcurrentReferenceHashMap.Segment.class,size));

        for(int i=0;i< this.segments.length;++i){
            this.segments[i] = new ConcurrentReferenceHashMap.Segment(roundedUpSegmetnCapacity);
        }

    }

    protected final class Segment extends ReentrantLock {
        private final ConcurrentReferenceHashMap<K,V>.ReferenceManager referenceManager =ConcurrentReferenceHashMap.this.createReferenceManager();
        private final int initialSize;
        private volatile ConcurrentReferenceHashMap.Reference<K,V>[] references;
        private volatile int count = 0;
        private int resizeThreshold;

        public Segment(int initialCapacity) {
            this.initialSize = 1 << ConcurrentReferenceHashMap.calculateShift(initialCapacity,1073741824);
            this.setReferences(this.createReferenceArray(this.initialSize));
        }


    }
}
