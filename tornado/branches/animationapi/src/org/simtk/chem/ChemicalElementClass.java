/* Copyright (c) 2005 Stanford University and Christopher Bruns
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Nov 14, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.chem;

import java.util.*;

/** Instantiates interface for ChemicalElement, using a single set of parameters
 * for each element in the periodic table
 *  
  * @author Christopher Bruns
  * 
 */
public class ChemicalElementClass implements ChemicalElement {
    private String name;
    private String symbol;
    private double mass;
    private double vanDerWaalsRadius;
    private double covalentRadius;
    
    private static Map<String, ChemicalElement> nameElementMap = new HashMap<String, ChemicalElement>();
    
    static public ChemicalElement HYDROGEN = 
        new ChemicalElementClass("hydrogen",        "H",  1.008,  1.20, 0.37);
    static public ChemicalElement LITHIUM = 
        new ChemicalElementClass("lithium",        "Li",  6.941,  1.82, 1.34);
    static public ChemicalElement BORON = 
        new ChemicalElementClass("boron",           "B", 10.811,  1.75, 0.82);
    static public ChemicalElement CARBON = 
        new ChemicalElementClass("carbon",          "C",  12.01,  1.70, 0.77);
    static public ChemicalElement NITROGEN = 
        new ChemicalElementClass("nitrogen",        "N",  14.01,  1.55, 0.75);
    static public ChemicalElement OXYGEN = 
        new ChemicalElementClass("oxygen",          "O",  16.00,  1.52, 0.73);
    static public ChemicalElement FLUORINE = 
        new ChemicalElementClass("fluorine",        "F",  19.00,  1.47, 0.71);
    static public ChemicalElement SODIUM = 
        new ChemicalElementClass("sodium",          "Na", 22.00,  2.27, 1.54);
    static public ChemicalElement MAGNESIUM = 
        new ChemicalElementClass("magnesium",       "Mg", 24.00,  1.73, 1.30);
    static public ChemicalElement PHOSPHORUS = 
        new ChemicalElementClass("phosphorus",      "P",  31.00,  1.80, 1.06);
    static public ChemicalElement SULFUR = 
        new ChemicalElementClass("sulfur",          "S",  32.06,  1.80, 1.02);
    static public ChemicalElement CHLORINE = 
        new ChemicalElementClass("chlorine",        "Cl", 35.453, 1.75, 0.99);
    static public ChemicalElement POTASSIUM = 
        new ChemicalElementClass("potassium",        "K", 39.098, 2.75, 1.96);
    static public ChemicalElement CALCIUM = 
        new ChemicalElementClass("calcium",         "Ca", 40.078, 1.94, 1.74);
    static public ChemicalElement MANGANESE = 
        new ChemicalElementClass("manganese",       "Mn", 54.938, 1.61, 1.39);
    static public ChemicalElement IRON = 
        new ChemicalElementClass("iron",            "Fe", 55.845, 1.56, 1.25);
    static public ChemicalElement NICKEL = 
        new ChemicalElementClass("nickel",          "Ni", 58.693, 1.63, 1.21);
    static public ChemicalElement COPPER = 
        new ChemicalElementClass("copper",          "Cu", 63.546, 1.40, 1.38);
    static public ChemicalElement ZINC = 
        new ChemicalElementClass("zinc",            "Zn", 65.409, 1.39, 1.31);
    static public ChemicalElement SELENIUM = 
        new ChemicalElementClass("selenium",        "Se", 78.96,  1.90, 1.16);
    static public ChemicalElement BROMINE = 
        new ChemicalElementClass("bromine",         "Br", 79.904, 1.85, 1.14);
    static public ChemicalElement IODINE = 
        new ChemicalElementClass("iodine",          "I", 126.90,  1.98, 1.33);
    static public ChemicalElement UNKNOWN_ELEMENT = 
        new ChemicalElementClass("unknown element", "?",  10.0,   1.50, 0.75);

    static ChemicalElement getElementByName(String elementName) {
        // Clean up the name string
        String name = elementName.trim().toUpperCase();
        if (nameElementMap.containsKey(name)) return nameElementMap.get(name);
        else return UNKNOWN_ELEMENT;        
    }
    
    private ChemicalElementClass(
            String name, 
            String symbol, 
            double mass, 
            double vdwRadius, 
            double covRadius) {
        setElementName(name);
        setElementSymbol(symbol);
        setMassInDaltons(mass);
        setVanDerWaalsRadius(vdwRadius);
        setCovalentRadius(covRadius);

        // Hash away lots of ways of getting the known elements
        nameElementMap.put(name, this);
        nameElementMap.put(symbol, this);
        nameElementMap.put(name.trim().toUpperCase(), this);
        nameElementMap.put(symbol.trim().toUpperCase(), this);
    }

    /**
     * @return Returns the covalentRadius.
     */
    public double getCovalentRadius() {
        return covalentRadius;
    }
    /**
     * @param radius The covalentRadius to set.
     */
    protected void setCovalentRadius(double radius) {
        this.covalentRadius = radius;
    }
    /**
     * @return Returns the Mass.
     */
    public double getMassInDaltons() {
        return mass;
    }
    /**
     * @param mass The Mass to set.
     */
    protected void setMassInDaltons(double mass) {
        this.mass = mass;
    }
    /**
     * @return Returns the Name.
     */
    public String getElementName() {
        return name;
    }
    /**
     * @param name The Name to set.
     */
    protected void setElementName(String name) {
        this.name = name;
    }
    /**
     * @return Returns the Symbol.
     */
    public String getElementSymbol() {
        return symbol;
    }
    /**
     * @param symbol The Symbol to set.
     */
    protected void setElementSymbol(String symbol) {
        this.symbol = symbol;
    }
    /**
     * @return Returns the vanDerWaalsRadius.
     */
    public double getVanDerWaalsRadius() {
        return vanDerWaalsRadius;
    }
    
    /**
     * @param vanDerWaalsRadius The vanDerWaalsRadius to set.
     */
    protected void setVanDerWaalsRadius(double vanDerWaalsRadius) {
        this.vanDerWaalsRadius = vanDerWaalsRadius;
    }
}
