/*
 * Created on Apr 21, 2005
 *
 */
package org.simtk.atomicstructure;

import java.awt.Color;

/**
 * @author Christopher Bruns
 *
 * \brief A chemical atom including members found in Protein Data Bank flat structure files.
 * 
 */
public class UnknownPDBAtom extends PDBAtom {
    public UnknownPDBAtom(String PDBLine) {super(PDBLine);}
    public Color getDefaultColor() {return Color.pink;}
    public String getElementSymbol() {return "??";}
    public double getMass() {return 10.0;} // Just a guess
    public double getVanDerWaalsRadius() {return 1.50;} // Just a guess
    public double getCovalentRadius() {return 0.75;} // Just a guess
}
