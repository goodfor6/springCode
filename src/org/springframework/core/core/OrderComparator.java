package org.springframework.core.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

/**
 * Created by Administrator on 2017/10/10 0010.
 */
public class OrderComparator implements Comparator<Object> {

    public static final OrderComparator INSTANCE = new OrderComparator();

    public Comparator<Object> withSourceProvider(final org.springframework.core.OrderComparator.OrderSourceProvider sourceProvider){
        return (o1,o2)-> {return doCompare(o1,o2,sourceProvider);};
    }

    public int compare(Object o1,Object o2){return doCompare(o1,o2,null);}

    private int doCompare(Object o1, Object o2, org.springframework.core.OrderComparator.OrderSourceProvider sourceProvider){
        boolean p1 = (o1 instanceof PriorityOrdered);
        boolean p2 = (o2 instanceof  PriorityOrdered);
        if(p1 && !p2){return -1;}
        else if(p2 && !p1){return 1 ;}
        int i1 = getOrder(o1,sourceProvider);
        int i2 = getOrder(o2,sourceProvider);
        return (i1 < i2) ? -1 : (i1 > i2)? 1:0;
    }

    private int getOrder(Object obj, org.springframework.core.OrderComparator.OrderSourceProvider sourceProvider){
        Integer order = null;
        if(sourceProvider != null){
            order = findOrder(sourceProvider.getOrderSource(obj));
        }
        return (order != null ? order:getOrder(obj));
    }

    protected int getOrder(Object obj){
        Integer order = findOrder(obj);
        return (order != null ? order : Ordered.LOWEST_PRECEDENCE);
    }

    protected Integer findOrder(Object obj){return (obj instanceof  Ordered ? ((Ordered) obj).getOrder() : null);}

    public Integer getPriority(Object obj){return null;}

    public static void sort(List<?> list){
        if(list.size() > 1){
            Collections.sort(list,INSTANCE);
        }
    }

    public static void sort(Object [] array){
        if(array.length > 1){
            Arrays.sort(array,INSTANCE);
        }
    }

    public static void sortIfNecessary(Object value){
        if(value instanceof  Object[]){
            sort((Object[]) value);
        }
        else if(value instanceof List){
            sort((List<?>)value);
        }
    }

    public static interface OrderSourceProvider{
        Object getOrderSource(Object obj);
    }


}
