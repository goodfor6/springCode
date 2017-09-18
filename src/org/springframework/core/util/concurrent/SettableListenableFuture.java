package org.springframework.core.util.concurrent;

import org.springframework.core.util.Assert;
import org.springframework.util.concurrent.SuccessCallback;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Administrator on 2017/9/18 0018.
 */
public class SettableListenableFuture<T>  implements ListenableFuture<T>{

    private final SettableTask<T> settableTask;
    private final ListenableFutureTask<T> listenableFuture;

    public SettableListenableFuture(){
        this.settableTask = new SettableTask<T>();
        this.listenableFuture = new ListenableFutureTask<T>(this.settableTask);
    }

    public boolean set(T value){
        boolean success = this.settableTask.setValue(value);
        if(success){
            this.listenableFuture.run();
        }
        return success;
    }

    public boolean setException(Throwable exception){
        Assert.notNull(exception,"exception' must not be null");
        boolean success = this.settableTask.setException(exception);
        if(success){
            this.listenableFuture.run();
        }
        return success;
    }

    public void  addCallback(ListenableFutureCallback<? super T> callback){this.listenableFuture.addCallback(callback);}

    public void addCallback(SuccessCallback<? super T>successCallback,FailureCallback failureCallback){
        this.listenableFuture.addCallback(successCallback,failureCallback);
    }

    public boolean cancel (boolean mayInterruptIfRunning){
        this.settableTask.setCancelled();
        boolean cancelled = this.listenableFuture.cancel(mayInterruptIfRunning);
        if(cancelled && mayInterruptIfRunning){
            interruptTask();
        }
        return cancelled;
    }

    public boolean isCancelled(){return this.listenableFuture.isCancelled();}

    public boolean isDone(){return this.listenableFuture.isDone();}

    public T get() throws InterruptedException,ExecutionException {
        return this.listenableFuture.get();
    }

    public T get(long timeout,TimeUnit unit)throws InterruptedException, ExecutionException, TimeoutException {
        return this.listenableFuture.get(timeout,unit);
    }

    protected void interruptTask(){

    }

    @Override
    public void addCallback(org.springframework.util.concurrent.ListenableFutureCallback<? super T> callback) {

    }


    public static class SettableTask<T>implements Callable<T> {
        private static final String NO_VALUE = SettableListenableFuture.class.getName() + ".NO_VALUE";
        private final AtomicReference<Object> value = new AtomicReference<>(NO_VALUE);
        private volatile boolean cancelled = false;

        public boolean setValue(T value) {
            if (this.cancelled){
                return false;
            }
            return this.value.compareAndSet(NO_VALUE, value);
        }

        public boolean setException(Throwable exception){
            if(this.cancelled){
                return false;
            }
            return this.value.compareAndSet(NO_VALUE,exception);
        }

        public void setCancelled(){this.cancelled = true;}

        public T call() throws Exception{
            if(value.get() instanceof Exception){
                throw (Exception)value.get();
            }
            return (T) value.get();
        }

    }

}
