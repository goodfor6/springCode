package org.springframework.core.util.backoff;


import org.springframework.util.backoff.BackOffExecution;

/**
 * Created by Administrator on 2017/9/15 0015.
 */
public interface BackOff {

    BackOffExecution start();
}
