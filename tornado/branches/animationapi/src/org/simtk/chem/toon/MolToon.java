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
 * Created on Apr 19, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.chem.toon;

import java.util.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Interface intended for future implemenations
  * of molecule representations, atom glyphs,
  * and maybe even secondary structure diagrams
 */
public interface MolToon {
    
    // Check if relevant parts of the data model have changed, 
    // and if so, change the rendering of this toon.
    /**
     * @return true if the underlying data changed, otherwise false
     */
    public boolean update();
    
    // set the coloring method, possibly including the color itself
    public void setColorMethod(MolColorMethod m);

    public void show(); // Ensure display
    public void hide(); // Remove from display
    
    public void add(MolToon component); // Add a subcomponent

    // MolToons can be trees
    Collection<MolToon> getComponents();
    
    public BoundingBox getBoundingBox();    
}
