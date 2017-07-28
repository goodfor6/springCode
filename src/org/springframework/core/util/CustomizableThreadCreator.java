package org.springframework.core.util;


import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2017/7/27.
 */
@SuppressWarnings("serial")
public class CustomizableThreadCreator implements Serializable {
    private String threadNamePrefix;
    private int threadPriority = 5;
    private boolean daemon = false;
    private ThreadGroup threadGroup;
    private final AtomicInteger threadCount = new AtomicInteger(0);

    public CustomizableThreadCreator(){ this.threadNamePrefix = this.getDefaultThreadNamePrefix();}

    public CustomizableThreadCreator(String threadNamePrefix){
        this.threadNamePrefix = threadNamePrefix != null?threadNamePrefix: this.getDefaultThreadNamePrefix();
    }

    public void setThreadNamePrefix(String threadNamePrefix){
        this.threadNamePrefix = threadNamePrefix != null? threadNamePrefix:this.getDefaultThreadNamePrefix();
    }

    public String getThreadNamePrefix(){return this.threadNamePrefix;}

    public void setThreadPriority(int threadPriority){ this.threadPriority = threadPriority;}

    public int getThreadPriority(){return this.threadPriority;}

    public void setDaemon(boolean daemon){this.daemon = daemon;}

    public  boolean isDaemon(){return this.daemon;}

    public void setThreadGroupName(String name){this.threadGroup = new ThreadGroup(name);}

    public void setThreadGroup(ThreadGroup threadGroup){
        this.threadGroup = threadGroup;
    }

    public ThreadGroup getThreadGroup(){return this.threadGroup;}

	public Thread createThread(Runnable runnable) {
		Thread thread = new Thread(getThreadGroup(), runnable, nextThreadName());
		thread.setPriority(getThreadPriority());
		thread.setDaemon(isDaemon());
		return thread;
	}

     protected String nextThreadName(){return getThreadNamePrefix()+this.threadCount.incrementAndGet();}

     protected String getDefaultThreadNamePrefix(){ return ClassUtils.getShortName(getClass())+"-";}


}
