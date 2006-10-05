package org.simtk.molecularstructure.nucleicacid;

import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import org.simtk.molecularstructure.SecondaryStructureClass;

public class Pseudoknot extends SecondaryStructureClass {
    private Collection<Duplex> helices = new Vector<Duplex>(2);

	public Pseudoknot(Duplex dup1, Duplex dup2) {
		for (Duplex dup : Arrays.asList(dup1, dup2)){
			helices.add(dup);
			residues.addAll(dup.residues());
		}
	}

	public Pseudoknot(Duplex dup1, Duplex dup2, String source) {
		this(dup1, dup2);
		setSource(source);
	}	
}
