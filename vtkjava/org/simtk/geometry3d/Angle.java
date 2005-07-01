/*
 * Created on Jun 17, 2005
 *
 */
package org.simtk.geometry3d;

import org.simtk.util.*;

public class Angle extends Number {

    static final long serialVersionUID = 1L;
    static public enum Units{DEGREES, RADIANS, GRADS, REVS};
    static public enum Range{SIGNED, UNSIGNED, UNLIMITED};

    static double TWO_PI = 2.0 * Math.PI;

    static double RADIANS_PER_DEGREE = Math.PI / 180.0;
    static double DEGREES_PER_RADIAN = 180.0 / Math.PI;

    static double RADIANS_PER_GRAD = Math.PI / 200.0;
    static double GRADS_PER_RADIAN = 200.0 / Math.PI;

    static double RADIANS_PER_REV = TWO_PI;
    static double REVS_PER_RADIAN = 1.0 / TWO_PI;

    private static Range defaultRange = Range.UNSIGNED;
    private static Units defaultUnits = Units.RADIANS;

    private double radianAngle = 0;
    private Range angleRange = defaultRange;
    private Units angleUnits = defaultUnits;

    private void initialize(double angle, Angle.Units u, Angle.Range r) {
        angleRange = r;
        angleUnits = u;
        if (angleUnits == Angle.Units.DEGREES)
            radianAngle = angle * RADIANS_PER_DEGREE;
        else if (angleUnits == Angle.Units.RADIANS)
            radianAngle = angle;
        else if (angleUnits == Angle.Units.GRADS)
            radianAngle = angle * RADIANS_PER_GRAD;
        else if (angleUnits == Angle.Units.REVS)
            radianAngle = angle * RADIANS_PER_REV;
        else radianAngle = angle; // should not get here...

        restrictToRange();
    }
    private void restrictToRange() {
        radianAngle = restrictToRange(radianAngle, angleRange);
    }
    static private double restrictToRange(double radians, Range range) {
        double minAngle = 0.0;
        double maxAngle = TWO_PI;

        double angle = radians;
        
        switch(range) {
        case SIGNED:
            minAngle = -Math.PI;
            maxAngle = Math.PI;
            break;
        case UNSIGNED:
            minAngle = 0.0;
            maxAngle = TWO_PI;
            break;
        case UNLIMITED:
        default:
            return angle;
        }
        
        // Within range? No adjustment
        if ( (angle >= minAngle) && (angle < maxAngle) ) return angle;

        // Above upper limit, subtract balance
        else if (angle >= maxAngle) {
            angle -= (TWO_PI * Math.floor((angle - maxAngle)/TWO_PI + 1.0));
            return angle;
        }
        
        // Below lower limit, add
        else if (angle < minAngle) {
            angle += (TWO_PI * Math.floor((minAngle - angle)/TWO_PI + 1.0));
            return angle;
        }
        
        else throw new ProgrammerLogicProblem("Unexpected angle value = " + angle);
    }
    
    public Angle(double angle, Angle.Units units, Angle.Range range) {
        initialize(angle, units, range);
    }
    public Angle(double angle, Angle.Units units) {initialize(angle, units, defaultRange);}
    public Angle(double angle) {initialize(angle, defaultUnits, defaultRange);}
    public Angle() {initialize(Double.NaN, defaultUnits, defaultRange);}
    
    public double cos() {return Math.cos(radianAngle);}
    public double sin() {return Math.sin(radianAngle);}
    public double tan() {return Math.tan(radianAngle);}
    
    public double getValue(Units u) {
        if (u == Units.RADIANS) return radianAngle;
        else if (u == Units.DEGREES) return radianAngle * DEGREES_PER_RADIAN;
        else if (u == Units.GRADS) return radianAngle * GRADS_PER_RADIAN;
        else if (u == Units.REVS) return radianAngle * REVS_PER_RADIAN;
        else return radianAngle;
    }
    public void setValue(double a, Units u) {initialize(a, u, angleRange);}
    public double getValue() {return getValue(angleUnits);}
    public void setValue(double a) {initialize(a, angleUnits, angleRange);}
    
    public void setUnits(Units u) {angleUnits = u;}
    public Units getUnits() {return angleUnits;}

    public void setRange(Range u) {
        angleRange = u;
        restrictToRange();
    }
    public Range getRange() {return angleRange;}
    
    public Angle plus(Angle a2) {
        return new Angle(getValue(angleUnits) + a2.getValue(angleUnits), angleUnits, angleRange);
    }
    public Angle minus(Angle a2) {
        return new Angle(getValue(angleUnits) - a2.getValue(angleUnits), angleUnits, angleRange);
    }

    @Override
    public String toString() {
        String answer = (new Double(getValue())).toString();
        if (angleUnits == Units.RADIANS) answer += " radians";
        if (angleUnits == Units.DEGREES) answer += " degrees";
        if (angleUnits == Units.DEGREES) answer += " grads";
        if (angleUnits == Units.REVS) answer += " revs";
        return answer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Angle)
            return (this == (Angle) obj);
        else if (obj instanceof Number)
            return (this == (Number) obj);
        else return false;
    }
    public boolean equals(Angle a2) {
        if ( restrictToRange(radianAngle, Range.UNSIGNED) == restrictToRange(a2.radianAngle, Range.UNSIGNED) ) return true;
        else return false;
    }
    public boolean equals(Number n2) {
        Angle a2 = new Angle(n2.doubleValue(), angleUnits);
        return (this == a2);
    }
    
    @Override
    public int hashCode() {
        Double d = restrictToRange(radianAngle, Range.UNSIGNED);
        return d.hashCode();
    }

    public double doubleValue() {return (double) getValue();}
    public byte byteValue() {return (byte) getValue();}
    public float floatValue() {return (float) getValue();}
    public int intValue() {return (int) getValue();}
    public long longValue() {return (long) getValue();}
    public short shortValue() {return (short) getValue();}
}
