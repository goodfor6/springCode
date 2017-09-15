package org.springframework.core.util.backoff;

/**
 * Created by Administrator on 2017/9/15 0015.
 */
public interface BackOffExecution {

    long STOP = -1;

    long nextBackOff();

}
