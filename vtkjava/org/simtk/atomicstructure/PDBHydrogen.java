/*
 * Created on Apr 25, 2005
 *
 */
package org.simtk.atomicstructure;

import java.awt.Color;

/** 
 * @author Christopher Bruns
 * 
 * hydrogen atom
 */
public class PDBHydrogen extends PDBAtom {
    public PDBHydrogen(String PDBLine) {super(PDBLine);}
	public double getMass() {return 1.008;} // guess value for unknown
    public String getElementSymbol() {return "H";}
	public double getVanDerWaalsRadius() {return 1.20;}
	public double getCovalentRadius() {return 0.37;}
	public Color getDefaultColor() {return Color.white;}
}
