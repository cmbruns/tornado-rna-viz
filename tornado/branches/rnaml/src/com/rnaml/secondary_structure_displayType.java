/**
 * secondary_structure_displayType.java
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

public class secondary_structure_displayType extends com.altova.xml.Node {

	public secondary_structure_displayType(secondary_structure_displayType node) {
		super(node);
	}

	public secondary_structure_displayType(org.w3c.dom.Node node) {
		super(node);
	}

	public secondary_structure_displayType(org.w3c.dom.Document doc) {
		super(doc);
	}

	public secondary_structure_displayType(com.altova.xml.Document doc, String namespaceURI, String prefix, String name) {
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
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "ss-base-coord" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "ss-base-coord", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new ss_base_coordType(tmpNode).adjustPrefix();
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

	public static int getss_base_coordMinCount() {
		return 1;
	}

	public static int getss_base_coordMaxCount() {
		return 1;
	}

	public int getss_base_coordCount() {
		return getDomChildCount(Element, null, "ss-base-coord");
	}

	public boolean hasss_base_coord() {
		return hasDomChild(Element, null, "ss-base-coord");
	}

	public ss_base_coordType newss_base_coord() {
		return new ss_base_coordType(domNode.getOwnerDocument().createElementNS(null, "ss-base-coord"));
	}

	public ss_base_coordType getss_base_coordAt(int index) throws Exception {
		return new ss_base_coordType(dereference(getDomChildAt(Element, null, "ss-base-coord", index)));
	}

	public org.w3c.dom.Node getStartingss_base_coordCursor() throws Exception {
		return getDomFirstChild(Element, null, "ss-base-coord" );
	}

	public org.w3c.dom.Node getAdvancedss_base_coordCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, null, "ss-base-coord", curNode );
	}

	public ss_base_coordType getss_base_coordValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new ss_base_coordType( dereference(curNode) );
	}

	public ss_base_coordType getss_base_coord() throws Exception 
 {
		return getss_base_coordAt(0);
	}

	public void removess_base_coordAt(int index) {
		removeDomChildAt(Element, null, "ss-base-coord", index);
	}

	public void removess_base_coord() {
		while (hasss_base_coord())
			removess_base_coordAt(0);
	}

	public void addss_base_coord(ss_base_coordType value) {
		appendDomElement(null, "ss-base-coord", value);	
	}

	public void insertss_base_coordAt(ss_base_coordType value, int index) {
		insertDomElementAt(null, "ss-base-coord", index, value);
	}

	public void replacess_base_coordAt(ss_base_coordType value, int index) {
		replaceDomElementAt(null, "ss-base-coord", index, value);
	}

	private org.w3c.dom.Node dereference(org.w3c.dom.Node node) {
		return node;
	}
}
