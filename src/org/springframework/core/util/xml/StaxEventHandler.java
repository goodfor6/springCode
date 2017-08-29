package org.springframework.core.util.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Namespace;

import org.springframework.asm.Attribute;
import org.xml.sax.Locator;

public class StaxEventHandler extends AbstractStaxHandler{
	
	private final XMLEventFactory eventFactory;
	private final XMLEventWriter eventWriter;

	public StaxEventHandler(XMLEventWriter eventWriter){
		this.eventFactory = XMLEventFactory.newInstance();
		this.eventWriter = eventWriter;
	}
	
	public StaxEventHandler(XMLEventWriter eventWriter, XMLEventFactory factory){
		this.eventFactory = factory;
		this.eventWriter = eventWriter;
	}
	
	public void setDocumentLocator(Locator locator){
		if(locator != null){
			this.eventFactory.setLocation(new LocatorLocationAdapter(locator));
		}
	}
	
	protected void startDocumentInternal()throws XMLStreamException{
		this.eventWriter.add(this.eventFactory.createStartDocument());
	}
	
	protected void endDocumentInternal() throws XMLStreamException{
		this.eventWriter.add(this.eventFactory.createEndDocument());
	}
	
	protected void startElementInternal(QName name, Attribute atts,
			Map<String, String>namespaceMapping)throws XMLStreamException{
		List<Attribute>attributes = getAttributes(atts);
		List<Namespace>namespaces = getNamespaces(namespaceMapping);
		this.eventWriter.add(this.eventFactory.createStartElement(name, attributes.iterator(), namespaces.iterator()));
	}
	
	private List<Namespace>result = new ArrayList<Namespace>(){
		
	}
	
	
	
	private static final class LocatorLocationAdapter implements Location{
		
		private final Locator locator;
		
		public LocatorLocationAdapter(Locator locator){
			this.locator = locator;
		}
		
		public int getLineNumber(){
			return this.locator.getLineNumber();
		}
		
		public int getColumnNumber(){
			return this.locator.getColumnNumber();
		}
		
		public int getCharacterOffset(){
			return -1;
		}
		
		public String getPublicId(){
			return this.locator.getPublicId();
		}
		
		public String getSystemId(){
			return this.locator.getSystemId();
		}
		
		
		
	}
	
	
}
