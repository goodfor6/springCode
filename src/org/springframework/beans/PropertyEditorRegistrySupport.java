package org.springframework.beans;

import java.beans.PropertyEditor;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.propertyeditors.ByteArrayPropertyEditor;
import org.springframework.beans.propertyeditors.CharArrayPropertyEditor;
import org.springframework.beans.propertyeditors.CharacterEditor;
import org.springframework.beans.propertyeditors.CharsetEditor;
import org.springframework.beans.propertyeditors.ClassArrayEditor;
import org.springframework.beans.propertyeditors.ClassEditor;
import org.springframework.beans.propertyeditors.CurrencyEditor;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.beans.propertyeditors.CustomMapEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.FileEditor;
import org.springframework.beans.propertyeditors.InputSourceEditor;
import org.springframework.beans.propertyeditors.InputStreamEditor;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.beans.propertyeditors.PatternEditor;
import org.springframework.beans.propertyeditors.PropertiesEditor;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.beans.propertyeditors.TimeZoneEditor;
import org.springframework.beans.propertyeditors.URIEditor;
import org.springframework.beans.propertyeditors.URLEditor;
import org.springframework.beans.propertyeditors.UUIDEditor;
import org.springframework.beans.propertyeditors.ZoneIdEditor;
import org.springframework.core.convert.ConversionService;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceArrayPropertyEditor;
import org.springframework.util.ClassUtils;
import org.xml.sax.InputSource;

public class PropertyEditorRegistrySupport implements PropertyEditorRegistry{
	
	private static Class<?>zoneIdClass;
	
	static {
		try {
			zoneIdClass=ClassUtils.forName("java.time.ZoneId",PropertyEditorRegistrySupport.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			zoneIdClass=null;
		}
	}
	
	private ConversionService conversionService;
	
	private boolean defaultEditorsActive=false;
	
	private boolean configValueEditorsActive=false;
	
	private Map<Class<?>,PropertyEditor> defaultEditors;
	
	private Map<Class<?>,PropertyEditor> overridenDefaultEditors;
	
	private Map<Class<?>,PropertyEditor>customEditors;
	
	private Map<String,CustomEditorHolder>customEditorsForPath;
	
	private Map<Class<?>,PropertyEditor>customEditorCache;
	
	public void setConversionService(ConversionService conversionService){
		this.conversionService=conversionService;
	}
	
	public ConversionService getConversionService(){
		return this.conversionService;
	}
	
	protected void registerDefaultEditors(){
		this.defaultEditorsActive=true;
	}
	
	public void useConfigValueEditors(){
		this.configValueEditorsActive=true;
	}
	
	public void overrideDefaultEditor(Class<?>requiredType,PropertyEditor propertyEditor){
		if(this.overridenDefaultEditors==null){
			this.overridenDefaultEditors=new HashMap<Class<?>,PropertyEditor>();
		}
		this.overridenDefaultEditors.put(requiredType, propertyEditor);
	}
	
	public PropertyEditor getDefaultEditor(Class<?>requiredType){
		if(!this.defaultEditorsActive){
			return null;
		}
		if(this.overridenDefaultEditors!=null){
			PropertyEditor editor=this.overridenDefaultEditors.get(requiredType);
			if(editor!=null){
				return editor;
			}
		}
		if(this.defaultEditors==null){
			createDefaultEditors();
		}
		return this.defaultEditors.get(requiredType);
		
	}
	
	private void createDefaultEditors(){
		this.defaultEditors.put(Charset.class, new CharsetEditor());
		this.defaultEditors.put(Class.class, new ClassEditor());
		this.defaultEditors.put(Class[].class, new ClassArrayEditor());
		this.defaultEditors.put(Currency.class, new CurrencyEditor());
	    this.defaultEditors.put(File.class, new FileEditor());
	    this.defaultEditors.put(InputStream.class, new InputStreamEditor());
	    this.defaultEditors.put(InputSource.class, new InputSourceEditor());
	    this.defaultEditors.put(Locale.class, new LocaleEditor());
	    this.defaultEditors.put(Pattern.class, new PatternEditor());
	    this.defaultEditors.put(Properties.class, new PropertiesEditor());
	    this.defaultEditors.put(Resource[].class, new ResourceArrayPropertyEditor());
	    this.defaultEditors.put(TimeZone.class, new TimeZoneEditor());
	    this.defaultEditors.put(URI.class, new URIEditor());
	    this.defaultEditors.put(URL.class, new URLEditor());
	    this.defaultEditors.put(UUID.class, new UUIDEditor());
	    if(zoneIdClass!=null){
	    	this.defaultEditors.put(zoneIdClass, new ZoneIdEditor());
	    }                                            
	    this.defaultEditors.put(Collection.class,new CustomCollectionEditor(Collection.class));
	    this.defaultEditors.put(Set.class, new CustomCollectionEditor(Set.class));
	    this.defaultEditors.put(SortedSet.class,new CustomCollectionEditor(SortedSet.class));
	    this.defaultEditors.put(List.class, new CustomCollectionEditor(List.class));
	    this.defaultEditors.put(SortedMap.class, new CustomMapEditor(SortedMap.class));
	    
	    this.defaultEditors.put(byte[].class, new ByteArrayPropertyEditor());
	    this.defaultEditors.put(char[].class, new CharArrayPropertyEditor());
	    
	    this.defaultEditors.put(char.class, new CharacterEditor(false));
	    this.defaultEditors.put(Character.class, new CharacterEditor(true));
	
	    // Spring's CustomBooleanEditor accepts more flag values than the JDK's default editor.
	    this.defaultEditors.put(boolean.class, new CustomBooleanEditor(false));
	    this.defaultEditors.put(Boolean.class, new CustomBooleanEditor(true));
	    // The JDK does not contain default editors for number wrapper types!
 		// Override JDK primitive number editors with our own CustomNumberEditor.
	    this.defaultEditors.put(byte.class, new CustomNumberEditor(Byte.class,false));
	    this.defaultEditors.put(Byte.class, new CustomNumberEditor(Byte.class,true));
	    this.defaultEditors.put(short.class, new CustomNumberEditor(Short.class,false));
	    this.defaultEditors.put(Short.class, new CustomNumberEditor(Short.class,true));
	    this.defaultEditors.put(int.class,new CustomNumberEditor(Integer.class,false));
	    this.defaultEditors.put(Integer.class, new CustomNumberEditor(Integer.class,true));
	    this.defaultEditors.put(long.class, new CustomNumberEditor(Long.class,false));
	    this.defaultEditors.put(Long.class, new CustomNumberEditor(Long.class,true));
	    this.defaultEditors.put(float.class, new CustomNumberEditor(Float.class,false));
	    this.defaultEditors.put(Float.class, new CustomNumberEditor(Float.class,true));
	    this.defaultEditors.put(double.class, new CustomNumberEditor(Double.class,false));
	    this.defaultEditors.put(Double.class,new CustomNumberEditor(Double.class,true));
	    this.defaultEditors.put(BigDecimal.class, new CustomNumberEditor(BigDecimal.class,true));
	    this.defaultEditors.put(BigInteger.class, new CustomNumberEditor(BigInteger.class,true));
	    // Only register config value editors if explicitly requested.
	    if(this.configValueEditorsActive){
	    	StringArrayPropertyEditor sae=new StringArrayPropertyEditor();
	    	this.defaultEditors.put(String[].class, sae);
	    	this.defaultEditors.put(short[].class, sae);
	    	this.defaultEditors.put(int[].class, sae);
	        this.defaultEditors.put(long[].class, sae);
	    }
	}
	
	protected void copyDefaultEditorsTo(PropertyEditorRegistrySupport target){
		target.defaultEditorsActive=this.defaultEditorsActive;
		target.configValueEditorsActive=this.configValueEditorsActive;
		target.defaultEditors=this.defaultEditors;
		target.overridenDefaultEditors=this.overridenDefaultEditors;
	}
	
	@Override
	public void registerCustomEditor(Class<?>requiredType,PropertyEditor propertyEditor){
		registerCustomEditor(requiredType,null,propertyEditor);
	}
	
	@Override
	public void registerCustomEditor(Class<?>requiredType,String propertyPath,PropertyEditor propertyEditor){
		if(requiredType==null&& propertyPath==null){
			throw new IllegalArgumentException("Either requiredType or propertyPath is required");
		}
		if(propertyPath!=null){
			if(this.customEditorsForPath==null){
				this.customEditorsForPath=new LinkedHashMap<String,CustomEditorHolder>(16);
			}                                                                                 
			this.customEditorsForPath.put(propertyPath, new CustomEditorHolder(propertyEditor,requiredType));
		}
		else{
			if(this.customEditors==null){
				this.customEditors=new LinkedHashMap<Class<?>,PropertyEditor>(16);
			}
			this.customEditors.put(requiredType, propertyEditor);
			this.customEditorCache=null;
		}
	}
	
	@Override
	public PropertyEditor findCustomEditor(Class<?> requiredType,String propertyPath){
		Class<?>requiredTypeToUse=requiredType;
		if(propertyPath!=null){
			if(this.customEditorsForPath!=null){
				PropertyEditor editor=getCustomEditor(propertyPath,requiredType);
				if(editor==null){
					List<String>strippedPaths=new LinkedList<String>();
					addStrippedPropertyPaths(strippedPaths,"",propertyPath);
					for(Iterator<String>it =strippedPaths.iterator();it.hasNext()&&editor==null;){
						String strippedPath=it.next();
						editor=getCustomEditor(strippedPath,requiredType);
					}
				}
				if(editor!=null){
					return editor;
				}
			}
			if(requiredType==null){
				requiredTypeToUse=getPropertyType(propertyPath);
			}
		}
			return getCustomEditor(requiredTypeToUse);
		}
	
	public boolean hasCustomEditorForElement(Class<?>elementType,String propertyPath){
		if(propertyPath!=null&& this.customEditorsForPath!=null){
			for(Map.Entry<String, CustomEditorHolder>entry:this.customEditorsForPath.entrySet()){
				if(PropertyAccessorUtils.matcheProperty(entry.getKey(), propertyPath)){
					if(entry.getValue().getPropertyEditor(elementType)!=null){
						return true;
					}
				}
			}
		}
		return (elementType!=null && this.customEditors!=null && this.customEditors.containsKey(elementType));
	}
	
	
	protected Class<?>guessPropertyTypeFormEditors(String propertyName){
		if(this.customEditorsForPath!=null){
			CustomEditorHolder editorHolder=this.customEditorsForPath.get(propertyName);
			if(editorHolder==null){
				List<String>strippedPaths=new LinkedList<String>();
				addStrippedPropertyPaths(strippedPaths,"",propertyName);
				for(Iterator<String>it=strippedPaths.iterator();it.hasNext()&&editorHolder==null;){
					String strippedName=it.next();
					editorHolder=this.customEditorsForPath.get(strippedName);
				}
			}
			if(editorHolder!=null){
				return editorHolder.getRegisteredType();
			}
		}
		return null;
	}
	
	protected void copyCustomEditorsTo(PropertyEditorRegistry target,String nestedProperty){
		String actualPropertyName=
				(nestedProperty!=null?PropertyAccessorUtils.getPropertyName(nestedProperty):null);
		if(this.customEditors!=null){
			for(Map.Entry<Class<?>, PropertyEditor> entry:this.customEditors.entrySet()){
				target.registerCustomEditor(entry.getKey(), entry.getValue());
			}
		}
		if(this.customEditorsForPath!=null){
			for(Map.Entry<String, CustomEditorHolder>entry:this.customEditorsForPath.entrySet()){
				String editorPath=entry.getKey();
				CustomEditorHolder editorHolder=entry.getValue();
				if(nestedProperty!=null){
					int pos =PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(editorPath);
					if(pos!=-1){
						String editorNestedProperty=editorPath.substring(0, pos);
						String editorNestedPath=editorPath.substring(pos+1);
						if(editorNestedProperty.equals(nestedProperty)|| editorNestedProperty.equals(actualPropertyName)){
							target.registerCustomEditor(editorHolder.getRegisteredType(),editorNestedPath,editorHolder.getPropertyEditor());
						}
					}
				}
				else{
					target.registerCustomEditor(editorHolder.getRegisteredType(),editorPath, editorHolder.getPropertyEditor());
				}
			}
		}
	}
	
	private PropertyEditor getCustomEditor(String propertyName,Class<?>requiredType){
		CustomEditorHolder holder=this.customEditorsForPath.get(propertyName);
		return (holder!=null?holder.getPropertyEditor(requiredType):null);
	}
	
	private void addStrippedPropertyPaths(List<String>strippedPaths,String nestedPath,String propertyPath){
		int startIndex=propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR);
		if(startIndex!=-1){
			int endIndex=propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR);
			if(endIndex!=-1){
				String prefix=propertyPath.substring(0, startIndex);
				String key=propertyPath.substring(startIndex,endIndex+1);
				String suffix=propertyPath.substring(endIndex+1,propertyPath.length());
				strippedPaths.add(nestedPath+prefix+suffix);
				addStrippedPropertyPaths(strippedPaths,nestedPath+prefix,suffix);
				addStrippedPropertyPaths(strippedPaths,nestedPath+prefix+key,suffix);
			}
		}
	}
	
	protected Class<?>getPropertyType(String propertyPath){
		return null;
	}
	
	private PropertyEditor getCustomEditor(Class<?>requiredType){
		if(requiredType==null||this.customEditors==null){
			return null;
		}
		PropertyEditor editor=this.customEditors.get(requiredType);
		if(editor==null){
			if(this.customEditorCache!=null){
				editor=this.customEditorCache.get(requiredType);
			}
			if(editor==null){
				for(Iterator<Class<?>>it =this.customEditors.keySet().iterator();it.hasNext()&&editor==null;){
					Class<?>key=it.next();
					if(key.isAssignableFrom(requiredType)){
						editor=this.customEditors.get(key);
						if(this.customEditorCache==null){
							this.customEditorCache=new HashMap<Class<?>,PropertyEditor>();
						}
						this.customEditorCache.put(requiredType, editor);
					}
				}
			}
		}
		return editor;
	}
	
	
	private static class CustomEditorHolder{
		
		private final PropertyEditor propertyEditor;
		
		private final Class<?> registeredType;
		
		private CustomEditorHolder(PropertyEditor propertyEditor,Class<?>registeredType){
			this.propertyEditor=propertyEditor;
			this.registeredType=registeredType;
		}
		private PropertyEditor getPropertyEditor(){
			return this.propertyEditor;
		}
		private Class<?>getRegisteredType(){
			return this.registeredType;
		}
		private PropertyEditor getPropertyEditor(Class<?> requiredType){
			if(this.registeredType==null ||
				(requiredType!=null && 
					(ClassUtils.isAssignable(this.registeredType, requiredType)||
					 ClassUtils.isAssignable(requiredType, this.registeredType)))
				||
				(requiredType==null && 
				(!Collection.class.isAssignableFrom(this.registeredType)&& !this.registeredType.isArray()))){
					return this.propertyEditor;
				}
			else{
				return null;
			}
		}
	}
	
	
	
	
	
	
	

}
