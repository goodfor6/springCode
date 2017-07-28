package org.springframework.core.util;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Created by Administrator on 2017/7/27 0027.
 */
public class CompositeIterator<E>implements Iterator<E> {

    private final Set<Iterator<E>> iterators = new LinkedHashSet();

    private boolean inUse = false;

    public CompositeIterator(){

    }

    public void add(Iterator<E> iterator){
        Assert.state(!this.inUse,"you can no longer add iterator to a compposite iterator that's already in use ");
        if(this.iterators.contains(iterator)){
            throw new IllegalArgumentException("You cannot add the same iterator twice");
        }else{
            this.iterators.add(iterator);
        }
    }

    public boolean hasNext(){
        this.inUse = true;
        Iterator var1 = this.iterators.iterator();

        Iterator iterator;
        do{
            if(!var1.hasNext()){
                return false;
            }
            iterator = (Iterator)var1.next();
        }while(!iterator.hasNext());
        return true;
    }

    public E next(){
        this.inUse = true;
        Iterator var1 = this.iterators.iterator();

        Iterator iterator;
        do{
            if(!var1.hasNext()){
                throw new NoSuchElementException("All iterators exhausted");
            }
            iterator = (Iterator)var1.next();
        }while(!iterator.hasNext());

       return  null;
    }

    public void remove(){ throw new UnsupportedOperationException(" CompositeIterator does not support remove() ");}


}
