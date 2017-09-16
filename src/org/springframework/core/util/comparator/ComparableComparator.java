package org.springframework.core.util.comparator;

import java.util.Comparator;

/**
 * Created by Administrator on 2017/9/16 0016.
 */
public class ComparableComparator<T extends Comparable<T>>implements Comparator<T> {

    public static final ComparableComparator INSTANCE = new ComparableComparator();

    public int compare(T o1,T o2){return o1.compareTo(o2);}

}
