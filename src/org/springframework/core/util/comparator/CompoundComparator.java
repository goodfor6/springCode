package org.springframework.core.util.comparator;

import org.springframework.core.util.Assert;
import org.springframework.util.comparator.InvertibleComparator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2017/9/16 0016.
 */
public class CompoundComparator<T> implements Comparator<T>,Serializable {

    private final List<InvertibleComparator> comparators;

    public CompoundComparator() {
        this.comparators = new ArrayList<InvertibleComparator>();
    }

    public CompoundComparator(Comparator... comparators) {
        Assert.notNull(comparators, "Comparators must not be null");
        this.comparators = new ArrayList<InvertibleComparator>(comparators.length);
        for (Comparator comparator : comparators) {
            this.addComparator(comparator);
        }
    }

    public void addComparator(Comparator<? extends T> comparator) {
        if (comparator instanceof InvertibleComparator) {
            this.comparators.add((InvertibleComparator) comparator);
        } else {
            this.comparators.add(new InvertibleComparator(comparator));
        }
    }

    public void addComparator(Comparator<? extends T> comparator, boolean ascending) {
        this.comparators.add(new InvertibleComparator(comparator, ascending));
    }

    public void setComparator(int index, Comparator<? extends T> comparator) {
        if (comparator instanceof InvertibleComparator) {
            this.comparators.set(index, (InvertibleComparator) comparator);
        } else {
            this.comparators.set(index, new InvertibleComparator(comparator));
        }
    }

    public void setComparators(int index, Comparator<T> comparator, boolean ascending) {
        this.comparators.set(index, new InvertibleComparator<T>(comparator, ascending));
    }

    public void invertOrder() {
        for (InvertibleComparator comparator : this.comparators) {
            comparator.invertOrder();
        }
    }

    public void invertOrder(int index) {
        this.comparators.get(index).invertOrder();
    }

    public void setAsendingOrder(int index) {
        this.comparators.get(index).setAscending(true);
    }

    public void setDescendingOrder(int index) {
        this.comparators.get(index).setAscending(false);
    }

    public int getComparatorCount() {
        return this.comparators.size();
    }

    public int compare(T o1, T o2) {
        Assert.state(this.comparators.size() > 0, "No sort definition s havae been added to this CompoundComparator to compare");
        for (InvertibleComparator comparator : this.comparators) {
            int result = comparator.compare(o1, o2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(!(obj instanceof CompoundComparator)){
            return false;
        }
        CompoundComparator<T> other = (CompoundComparator<T>)obj;
        return this.comparators.equals(other.comparators);
    }

    public int hashCode(){return this.comparators.hashCode();}

    public String toString(){return "CompoundComparator:"+this.comparators;}


}
