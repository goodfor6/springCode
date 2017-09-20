package org.springframework.core.core;

/**
 * Created by Administrator on 2017/9/20 0020.
 */
public interface AttributeAccessor {

    void setAttribute(String name,Object value);

    Object getAttribute(String name);

    Object removeAttribute(String name);

    boolean hasAttribute(String name);

    String[] attributeNames();
}
