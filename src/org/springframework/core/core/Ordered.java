package org.springframework.core.core;

/**
 * Created by Administrator on 2017/10/10 0010.
 */
public interface Ordered {

    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    int getOrder();
}
