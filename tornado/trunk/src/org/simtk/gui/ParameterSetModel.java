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
 * Created on Feb 14, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.gui;

import java.util.Observable;

/**
 *  
  * @author Christopher Bruns
  * 
  * MVC Model for generic parameter sets that are adjustable using an UndoableDialog
 */
public class ParameterSetModel extends Observable {
    private Object parameters = null;

    public void setParameters(Object parameters) {
        if (null == parameters) return;
        
        // Check if this is really a change
        if (! parameters.equals(this.parameters)) {
            this.parameters = parameters;
            setChanged();
            notifyObservers(parameters);
        }
    }
    public Object getParameters() {
        return this.parameters;
    }
}
