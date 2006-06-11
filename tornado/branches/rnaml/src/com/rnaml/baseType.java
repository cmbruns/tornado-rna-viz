/**
 * baseType.java
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

public class baseType extends com.altova.xml.Node {

	public baseType(baseType node) {
		super(node);
	}

	public baseType(org.w3c.dom.Node node) {
		super(node);
	}

	public baseType(org.w3c.dom.Document doc) {
		super(doc);
	}

	public baseType(com.altova.xml.Document doc, String namespaceURI, String prefix, String name) {
		super(doc, namespaceURI, prefix, name);
	}
	
	public void adjustPrefix() {
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Attribute, null, "comment" );
				tmpNode != null;
				tmpNode = getDomNextChild( Attribute, null, "comment", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Attribute, null, "reference-ids" );
				tmpNode != null;
				tmpNode = getDomNextChild( Attribute, null, "reference-ids", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Attribute, null, "analysis-ids" );
				tmpNode != null;
				tmpNode = getDomNextChild( Attribute, null, "analysis-ids", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Attribute, null, "database-ids" );
				tmpNode != null;
				tmpNode = getDomNextChild( Attribute, null, "database-ids", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "position" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "position", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new positionType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "base-type" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "base-type", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new base_typeType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "alt-loc" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "alt-loc", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new alt_locType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "insertion" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "insertion", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new insertionType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "atom" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "atom", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new atomType(tmpNode).adjustPrefix();
		}
	}

	public static int getcommentMinCount() {
		return 0;
	}

	public static int getcommentMaxCount() {
		return 1;
	}

	public int getcommentCount() {
		return getDomChildCount(Attribute, null, "comment");
	}

	public boolean hascomment() {
		return hasDomChild(Attribute, null, "comment");
	}

	public SchemaString newcomment() {
		return new SchemaString();
	}

	public SchemaString getcommentAt(int index) throws Exception {
		return new SchemaString(getDomNodeValue(dereference(getDomChildAt(Attribute, null, "comment", index))));
	}

	public org.w3c.dom.Node getStartingcommentCursor() throws Exception {
		return getDomFirstChild(Attribute, null, "comment" );
	}

	public org.w3c.dom.Node getAdvancedcommentCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Attribute, null, "comment", curNode );
	}

	public SchemaString getcommentValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new SchemaString(getDomNodeValue( dereference(curNode) ) );
	}

	public SchemaString getcomment() throws Exception 
 {
		return getcommentAt(0);
	}

	public void removecommentAt(int index) {
		removeDomChildAt(Attribute, null, "comment", index);
	}

	public void removecomment() {
		while (hascomment())
			removecommentAt(0);
	}

	public void addcomment(SchemaString value) {
		if( value.isNull() == false )
		{
			appendDomChild(Attribute, null, "comment", value.toString());
		}
	}

	public void addcomment(String value) throws Exception {
		addcomment(new SchemaString(value));
	}

	public void insertcommentAt(SchemaString value, int index) {
		insertDomChildAt(Attribute, null, "comment", index, value.toString());
	}

	public void insertcommentAt(String value, int index) throws Exception {
		insertcommentAt(new SchemaString(value), index);
	}

	public void replacecommentAt(SchemaString value, int index) {
		replaceDomChildAt(Attribute, null, "comment", index, value.toString());
	}

	public void replacecommentAt(String value, int index) throws Exception {
		replacecommentAt(new SchemaString(value), index);
	}

	public static int getreference_idsMinCount() {
		return 0;
	}

	public static int getreference_idsMaxCount() {
		return 1;
	}

	public int getreference_idsCount() {
		return getDomChildCount(Attribute, null, "reference-ids");
	}

	public boolean hasreference_ids() {
		return hasDomChild(Attribute, null, "reference-ids");
	}

	public SchemaIDRef newreference_ids() {
		return new SchemaIDRef();
	}

	public SchemaIDRef getreference_idsAt(int index) throws Exception {
		return new SchemaIDRef(getDomNodeValue(dereference(getDomChildAt(Attribute, null, "reference-ids", index))));
	}

	public org.w3c.dom.Node getStartingreference_idsCursor() throws Exception {
		return getDomFirstChild(Attribute, null, "reference-ids" );
	}

	public org.w3c.dom.Node getAdvancedreference_idsCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Attribute, null, "reference-ids", curNode );
	}

	public SchemaIDRef getreference_idsValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new SchemaIDRef(getDomNodeValue( dereference(curNode) ) );
	}

	public SchemaIDRef getreference_ids() throws Exception 
 {
		return getreference_idsAt(0);
	}

	public void removereference_idsAt(int index) {
		removeDomChildAt(Attribute, null, "reference-ids", index);
	}

	public void removereference_ids() {
		while (hasreference_ids())
			removereference_idsAt(0);
	}

	public void addreference_ids(SchemaIDRef value) {
		if( value.isNull() == false )
		{
			appendDomChild(Attribute, null, "reference-ids", value.toString());
		}
	}

	public void addreference_ids(String value) throws Exception {
		addreference_ids(new SchemaIDRef(value));
	}

	public void insertreference_idsAt(SchemaIDRef value, int index) {
		insertDomChildAt(Attribute, null, "reference-ids", index, value.toString());
	}

	public void insertreference_idsAt(String value, int index) throws Exception {
		insertreference_idsAt(new SchemaIDRef(value), index);
	}

	public void replacereference_idsAt(SchemaIDRef value, int index) {
		replaceDomChildAt(Attribute, null, "reference-ids", index, value.toString());
	}

	public void replacereference_idsAt(String value, int index) throws Exception {
		replacereference_idsAt(new SchemaIDRef(value), index);
	}

	public static int getanalysis_idsMinCount() {
		return 0;
	}

	public static int getanalysis_idsMaxCount() {
		return 1;
	}

	public int getanalysis_idsCount() {
		return getDomChildCount(Attribute, null, "analysis-ids");
	}

	public boolean hasanalysis_ids() {
		return hasDomChild(Attribute, null, "analysis-ids");
	}

	public SchemaIDRef newanalysis_ids() {
		return new SchemaIDRef();
	}

	public SchemaIDRef getanalysis_idsAt(int index) throws Exception {
		return new SchemaIDRef(getDomNodeValue(dereference(getDomChildAt(Attribute, null, "analysis-ids", index))));
	}

	public org.w3c.dom.Node getStartinganalysis_idsCursor() throws Exception {
		return getDomFirstChild(Attribute, null, "analysis-ids" );
	}

	public org.w3c.dom.Node getAdvancedanalysis_idsCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Attribute, null, "analysis-ids", curNode );
	}

	public SchemaIDRef getanalysis_idsValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new SchemaIDRef(getDomNodeValue( dereference(curNode) ) );
	}

	public SchemaIDRef getanalysis_ids() throws Exception 
 {
		return getanalysis_idsAt(0);
	}

	public void removeanalysis_idsAt(int index) {
		removeDomChildAt(Attribute, null, "analysis-ids", index);
	}

	public void removeanalysis_ids() {
		while (hasanalysis_ids())
			removeanalysis_idsAt(0);
	}

	public void addanalysis_ids(SchemaIDRef value) {
		if( value.isNull() == false )
		{
			appendDomChild(Attribute, null, "analysis-ids", value.toString());
		}
	}

	public void addanalysis_ids(String value) throws Exception {
		addanalysis_ids(new SchemaIDRef(value));
	}

	public void insertanalysis_idsAt(SchemaIDRef value, int index) {
		insertDomChildAt(Attribute, null, "analysis-ids", index, value.toString());
	}

	public void insertanalysis_idsAt(String value, int index) throws Exception {
		insertanalysis_idsAt(new SchemaIDRef(value), index);
	}

	public void replaceanalysis_idsAt(SchemaIDRef value, int index) {
		replaceDomChildAt(Attribute, null, "analysis-ids", index, value.toString());
	}

	public void replaceanalysis_idsAt(String value, int index) throws Exception {
		replaceanalysis_idsAt(new SchemaIDRef(value), index);
	}

	public static int getdatabase_idsMinCount() {
		return 0;
	}

	public static int getdatabase_idsMaxCount() {
		return 1;
	}

	public int getdatabase_idsCount() {
		return getDomChildCount(Attribute, null, "database-ids");
	}

	public boolean hasdatabase_ids() {
		return hasDomChild(Attribute, null, "database-ids");
	}

	public SchemaIDRef newdatabase_ids() {
		return new SchemaIDRef();
	}

	public SchemaIDRef getdatabase_idsAt(int index) throws Exception {
		return new SchemaIDRef(getDomNodeValue(dereference(getDomChildAt(Attribute, null, "database-ids", index))));
	}

	public org.w3c.dom.Node getStartingdatabase_idsCursor() throws Exception {
		return getDomFirstChild(Attribute, null, "database-ids" );
	}

	public org.w3c.dom.Node getAdvanceddatabase_idsCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Attribute, null, "database-ids", curNode );
	}

	public SchemaIDRef getdatabase_idsValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new SchemaIDRef(getDomNodeValue( dereference(curNode) ) );
	}

	public SchemaIDRef getdatabase_ids() throws Exception 
 {
		return getdatabase_idsAt(0);
	}

	public void removedatabase_idsAt(int index) {
		removeDomChildAt(Attribute, null, "database-ids", index);
	}

	public void removedatabase_ids() {
		while (hasdatabase_ids())
			removedatabase_idsAt(0);
	}

	public void adddatabase_ids(SchemaIDRef value) {
		if( value.isNull() == false )
		{
			appendDomChild(Attribute, null, "database-ids", value.toString());
		}
	}

	public void adddatabase_ids(String value) throws Exception {
		adddatabase_ids(new SchemaIDRef(value));
	}

	public void insertdatabase_idsAt(SchemaIDRef value, int index) {
		insertDomChildAt(Attribute, null, "database-ids", index, value.toString());
	}

	public void insertdatabase_idsAt(String value, int index) throws Exception {
		insertdatabase_idsAt(new SchemaIDRef(value), index);
	}

	public void replacedatabase_idsAt(SchemaIDRef value, int index) {
		replaceDomChildAt(Attribute, null, "database-ids", index, value.toString());
	}

	public void replacedatabase_idsAt(String value, int index) throws Exception {
		replacedatabase_idsAt(new SchemaIDRef(value), index);
	}

	public static int getpositionMinCount() {
		return 1;
	}

	public static int getpositionMaxCount() {
		return 1;
	}

	public int getpositionCount() {
		return getDomChildCount(Element, null, "position");
	}

	public boolean hasposition() {
		return hasDomChild(Element, null, "position");
	}

	public positionType newposition() {
		return new positionType(domNode.getOwnerDocument().createElementNS(null, "position"));
	}

	public positionType getpositionAt(int index) throws Exception {
		return new positionType(dereference(getDomChildAt(Element, null, "position", index)));
	}

	public org.w3c.dom.Node getStartingpositionCursor() throws Exception {
		return getDomFirstChild(Element, null, "position" );
	}

	public org.w3c.dom.Node getAdvancedpositionCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, null, "position", curNode );
	}

	public positionType getpositionValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new positionType( dereference(curNode) );
	}

	public positionType getposition() throws Exception 
 {
		return getpositionAt(0);
	}

	public void removepositionAt(int index) {
		removeDomChildAt(Element, null, "position", index);
	}

	public void removeposition() {
		while (hasposition())
			removepositionAt(0);
	}

	public void addposition(positionType value) {
		appendDomElement(null, "position", value);	
	}

	public void insertpositionAt(positionType value, int index) {
		insertDomElementAt(null, "position", index, value);
	}

	public void replacepositionAt(positionType value, int index) {
		replaceDomElementAt(null, "position", index, value);
	}

	public static int getbase_typeMinCount() {
		return 0;
	}

	public static int getbase_typeMaxCount() {
		return 1;
	}

	public int getbase_typeCount() {
		return getDomChildCount(Element, null, "base-type");
	}

	public boolean hasbase_type() {
		return hasDomChild(Element, null, "base-type");
	}

	public base_typeType newbase_type() {
		return new base_typeType(domNode.getOwnerDocument().createElementNS(null, "base-type"));
	}

	public base_typeType getbase_typeAt(int index) throws Exception {
		return new base_typeType(dereference(getDomChildAt(Element, null, "base-type", index)));
	}

	public org.w3c.dom.Node getStartingbase_typeCursor() throws Exception {
		return getDomFirstChild(Element, null, "base-type" );
	}

	public org.w3c.dom.Node getAdvancedbase_typeCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, null, "base-type", curNode );
	}

	public base_typeType getbase_typeValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new base_typeType( dereference(curNode) );
	}

	public base_typeType getbase_type() throws Exception 
 {
		return getbase_typeAt(0);
	}

	public void removebase_typeAt(int index) {
		removeDomChildAt(Element, null, "base-type", index);
	}

	public void removebase_type() {
		while (hasbase_type())
			removebase_typeAt(0);
	}

	public void addbase_type(base_typeType value) {
		appendDomElement(null, "base-type", value);	
	}

	public void insertbase_typeAt(base_typeType value, int index) {
		insertDomElementAt(null, "base-type", index, value);
	}

	public void replacebase_typeAt(base_typeType value, int index) {
		replaceDomElementAt(null, "base-type", index, value);
	}

	public static int getalt_locMinCount() {
		return 0;
	}

	public static int getalt_locMaxCount() {
		return 1;
	}

	public int getalt_locCount() {
		return getDomChildCount(Element, null, "alt-loc");
	}

	public boolean hasalt_loc() {
		return hasDomChild(Element, null, "alt-loc");
	}

	public alt_locType newalt_loc() {
		return new alt_locType(domNode.getOwnerDocument().createElementNS(null, "alt-loc"));
	}

	public alt_locType getalt_locAt(int index) throws Exception {
		return new alt_locType(dereference(getDomChildAt(Element, null, "alt-loc", index)));
	}

	public org.w3c.dom.Node getStartingalt_locCursor() throws Exception {
		return getDomFirstChild(Element, null, "alt-loc" );
	}

	public org.w3c.dom.Node getAdvancedalt_locCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, null, "alt-loc", curNode );
	}

	public alt_locType getalt_locValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new alt_locType( dereference(curNode) );
	}

	public alt_locType getalt_loc() throws Exception 
 {
		return getalt_locAt(0);
	}

	public void removealt_locAt(int index) {
		removeDomChildAt(Element, null, "alt-loc", index);
	}

	public void removealt_loc() {
		while (hasalt_loc())
			removealt_locAt(0);
	}

	public void addalt_loc(alt_locType value) {
		appendDomElement(null, "alt-loc", value);	
	}

	public void insertalt_locAt(alt_locType value, int index) {
		insertDomElementAt(null, "alt-loc", index, value);
	}

	public void replacealt_locAt(alt_locType value, int index) {
		replaceDomElementAt(null, "alt-loc", index, value);
	}

	public static int getinsertionMinCount() {
		return 0;
	}

	public static int getinsertionMaxCount() {
		return 1;
	}

	public int getinsertionCount() {
		return getDomChildCount(Element, null, "insertion");
	}

	public boolean hasinsertion() {
		return hasDomChild(Element, null, "insertion");
	}

	public insertionType newinsertion() {
		return new insertionType(domNode.getOwnerDocument().createElementNS(null, "insertion"));
	}

	public insertionType getinsertionAt(int index) throws Exception {
		return new insertionType(dereference(getDomChildAt(Element, null, "insertion", index)));
	}

	public org.w3c.dom.Node getStartinginsertionCursor() throws Exception {
		return getDomFirstChild(Element, null, "insertion" );
	}

	public org.w3c.dom.Node getAdvancedinsertionCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, null, "insertion", curNode );
	}

	public insertionType getinsertionValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new insertionType( dereference(curNode) );
	}

	public insertionType getinsertion() throws Exception 
 {
		return getinsertionAt(0);
	}

	public void removeinsertionAt(int index) {
		removeDomChildAt(Element, null, "insertion", index);
	}

	public void removeinsertion() {
		while (hasinsertion())
			removeinsertionAt(0);
	}

	public void addinsertion(insertionType value) {
		appendDomElement(null, "insertion", value);	
	}

	public void insertinsertionAt(insertionType value, int index) {
		insertDomElementAt(null, "insertion", index, value);
	}

	public void replaceinsertionAt(insertionType value, int index) {
		replaceDomElementAt(null, "insertion", index, value);
	}

	public static int getatomMinCount() {
		return 0;
	}

	public static int getatomMaxCount() {
		return Integer.MAX_VALUE;
	}

	public int getatomCount() {
		return getDomChildCount(Element, null, "atom");
	}

	public boolean hasatom() {
		return hasDomChild(Element, null, "atom");
	}

	public atomType newatom() {
		return new atomType(domNode.getOwnerDocument().createElementNS(null, "atom"));
	}

	public atomType getatomAt(int index) throws Exception {
		return new atomType(dereference(getDomChildAt(Element, null, "atom", index)));
	}

	public org.w3c.dom.Node getStartingatomCursor() throws Exception {
		return getDomFirstChild(Element, null, "atom" );
	}

	public org.w3c.dom.Node getAdvancedatomCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, null, "atom", curNode );
	}

	public atomType getatomValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new atomType( dereference(curNode) );
	}

	public atomType getatom() throws Exception 
 {
		return getatomAt(0);
	}

	public void removeatomAt(int index) {
		removeDomChildAt(Element, null, "atom", index);
	}

	public void removeatom() {
		while (hasatom())
			removeatomAt(0);
	}

	public void addatom(atomType value) {
		appendDomElement(null, "atom", value);	
	}

	public void insertatomAt(atomType value, int index) {
		insertDomElementAt(null, "atom", index, value);
	}

	public void replaceatomAt(atomType value, int index) {
		replaceDomElementAt(null, "atom", index, value);
	}

	private org.w3c.dom.Node dereference(org.w3c.dom.Node node) {
		return node;
	}
}
