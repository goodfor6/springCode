package org.springframework.core.util.comparator;

import org.springframework.core.util.Assert;

import java.util.Comparator;

/**
 * Created by Administrator on 2017/9/16 0016.
 */
public class InstanceComparator<T> implements Comparator<T> {

    private final Class<?>[]instanceOrder;

    public InstanceComparator(Class<?>... instanceOrder){
        Assert.notNull(instanceOrder,"instanceOrder'must not be null");
        this.instanceOrder = instanceOrder;
    }

    public int compare(T o1,T o2){
        int i1 = getOrder(o1);
        int i2 = getOrder(o2);
        return (i1 < i2? -1:(i1 == i2 ? 0 :1));
    }

    private int getOrder(T object){
        if(object != null){
            for(int i = 0; i < this.instanceOrder.length; i++){
                if(this.instanceOrder[i].isInstance(object)){
                    return i;
                }
            }
        }
        return this.instanceOrder.length;
    }
}
