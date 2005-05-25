/*
 * Created on May 1, 2005
 *
 */
package org.simtk.molecularstructure;

/**
 *  
  * @author Christopher Bruns
  * 
  * FunctionalGroup defines the atom names that form a logical subset of atoms in a Residue or Molecule
 */
public class FunctionalGroup {
    String[] atomNames;
    public FunctionalGroup(String[] n) {atomNames = n;}
    public String[] getAtomNames() {return atomNames;}
}
