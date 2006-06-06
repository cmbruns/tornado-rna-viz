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
 * Created on Apr 20, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.chem.toon;

import java.text.ParseException;

import javax.swing.*;
import java.net.*;
import java.util.*;

import org.simtk.chem.toon.SpaceFillingMolecule;
import org.simtk.moleculargraphics.*;

// import org.simtk.molecularstructure.atom.*;
// import org.simtk.molecularstructure.*;
import org.simtk.chem.Atom;
import org.simtk.chem.Polymer;
import org.simtk.chem.Residue;
import org.simtk.chem.pdb.*;

import java.awt.*;

public class HighlightAtomsTest {
    static {VTKLibraries.load();}
    Collection<Atom> selectedAtoms = new HashSet<Atom>();

    public HighlightAtomsTest() {
        // Create graphics window
        JFrame frame = new JFrame("Test space filling atoms");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        StructureCanvas canvas = new StructureCanvas();
        frame.getContentPane().add(canvas);

        ClassLoader classLoader = getClass().getClassLoader();
        URL structureUrl = classLoader.getResource("resources/structures/OneRNAHairpin.pdb");
        PdbStructure moleculeCollection = null;
        // MoleculeCollection moleculeCollection = new MoleculeCollection();
        try {moleculeCollection = BasePdbStructure.createPdbStructure(structureUrl);}
        catch (Exception exc) {assert(false);}
        
        // Select a couple of residues
        Polymer molecule = (Polymer) moleculeCollection.iterator().next();
        for (int resNum = 245; resNum <= 246; resNum ++) {
            Residue residue = molecule.getResidueByNumber(resNum);
            for (Atom atom : residue.atoms()) {
                select(atom);
            }
        }
        
        System.out.println(""+selectedAtoms.size()+" atoms selected\n");
        
        SpaceFillingMolecule toon = new SpaceFillingMolecule(moleculeCollection);
        // toon.hide(selectedAtoms);
        
        canvas.GetRenderer().AddActor(toon.getVtkAssembly());
        canvas.setBackgroundColor(Color.white);

        // canvas.GetRenderWindow().SetStereoTypeToAnaglyph();
        // canvas.GetRenderWindow().StereoRenderOn();
        
        // canvas.setCenter(moleculeCollection.getCenterOfMass());
        BoundingBox box = toon.getBoundingBox();
        canvas.setCenter(box.getCenter());

        frame.pack();
        frame.setVisible(true);        
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        new HighlightAtomsTest();
    }
    
    private void select(Atom atom) {
        selectedAtoms.add(atom);
    }

}