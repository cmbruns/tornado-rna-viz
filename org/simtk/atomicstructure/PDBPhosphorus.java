/*
 * Created on Apr 25, 2005
 *
 */
package org.simtk.atomicstructure;

import java.awt.Color;

/** 
 * @author Christopher Bruns
 * 
 * phosphorus atom
 */
public class PDBPhosphorus extends PDBAtom {
    static Color color = new Color(200, 255, 80);
    public PDBPhosphorus(String PDBLine) {super(PDBLine);}
	public double getMass() {return 31.0;} // guess value for unknown
    public String getElementSymbol() {return "P";}
    // http://www.webelements.com/webelements/elements/text/O/radii.html
	public double getVanDerWaalsRadius() {return 1.80;}
	public double getCovalentRadius() {return 1.06;}
	public Color getDefaultColor() {return color;}
}
