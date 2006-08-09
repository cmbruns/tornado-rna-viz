package org.simtk.molecularstructure.nucleicacid;

import java.util.*;

import org.simtk.molecularstructure.Residue;
import org.simtk.molecularstructure.SecondaryStructureClass;

public class BaseTriple extends SecondaryStructureClass {
    private Collection<BasePair> basePairs = new Vector<BasePair>(3);

	public BaseTriple(BasePair bp1, BasePair bp2, BasePair bp3 ){
		super(6);
		basePairs.addAll(Arrays.asList(bp1, bp2, bp3));
		for (BasePair bp: (basePairs)){
			this.addResidue(bp.getResidue1());
			this.addResidue(bp.getResidue2());
		}
	}

}
