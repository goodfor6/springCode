package org.springframework.core.util.concurrent;

import org.springframework.util.concurrent.SuccessCallback;

/**
 * Created by Administrator on 2017/9/14 0014.
 */
public interface ListenableFutureCallback<T> extends SuccessCallback<T>,FailureCallback{
}
