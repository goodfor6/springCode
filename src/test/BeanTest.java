package test;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

public class BeanTest {
	private static void copyProperties(Object source, Object target, Class<?> editable, String... ignoreProperties)
			throws BeansException {

		Assert.notNull(source, "Source must not be null");
		Assert.notNull(target, "Target must not be null");

		Class<?> actualEditable = target.getClass();
		if (editable != null) {
			if (!editable.isInstance(target)) {
				throw new IllegalArgumentException("Target class [" + target.getClass().getName() +
						"] not assignable to Editable class [" + editable.getName() + "]");
			}
			actualEditable = editable;
		}
		PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
		List<String> ignoreList = (ignoreProperties != null ? Arrays.asList(ignoreProperties) : null);

		for (PropertyDescriptor targetPd : targetPds) {
			Method writeMethod = targetPd.getWriteMethod();
			if (writeMethod != null && (ignoreList == null || !ignoreList.contains(targetPd.getName()))) {
				PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
				if (sourcePd != null) {
					Method readMethod = sourcePd.getReadMethod();
					if (readMethod != null &&
							ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
						try {
							if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
								readMethod.setAccessible(true);
							}
							Object value = readMethod.invoke(source);
							if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
								writeMethod.setAccessible(true);
							}
							writeMethod.invoke(target, value);
						}
						catch (Throwable ex) {
							throw new FatalBeanException(
									"Could not copy property '" + targetPd.getName() + "' from source to target", ex);
						}
					}
				}
			}
		}
	}
	
	/*private static void copyProperties(final Object source, final Object target, final Class editable,
			final String[] ignoreProperties,  final boolean ignore) {

		if(StringUtils.isNull(source)){
			LOG.warn("Source must not be null");
			return;
		}
		if(StringUtils.isNull(target)){
			LOG.warn("Target must not be null");
			return;
		}

		Class actualEditable = target.getClass();
		if (editable != null) {
			if (!editable.isInstance(target)) {
				throw new IllegalArgumentException("Target class [" + target.getClass().getName()
						+ "] not assignable to Editable class [" + editable.getName() + "]");
			}
			actualEditable = editable;
		}
		final PropertyDescriptor[] targetPds = BeanUtils.getPropertyDescriptors(actualEditable);
		List ignoreList = null;
		if (ignoreProperties != null) {
			ignoreList = Arrays.asList(ignoreProperties);
		}

		for (int i = 0; i < targetPds.length; i++) {
			final PropertyDescriptor targetPd = targetPds[i];
			if (targetPd.getWriteMethod() != null) {
				if (ignore) {
					// 如果是忽略模式，则只有在忽略字段的才忽略
					if (!(ignoreProperties == null || (!ignoreList.contains(targetPd.getName())))) {
						continue;
					}
				} else {
					// 如果是非忽略模式，则只有在忽略字段的才拷贝
					if (ignoreProperties == null || (!ignoreList.contains(targetPd.getName()))) {
						continue;
					}
				}
				
				PropertyDescriptor sourcePd = BeanUtils.getPropertyDescriptor(source.getClass(), targetPd
						.getName());
				
				if (sourcePd != null && sourcePd.getReadMethod() != null) {
					try {
					
						final Method readMethod = sourcePd.getReadMethod();
						if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
							readMethod.setAccessible(true);
						}
						final Object value = readMethod.invoke(source, new Object[0]);
						final Method writeMethod = targetPd.getWriteMethod();
						if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
							writeMethod.setAccessible(true);
						}
						writeMethod.invoke(target, new Object[] { sourceToTarget(value, sourcePd, targetPd) });
						
					} catch (final Throwable ex) {
						throw new RuntimeException("Could not copy properties from source to target", ex);
					}
				}
			}
		}
	}*/
	
	
	public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) throws BeansException {
		CachedIntrospectionResults cr = CachedIntrospectionResults.forClass(clazz);
		return cr.getPropertyDescriptors();
	}
	
	public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName)
			throws BeansException {

		CachedIntrospectionResults cr = CachedIntrospectionResults.forClass(clazz);
		return cr.getPropertyDescriptor(propertyName);
	}

}
