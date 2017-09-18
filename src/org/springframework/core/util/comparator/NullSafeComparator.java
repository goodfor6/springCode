package org.springframework.core.util.comparator;

import org.springframework.core.util.Assert;

import java.util.Comparator;

/**
 * Created by Administrator on 2017/9/18 0018.
 */
public class NullSafeComparator <T> implements Comparator<T> {

    public static final NullSafeComparator NULLS_LOW = new NullSafeComparator<Object>(true);

    public static final NullSafeComparator NULLS_HITH = new NullSafeComparator<Object>(false);

    private final Comparator<T> nonNullComparator;

    private final boolean nullsLow;

    private NullSafeComparator(boolean nullsLow){
        this.nonNullComparator = new ComparableComparator();
        this.nullsLow = nullsLow;
    }

    public NullSafeComparator(Comparator<T> comparator, boolean nullsLow){
        Assert.notNull(comparator,"The non-null comparator is required");
        this.nonNullComparator = comparator;
        this.nullsLow = nullsLow;
    }

    public int compare(T o1,T o2){
        if(o1 == o2){
            return 0;
        }
        if(o1 == null){
            return (this.nullsLow ? -1 :1);
        }
        if(o2 == null){
            return (this.nullsLow ? 1 : -1);
        }
        return this.nonNullComparator.compare(o1,o2);

    }


}
