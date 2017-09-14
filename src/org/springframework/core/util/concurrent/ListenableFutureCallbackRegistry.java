package org.springframework.core.util.concurrent;

import org.springframework.core.util.Assert;
import org.springframework.util.concurrent.SuccessCallback;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Administrator on 2017/9/14 0014.
 */
public class ListenableFutureCallbackRegistry<T> {

    private final Queue<SuccessCallback<? super T>> successCallbacks = new LinkedList<>();

    private final Queue<FailureCallback>failureCallbacks = new LinkedList<>();

    private State state = State.NEW;

    private Object result = null;

    private final Object mutex = new Object();

    public void addCallback(ListenableFutureCallback<? super T>callback){
        Assert.notNull(callback,"'callback' must not be null");
        synchronized(this.mutex){
            switch(this.state){
                case NEW:
                    this.successCallbacks.add(callback);
                    this.failureCallbacks.add(callback);
                    break;
                case SUCCESS:
                    callback.onSuccess((T)this.result);
                    break;
                case FAILURE:
                    callback.onFailure((Throwable)this.result);
                    break;
            }
        }
    }

    public void addSuccessCallback(SuccessCallback<? super T>callback){
        Assert.notNull(callback,"'callback' must not be null");
        synchronized(this.mutex){
            switch(this.state){
                case NEW:
                    this.successCallbacks.add(callback);
                    break;
                case SUCCESS:
                    callback.onSuccess((T)this.result);
                    break;
            }
        }
    }

    public void addFailureCallback(FailureCallback callback){
        Assert.notNull(callback,"'callback' must not be null");
        synchronized(this.mutex){
            switch(this.state){
                case NEW :
                    this.failureCallbacks.add(callback);
                    break;
                case FAILURE:
                    callback.onFailure((Throwable) this.result);
                    break;
            }
        }
    }

    public void success(T result){
        synchronized (this.mutex){
            this.state = State.SUCCESS;
            this.result = result;
            while(!this.successCallbacks.isEmpty()){
                this.successCallbacks.poll().onSuccess(result);
            }
        }
    }

    public void failure(Throwable ex){
        synchronized(this.mutex){
            this.state = State.FAILURE;
            this.result = ex;
            while(!this.failureCallbacks.isEmpty()){
                this.failureCallbacks.poll().onFailure(ex);
            }
        }
    }

    private enum State{NEW,SUCCESS,FAILURE}
}
