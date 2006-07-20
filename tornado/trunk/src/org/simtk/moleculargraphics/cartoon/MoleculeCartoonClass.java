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
 * Created on Jul 6, 2005
 *
 */
package org.simtk.moleculargraphics.cartoon;

import org.simtk.molecularstructure.*;
import java.util.*;
import vtk.*;

public abstract class MoleculeCartoonClass 
implements MoleculeCartoon 
{
    protected Set<vtkActor> actorSet = new HashSet<vtkActor>(); 
    protected vtkPolyDataMapper mapper = new vtkPolyDataMapper();
    protected ToonColors toonColors = new ToonColors(mapper);
    protected Set<BaseCartoon> subToons = new HashSet<BaseCartoon>();
    
    public void colorToon(Object object, ColorScheme colorScheme) {
        for (BaseCartoon subToon : subToons) {
            subToon.colorToon(object, colorScheme);
        }
        toonColors.setColor(object, colorScheme);
    }
    
    public void colorToon(ColorScheme colorScheme) {
        for (BaseCartoon subToon : subToons) {
            subToon.colorToon(colorScheme);
        }
        toonColors.setColor(colorScheme);
    }
    
    public Set<vtkActor> vtkActors() {
        return actorSet;
    }    

    public void add(MoleculeCollection m) {
        for (Molecule molecule : m.molecules()) {
            if (! (molecule instanceof LocatedMolecule)) continue;
            try {addMolecule((LocatedMolecule)molecule);}
            catch (NoCartoonCreatedException exc) {}
        }
        updateActors();
    }
    
    public void updateActors() {
        for (BaseCartoon subToon : subToons) {
            if (subToon instanceof ActorCartoon) {
                ActorCartoon toon = (ActorCartoon) subToon;
                if (toon.isPopulated())
                    actorSet.add(toon.getActor());
                else
                    actorSet.remove(toon.getActor());
            }
            else if (subToon instanceof MoleculeCartoon) {
                actorSet.addAll(((MoleculeCartoon)subToon).vtkActors());                
            }
        }        
    }
}
