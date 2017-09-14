package org.springframework.core.util.concurrent;


import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.SuccessCallback;

import java.util.concurrent.ExecutionException;

/**
 * Created by Administrator on 2017/9/14 0014.
 */
public abstract class ListenableFutureAdapter<T,S>extends FutureAdapter<T,S> implements ListenableFuture<T>{

    protected ListenableFutureAdapter(ListenableFuture<S>adaptee){super(adaptee);}

    public void addCallback(final ListenableFutureCallback<? super T> callback){addCallback(callback,callback);}

    public void addCallback(final SuccessCallback<? super T> successCallback, final FailureCallback failureCallback){
        ListenableFuture<S> listenableAdaptee = (ListenableFuture<S>)getAdaptee();
        listenableAdaptee.addCallback(new ListenableFutureCallback<S>(){

            @Override
            public void onSuccess(S result) {
                try{
                    successCallback.onSuccess(adaptInternal(result));
                }
                catch(ExecutionException ex){
                    Throwable cause = ex.getCause();
                    onFailure(cause != null ? cause :ex);
                }
                catch(Throwable ex){
                    onFailure(ex);
                }
            }

            @Override
            public void onFailure(Throwable ex) {
                failureCallback.onFailure(ex);
            }
        });
    }
}
