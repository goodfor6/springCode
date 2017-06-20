package org.springframework.beans;

import java.beans.PropertyEditor;

public interface PropertyEditorRegistry {

	void registerCustomEditor(Class<?>requriedType,PropertyEditor propertyEditor);
    
	void registerCustomEditor(Class<?> requriedType,String propertyPath,PropertyEditor propertyEditor);
	
	PropertyEditor findCustomEditor(Class<?>requiredType ,String propertyPath);
}
