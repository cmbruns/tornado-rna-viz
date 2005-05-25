/*
 * Created on Apr 21, 2005
 *
 */
package org.simtk.molecularstructure.nucleicacid;

import org.simtk.atomicstructure.PDBAtomSet;

/**
 * @author Christopher Bruns
 *
 * \brief A single molecule of ribonucleic acid (RNA)
 */
public class RNA extends NucleicAcid {
    public RNA() {} // Empty molecule
    public RNA(PDBAtomSet atomSet) {super(atomSet);}
}
