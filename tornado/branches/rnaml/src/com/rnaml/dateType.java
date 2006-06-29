/**
 * dateType.java
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

public class dateType extends com.altova.xml.Node {

	public dateType(dateType node) {
		super(node);
	}

	public dateType(org.w3c.dom.Node node) {
		super(node);
	}

	public dateType(org.w3c.dom.Document doc) {
		super(doc);
	}

	public dateType(com.altova.xml.Document doc, String namespaceURI, String prefix, String name) {
		super(doc, namespaceURI, prefix, name);
	}
	
	public void adjustPrefix() {
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "day" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "day", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new dayType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "month" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "month", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new monthType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, null, "year" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, null, "year", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
			new yearType(tmpNode).adjustPrefix();
		}
	}

	public static int getdayMinCount() {
		return 0;
	}

	public static int getdayMaxCount() {
		return 1;
	}

	public int getdayCount() {
		return getDomChildCount(Element, null, "day");
	}

	public boolean hasday() {
		return hasDomChild(Element, null, "day");
	}

	public dayType newday() {
		return new dayType(domNode.getOwnerDocument().createElementNS(null, "day"));
	}

	public dayType getdayAt(int index) throws Exception {
		return new dayType(dereference(getDomChildAt(Element, null, "day", index)));
	}

	public org.w3c.dom.Node getStartingdayCursor() throws Exception {
		return getDomFirstChild(Element, null, "day" );
	}

	public org.w3c.dom.Node getAdvanceddayCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, null, "day", curNode );
	}

	public dayType getdayValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new dayType( dereference(curNode) );
	}

	public dayType getday() throws Exception 
 {
		return getdayAt(0);
	}

	public void removedayAt(int index) {
		removeDomChildAt(Element, null, "day", index);
	}

	public void removeday() {
		while (hasday())
			removedayAt(0);
	}

	public void addday(dayType value) {
		appendDomElement(null, "day", value);	
	}

	public void insertdayAt(dayType value, int index) {
		insertDomElementAt(null, "day", index, value);
	}

	public void replacedayAt(dayType value, int index) {
		replaceDomElementAt(null, "day", index, value);
	}

	public static int getmonthMinCount() {
		return 0;
	}

	public static int getmonthMaxCount() {
		return 1;
	}

	public int getmonthCount() {
		return getDomChildCount(Element, null, "month");
	}

	public boolean hasmonth() {
		return hasDomChild(Element, null, "month");
	}

	public monthType newmonth() {
		return new monthType(domNode.getOwnerDocument().createElementNS(null, "month"));
	}

	public monthType getmonthAt(int index) throws Exception {
		return new monthType(dereference(getDomChildAt(Element, null, "month", index)));
	}

	public org.w3c.dom.Node getStartingmonthCursor() throws Exception {
		return getDomFirstChild(Element, null, "month" );
	}

	public org.w3c.dom.Node getAdvancedmonthCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, null, "month", curNode );
	}

	public monthType getmonthValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new monthType( dereference(curNode) );
	}

	public monthType getmonth() throws Exception 
 {
		return getmonthAt(0);
	}

	public void removemonthAt(int index) {
		removeDomChildAt(Element, null, "month", index);
	}

	public void removemonth() {
		while (hasmonth())
			removemonthAt(0);
	}

	public void addmonth(monthType value) {
		appendDomElement(null, "month", value);	
	}

	public void insertmonthAt(monthType value, int index) {
		insertDomElementAt(null, "month", index, value);
	}

	public void replacemonthAt(monthType value, int index) {
		replaceDomElementAt(null, "month", index, value);
	}

	public static int getyearMinCount() {
		return 1;
	}

	public static int getyearMaxCount() {
		return 1;
	}

	public int getyearCount() {
		return getDomChildCount(Element, null, "year");
	}

	public boolean hasyear() {
		return hasDomChild(Element, null, "year");
	}

	public yearType newyear() {
		return new yearType(domNode.getOwnerDocument().createElementNS(null, "year"));
	}

	public yearType getyearAt(int index) throws Exception {
		return new yearType(dereference(getDomChildAt(Element, null, "year", index)));
	}

	public org.w3c.dom.Node getStartingyearCursor() throws Exception {
		return getDomFirstChild(Element, null, "year" );
	}

	public org.w3c.dom.Node getAdvancedyearCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, null, "year", curNode );
	}

	public yearType getyearValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.altova.xml.XmlException("Out of range");
		else
			return new yearType( dereference(curNode) );
	}

	public yearType getyear() throws Exception 
 {
		return getyearAt(0);
	}

	public void removeyearAt(int index) {
		removeDomChildAt(Element, null, "year", index);
	}

	public void removeyear() {
		while (hasyear())
			removeyearAt(0);
	}

	public void addyear(yearType value) {
		appendDomElement(null, "year", value);	
	}

	public void insertyearAt(yearType value, int index) {
		insertDomElementAt(null, "year", index, value);
	}

	public void replaceyearAt(yearType value, int index) {
		replaceDomElementAt(null, "year", index, value);
	}

	private org.w3c.dom.Node dereference(org.w3c.dom.Node node) {
		return node;
	}
}