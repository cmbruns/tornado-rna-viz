/*
 * Created on Apr 25, 2005
 *
 */
package org.simtk.atomicstructure;

import java.awt.Color;

/** 
 * @author Christopher Bruns
 * 
 * sulfur (sulphur) atom
 */
public class PDBSulfur extends PDBAtom {
    static Color color = new Color(255, 255, 150);
    public PDBSulfur(String PDBLine) {super(PDBLine);}
	public double getMass() {return 32.06;}
    public String getElementSymbol() {return "S";}
    // http://www.webelements.com/webelements/elements/text/O/radii.html
	public double getVanDerWaalsRadius() {return 1.80;}
	public double getCovalentRadius() {return 1.02;}
	public Color getDefaultColor() {return color;}
}
