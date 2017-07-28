package org.springframework.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import static javafx.scene.input.KeyCode.O;

/**
 * Created by Administrator on 2017/7/27 0027.
 */
public class ConcurrencyThrottleSupport implements Serializable {
    public static final int UNBOUNDER_CONCURRENCY = -1;
    public static final int NO_CONCURRENCY = 0;
    protected transient Log logger = LogFactory.getLog(this.getClass());
    private transient Object monitor = new Object();
    private int concurrencyLimit = -1;
    private int concurrencyCount = 0;
    public ConcurrencyThrottleSupport(){

    }

    public void setConcurrencyLimit(int concurrencyLimit){ this.concurrencyLimit = concurrencyLimit;}

    public int getConcurrencyLimit(){return this.concurrencyLimit;}

    public boolean isThrottleActive(){return this.concurrencyLimit > 0;}

    protected void beforeAccess(){
        if(this.concurrencyLimit == 0){
            throw new IllegalStateException("Currently no invocations allowed - concurrency limit set to NO_CONCURRENCY");
        }else{
            if(this.concurrencyLimit > 0){
               boolean debug = this.logger.isDebugEnabled();
               Object var2 = this.monitor;
               synchronized(this.monitor){
                   boolean interrupted = false;
                   while (this.concurrencyCount >= this.concurrencyLimit){
                       if(interrupted){
                           throw new IllegalStateException(" Thread was interrupted while wating for invocation access, but concurrency limit still does not allow for entering");
                       }
                       if(debug){
                           this.logger.debug("Concurrency count "+ this.concurrencyCount +" has reached limit "+ this.concurrencyLimit+" - blocking");
                       }
                       try {
                           this.monitor.wait();
                       }catch(InterruptedException var6){
                           Thread.currentThread().interrupt();
                           interrupted = true;
                       }
                   }

                   if(debug){
                       this.logger.debug("Entering throttle at concurrency count"+ this.concurrencyCount);
                   }
                   ++this.concurrencyCount;
               }
            }
        }
    }

    protected void afterAccess(){
        if(this.concurrencyLimit >= 0){
            Object var1 = this.monitor;
            synchronized(this.monitor){
                --this.concurrencyCount;
                if(this.logger.isDebugEnabled()){
                    this.logger.debug(" Returning from throttle at concurrency count "+ this.concurrencyCount);
                }
                this.monitor.notify();
            }
        }
    }

    private void readObject(ObjectInputStream ois)throws IOException,ClassNotFoundException{
        ois.defaultReadObject();
        this.logger = LogFactory.getLog(this.getClass());
        this.monitor = new Object();
    }
}
