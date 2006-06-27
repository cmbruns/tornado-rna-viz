/**
 * seq_dataType.java
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

public class seq_dataType extends com.altova.xml.Node {

	public seq_dataType(seq_dataType node) {
		super(node);
	}

	public seq_dataType(org.w3c.dom.Node node) {
		super(node);
	}

	public seq_dataType(org.w3c.dom.Document doc) {
		super(doc);
	}

	public seq_dataType(com.altova.xml.Document doc, String namespaceURI, String prefix, String name) {
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
