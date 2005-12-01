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
 * Created on Dec 1, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.tornadomorph;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import org.simtk.moleculargraphics.*;
import org.simtk.molecularstructure.*;
import org.simtk.mvc.*;

class AlignmentPanel extends MinimizablePanel implements ComponentListener {
    AlignmentTextArea alignmentTextArea;
    AlignmentScrollBar alignmentScrollBar;
    AlignmentPanel(
            String startingMoleculeLabel,
            String finalMoleculeLabel,
            ObservableInterface startingMoleculeLoadBroadcaster,
            ObservableInterface targetMoleculeLoadBroadcaster
    ) {
        super("Sequence Alignment");
        
        Container panel = getContentPane();
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // vertical
        
        // place for the alignment
        alignmentTextArea = new AlignmentTextArea(
                startingMoleculeLabel,
                finalMoleculeLabel,
                startingMoleculeLoadBroadcaster,
                targetMoleculeLoadBroadcaster
        );
        panel.add(alignmentTextArea);
        
        // scrollbar
        alignmentScrollBar = new AlignmentScrollBar(Adjustable.HORIZONTAL);
        alignmentScrollBar.addAdjustmentListener(alignmentTextArea);
        panel.add(alignmentScrollBar);
        
        addComponentListener(this);
        // updateScrollBarParameters();
    }
    
    /**
     * Make sure scroll bar geometry matches the loaded sequences
     */
    private void updateScrollBarParameters() {
        // Use pixel units for scrollbar
        
        // Minimum is always zero
        alignmentScrollBar.setMinimum(0);
        
        int sequenceWidth = alignmentTextArea.getTotalSequenceWidth();
        int windowWidth = alignmentTextArea.getVisibleSequenceWidth();
        int barValue = alignmentScrollBar.getValue();
        
        int oldVisibleAmount = alignmentScrollBar.getVisibleAmount();
        int newVisibleAmount = windowWidth;
        
        /* This way its keeps the left side of the sequence the same on resize
         * 
         */
        if (barValue > (sequenceWidth - windowWidth))
            barValue = sequenceWidth - windowWidth;
        if (barValue < 0)
            barValue = 0;
        
        alignmentScrollBar.setValue(barValue);
        
        alignmentScrollBar.setMaximum(Math.max(sequenceWidth, windowWidth));
        alignmentScrollBar.setVisibleAmount(newVisibleAmount);
        alignmentScrollBar.setBlockIncrement( (int)(0.95 * windowWidth) );
        alignmentScrollBar.setUnitIncrement(alignmentTextArea.getResidueWidth());
        
        System.out.println("scrollbar value = " + barValue);
        System.out.println("window width = " + windowWidth);
        System.out.println("sequence width = " + sequenceWidth);
        
    }
    
    // Panel where the alignment text is shown
    class AlignmentTextArea extends WhitePanel implements AdjustmentListener {
        private SequenceTextPanel startingSequencePanel;
        private SequenceTextPanel targetSequencePanel;
        
        AlignmentTextArea(
                String startingMoleculeLabel,
                String finalMoleculeLabel,
                ObservableInterface startingMoleculeLoadBroadcaster,
                ObservableInterface targetMoleculeLoadBroadcaster
                ) {
            GridBagLayout gridBag = new GridBagLayout();
            setLayout(gridBag);
            
            startingSequencePanel = new SequenceTextPanel(gridBag);
            targetSequencePanel = new SequenceTextPanel(gridBag);
            
            // First sequence
            add(new SequenceLabel(startingMoleculeLabel + ": ", gridBag));
            startingMoleculeLoadBroadcaster.addObserver(startingSequencePanel);
            add(startingSequencePanel);
            
            // Second sequence
            add(new SequenceLabel(finalMoleculeLabel + ": ", gridBag));
            targetMoleculeLoadBroadcaster.addObserver(targetSequencePanel);
            add(targetSequencePanel);
        }
        
        private int getResidueWidth() {
            return startingSequencePanel.getResidueWidth();
        }
        
        private int getTotalSequenceWidth() {
            return Math.max(startingSequencePanel.getTotalSequenceWidth(),
                    targetSequencePanel.getTotalSequenceWidth());
        }
        
        private int getVisibleSequenceWidth() {
            return Math.max(startingSequencePanel.getWidth(),
                    targetSequencePanel.getWidth());
        }
        
        public void adjustmentValueChanged(AdjustmentEvent event) {
            JScrollBar scrollBar = (JScrollBar) event.getAdjustable();
            
            // Scroll the sequences to where the scrollbar tells them
            int pixelValue = scrollBar.getValue();
            
            startingSequencePanel.setLeftEdgePixel(pixelValue);
            targetSequencePanel.setLeftEdgePixel(pixelValue);
        }
        
        static final long serialVersionUID = 01L;
        
        // The actual sequence text
        class SequenceTextPanel extends BasePanel implements Observer {
            SequenceCanvas sequenceCanvas = new SequenceCanvas();
            
            SequenceTextPanel(GridBagLayout gb) {
                GridBagConstraints gc = new GridBagConstraints();
                gc.fill = GridBagConstraints.HORIZONTAL; // stretch
                gc.weightx = 1.0; // stretch
                gc.gridwidth = GridBagConstraints.REMAINDER; // finish line
                gb.setConstraints(this, gc);
                
                // BoxLayout does cause child to stretch to fill
                // default layout (BorderLayout?) does not cause child to stretch
                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                
                sequenceCanvas.setParentContainer(this);
                
                add(sequenceCanvas);
                add(Box.createHorizontalGlue()); // Keep alignment on the left side
                
                setBackground(Color.white);
            }
            
            public void setBackground(Color c) {
                super.setBackground(c);
                if (sequenceCanvas != null) sequenceCanvas.setBackground(c);
            }
            
            public void setLeftEdgePixel(int p) {
                sequenceCanvas.setLeftEdgePixel(p);
            }
            
            public int getLeftEdgePixel() {return sequenceCanvas.getLeftEdgePixel();}
            
            /**
             * Respond to loading a new molecule
             * @param observable
             * @param object the new MoleculeCollection that has been loaded
             */
            public void update(Observable observable, Object object) {
                if ( (object != null) && (object instanceof MoleculeCollection) ) {
                    MoleculeCollection molecules = (MoleculeCollection) object;
                    setMolecules(molecules);
                }
            }
            
            private void setMolecules(MoleculeCollection molecules) {
                ObservableBiopolymer singleMolecule = null;
                for (Iterator molIter = molecules.molecules().iterator(); molIter.hasNext();) {
                    StructureMolecule molecule = (StructureMolecule) molIter.next();
                    if (molecule instanceof ObservableBiopolymer) {
                        singleMolecule = (ObservableBiopolymer) molecule;
                        break;
                    }
                }
                
                if ((sequenceCanvas != null) && (singleMolecule != null)) 
                    sequenceCanvas.setMolecule(singleMolecule);
                
                // New sequence may cause change in scroll bar geometry
                AlignmentPanel.this.updateScrollBarParameters();
            }
            
            int getResidueWidth() {
                return sequenceCanvas.getResidueWidth();
            }
            
            int getTotalSequenceWidth() {
                return sequenceCanvas.getTotalSequenceWidth();
            }
            
            static final long serialVersionUID = 01L;
        }
        
        class SequenceLabel extends JLabel {
            SequenceLabel(String text, GridBagLayout gb) {
                super(text);
                setMaximumSize(getPreferredSize()); // don't grow
                GridBagConstraints gc = new GridBagConstraints();
                gc.fill = GridBagConstraints.NONE; // don't expand
                gc.gridx = 0; // put on left
                gb.setConstraints(this, gc);
            }
            static final long serialVersionUID = 01L;                
        }
    }
    class AlignmentScrollBar extends JScrollBar {
        AlignmentScrollBar(int p) {
            super(p);
            
            int initialVisibleWidth = 200;
            
            setValue(0); // start at left
            setUnitIncrement(10);
            setBlockIncrement((int)(initialVisibleWidth * 0.95));
            setMinimum(0);
            setMaximum(initialVisibleWidth);
            setVisibleAmount(initialVisibleWidth);
        }
        static final long serialVersionUID = 01L;            
    }
    
    // ComponentListener interface
    public void componentMoved(ComponentEvent event) {}
    public void componentResized(ComponentEvent event) {
        if (event.getSource() == this)
            updateScrollBarParameters();
    }
    public void componentHidden(ComponentEvent event) {}
    public void componentShown(ComponentEvent event) {}
    
    static final long serialVersionUID = 01L;
}
