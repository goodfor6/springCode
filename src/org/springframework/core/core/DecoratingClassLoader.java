package org.springframework.core.core;

import org.springframework.core.util.Assert;
import org.springframework.core.util.ClassUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017/9/26 0026.
 */
public abstract class DecoratingClassLoader extends ClassLoader{

    protected static final boolean parallelCapableClassLoaderAvailable = ClassUtils.hasMethod(ClassLoader.class,"registerAsParallelCapable");

    static{
        if(parallelCapableClassLoaderAvailable){
            ClassLoader.registerAsParallelCapable();
        }
    }

    private final Set<String> excludePackages = new HashSet<String>();

    private final Set<String> excludedClasses = new HashSet<String>();

    private final Object exclusionMonitor =new Object();

    public DecoratingClassLoader(){}

    public DecoratingClassLoader(ClassLoader parent){super(parent);}

    public void excludePackages(String packageName){
        Assert.notNull(packageName,"Package name must not be null");
        synchronized(this.exclusionMonitor){
            this.excludedClasses.add(packageName);
        }
    }

    public void excludeClass(String className){
        Assert.notNull(className,"Class name must not be null");
        synchronized (this.exclusionMonitor){
            this.excludedClasses.add(className);
        }
    }

    protected boolean isExcluded(String className){
        synchronized (this.exclusionMonitor){
            if(this.excludedClasses.contains(className)){
                return true;
            }
            for(String packageName : this.excludePackages){
                if(className.startsWith(packageName)){
                    return true;
                }
            }
        }
        return false;
    }

}
