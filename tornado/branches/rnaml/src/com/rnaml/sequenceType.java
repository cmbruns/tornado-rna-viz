/**
 * sequenceType.java
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

public class sequenceType extends com.altova.xml.Node {

	public sequenceType(sequenceType node) {
		super(node);
	}

	public sequenceType(org.w3c.dom.Node node) {
		super(node);
	}

	public sequenceType(org.w3c.dom.Document doc) {
		super(doc);
	}

	public sequenceType(com.altova.xml.Document doc, String namespaceURI, String prefix, String name) {
		super(doc, namespaceURI, prefix, name);
	}
	
	public void adjustPrefix() {
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Attribute, null, "length" );
				tmpNode != null;
				tmpNode = getDomNextChild( Attribute, null, "length", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Attribute, null, "circular" );
				tmpNode != null;
				tmpNode = getDomNextChild( Attribute, null, "circular", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
		}
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
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "numbering-system" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "numbering-system", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new numbering_systemType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "numbering-table" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "numbering-table", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new numbering_tableType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "seq-data" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "seq-data", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new seq_dataType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "seq-annotation" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "seq-annotation", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new seq_annotationType(tmpNode).adjustPrefix();
		}
	}

	public static int getlengthMinCount() {
		return 0;
	}

	public static int getlengthMaxCount() {
		return 1;
	}

	public int getlengthCount() {
		return getDomChildCount(Attribute, null, "length");
	}

	public boolean haslength() {
		return hasDomChild(Attribute, null, "length");
	}

	public SchemaString newlength() {
		return new SchemaString();
	}

	public SchemaString getlengthAt(int index) throws Exception {
		return new SchemaString(getDomNodeValue(dereference(getDomChildAt(Attribute, null, "length", index))));
	}

	public org.w3c.dom.Node getStartinglengthCursor() throws Exception {
		return getDomFirstChild(Attribute, null, "length" );
	}

	public org.w3c.dom.Node getAdvancedlengthCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Attribute, null, "length", curNode );
	}

	public SchemaString getlengthValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new SchemaString(getDomNodeValue( dereference(curNode) ) );
	}

	public SchemaString getlength() throws Exception 
 {
		return getlengthAt(0);
	}

	public void removelengthAt(int index) {
		removeDomChildAt(Attribute, null, "length", index);
	}

	public void removelength() {
		while (haslength())
			removelengthAt(0);
	}

	public void addlength(SchemaString value) {
		if( value.isNull() == false )
		{
			appendDomChild(Attribute, null, "length", value.toString());
		}
	}

	public void addlength(String value) throws Exception {
		addlength(new SchemaString(value));
	}

	public void insertlengthAt(SchemaString value, int index) {
		insertDomChildAt(Attribute, null, "length", index, value.toString());
	}

	public void insertlengthAt(String value, int index) throws Exception {
		insertlengthAt(new SchemaString(value), index);
	}

	public void replacelengthAt(SchemaString value, int index) {
		replaceDomChildAt(Attribute, null, "length", index, value.toString());
	}

	public void replacelengthAt(String value, int index) throws Exception {
		replacelengthAt(new SchemaString(value), index);
	}

	public static int getcircularMinCount() {
		return 0;
	}

	public static int getcircularMaxCount() {
		return 1;
	}

	public int getcircularCount() {
		return getDomChildCount(Attribute, null, "circular");
	}

	public boolean hascircular() {
		return hasDomChild(Attribute, null, "circular");
	}

	public EnumerationType4 newcircular() {
		return new EnumerationType4();
	}

	public EnumerationType4 getcircularAt(int index) throws Exception {
		return new EnumerationType4(getDomNodeValue(dereference(getDomChildAt(Attribute, null, "circular", index))));
	}

	public org.w3c.dom.Node getStartingcircularCursor() throws Exception {
		return getDomFirstChild(Attribute, null, "circular" );
	}

	public org.w3c.dom.Node getAdvancedcircularCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Attribute, null, "circular", curNode );
	}

	public EnumerationType4 getcircularValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new EnumerationType4(getDomNodeValue( dereference(curNode) ) );
	}

	public EnumerationType4 getcircular() throws Exception 
 {
		return getcircularAt(0);
	}

	public void removecircularAt(int index) {
		removeDomChildAt(Attribute, null, "circular", index);
	}

	public void removecircular() {
		while (hascircular())
			removecircularAt(0);
	}

	public void addcircular(EnumerationType4 value) {
		if( value.isNull() == false )
		{
			appendDomChild(Attribute, null, "circular", value.toString());
		}
	}

	public void addcircular(String value) throws Exception {
		addcircular(new EnumerationType4(value));
	}

	public void insertcircularAt(EnumerationType4 value, int index) {
		insertDomChildAt(Attribute, null, "circular", index, value.toString());
	}

	public void insertcircularAt(String value, int index) throws Exception {
		insertcircularAt(new EnumerationType4(value), index);
	}

	public void replacecircularAt(EnumerationType4 value, int index) {
		replaceDomChildAt(Attribute, null, "circular", index, value.toString());
	}

	public void replacecircularAt(String value, int index) throws Exception {
		replacecircularAt(new EnumerationType4(value), index);
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

	public static int getnumbering_systemMinCount() {
		return 0;
	}

	public static int getnumbering_systemMaxCount() {
		return Integer.MAX_VALUE;
	}

	public int getnumbering_systemCount() {
		return getDomChildCount(Element, null, "numbering-system");
	}

	public boolean hasnumbering_system() {
		return hasDomChild(Element, null, "numbering-system");
	}

	public numbering_systemType newnumbering_system() {
		return new numbering_systemType(domNode.getOwnerDocument().createElementNS(null, "numbering-system"));
	}

	public numbering_systemType getnumbering_systemAt(int index) throws Exception {
		return new numbering_systemType(dereference(getDomChildAt(Element, null, "numbering-system", index)));
	}

	public org.w3c.dom.Node getStartingnumbering_systemCursor() throws Exception {
		return getDomFirstChild(Element, null, "numbering-system" );
	}

	public org.w3c.dom.Node getAdvancednumbering_systemCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, null, "numbering-system", curNode );
	}

	public numbering_systemType getnumbering_systemValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new numbering_systemType( dereference(curNode) );
	}

	public numbering_systemType getnumbering_system() throws Exception 
 {
		return getnumbering_systemAt(0);
	}

	public void removenumbering_systemAt(int index) {
		removeDomChildAt(Element, null, "numbering-system", index);
	}

	public void removenumbering_system() {
		while (hasnumbering_system())
			removenumbering_systemAt(0);
	}

	public void addnumbering_system(numbering_systemType value) {
		appendDomElement(null, "numbering-system", value);	
	}

	public void insertnumbering_systemAt(numbering_systemType value, int index) {
		insertDomElementAt(null, "numbering-system", index, value);
	}

	public void replacenumbering_systemAt(numbering_systemType value, int index) {
		replaceDomElementAt(null, "numbering-system", index, value);
	}

	public static int getnumbering_tableMinCount() {
		return 0;
	}

	public static int getnumbering_tableMaxCount() {
		return Integer.MAX_VALUE;
	}

	public int getnumbering_tableCount() {
		return getDomChildCount(Element, null, "numbering-table");
	}

	public boolean hasnumbering_table() {
		return hasDomChild(Element, null, "numbering-table");
	}

	public numbering_tableType newnumbering_table() {
		return new numbering_tableType(domNode.getOwnerDocument().createElementNS(null, "numbering-table"));
	}

	public numbering_tableType getnumbering_tableAt(int index) throws Exception {
		return new numbering_tableType(dereference(getDomChildAt(Element, null, "numbering-table", index)));
	}

	public org.w3c.dom.Node getStartingnumbering_tableCursor() throws Exception {
		return getDomFirstChild(Element, null, "numbering-table" );
	}

	public org.w3c.dom.Node getAdvancednumbering_tableCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, null, "numbering-table", curNode );
	}

	public numbering_tableType getnumbering_tableValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new numbering_tableType( dereference(curNode) );
	}

	public numbering_tableType getnumbering_table() throws Exception 
 {
		return getnumbering_tableAt(0);
	}

	public void removenumbering_tableAt(int index) {
		removeDomChildAt(Element, null, "numbering-table", index);
	}

	public void removenumbering_table() {
		while (hasnumbering_table())
			removenumbering_tableAt(0);
	}

	public void addnumbering_table(numbering_tableType value) {
		appendDomElement(null, "numbering-table", value);	
	}

	public void insertnumbering_tableAt(numbering_tableType value, int index) {
		insertDomElementAt(null, "numbering-table", index, value);
	}

	public void replacenumbering_tableAt(numbering_tableType value, int index) {
		replaceDomElementAt(null, "numbering-table", index, value);
	}

	public static int getseq_dataMinCount() {
		return 0;
	}

	public static int getseq_dataMaxCount() {
		return 1;
	}

	public int getseq_dataCount() {
		return getDomChildCount(Element, null, "seq-data");
	}

	public boolean hasseq_data() {
		return hasDomChild(Element, null, "seq-data");
	}

	public seq_dataType newseq_data() {
		return new seq_dataType(domNode.getOwnerDocument().createElementNS(null, "seq-data"));
	}

	public seq_dataType getseq_dataAt(int index) throws Exception {
		return new seq_dataType(dereference(getDomChildAt(Element, null, "seq-data", index)));
	}

	public org.w3c.dom.Node getStartingseq_dataCursor() throws Exception {
		return getDomFirstChild(Element, null, "seq-data" );
	}

	public org.w3c.dom.Node getAdvancedseq_dataCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, null, "seq-data", curNode );
	}

	public seq_dataType getseq_dataValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new seq_dataType( dereference(curNode) );
	}

	public seq_dataType getseq_data() throws Exception 
 {
		return getseq_dataAt(0);
	}

	public void removeseq_dataAt(int index) {
		removeDomChildAt(Element, null, "seq-data", index);
	}

	public void removeseq_data() {
		while (hasseq_data())
			removeseq_dataAt(0);
	}

	public void addseq_data(seq_dataType value) {
		appendDomElement(null, "seq-data", value);	
	}

	public void insertseq_dataAt(seq_dataType value, int index) {
		insertDomElementAt(null, "seq-data", index, value);
	}

	public void replaceseq_dataAt(seq_dataType value, int index) {
		replaceDomElementAt(null, "seq-data", index, value);
	}

	public static int getseq_annotationMinCount() {
		return 0;
	}

	public static int getseq_annotationMaxCount() {
		return 1;
	}

	public int getseq_annotationCount() {
		return getDomChildCount(Element, null, "seq-annotation");
	}

	public boolean hasseq_annotation() {
		return hasDomChild(Element, null, "seq-annotation");
	}

	public seq_annotationType newseq_annotation() {
		return new seq_annotationType(domNode.getOwnerDocument().createElementNS(null, "seq-annotation"));
	}

	public seq_annotationType getseq_annotationAt(int index) throws Exception {
		return new seq_annotationType(dereference(getDomChildAt(Element, null, "seq-annotation", index)));
	}

	public org.w3c.dom.Node getStartingseq_annotationCursor() throws Exception {
		return getDomFirstChild(Element, null, "seq-annotation" );
	}

	public org.w3c.dom.Node getAdvancedseq_annotationCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, null, "seq-annotation", curNode );
	}

	public seq_annotationType getseq_annotationValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new seq_annotationType( dereference(curNode) );
	}

	public seq_annotationType getseq_annotation() throws Exception 
 {
		return getseq_annotationAt(0);
	}

	public void removeseq_annotationAt(int index) {
		removeDomChildAt(Element, null, "seq-annotation", index);
	}

	public void removeseq_annotation() {
		while (hasseq_annotation())
			removeseq_annotationAt(0);
	}

	public void addseq_annotation(seq_annotationType value) {
		appendDomElement(null, "seq-annotation", value);	
	}

	public void insertseq_annotationAt(seq_annotationType value, int index) {
		insertDomElementAt(null, "seq-annotation", index, value);
	}

	public void replaceseq_annotationAt(seq_annotationType value, int index) {
		replaceDomElementAt(null, "seq-annotation", index, value);
	}

	private org.w3c.dom.Node dereference(org.w3c.dom.Node node) {
		return node;
	}
}