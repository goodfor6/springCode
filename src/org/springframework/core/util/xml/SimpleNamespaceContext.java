
package org.springframework.core.util.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.springframework.core.util.Assert;

public class SimpleNamespaceContext implements NamespaceContext{
	private Map<String, String> prefixToNamespaceUri = new HashMap<String, String>();
	private Map<String, List<String>> namespaceUriToPrefixes = new HashMap<String, List<String>>();
	private String defaultNamespaceUri = "";
	
	public String getNamespaceURI(String prefix){
		Assert.notNull(prefix, "prefix is null");
		if(XMLConstants.XML_NS_PREFIX.equals(prefix)){
			return XMLConstants.XML_NS_URI;
		}
		else if(XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)){
			return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
		}
		else if(XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)){
			return defaultNamespaceUri;
		}
		else if(prefixToNamespaceUri.containsKey(prefix)){
			return prefixToNamespaceUri.get(prefix);
		}
		return "";
	}
	
	public String getPrefix(String namespaceUri){
		List<?> prefixes = getPrefixesInternal(namespaceUri);
		return prefixes.isEmpty() ? null : (String)prefixes.get(0);
	}
	
	public Iterator<String> getPrefixes(String namespaceUri){
		return getPrefixesInternal(namespaceUri).iterator();
	}
	
	public void setBindings(Map<String, String>bindings){
		for(Map.Entry<String, String>entry : bindings.entrySet()){
			bindNamespaceUri(entry.getKey(), entry.getValue());
		}
	}
	
	public void bindDefaultNamespaceUri(String namespaceUri){
		bindNamespaceUri(XMLConstants.DEFAULT_NS_PREFIX, namespaceUri);
	}
	
	public void bindNamespaceUri(String prefix, String namespaceUri){
		Assert.notNull(prefix, "No prefix giver");
		Assert.notNull(namespaceUri, "No numespaceUri given");
		if(XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)){
			defaultNamespaceUri = namespaceUri;
		}
		else{
			prefixToNamespaceUri.put(prefix, namespaceUri);
			getPrefixesInternal(namespaceUri).add(prefix);
		}
	}
	public void clear(){
		prefixToNamespaceUri.clear();
	}
	
	public Iterator<String> getBoundPrefixes(){
		return prefixToNamespaceUri.keySet().iterator();
	}
	
	private List<String> getPrefixesInternal(String namespaceUri){
		if(defaultNamespaceUri.equals(namespaceUri)){
			return Collections.singletonList(XMLConstants.DEFAULT_NS_PREFIX);
		}
		else if(XMLConstants.XML_NS_URI.equals(namespaceUri)){
			return Collections.singletonList(XMLConstants.XML_NS_PREFIX);
		}
		else if(XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceUri)){
			return Collections.singletonList(XMLConstants.XMLNS_ATTRIBUTE);
		}
		else{
			List<String> list = namespaceUriToPrefixes.get(namespaceUri);
			if(list == null){
				list = new ArrayList<String>();
				namespaceUriToPrefixes.put(namespaceUri, list);
			}
			return list;
		}
	}
	
	public void removeBinding(String prefix){
		if(XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)){
			defaultNamespaceUri = ""; 
		}
		else{
			String namespaceUri = prefixToNamespaceUri.remove(prefix);
			List<String> prefixes = getPrefixesInternal(namespaceUri);
			prefixes.remove(prefix);
		}
	}
	
}






















