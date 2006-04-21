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
package org.simtk.mol.toon;

import java.util.*;

public abstract class BaseMolToon implements MolToon {

    protected HashSet<MolToon> components = new HashSet<MolToon>(); 
    protected MolColorMethod molColorMethod;

    public void setColorMethod(MolColorMethod m) {
        molColorMethod = m;
    }

    public Collection<MolToon> getComponents() {
        return components;
    }

    public void show() {
        for (MolToon t: components) t.show();
    }

    public void hide() {
        for (MolToon t: components) t.hide();
    }
    
    public void add(MolToon component) {
        if (! components.contains(component))
            components.add(component);
    }
    
    public boolean update() {
        boolean isChanged = false;
        for (MolToon t: components) {
            if (t.update()) isChanged = true;        
        }
        return isChanged;
    }
}
