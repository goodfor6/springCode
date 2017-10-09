package org.springframework.core.core;

import java.io.IOException;

/**
 * Created by Administrator on 2017/10/9 0009.
 */
public   class NestedIOException  extends IOException {
     static {
         NestedExceptionUtils.class.getName();

     }

     public NestedIOException(String msg){super(msg);}

     public NestedIOException(String msg,Throwable cause){super(msg,cause);}

     public String getMessage(){return NestedExceptionUtils.buildMessage(super.getMessage(),getCause());}


}
