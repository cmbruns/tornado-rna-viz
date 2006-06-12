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
package org.simtk.moleculargraphics.cartoon;

import org.simtk.util.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.*;
import java.util.*;
import vtk.*;

/** 
 * @author Christopher Bruns
 * 
 * Rope and cylinder RNA structure, using modular components
 */
public class RopeAndCylinder extends MolecularCartoonClass {

    DuplexCylinderCartoon duplexes = new DuplexCylinderCartoon();
    BackboneStick ropes = new BackboneStick(1.00);
    
    vtkAssembly assembly = new vtkAssembly();
    
    public RopeAndCylinder() {
        assembly.AddPart(duplexes.getAssembly());
        assembly.AddPart(ropes.getActor());
    }
    
    public void select(Selectable s) {
        duplexes.select(s);
        ropes.select(s);
    }
    public void unSelect(Selectable s) {
        duplexes.unSelect(s);
        ropes.unSelect(s);
    }
    public void unSelect() {
        duplexes.unSelect();
        ropes.unSelect();
    }
    public void highlight(LocatedMolecule m) {
        duplexes.highlight(m);
        ropes.highlight(m);
    }
    public void hide(LocatedMolecule m) {
        duplexes.hide(m);
        ropes.hide(m);
    }
    public void show(LocatedMolecule m) {
        duplexes.show(m);
        ropes.show(m);
    }
    public void hide() {
        duplexes.hide();
        ropes.hide();
    }
    public void show() {
        duplexes.show();
        ropes.show();
    }
    public void updateCoordinates() {
        duplexes.updateCoordinates();
        ropes.updateCoordinates();
    }
    public void add(LocatedMolecule m) {
        super.add(m); // This is needed to avoid hosing display
        
        if (m instanceof NucleicAcid) add((NucleicAcid) m);        
    }
    
    protected void add(NucleicAcid molecule) {
        // Distinguish duplex residues from rope residues

        Set<LocatedResidue> allResidues = new LinkedHashSet<LocatedResidue>();
        for (Residue residue : molecule.residues())
            if (residue instanceof LocatedResidue) allResidues.add((LocatedResidue) residue);

        Set<LocatedResidue> duplexResidues = new HashSet<LocatedResidue>();
        for (SecondaryStructure structure : molecule.secondaryStructures()) {
            if (structure instanceof Duplex) {
                Duplex duplex = (Duplex) structure;
                for (Residue residue : duplex.residues()) {
                    if (residue instanceof LocatedResidue) duplexResidues.add((LocatedResidue) residue);                    
                }
            }
        }
        
        // Duplex residues that attach to non-duplex residues should also get ropes
        Set<LocatedResidue> ropeResidues = new LinkedHashSet<LocatedResidue>();
        for (LocatedResidue residue : allResidues) {
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
        List parentObjects = new Vector();
        parentObjects.add(molecule);
        
        for (LocatedResidue residue : ropeResidues) {
            ropes.addResidue(residue, parentObjects);
        }

        duplexes.add(molecule);        
        // ropes.add(molecule);
    }
    
    public void clear() {
        super.clear();
        duplexes.clear();
        ropes.clear();
    }
    
    public vtkAssembly getAssembly() {return assembly;}
}
