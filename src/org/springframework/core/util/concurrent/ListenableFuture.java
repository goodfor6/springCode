package org.springframework.core.util.concurrent;

import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.SuccessCallback;

import java.util.concurrent.Future;

/**
 * Created by Administrator on 2017/9/14 0014.
 */
public interface ListenableFuture<T> extends Future<T> {

    void addCallback(ListenableFutureCallback<? super T> callback);
    void addCallback(SuccessCallback<? super T> successCallback, FailureCallback failureCallback);
}
