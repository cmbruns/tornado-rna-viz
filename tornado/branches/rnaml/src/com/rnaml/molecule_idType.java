/**
 * molecule_idType.java
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

public class molecule_idType extends com.altova.xml.Node {

	public molecule_idType(molecule_idType node) {
		super(node);
	}

	public molecule_idType(org.w3c.dom.Node node) {
		super(node);
	}

	public molecule_idType(org.w3c.dom.Document doc) {
		super(doc);
	}

	public molecule_idType(com.altova.xml.Document doc, String namespaceURI, String prefix, String name) {
		super(doc, namespaceURI, prefix, name);
	}
	
	public void adjustPrefix() {
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Attribute, null, "ref" );
				tmpNode != null;
				tmpNode = getDomNextChild( Attribute, null, "ref", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
		}
	}

	public static int getrefMinCount() {
		return 1;
	}

	public static int getrefMaxCount() {
		return 1;
	}

	public int getrefCount() {
		return getDomChildCount(Attribute, null, "ref");
	}

	public boolean hasref() {
		return hasDomChild(Attribute, null, "ref");
	}

	public SchemaNMToken newref() {
		return new SchemaNMToken();
	}

	public SchemaNMToken getrefAt(int index) throws Exception {
		return new SchemaNMToken(getDomNodeValue(dereference(getDomChildAt(Attribute, null, "ref", index))));
	}

	public org.w3c.dom.Node getStartingrefCursor() throws Exception {
		return getDomFirstChild(Attribute, null, "ref" );
	}

	public org.w3c.dom.Node getAdvancedrefCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Attribute, null, "ref", curNode );
	}

	public SchemaNMToken getrefValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new SchemaNMToken(getDomNodeValue( dereference(curNode) ) );
	}

	public SchemaNMToken getref() throws Exception 
 {
		return getrefAt(0);
	}

	public void removerefAt(int index) {
		removeDomChildAt(Attribute, null, "ref", index);
	}

	public void removeref() {
		while (hasref())
			removerefAt(0);
	}

	public void addref(SchemaNMToken value) {
		if( value.isNull() == false )
		{
			appendDomChild(Attribute, null, "ref", value.toString());
		}
	}

	public void addref(String value) throws Exception {
		addref(new SchemaNMToken(value));
	}

	public void insertrefAt(SchemaNMToken value, int index) {
		insertDomChildAt(Attribute, null, "ref", index, value.toString());
	}

	public void insertrefAt(String value, int index) throws Exception {
		insertrefAt(new SchemaNMToken(value), index);
	}

	public void replacerefAt(SchemaNMToken value, int index) {
		replaceDomChildAt(Attribute, null, "ref", index, value.toString());
	}

	public void replacerefAt(String value, int index) throws Exception {
		replacerefAt(new SchemaNMToken(value), index);
	}

	private org.w3c.dom.Node dereference(org.w3c.dom.Node node) {
		return node;
	}
}
