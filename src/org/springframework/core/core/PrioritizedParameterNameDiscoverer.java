package org.springframework.core.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/11 0011.
 */
public class PrioritizedParameterNameDiscoverer implements ParameterNameDiscoverer {

    private final List<ParameterNameDiscoverer> parameterNameDiscoverers= new LinkedList<ParameterNameDiscoverer>();

    public void addDiscoverer(ParameterNameDiscoverer pnd){this.parameterNameDiscoverers.add(pnd);}

    public String[] getParameterNames(Method method){
        for(ParameterNameDiscoverer pnd : this.parameterNameDiscoverers){
            String[] result = pnd.getParameterNames(method);
            if(result != null){
                return result;
            }
        }
        return null;
    }


    public String[] getParameterNames(Constructor<?> ctor){
        for(ParameterNameDiscoverer pnd : this.parameterNameDiscoverers){
            String [] result = pnd.getParameterNames(ctor);
            if(result != null){
                return result;
            }
        }
        return null;
    }
}
