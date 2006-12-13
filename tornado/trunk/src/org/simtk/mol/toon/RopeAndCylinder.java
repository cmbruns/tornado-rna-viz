/*
 * Copyright (c) 2005, Stanford University. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met: 
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution. 
 *  - Neither the name of the Stanford University nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE. 
 */

/*
 * Created on Apr 28, 2005
 *
 */
package org.simtk.mol.toon;

import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.*;
import java.util.*;
import vtk.*;

/** 
 * @author Christopher Bruns
 * 
 * Rope and cylinder RNA structure, using modular components
 */
public class RopeAndCylinder extends CompositeCartoon {

    DuplexCylinderCartoon duplexes = new DuplexCylinderCartoon();
    // BackboneStick nucRopes = new BackboneStick(1.00);
    BackboneTubeCartoon nucRopes = new BackboneTubeCartoon(1.00);
    BackboneStick proteinRopes = new BackboneStick(0.5);
    BallAndStickCartoon cpks = new BallAndStickCartoon();
    
    vtkAssembly assembly = new vtkAssembly();
    
    public RopeAndCylinder() {
        addSubToon(duplexes);
        addSubToon(nucRopes);
        addSubToon(proteinRopes);
        addSubToon(cpks);
    }
    
    public void addMolecule(Molecule m) {
        if (m instanceof NucleicAcid) {
            addNucleicAcid((NucleicAcid) m); 
        }
        else if (m instanceof Biopolymer) {
            proteinRopes.addMolecule(m);
        }
        else if (m.isSolvent()) {
            return; // No solvent please
        }
        else {
            cpks.addMolecule(m);
        }
    }
    
    protected void addNucleicAcid(NucleicAcid molecule) {
        // Distinguish duplex residues from rope residues

        Set<Residue> allResidues = new LinkedHashSet<Residue>();
        for (Residue residue : molecule.residues())
            allResidues.add(residue);
        
        Set<Residue> duplexResidues = new HashSet<Residue>();
        for (SecondaryStructure structure : molecule.displayableStructures()) {
            if (structure instanceof Duplex) {
                Duplex duplex = (Duplex) structure;
                for (Residue residue : duplex.residues()) {
                    duplexResidues.add(residue);                    
                }
            }
        }
        
        // Duplex residues that attach to non-duplex residues should also get ropes
        List<Residue> ropeResidues = new Vector<Residue>();
        for (Residue residue : allResidues) {
            boolean isRopeResidue = false;
            if (duplexResidues.contains(residue)) { // In a duplex             
                // Only put a rope if this attaches to a non-duplex residue

                // Attaches to rope upstream?
                Residue next = residue.getNextResidue();
                if ( (next != null) && !(duplexResidues.contains(next)) )
                        isRopeResidue = true;

                // Attaches to rope downstream?
                Residue previous = residue.getPreviousResidue();
                if ( (previous != null) && !(duplexResidues.contains(previous)) )
                        isRopeResidue = true;
            }
            else { // not in a duplex
                isRopeResidue = true;
            }
            
            if (isRopeResidue) ropeResidues.add(residue);
        }
        
        // Rope cartoon needs to know what molecule it came from
        List<Object> parentObjects = new Vector<Object>();
        parentObjects.add(molecule);
        
        nucRopes.addResidues(ropeResidues);
//        for (Residue residue : ropeResidues) {
//            nucRopes.addResidue(residue, parentObjects);
//        }

        duplexes.addMolecule(molecule);        
        // ropes.add(molecule);
        
        actorSet.addAll(duplexes.vtkActors());
    }   
}