package org.simtk.molecularstructure.nucleicacid;

import java.util.Collection;

import org.simtk.molecularstructure.Residue;
import org.simtk.molecularstructure.SecondaryStructureClass;

public class BaseStack extends SecondaryStructureClass {

	public BaseStack() {
		super(2);
	}

	public BaseStack(Collection<Residue> residues) {
		super(residues, 2);
	}

	public BaseStack(Collection<Residue> residues, String source) {
		super(residues, 2, source);
	}

}
