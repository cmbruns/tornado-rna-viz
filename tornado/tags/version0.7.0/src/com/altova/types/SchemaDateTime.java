/**
 * SchemaDateTime.java
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
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.text.ParseException;


public class SchemaDateTime extends SchemaCalendarBase {

  // construction
  public SchemaDateTime() {
    setEmpty();
  }

  public SchemaDateTime(SchemaDateTime newvalue) {
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

  public SchemaDateTime(int newyear, int newmonth, int newday, int newhour, int newminute, int newsecond, double newpartsecond, int newoffsetTZ) {
    setInternalValues( newyear, newmonth, newday, newhour, newminute, newsecond, newpartsecond, SchemaCalendarBase.TZ_OFFSET, newoffsetTZ);
    isempty = false;
  }

  public SchemaDateTime(int newyear, int newmonth, int newday, int newhour, int newminute, int newsecond, double newpartsecond) {
    setInternalValues( newyear, newmonth, newday, newhour, newminute, newsecond, newpartsecond, SchemaCalendarBase.TZ_MISSING, 0);
    isempty = false;
  }

  public SchemaDateTime(Calendar newvalue) {
    setValue( newvalue );
  }

  public SchemaDateTime(String newvalue) {
    parse(newvalue);
  }

  public SchemaDateTime(SchemaType newvalue) {
    assign( newvalue );
  }

  public SchemaDateTime(SchemaTypeCalendar newvalue) {
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

  public int getHour() {
    return hour;
  }

  public int getMinute() {
    return minute;
  }

  public int getSecond() {
    return second;
  }

  public double getPartSecond() {
    return partsecond;
  }

  public int getMillisecond() {
    return (int)java.lang.Math.round(partsecond*1000);
  }

  public int hasTimezone() {
    return hasTZ;
  }

  public int getTimezoneOffset() {
    if( hasTZ != TZ_OFFSET )
      return 0;
    return offsetTZ;
  }

  public Calendar getValue() {
    Calendar cal = Calendar.getInstance();
    cal.set( year, month-1, day, hour, minute, second);
    cal.set( Calendar.MILLISECOND, getMillisecond() );
    hasTZ = TZ_OFFSET; // necessary, because Calendar object always has timezone.
	cal.set(Calendar.ZONE_OFFSET, offsetTZ * 60000);
    //cal.setTimeZone( (TimeZone)new SimpleTimeZone(offsetTZ * 60000, "") );
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

  public void setHour(int newhour) {
    hour = newhour;
    isempty = false;
  }

  public void setMinute(int newminute) {
    minute = newminute;
    isempty = false;
  }

  public void setSecond(int newsecond) {
    second = newsecond;
    isempty = false;
  }

  public void setPartSecond(double newpartsecond) {
    partsecond = newpartsecond;
    isempty = false;
  }

  public void setMillisecond(int newmillisecond) {
    partsecond = (double)newmillisecond / 1000;
    isempty = false;
  }

  public void setTimezone(int newhasTZ, int newoffsetTZminutes ) {
    hasTZ = newhasTZ;
    offsetTZ = newoffsetTZminutes;
    isempty = false;
  }

  public void setValue(Calendar newvalue) {
    if( newvalue == null ) {
      setEmpty();
      return;
    }
    year = newvalue.get( Calendar.YEAR );
    month = newvalue.get( Calendar.MONTH ) + 1;
    day = newvalue.get( Calendar.DAY_OF_MONTH );
    hour = newvalue.get( Calendar.HOUR_OF_DAY );
    minute = newvalue.get( Calendar.MINUTE );
    second = newvalue.get( Calendar.SECOND );
    setMillisecond( newvalue.get( Calendar.MILLISECOND ) );
    hasTZ = TZ_MISSING;
    isempty = false;
  }

  public void parse(String newvalue) {
    if( newvalue == null  || newvalue.length() == 0)
      setEmpty();
    else {
      if (newvalue.length() < 10)
        throw new StringParseException(newvalue + " cannot be converted to a dateTime value", 0);
      try {
        int nStart = newvalue.indexOf("T");
        if (nStart == -1) {
          nStart = newvalue.length();
        }
        parseDate( newvalue.substring(0,nStart) );
        if( nStart < newvalue.length() )
          parseTime( newvalue.substring(nStart+1, newvalue.length()));
        else {
          hour = 0;
          minute = 0;
          second = 0;
          partsecond = 0.0;
          hasTZ = TZ_MISSING;
          offsetTZ = 0;
        }
      }
      catch (NumberFormatException e) {
        throw new StringParseException(newvalue + " cannot be converted to a dateTime value", 2);
      }
      isempty = false;
    }
  }

  public void assign(SchemaType newvalue) {
    if( newvalue == null || newvalue.isNull() || newvalue.isEmpty())
      setEmpty();
    else if( newvalue instanceof SchemaDateTime ) {
      setInternalValues( ((SchemaDateTime)newvalue).year, ((SchemaDateTime)newvalue).month, ((SchemaDateTime)newvalue).day, ((SchemaDateTime)newvalue).hour, ((SchemaDateTime)newvalue).minute, ((SchemaDateTime)newvalue).second, ((SchemaDateTime)newvalue).partsecond, ((SchemaDateTime)newvalue).hasTZ, ((SchemaDateTime)newvalue).offsetTZ);
      isempty = false;
    }
    else if( newvalue instanceof SchemaDate ) {
      setInternalValues( ((SchemaDate)newvalue).year, ((SchemaDate)newvalue).month, ((SchemaDate)newvalue).day, 0, 0, 0, 0.0, SchemaCalendarBase.TZ_MISSING, 0);
      isempty = false;
    }
    else if( newvalue instanceof SchemaString )
      parse( newvalue.toString() );
    else
      throw new TypesIncompatibleException( newvalue, this );
  }

  // further
  public Object clone() {
    return new SchemaDateTime( this );
  }

  public String toString() {
    if( isempty)
      return "";
    StringBuffer s = new StringBuffer();
    s.append( toDateString() );
    s.append("T");
    s.append( toTimeString() );
    return s.toString();
  }

  public static SchemaDateTime now() {
    return new SchemaDateTime(Calendar.getInstance());
  }

  // ---------- interface SchemaTypeCalendar ----------
  public int calendarType() {
    return CALENDAR_VALUE_DATETIME;
  }

  public SchemaDateTime dateTimeValue() {
    return new SchemaDateTime( this );
  }

  public SchemaDate dateValue() {
    return new SchemaDate( this );
  }

  public SchemaTime timeValue() {
      return new SchemaTime( this  ); // 	result.hasTZ = hasTZ;
  }
}
