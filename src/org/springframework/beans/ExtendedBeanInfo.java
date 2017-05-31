package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExtendedBeanInfo  implements BeanInfo{
	
	private static Log logger=LogFactory.getLog(ExtendedBeanInfo.class);
	
	private final BeanInfo delegate;
	
	private final Set<PropertyDescriptor> propertyDescriptors=new TreeSet<PropertyDescriptor>(new PropertyDescriptorComparator());

	public ExtendedBeanInfo(BeanInfo delegate)throws IntrospectionException{
		this.delegate=delegate;
		for(PropertyDescriptor pd:delegate.getPropertyDescriptors()){
			try{
				this.propertyDescriptors.add(pd instanceof IndexedPropertyDescriptor?
						new SimpleIndexedPropertyDescriptor((IndexedPropertyDescriptor)pd):
						new SimplePropertyDescriptor(pd));
			}
		}
	}
	
	static class SimpleIndexedPropertyDescriptor extends IndexedPropertyDescriptor{
		private Method readMethod;
		
		private Method writeMethod;
		
		private Class<?>propertyType;
		
		private Method indexedReadMethod;
		
		private Method indexedWriteMethod;
		
		private Class<?>indexedPropertyType;
		
		private Class<?>propertyEditorClass;
		
		public SimpleIndexedPropertyDescriptor(IndexedPropertyDescriptor original)throws IntrospectionException{
			this(original.getName(),original.getReadMethod(),original.getWriteMethod(),
					original.getIndexedReadMethod(),original.getIndexedWriteMethod());
			PropertyDescriptorUtils.copyNonMethodProperties(original, this);
		}
		
		
	}
	static class PropertyDescritorComparator implements Comparator<PropertyDescriptor>{
	@Override
	public int compare(PropertyDescriptor desc1,PropertyDescriptor desc2){
		String left=desc1.getName();
		String right=desc2.getName();
		for(int i=0;i<left.length();i++){
			if(right.length()==i){
				return 1;
			}
			int result=left.getBytes()[i]-right.getBytes()[i];
			if(result!=0){
				return result;
			}
		}
		return left.length()-right.length();
	}
		
  }
}
