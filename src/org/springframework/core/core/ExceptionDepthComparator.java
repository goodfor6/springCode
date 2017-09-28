package org.springframework.core.core;

import org.springframework.core.util.Assert;

import java.util.*;

/**
 * Created by Administrator on 2017/9/28 0028.
 */
public class ExceptionDepthComparator implements Comparator<Class<? extends Throwable>> {

    private final Class<? extends Throwable> targetException;

    public ExceptionDepthComparator(Throwable exception){
        Assert.notNull(exception,"Target exception must not be null");
        this.targetException = exception.getClass();
    }

    public ExceptionDepthComparator(Class<? extends Throwable> exceptionType){
        Assert.notNull(exceptionType,"Target exception type must not be null");
        this.targetException = exceptionType;
    }

    public int compare(Class<? extends Throwable>o1,Class<? extends Throwable>o2){
        int depth1 = getDepth(o1,this.targetException,0);
        int depth2 = getDepth(o2,this.targetException,0);
        return (depth1 - depth2);
    }

    private int getDepth(Class<?>declaredException,Class<?> exceptionToMatch,int depth){
        if(declaredException.equals(exceptionToMatch)){
            return depth;
        }
        if(Throwable.class.equals(exceptionToMatch)){
            return Integer.MAX_VALUE;
        }
        return getDepth(declaredException,exceptionToMatch.getSuperclass(),depth +1);
    }

    public static Class<? extends Throwable> findClosestMatch(
            Collection<Class<? extends Throwable>> exceptionTypes,Throwable targetException
    ){
        Assert.notEmpty(exceptionTypes,"Exception types must not be empty");
        if(exceptionTypes.size() == 1){
            return exceptionTypes.iterator().next();
        }
        List<Class<? extends Throwable>> handledExceptions =
                new ArrayList<>(exceptionTypes);
        Collections.sort(handledExceptions,new ExceptionDepthComparator(targetException));
        return handledExceptions.get(0);
    }
}
