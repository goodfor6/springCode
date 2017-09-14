package org.springframework.core.util.concurrent;

import org.springframework.util.concurrent.*;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.*;

/**
 * Created by Administrator on 2017/9/14 0014.
 */
public class ListenableFutureTask<T> extends FutureTask<T> implements ListenableFuture<T> {

    private final ListenableFutureCallbackRegistry<T> callbacks = new ListenableFutureCallbackRegistry<T>();

    public ListenableFutureTask(Callable<T> callable){super(callable);}

    public ListenableFutureTask(Runnable runable,T result){super(runable,result);}

    public void addCallback(ListenableFutureCallback<? super T> callback) {
        this.callbacks.addCallback(callback);
    }

    @Override
    public void addCallback(SuccessCallback<? super T> successCallback, FailureCallback failureCallback) {
        this.callbacks.addSuccessCallback(successCallback);
        this.callbacks.addFailureCallback(failureCallback);
    }

    protected final void done(){
        Throwable cause;
        try{
            T result = get();
            this.callbacks.success(result);
            return ;
        }
        catch(InterruptedException ex){
            Thread.currentThread().interrupt();
            return;
        }
        catch(ExecutionException ex){
            Thread.currentThread().interrupt();
            return;
        }
        catch(Throwable ex){
            cause = ex;
        }
        this.callbacks.failure(cause);
    }

}
