package org.springframework.core.util.concurrent;

/**
 * Created by Administrator on 2017/9/13 0013.
 */
public interface  FailureCallback {

    void onFailure(Throwable ex);

}
