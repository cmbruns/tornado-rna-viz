/**
 * book_titleType.java
 *
 * This file was generated by XMLSpy 2006r3 Enterprise Edition.
 *
 * YOU SHOULD NOT MODIFY THIS FILE, BECAUSE IT WILL BE
 * OVERWRITTEN WHEN YOU RE-RUN CODE GENERATION.
 *
 * Refer to the XMLSpy Documentation for further details.
 * http://www.altova.com/xmlspy
 */


package com.rnaml;

import com.altova.types.*;

public class book_titleType extends com.altova.xml.Node {

	public book_titleType(book_titleType node) {
		super(node);
	}

	public book_titleType(org.w3c.dom.Node node) {
		super(node);
	}

	public book_titleType(org.w3c.dom.Document doc) {
		super(doc);
	}

	public book_titleType(com.altova.xml.Document doc, String namespaceURI, String prefix, String name) {
		super(doc, namespaceURI, prefix, name);
	}
	

	public SchemaString getValue() {
		return new SchemaString(getDomNodeValue(dereference(domNode)));
	}

	public void setValue(SchemaType value) {
		setDomNodeValue(domNode, value.toString());
	}

	public void assign(SchemaType value) {
		setValue(value);
	}

	public void adjustPrefix() {
	}
	public void addTextNode(String value) throws Exception {
		appendDomChild(Text, null, null, value.toString());
	}
	public void addComment(String value) throws Exception {
			appendDomChild(Comment, null, null, value.toString());
		}
	public void addCDataNode(String value) throws Exception {
			appendDomChild(CData, null, null, value.toString());
		}
	public void addProcessingInstruction(String name, String value) throws Exception {
			appendDomChild(ProcessingInstruction, null, name.toString(), value.toString());
		}

	private org.w3c.dom.Node dereference(org.w3c.dom.Node node) {
		return node;
	}
}
