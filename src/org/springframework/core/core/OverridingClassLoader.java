package org.springframework.core.core;

import org.springframework.core.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2017/10/10 0010.
 */
public class OverridingClassLoader extends  DecoratingClassLoader {

    public static final String[] DEFAULT_EXCLUDED_PACKAGES = new String[]{"java.","javax.","sun.","oracle."};

    private static final String CLASS_FILE_SUFFIX = ".class";

    static{
        if(parallelCapableClassLoaderAvailable){
            ClassLoader.registerAsParallelCapable();
        }
    }

    public OverridingClassLoader(ClassLoader parent){
        super(parent);
        for(String packageName: DEFAULT_EXCLUDED_PACKAGES){
            excludePackages(packageName);
        }
    }

    protected Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException{
        Class<?> result = null;
        if(isEligibleForOverriding(name)){
            result = loadClassForOverriding(name);
        }
        if(result != null){
            if(resolve){
                resolveClass(result);
            }
            return result;
        }
        else {
            return super.loadClass(name,resolve);
        }
    }

    protected boolean isEligibleForOverriding(String className){return !isExcluded(className);}

    protected Class<?> loadClassForOverriding(String name) throws ClassNotFoundException{
        Class<?> result = findLoadedClass(name);
        if(result == null){
            byte[] bytes = loadBytesForClass(name);
            if(bytes != null){
                result = defineClass(name,bytes,0,bytes.length);
            }
        }
        return result;
    }

    protected byte[] loadBytesForClass(String name)throws ClassNotFoundException{
        InputStream is = openStreamForClass(name);
        if(is == null){
            return null;
        }
        try{
            byte[] bytes = FileCopyUtils.copyToByteArray(is);
            return transformIfNecessary(name,bytes);
        }catch(IOException ex){
            throw new ClassNotFoundException("Cannot load resource for class{"+name+"]",ex);
        }
    }


    protected InputStream openStreamForClass(String name){
        String internalName = name.replace('.','/')+CLASS_FILE_SUFFIX;
        return getParent().getResourceAsStream(internalName);
    }

    protected byte[] transformIfNecessary(String name,byte[] bytes){return bytes;}



}
