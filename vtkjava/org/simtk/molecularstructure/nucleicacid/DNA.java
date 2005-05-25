/*
 * Created on Apr 21, 2005
 *
 */
package org.simtk.molecularstructure.nucleicacid;

import org.simtk.atomicstructure.PDBAtomSet;

/**
 * @author Christopher Bruns
 *
 * \brief A molecule of deoxyribonucleic acid (DNA), the blueprint of life.
 */
public class DNA extends NucleicAcid {
    public DNA() {} // Empty molecule
    public DNA(PDBAtomSet atomSet) {super(atomSet);}
}
