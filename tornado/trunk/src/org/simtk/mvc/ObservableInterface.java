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
 * Created on Nov 3, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.mvc;

import java.util.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * This interface is meant to be compatible with java.util.Observable
 */
public interface ObservableInterface {
    public void addObserver(Observer o);
    // Adds an observer to the set of observers for this object, provided that it is not the same as some observer already in the set.
    
    public void    deleteObserver(Observer o);
    //  Deletes an observer from the set of observers of this object.
    
    public void    deleteObservers();
    //  Clears the observer list so that this object no longer has any observers.
    
    public boolean     hasChanged();
    //  Tests if this object has changed.
    
    public void    notifyObservers();
    //  If this object has changed, as indicated by the hasChanged method, then notify all of its observers and then call the clearChanged method to indicate that this object has no longer changed.
    
    public void    notifyObservers(Object arg);
    //  If this object has changed, as indicated by the hasChanged method, then notify all of its observers and then call the clearChanged method to indicate that this object has no longer changed.
}
