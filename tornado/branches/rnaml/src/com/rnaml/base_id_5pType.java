/**
 * base_id_5pType.java
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

public class base_id_5pType extends com.altova.xml.Node {

	public base_id_5pType(base_id_5pType node) {
		super(node);
	}

	public base_id_5pType(org.w3c.dom.Node node) {
		super(node);
	}

	public base_id_5pType(org.w3c.dom.Document doc) {
		super(doc);
	}

	public base_id_5pType(com.altova.xml.Document doc, String namespaceURI, String prefix, String name) {
		super(doc, namespaceURI, prefix, name);
	}
	
	public void adjustPrefix() {
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "base-id" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "base-id", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new base_idType(tmpNode).adjustPrefix();
		}
	}

	public static int getbase_idMinCount() {
		return 1;
	}

	public static int getbase_idMaxCount() {
		return 1;
	}

	public int getbase_idCount() {
		return getDomChildCount(Element, null, "base-id");
	}

	public boolean hasbase_id() {
		return hasDomChild(Element, null, "base-id");
	}

	public base_idType newbase_id() {
		return new base_idType(domNode.getOwnerDocument().createElementNS(null, "base-id"));
	}

	public base_idType getbase_idAt(int index) throws Exception {
		return new base_idType(dereference(getDomChildAt(Element, null, "base-id", index)));
	}

	public org.w3c.dom.Node getStartingbase_idCursor() throws Exception {
		return getDomFirstChild(Element, null, "base-id" );
	}

	public org.w3c.dom.Node getAdvancedbase_idCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, null, "base-id", curNode );
	}

	public base_idType getbase_idValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new base_idType( dereference(curNode) );
	}

	public base_idType getbase_id() throws Exception 
 {
		return getbase_idAt(0);
	}

	public void removebase_idAt(int index) {
		removeDomChildAt(Element, null, "base-id", index);
	}

	public void removebase_id() {
		while (hasbase_id())
			removebase_idAt(0);
	}

	public void addbase_id(base_idType value) {
		appendDomElement(null, "base-id", value);	
	}

	public void insertbase_idAt(base_idType value, int index) {
		insertDomElementAt(null, "base-id", index, value);
	}

	public void replacebase_idAt(base_idType value, int index) {
		replaceDomElementAt(null, "base-id", index, value);
	}

	private org.w3c.dom.Node dereference(org.w3c.dom.Node node) {
		return node;
	}
}