/*
 * Created on Apr 25, 2005
 *
 */
package org.simtk.atomicstructure;

import java.awt.Color;

/** 
 * @author Christopher Bruns
 * 
 * nitrogen atom
 */
public class PDBNitrogen extends PDBAtom {
    static Color color = new Color(110, 170, 255);
    public PDBNitrogen(String PDBLine) {super(PDBLine);}
	public double getMass() {return 14.01;} // guess value for unknown
    public String getElementSymbol() {return "N";}
	public double getVanDerWaalsRadius() {return 1.55;}
	public double getCovalentRadius() {return 0.75;}
	public Color getDefaultColor() {return color;}
}
