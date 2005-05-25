/*
 * Created on Apr 25, 2005
 *
 */
package org.simtk.atomicstructure;

import java.awt.Color;

/** 
 * @author Christopher Bruns
 * 
 * oxygen atom
 */
public class PDBOxygen extends PDBAtom {
    static Color color = new Color(255, 90, 110);
    public PDBOxygen(String PDBLine) {super(PDBLine);}
	public double getMass() {return 16.00;} // guess value for unknown
    public String getElementSymbol() {return "O";}
    // http://www.webelements.com/webelements/elements/text/O/radii.html
	public double getVanDerWaalsRadius() {return 1.52;}
	public double getCovalentRadius() {return 0.73;}
	public Color getDefaultColor() {return color;}
}
