package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.Method;

import org.springframework.core.Ordered;

public class ExtendedBeanInfoFactory implements BeanInfoFactory,Ordered {

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
		return (supports(beanClass)?new ExtendedBeanInfo(Introspector.getBeanInfo(beanClass)):null);
	}
	private boolean supports(Class<?>beanClass){
		for(Method method:beanClass.getMethods()){
			if(ExtendedBeanInfo.isCandidatewriteMethod(method)){
				return true;
			}
		}
		return false;
	}

}
