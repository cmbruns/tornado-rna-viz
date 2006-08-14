package org.simtk.molecularstructure.nucleicacid;

import java.util.*;

import org.simtk.molecularstructure.SecondaryStructureClass;

public class BaseTriple extends SecondaryStructureClass {
    private Collection<BasePair> basePairs = new Vector<BasePair>(3);

    public Collection<BasePair> basePairs() {return basePairs;}
    
	public BaseTriple(Collection<BasePair> basepairs){
		super(6);
		basePairs.addAll(basepairs);
		for (BasePair bp: (basePairs)){
			this.addResidue(bp.getResidue1());
			this.addResidue(bp.getResidue2());
		}
	}

	public BaseTriple(Collection<BasePair> basepairs, String source){
		this(basepairs);
		setSource(source);
	}
    
	public BaseTriple(BasePair bp1, BasePair bp2, BasePair bp3 ){
		this(Arrays.asList(bp1, bp2, bp3));
	}

}
