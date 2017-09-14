package org.springframework.core.util.concurrent;

import org.springframework.core.util.Assert;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Administrator on 2017/9/13 0013.
 */
public abstract class FutureAdapter <T,S> implements Future<T> {

    private final Future<S> adaptee;

    private Object result = null;

    private State state = State.NEW;

    private final Object mutex = new Object();

    protected FutureAdapter(Future<S> adaptee){
            Assert.notNull(adaptee,"'delegate' must not be null");
            this.adaptee = adaptee;
        }

    protected Future<S>getAdaptee(){return adaptee;}

    public boolean cancle(boolean mayInterruptIfRunning){return adaptee.cancel(mayInterruptIfRunning);}

    public boolean isCancelled(){return adaptee.isCancelled();}

    public boolean idDone(){return adaptee.isDone();}

    public T get() throws InterruptedException,ExecutionException{return adaptInternal(adaptee.get());}

    public T get(long timeout,TimeUnit unit)throws InterruptedException,ExecutionException,TimeoutException {
            return adaptInternal(this.adaptee.get(timeout,unit));
        }

    final T adaptInternal(S adapteeResult)throws ExecutionException{
        synchronized(this.mutex){
            switch(this.state){
                case SUCCESS:
                    return (T) this.result;
                case FAILURE:
                    throw (ExecutionException)this.result;

                case NEW:
                    try{
                        T adapted  = adapt(adapteeResult);
                        this.result = adapted;
                        this.state = State.SUCCESS;
                        return adapted;
        }catch(ExecutionException ex){
                        this.result = ex;
                        this.state = State.FAILURE;
                        throw ex;
        }
        default : throw new IllegalStateException();
        }
        }
    }

    protected abstract T adapt(S adapteeResult) throws ExecutionException;

    private enum State {NEW,SUCCESS,FAILURE}
}
