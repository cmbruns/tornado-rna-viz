/**
 * SchemaDate.java
 *
 * This file was generated by XMLSpy 2006r3 Enterprise Edition.
 *
 * YOU SHOULD NOT MODIFY THIS FILE, BECAUSE IT WILL BE
 * OVERWRITTEN WHEN YOU RE-RUN CODE GENERATION.
 *
 * Refer to the XMLSpy Documentation for further details.
 * http://www.altova.com/xmlspy
 */


package com.altova.types;

import java.lang.*;
import java.util.Calendar;
import java.text.ParseException;


public class SchemaDate extends SchemaCalendarBase {

  // construction
  public SchemaDate() {
    setEmpty();
  }

  public SchemaDate(SchemaDate newvalue) {
    year = newvalue.year;
    month = newvalue.month;
    day = newvalue.day;
    hour = newvalue.hour;
    minute = newvalue.minute;
    second = newvalue.second;
    partsecond = newvalue.partsecond;
    hasTZ = newvalue.hasTZ;
    offsetTZ = newvalue.offsetTZ;
    isempty = newvalue.isempty;
  }
  
  public SchemaDate (SchemaDateTime rhs) {
    year = rhs.year;
    month = rhs.month;
    day = rhs.day;
    hour = rhs.hour; 
    minute = rhs.minute;
    second = rhs.second;
    partsecond = rhs.partsecond;
    hasTZ = rhs.hasTZ;
    offsetTZ = rhs.offsetTZ;
    isempty = rhs.isempty;
  }

  public SchemaDate(int newyear, int newmonth, int newday) {
    setInternalValues( newyear, newmonth, newday, 0, 0, 0, 0.0, SchemaCalendarBase.TZ_MISSING, 0);
    isempty = false;
  }

  public SchemaDate(Calendar newvalue) {
    setValue( newvalue );
  }

  public SchemaDate(String newvalue) {
    parse(newvalue);
  }

  public SchemaDate(SchemaType newvalue) {
    assign( newvalue );
  }

  public SchemaDate(SchemaTypeCalendar newvalue) {
    assign( (SchemaType)newvalue );
  }

  // getValue, setValue
  public int getYear() {
    return year;
  }

  public int getMonth() {
    return month;
  }

  public int getDay() {
    return day;
  }

  public Calendar getValue() {
    Calendar cal = Calendar.getInstance();
    cal.set( year, month-1, day );
    return cal;
  }

  public void setYear(int newyear) {
    year = newyear;
    isempty = false;
  }

  public void setMonth(int newmonth) {
    month = newmonth;
    isempty = false;
  }

  public void setDay(int newday) {
    day = newday;
    isempty = false;
  }

  public void setValue(Calendar newvalue) {
    if(newvalue == null) 
      setEmpty();
    else {
      setInternalValues( newvalue.get( Calendar.YEAR ), newvalue.get( Calendar.MONTH ) + 1, newvalue.get( Calendar.DAY_OF_MONTH ), 0, 0, 0, 0.0, SchemaCalendarBase.TZ_MISSING, 0);
      isempty = false;
    }
  }

  public void parse(String newvalue) throws StringParseException {
    if( newvalue == null ||newvalue.length() == 0)
      setEmpty();
    else
      parseDate(newvalue);
  }

  public void assign(SchemaType newvalue) {
    if( newvalue == null || newvalue.isNull() || newvalue.isEmpty())
      setEmpty();
    else {
      isempty = false;
      if( newvalue instanceof SchemaDate ) {
        year = ((SchemaDate)newvalue).year;
        month = ((SchemaDate)newvalue).month;
        day = ((SchemaDate)newvalue).day;
      }
      else if( newvalue instanceof SchemaDateTime ) {
        year = ((SchemaDateTime)newvalue).year;
        month = ((SchemaDateTime)newvalue).month;
        day = ((SchemaDateTime)newvalue).day;
      }
      else if( newvalue instanceof SchemaString ) {
        parse( newvalue.toString() );
      }
      else
        throw new TypesIncompatibleException( newvalue, this );
    }
  }

  // further
  public Object clone() {
    return new SchemaDate( this );
  }

  public String toString() {
    if( isEmpty()  )
      return "";
    return toDateString();
  }


  public static SchemaDate now() {
    return new SchemaDate(Calendar.getInstance());
  }

  // ---------- interface SchemaTypeCalendar ----------
  public int calendarType() {
    return CALENDAR_VALUE_DATE;
  }

  public SchemaDateTime dateTimeValue() {
    return new SchemaDateTime( this );
  }

  public SchemaDate dateValue() {
    return new SchemaDate( this );
  }

  public SchemaTime timeValue() {
    throw new TypesIncompatibleException(this, new SchemaTime( "2003-07-28" ) );
  }
}
