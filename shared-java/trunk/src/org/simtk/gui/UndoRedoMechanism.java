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
 * Created on Sep 20, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.gui;

import javax.swing.*;
import javax.swing.undo.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

public class UndoRedoMechanism {
    private UndoAction undoAction;
    private RedoAction redoAction;
    private UndoManager undoManager;

    public UndoRedoMechanism() {
        undoManager = new UndoManager();
        undoAction = new UndoAction();
        redoAction = new RedoAction();
    }
    
    public Action getUndoAction() {return this.undoAction;}
    public Action getRedoAction() {return this.redoAction;}
    public void addEdit(UndoableEdit edit) {
        undoManager.addEdit(edit);
        undoAction.updateUndoState();
        redoAction.updateRedoState();
    }

    // The Undo action
    @SuppressWarnings("serial") class UndoAction extends AbstractAction {
        public UndoAction() {
            super("Can't Undo");
            setEnabled(false);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
            putValue(SMALL_ICON, new ImageIcon(getClass().getClassLoader().getResource("resources/images/undo16.png")));
        }
    
        public void actionPerformed(ActionEvent e) {
            try {
                undoManager.undo();
            } catch (CannotUndoException ex) {
                System.err.println("Unable to undo: " + ex);
                ex.printStackTrace();
            }
            updateUndoState();
            redoAction.updateRedoState();
        }
    
        protected void updateUndoState() {
            if (undoManager.canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, undoManager.getUndoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Can't Undo");
            }
        }
    }
    
    // The Redo action
    @SuppressWarnings("serial") 
    class RedoAction extends AbstractAction {
        public RedoAction() {
            super("Can't Redo");
            setEnabled(false);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
            putValue(SMALL_ICON, new ImageIcon(getClass().getClassLoader().getResource("resources/images/redo16.png")));
        }
    
        public void actionPerformed(ActionEvent e) {
            try {
                undoManager.redo();
            } catch (CannotRedoException ex) {
                System.err.println("Unable to redo: " + ex);
                ex.printStackTrace();
            }
            updateRedoState();
            undoAction.updateUndoState();
        }
    
        protected void updateRedoState() {
            if (undoManager.canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, undoManager.getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Can't Redo");
            }
        }
    }
}
