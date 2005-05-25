/*
 * Created on Apr 25, 2005
 *
 */
package org.simtk.atomicstructure;

import java.awt.Color;

/** 
 * @author Christopher Bruns
 * 
 * carbon atom
 */
public class PDBCarbon extends PDBAtom {
    Color color = new Color(220, 220, 220);

    public PDBCarbon(String PDBLine) {super(PDBLine);}

	public double getMass() {return 12.01;}
    public String getElementSymbol() {return "C";}
	public double getVanDerWaalsRadius() {return 1.70;}
	public double getCovalentRadius() {return 0.77;}
	public Color getDefaultColor() {return color;}
}
