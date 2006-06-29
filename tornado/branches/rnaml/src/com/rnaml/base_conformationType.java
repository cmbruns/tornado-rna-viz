/**
 * base_conformationType.java
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

public class base_conformationType extends com.altova.xml.Node {

	public base_conformationType(base_conformationType node) {
		super(node);
	}

	public base_conformationType(org.w3c.dom.Node node) {
		super(node);
	}

	public base_conformationType(org.w3c.dom.Document doc) {
		super(doc);
	}

	public base_conformationType(com.altova.xml.Document doc, String namespaceURI, String prefix, String name) {
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
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "base-id" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "base-id", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new base_idType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "pucker" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "pucker", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new puckerType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "glycosyl" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "glycosyl", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new glycosylType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "base-torsion-angles" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "base-torsion-angles", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new base_torsion_anglesType(tmpNode).adjustPrefix();
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

	public static int getpuckerMinCount() {
		return 0;
	}

	public static int getpuckerMaxCount() {
		return 1;
	}

	public int getpuckerCount() {
		return getDomChildCount(Element, null, "pucker");
	}

	public boolean haspucker() {
		return hasDomChild(Element, null, "pucker");
	}

	public puckerType newpucker() {
		return new puckerType(domNode.getOwnerDocument().createElementNS(null, "pucker"));
	}

	public puckerType getpuckerAt(int index) throws Exception {
		return new puckerType(dereference(getDomChildAt(Element, null, "pucker", index)));
	}

	public org.w3c.dom.Node getStartingpuckerCursor() throws Exception {
		return getDomFirstChild(Element, null, "pucker" );
	}

	public org.w3c.dom.Node getAdvancedpuckerCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, null, "pucker", curNode );
	}

	public puckerType getpuckerValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new puckerType( dereference(curNode) );
	}

	public puckerType getpucker() throws Exception 
 {
		return getpuckerAt(0);
	}

	public void removepuckerAt(int index) {
		removeDomChildAt(Element, null, "pucker", index);
	}

	public void removepucker() {
		while (haspucker())
			removepuckerAt(0);
	}

	public void addpucker(puckerType value) {
		appendDomElement(null, "pucker", value);	
	}

	public void insertpuckerAt(puckerType value, int index) {
		insertDomElementAt(null, "pucker", index, value);
	}

	public void replacepuckerAt(puckerType value, int index) {
		replaceDomElementAt(null, "pucker", index, value);
	}

	public static int getglycosylMinCount() {
		return 0;
	}

	public static int getglycosylMaxCount() {
		return 1;
	}

	public int getglycosylCount() {
		return getDomChildCount(Element, null, "glycosyl");
	}

	public boolean hasglycosyl() {
		return hasDomChild(Element, null, "glycosyl");
	}

	public glycosylType newglycosyl() {
		return new glycosylType(domNode.getOwnerDocument().createElementNS(null, "glycosyl"));
	}

	public glycosylType getglycosylAt(int index) throws Exception {
		return new glycosylType(dereference(getDomChildAt(Element, null, "glycosyl", index)));
	}

	public org.w3c.dom.Node getStartingglycosylCursor() throws Exception {
		return getDomFirstChild(Element, null, "glycosyl" );
	}

	public org.w3c.dom.Node getAdvancedglycosylCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, null, "glycosyl", curNode );
	}

	public glycosylType getglycosylValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new glycosylType( dereference(curNode) );
	}

	public glycosylType getglycosyl() throws Exception 
 {
		return getglycosylAt(0);
	}

	public void removeglycosylAt(int index) {
		removeDomChildAt(Element, null, "glycosyl", index);
	}

	public void removeglycosyl() {
		while (hasglycosyl())
			removeglycosylAt(0);
	}

	public void addglycosyl(glycosylType value) {
		appendDomElement(null, "glycosyl", value);	
	}

	public void insertglycosylAt(glycosylType value, int index) {
		insertDomElementAt(null, "glycosyl", index, value);
	}

	public void replaceglycosylAt(glycosylType value, int index) {
		replaceDomElementAt(null, "glycosyl", index, value);
	}

	public static int getbase_torsion_anglesMinCount() {
		return 0;
	}

	public static int getbase_torsion_anglesMaxCount() {
		return 1;
	}

	public int getbase_torsion_anglesCount() {
		return getDomChildCount(Element, null, "base-torsion-angles");
	}

	public boolean hasbase_torsion_angles() {
		return hasDomChild(Element, null, "base-torsion-angles");
	}

	public base_torsion_anglesType newbase_torsion_angles() {
		return new base_torsion_anglesType(domNode.getOwnerDocument().createElementNS(null, "base-torsion-angles"));
	}

	public base_torsion_anglesType getbase_torsion_anglesAt(int index) throws Exception {
		return new base_torsion_anglesType(dereference(getDomChildAt(Element, null, "base-torsion-angles", index)));
	}

	public org.w3c.dom.Node getStartingbase_torsion_anglesCursor() throws Exception {
		return getDomFirstChild(Element, null, "base-torsion-angles" );
	}

	public org.w3c.dom.Node getAdvancedbase_torsion_anglesCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, null, "base-torsion-angles", curNode );
	}

	public base_torsion_anglesType getbase_torsion_anglesValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new base_torsion_anglesType( dereference(curNode) );
	}

	public base_torsion_anglesType getbase_torsion_angles() throws Exception 
 {
		return getbase_torsion_anglesAt(0);
	}

	public void removebase_torsion_anglesAt(int index) {
		removeDomChildAt(Element, null, "base-torsion-angles", index);
	}

	public void removebase_torsion_angles() {
		while (hasbase_torsion_angles())
			removebase_torsion_anglesAt(0);
	}

	public void addbase_torsion_angles(base_torsion_anglesType value) {
		appendDomElement(null, "base-torsion-angles", value);	
	}

	public void insertbase_torsion_anglesAt(base_torsion_anglesType value, int index) {
		insertDomElementAt(null, "base-torsion-angles", index, value);
	}

	public void replacebase_torsion_anglesAt(base_torsion_anglesType value, int index) {
		replaceDomElementAt(null, "base-torsion-angles", index, value);
	}

	private org.w3c.dom.Node dereference(org.w3c.dom.Node node) {
		return node;
	}
}