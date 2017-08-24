package org.springframework.core.util.xml;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class DomContentHandler  implements ContentHandler{
		
	private final Document document;
	private final List<Element> elements = new ArrayList<Element>();
	private final Node node;

	DomContentHandler(Node node){
		Assert.notNull(node,"node must not be null");
		this.node = node;
		if(node instanceof Document){
			document = (Document)node;
		}
		else{
			document = node.getOwnerDocument();
		}
		Assert.notNull(document,"document must not be null");
	}
	
	private Node getParent(){
		if(!elements.isEmpty()){
			return elements.get(elements.size()-1);
		}
		else {
			return node;
		}
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
		Node parent = getParent();
		Element element = document.createElementNS(uri, qName);
		for(int i = 0; i < attributes.getLength(); i++){
			String attrUri = attributes.getURI(i);
			String attrQname = attributes.getQName(i);
			String value = attributes.getValue(i);
			if(!attrQname.startsWith("xmlns")){
				element.setAttributeNS(attrUri,attrQname, value);
			}
		}
		element = (Element)parent.appendChild(element);
		elements.add(element);
	}
	
}
