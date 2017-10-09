package org.springframework.core.core;

import org.springframework.core.NestedExceptionUtils;

/**
 * Created by Administrator on 2017/10/9 0009.
 */
public abstract class NestedCheckedException extends Exception  {

    private static final long serialVersionUID = 7100714597678207546L;

    static {
        NestedExceptionUtils.class.getName();
    }

    public NestedCheckedException(String msg){super(msg);}

    public NestedCheckedException(String msg,Throwable cause){super(msg,cause);}

    public String getMessage(){return NestedExceptionUtils.buildMessage(super.getMessage(),getCause());}

    public Throwable getRootCause(){
        Throwable rootCause = null;
        Throwable cause = getCause();
        while(cause != null && cause != rootCause){
            rootCause = cause;
            cause = cause.getCause();
        }
        return rootCause;
    }

    public Throwable getMostSpecificCause(){
        Throwable rootCause = getRootCause();
        return (rootCause != null ? rootCause : this);
    }

    public boolean contains (Class<?>exType){
        if(exType == null){
            return false;
        }
        if(exType.isInstance(this)){
            return true;
        }
        Throwable cause = getCause();
        if(cause == this){
            return false;
        }
        if(cause instanceof  NestedCheckedException){
            return ((NestedCheckedException)cause).contains(exType);
        }
        else{
            while(cause != null){
                if(exType.isInstance(cause)){
                    return true;
                }
                if(cause.getCause() == cause){
                    break;
                }
                cause = cause.getCause();
            }
            return false;
        }
    }
}
