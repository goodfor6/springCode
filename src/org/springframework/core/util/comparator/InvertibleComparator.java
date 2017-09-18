package org.springframework.core.util.comparator;

import org.springframework.core.util.Assert;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Administrator on 2017/9/18 0018.
 */
public class InvertibleComparator<T> implements Comparator<T>,Serializable {

    private final Comparator<T> comparator;

    private boolean ascending = true;

    public InvertibleComparator(Comparator<T> comparator){
        Assert.notNull(comparator,"Comparator must not be null");
        this.comparator = comparator;
    }

    public InvertibleComparator(Comparator<T> comparator,boolean ascending){
        Assert.notNull(comparator,"Comparator must not be null");
        this.comparator = comparator;
        setAscending(ascending);
    }

    public void setAscending(boolean ascending){this.ascending = ascending;}

    public boolean isAscending(){return this.ascending;}

    public void invertOrder(){this.ascending = !this.ascending;}

    public int compare(T o1,T o2){
        int result = this.comparator.compare(o1,o2);
        if(result != 0){
            if(!this.ascending){
                if(Integer.MIN_VALUE == result){
                    result = Integer.MAX_VALUE;
                }
                else {
                    result *=-1;
                }
            }
            return result;
        }
        return 0;
    }

    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(!(obj instanceof InvertibleComparator)){
            return false;
        }
        InvertibleComparator<T> other = (InvertibleComparator<T>)obj;
        return (this.comparator.equals(other.comparator)&& this.ascending == other.ascending);
    }

    public int hasCode(){return this.comparator.hashCode();}

    public String toString(){return "InvertibleComparator:["+this.comparator+"];ascending ="+this.ascending;}

}
