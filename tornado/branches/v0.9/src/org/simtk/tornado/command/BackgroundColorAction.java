/* Portions copyright (c) 2006 Stanford University and Christopher Bruns
 * Contributors:
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
 * IN NO EVENT SHALL THE AUTHORS, CONTRIBUTORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Nov 2, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.tornado.command;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

import org.simtk.moleculargraphics.Colorable;
import org.simtk.gui.UndoRedoMechanism;

public class BackgroundColorAction extends AbstractAction {
    Color color;
    String colorName;
    Colorable colorable;
    UndoRedoMechanism undoRedoMechanism;
    
    public BackgroundColorAction(Color c, String colorName, Colorable colorable, UndoRedoMechanism undoer) {
        this.color = c;
        this.colorName = colorName;
        this.colorable = colorable;
        this.undoRedoMechanism = undoer;
    }
    
    public void actionPerformed(ActionEvent e) {
        Color oldColor = colorable.getColor();
        Color newColor = color;
        
        if (oldColor.equals(newColor)) return;
        
        colorable.setColor(newColor);

        // Permit "undo" of background color selection
        UndoableEdit edit = new BackgroundColorEdit(oldColor, newColor, colorName);
        undoRedoMechanism.addEdit(edit);
    }
    
    // Undoable actions need to create UndoableEdit objects
    class BackgroundColorEdit extends AbstractUndoableEdit {
        private Color startColor;
        private Color endColor;
        private String newColorName;
        BackgroundColorEdit(Color startColor, Color endColor, String newColorName) {
            this.startColor = startColor;
            this.endColor = endColor;
            this.newColorName = newColorName;
        }
        
        public void redo() {
            super.redo();
            colorable.setColor(endColor);
        }
        public void undo() {
            super.undo();
            colorable.setColor(startColor);
        }
        public String getPresentationName() {
            if (newColorName == null)
                return "Set background color";
            else
                return "Set background to " + newColorName;
        }
        public String getRedoPresentationName() {
            return "Redo " + getPresentationName();
        }
        public String getUndoPresentationName() {
            return "Undo " + getPresentationName();
        }
    }
}
