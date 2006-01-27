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
package org.simtk.molecularstructure.atom;

import java.awt.*;

/** Instantiates interface for ChemicalElement, using a single set of parameters
 * for each element in the periodic table
 *  
  * @author Christopher Bruns
  * 
 */
public class ChemicalElementClass implements ChemicalElement {
    String m_Name;
    String m_Symbol;
    double m_Mass;
    double m_VanDerWaalsRadius;
    double m_CovalentRadius;
    Color m_DefaultColor;
    static private Color classDefaultColor = Color.cyan;
    
    static public ChemicalElement HYDROGEN = 
        new ChemicalElementClass("hydrogen",        "H",  1.008,  1.20, 0.37, Color.white);
    static public ChemicalElement LITHIUM = 
        new ChemicalElementClass("lithium",        "Li",  6.941,  1.82, 1.34, classDefaultColor);
    static public ChemicalElement BORON = 
        new ChemicalElementClass("boron",           "B", 10.811,  1.75, 0.82, classDefaultColor);
    static public ChemicalElement CARBON = 
        new ChemicalElementClass("carbon",          "C",  12.01,  1.70, 0.77, Color.GRAY);
    static public ChemicalElement NITROGEN = 
        new ChemicalElementClass("nitrogen",        "N",  14.01,  1.55, 0.75, new Color(110, 170, 255)); // blue
    static public ChemicalElement OXYGEN = 
        new ChemicalElementClass("oxygen",          "O",  16.00,  1.52, 0.73, new Color(255, 90, 115)); // red
    static public ChemicalElement FLUORINE = 
        new ChemicalElementClass("fluorine",        "F",  19.00,  1.47, 0.71, classDefaultColor);
    static public ChemicalElement SODIUM = 
        new ChemicalElementClass("sodium",          "Na", 22.00,  2.27, 1.54, classDefaultColor);
    static public ChemicalElement MAGNESIUM = 
        new ChemicalElementClass("magnesium",       "Mg", 24.00,  1.73, 1.30, classDefaultColor);
    static public ChemicalElement PHOSPHORUS = 
        new ChemicalElementClass("phosphorus",      "P",  31.00,  1.80, 1.06, new Color(200, 255, 80)); // green
    static public ChemicalElement SULFUR = 
        new ChemicalElementClass("sulfur",          "S",  32.06,  1.80, 1.02, new Color(255, 255, 150)); // yellow
    static public ChemicalElement CHLORINE = 
        new ChemicalElementClass("chlorine",        "Cl", 35.453, 1.75, 0.99, Color.green);
    static public ChemicalElement POTASSIUM = 
        new ChemicalElementClass("potassium",        "K", 39.098, 2.75, 1.96, classDefaultColor);
    static public ChemicalElement CALCIUM = 
        new ChemicalElementClass("calcium",         "Ca", 40.078, 1.94, 1.74, new Color(220, 210, 200)); // warm white
    static public ChemicalElement MANGANESE = 
        new ChemicalElementClass("manganese",       "Mn", 54.938, 1.61, 1.39, classDefaultColor);
    static public ChemicalElement IRON = 
        new ChemicalElementClass("iron",            "Fe", 55.845, 1.56, 1.25, classDefaultColor);
    static public ChemicalElement NICKEL = 
        new ChemicalElementClass("nickel",          "Ni", 58.693, 1.63, 1.21, classDefaultColor);
    static public ChemicalElement COPPER = 
        new ChemicalElementClass("copper",          "Cu", 63.546, 1.40, 1.38, Color.orange);
    static public ChemicalElement ZINC = 
        new ChemicalElementClass("zinc",            "Zn", 65.409, 1.39, 1.31, Color.gray);
    static public ChemicalElement SELENIUM = 
        new ChemicalElementClass("selenium",        "Se", 78.96,  1.90, 1.16, classDefaultColor);
    static public ChemicalElement BROMINE = 
        new ChemicalElementClass("bromine",         "Br", 79.904, 1.85, 1.14, classDefaultColor);
    static public ChemicalElement IODINE = 
        new ChemicalElementClass("iodine",          "I", 126.90,  1.98, 1.33, new Color(180, 0, 255)); // purple
    static public ChemicalElement UNKNOWN_ELEMENT = 
        new ChemicalElementClass("unknown element", "?",  10.0,   1.50, 0.75, classDefaultColor);

    // TODO make sure this has all of the elements in it
    static private ChemicalElement[] staticElements = {
        HYDROGEN, LITHIUM, BORON, CARBON, NITROGEN, OXYGEN, 
        FLUORINE, SODIUM, MAGNESIUM, PHOSPHORUS, SULFUR,
        CHLORINE, POTASSIUM, CALCIUM, MANGANESE, IRON,
        NICKEL, COPPER, ZINC, SELENIUM, BROMINE, IODINE
    };

    static ChemicalElement getElementByName(String elementName) {
        // Clean up the name string
        String name = elementName.trim().toUpperCase();

        for (int i = 0; i < staticElements.length; i ++) {
            ChemicalElement testElement = staticElements[i];
            String testElementName = testElement.getElementName().trim().toUpperCase();
            String testElementSymbol = testElement.getElementSymbol().trim().toUpperCase();
            if (name.equals(testElementName)) return testElement;
            if (name.equals(testElementSymbol)) return testElement;
        }
        return UNKNOWN_ELEMENT;
    }
    
    private ChemicalElementClass(
            String name, 
            String symbol, 
            double mass, 
            double vdwRadius, 
            double covRadius,
            Color defaultColor) {
        setElementName(name);
        setElementSymbol(symbol);
        setMass(mass);
        setVanDerWaalsRadius(vdwRadius);
        setCovalentRadius(covRadius);
        setDefaultAtomColor(defaultColor);
    }

    public Color getDefaultAtomColor() {return m_DefaultColor;}
    protected void setDefaultAtomColor(Color c) {m_DefaultColor = c;}
    
    /**
     * @return Returns the covalentRadius.
     */
    public double getCovalentRadius() {
        return m_CovalentRadius;
    }
    /**
     * @param radius The covalentRadius to set.
     */
    protected void setCovalentRadius(double radius) {
        m_CovalentRadius = radius;
    }
    /**
     * @return Returns the Mass.
     */
    public double getMass() {
        return m_Mass;
    }
    /**
     * @param mass The Mass to set.
     */
    protected void setMass(double mass) {
        m_Mass = mass;
    }
    /**
     * @return Returns the Name.
     */
    public String getElementName() {
        return m_Name;
    }
    /**
     * @param name The Name to set.
     */
    protected void setElementName(String name) {
        m_Name = name;
    }
    /**
     * @return Returns the Symbol.
     */
    public String getElementSymbol() {
        return m_Symbol;
    }
    /**
     * @param symbol The Symbol to set.
     */
    protected void setElementSymbol(String symbol) {
        m_Symbol = symbol;
    }
    /**
     * @return Returns the vanDerWaalsRadius.
     */
    public double getVanDerWaalsRadius() {
        return m_VanDerWaalsRadius;
    }
    
    /**
     * @param derWaalsRadius The vanDerWaalsRadius to set.
     */
    protected void setVanDerWaalsRadius(double derWaalsRadius) {
        m_VanDerWaalsRadius = derWaalsRadius;
    }
}
