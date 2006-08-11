/* Copyright (c) 2005 Stanford University and Christopher Bruns
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Dec 12, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.molecularstructure;

import java.util.*;

public class SecondaryStructureClass implements SecondaryStructure {
    protected Collection<Residue> residues = null;
    protected Vector<Biopolymer> parentMolecules = new Vector<Biopolymer>();
    protected SourceType sourceType = SourceType.UNKNOWN;
    protected int resLimit;// -1 indicates no limit

    public enum SourceType {
    	TORNADO(Arrays.asList("tornado")), 
    	RNAVIEW(Arrays.asList("rnaview")),
    	MFOLD_UNAFOLD(Arrays.asList("mfold", "unafold")),
    	UNKNOWN(Arrays.asList("!"));
    	
    	public final List<String> designations;
    	SourceType(List<String> des){ this.designations = des; }

        public static SourceType getSourceType(String str){
        	for (SourceType et: SourceType.values()){
        		if (et.designations.contains(str.toLowerCase())){
        			return et;
        		}
        	}
        	return UNKNOWN;
        }
    };
    
    
    public SecondaryStructureClass(){
    	this(-1);
    }
    
    public SecondaryStructureClass(int resLimit){
    	this.resLimit = resLimit;
    	this.residues = resLimit>=0? new Vector<Residue>(resLimit): new Vector<Residue>();
    }
    
    public SecondaryStructureClass(Collection<Residue> residues){
    	this(residues, -1);
    }
    
    public SecondaryStructureClass(Collection<Residue> residues, int resLimit){
    	this(resLimit);
    	if ((resLimit>=0)&&(resLimit<residues.size())) {
    		throw new IllegalArgumentException();
    	}
    	this.residues.addAll(residues);
    }
    
    public SecondaryStructureClass(Collection<Residue> residues, int resLimit, String source){
    	this(residues, resLimit);
    	this.setSource(source);
    }

    public Iterator<Residue> getResidueIterator() {
        return this.residues.iterator();
    }
    
    public Collection<Residue> residues() {return residues;}

    public void addResidue(Residue residue) {
    	if (!residues.contains(residue)){
    		this.residues.add(residue);
    		residue.secondaryStructures().add(this);
    	}
    }

    public void setMolecule(Biopolymer biopolymer) {
        //this.parentMolecules.set(0,biopolymer);
    	addMolecule(biopolymer);
    }
    
    public boolean addMolecule(Biopolymer biopolymer){
    	if (!parentMolecules.contains(biopolymer)){
        	parentMolecules.add(biopolymer);
    		return true;
    	}
    	return false;
    }

    public SourceType getSource() {
		return sourceType;
	}

	public void setSource(String source) {
		this.sourceType = SourceType.getSourceType(source);
	}

}
