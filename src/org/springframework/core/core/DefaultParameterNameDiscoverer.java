package org.springframework.core.core;

import org.springframework.core.JdkVersion;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.PrioritizedParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;

/**
 * Created by Administrator on 2017/9/28 0028.
 */
public class DefaultParameterNameDiscoverer extends PrioritizedParameterNameDiscoverer {

    private static final boolean standardRefelectionAvailable =
            (JdkVersion.getMajorJavaVersion() >= JdkVersion.JAVA_18 );

    public DefaultParameterNameDiscoverer(){
        if(standardRefelectionAvailable){
            addDiscoverer(new StandardReflectionParameterNameDiscoverer());
        }
        addDiscoverer(new LocalVariableTableParameterNameDiscoverer());
    }
}
