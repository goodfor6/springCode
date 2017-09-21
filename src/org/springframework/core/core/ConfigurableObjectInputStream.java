package org.springframework.core.core;

import java.io.ObjectStreamClass;
import org.springframework.core.util.ClassUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.lang.reflect.Proxy;

/**
 * Created by Administrator on 2017/9/21 0021.
 */
public class ConfigurableObjectInputStream extends ObjectInputStream {

    private final ClassLoader classLoader;

    private final boolean acceptProxyClasses;

    public ConfigurableObjectInputStream(InputStream in, ClassLoader classLoader)throws IOException {
        this(in,classLoader,true);
    }

    public ConfigurableObjectInputStream(InputStream in,ClassLoader classLoader, boolean acceptProxyClasses)throws IOException{
        super(in);
        this.classLoader = classLoader;
        this.acceptProxyClasses = acceptProxyClasses;
    }

    protected Class<?>resolverClass(ObjectStreamClass classDesc)throws IOException ,ClassNotFoundException{
        try{
            if(this.classLoader != null){
                return ClassUtils.forName(classDesc.getName(),this.classLoader);
            }
            else{
                return super.resolveClass(classDesc);
            }
        }
        catch (ClassNotFoundException ex){
            return resolveFallbackIfPossible(classDesc.getName(),ex);
        }
    }

    protected Class<?> resolveProxyClass( String[] interfaces)throws IOException,ClassNotFoundException{
        if(!this.acceptProxyClasses){
            throw new NotSerializableException("Not allowed to accept serialized proxy classes");
        }
        if(this.classLoader != null){
            Class<?>[] resolvedInterfaces = new Class<?>[interfaces.length];
            for(int i= 0; i < interfaces.length ; i++){
                try{
                    resolvedInterfaces[i] = ClassUtils.forName(interfaces[i],this.classLoader);
                }
                catch (ClassNotFoundException ex){
                    resolvedInterfaces[i] = resolveFallbackIfPossible(interfaces[i],ex);
                }
            }
            try{
                return Proxy.getProxyClass(this.classLoader,resolvedInterfaces);
            }
            catch (IllegalArgumentException ex){
                throw new ClassNotFoundException(null,ex);
            }
        }
        else{
            try{
                return super.resolveProxyClass(interfaces);
            }
            catch (ClassNotFoundException ex){
                Class<?>[] resolvedInterfaces = new Class<?>[interfaces.length];
                for(int i=0;i < interfaces.length; i++){
                    resolvedInterfaces[i] = resolveFallbackIfPossible(interfaces[i],ex);
                }
                return Proxy.getProxyClass(getFallbackClassLoader(),resolvedInterfaces);
            }
        }
    }

    protected Class<?> resolveFallbackIfPossible(String className,ClassNotFoundException ex)throws IOException,ClassNotFoundException{
        throw ex;
    }

    protected ClassLoader getFallbackClassLoader() throws IOException{ return null;}

}
