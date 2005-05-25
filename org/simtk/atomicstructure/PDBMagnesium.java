/*
 * Created on Apr 25, 2005
 *
 */
package org.simtk.atomicstructure;

import java.awt.Color;

/** 
 * @author Christopher Bruns
 * 
 * magnesium atom
 */
public class PDBMagnesium extends PDBAtom {
    public PDBMagnesium(String PDBLine) {super(PDBLine);}
	public double getMass() {return 24.0;} // guess value for unknown
    public String getElementSymbol() {return "Mg";}
	public double getVanDerWaalsRadius() {return 1.73;}
	public double getCovalentRadius() {return 1.30;}
	public Color getDefaultColor() {return Color.cyan;}
}
