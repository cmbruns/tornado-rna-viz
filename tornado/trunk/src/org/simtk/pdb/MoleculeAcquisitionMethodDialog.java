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
package org.simtk.pdb;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.net.*;
import org.simtk.gui.*;
import java.util.*;
import org.simtk.molecularstructure.*;
import org.simtk.mvc.*;

public abstract class MoleculeAcquisitionMethodDialog extends JDialog implements ActionListener, Observer {
    static final long serialVersionUID = 01L;
    JButton loadFileButton = null;
    JButton webPDBButton = null;
    JButton cancelButton = null;
    JTextField idField = null;            
    JComboBox bioUnitList;
    Frame parent = null;
    MoleculeFileChooser moleculeFileChooser = null;
    String defaultPdbId = "1MRP";
    private String pdbId = null;
    
    private volatile boolean isLoadingFile = false;
    SimpleObservable fileLoadObservable = new SimpleObservable();
    
    Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
    Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    
    /**
     * Overload this method to read the structure from a stream
     * @param structureStream
     * @return true if the read was successful, false if the dialog should keep prodding the user to load a structure
     */
//   protected void readStructureFromStream(InputStream structureStream) 
//   throws IOException, InterruptedException {
//       if (structureStream == null) throw new IOException("Attempt to load structure from null stream");
//   }
   
   /**
    * Overload this to make use of preloaded molecule collection
    * 
    * @param molecules equals null when load fails
    */
   protected abstract void readStructureFromMoleculeCollection(MoleculeCollection molecules);
   
   public void setDefaultPdbId(String id) {
       defaultPdbId = id;
       idField.setText(id);
   }
   public String getDefaultPdbId() {return defaultPdbId;}

   private void setButtonsAreEnabled(boolean buttonsAreEnabled) {
       loadFileButton.setEnabled(buttonsAreEnabled);
       webPDBButton.setEnabled(buttonsAreEnabled);
       // TODO the cancel button should be able to top the load process
       cancelButton.setEnabled(buttonsAreEnabled);
       idField.setEnabled(buttonsAreEnabled);
   }
   
   //   MoleculeAcquisitionMethodDialog() {
//        initializeDialog();
//   }
    
    public MoleculeAcquisitionMethodDialog(JFrame f) {
        super(f);
        // System.out.println("MoleculeAcquisitionMethodDialog constructor");
        parent = f;
        initializeDialog();
        fileLoadObservable.addObserver(this);
    }
    
    void initializeDialog() {
        setTitle("Choose Molecule Source");
        setModal(false);
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        setContentPane(contentPanel);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0,5)));
        JLabel label = new JLabel("Choose molecule source:");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(label);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0,8)));
        contentPanel.add(new JSeparator());
        contentPanel.add(Box.createRigidArea(new Dimension(0,8)));

        loadFileButton = new JButton("From file...");
        loadFileButton.addActionListener(this);
        loadFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(loadFileButton);

        contentPanel.add(Box.createRigidArea(new Dimension(0,8)));
        contentPanel.add(new JSeparator());
        contentPanel.add(Box.createRigidArea(new Dimension(0,8)));
        
        JPanel webPDBPanel = new JPanel();
        webPDBPanel.setLayout(new BoxLayout(webPDBPanel, BoxLayout.X_AXIS));

        label = new JLabel(" PDB ID (4 characters): ");
        webPDBPanel.add(label);

        idField = new JTextField(defaultPdbId, 4);
        idField.addActionListener(this);
        webPDBPanel.add(idField);

        webPDBButton = new JButton("From web");
        webPDBButton.addActionListener(this);
        webPDBPanel.add(webPDBButton);

        webPDBPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(webPDBPanel);

        String unitOptions[] = {"Biological Unit (recommended)", "Crystallographic Unit"};
        bioUnitList = new JComboBox(unitOptions);
        bioUnitList.setSelectedIndex(0); // biological unit
        contentPanel.add(bioUnitList);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0,8)));
        contentPanel.add(new JSeparator());
        contentPanel.add(Box.createRigidArea(new Dimension(0,8)));

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(cancelButton);
        contentPanel.add(Box.createRigidArea(new Dimension(0,5)));

        pack();
    }

    boolean isPDBFile(File f) {
        // Determine file type from the name
        if (f == null) return false;
        
        String name = f.getName().toLowerCase();
        if ( name.endsWith(".pdb") ) return true;
        if ( name.endsWith(".pdb1") ) return true;
        if ( name.endsWith(".pdb2") ) return true;
        if ( name.endsWith(".ent") ) return true;
        if ( name.endsWith(".brk") ) return true;

        if ( name.toLowerCase().endsWith(".pqr") ) return false;
        if ( name.toLowerCase().indexOf(".pqr") >= 0 ) return false;

        if ( name.toLowerCase().indexOf(".pdb") >= 0 ) return true;

        return false;
    }
    
    protected void setPdbId(String id) {pdbId = id;}
    protected String getPdbId() {return pdbId;}
    
    public void actionPerformed(ActionEvent e) {
        // Don't launch a second process if a previous one is not complete
        if (isLoadingFile) return;
        

        setPdbId(null);
        
        // Load structure from file
        if ( e.getSource() == loadFileButton ) {
            // Load molecule from file using file browser dialog

            // Tell everyone we are busy
            isLoadingFile = true;
            setCursor(waitCursor);
            setButtonsAreEnabled(false);

            String [] extensions = {"pdb", "pqr"};
            if (moleculeFileChooser == null) moleculeFileChooser = new MoleculeFileChooser(parent, extensions);

            File inputFile = moleculeFileChooser.getFile();

            LoadFilePDBProcess loadFilePDBProcess = 
                new LoadFilePDBProcess(inputFile, fileLoadObservable);
            loadFilePDBProcess.start();
            
            // And another process to monitor the download process
            ProgressManager progressManager = 
                new ProgressManager(loadFilePDBProcess, this, "Loading structure " + inputFile.getName());
            progressManager.start();
        }

        // Download structure from PDB web site
        else if ( (e.getSource() == webPDBButton) ||
             (e.getSource() == idField) ) {

            // Tell everyone we are busy
            isLoadingFile = true;
            setCursor(waitCursor);
            setButtonsAreEnabled(false);

            // Load PDB molecule from the internet
            String pdbId = idField.getText().trim().toLowerCase();
            
            // Get the PDB file over the internet
            boolean isBioUnit = true;
            if (bioUnitList.getSelectedIndex() != 0)
                isBioUnit = false;
            
            // Start the background download process
            LoadWebPDBProcess loadWebPDBProcess = new LoadWebPDBProcess(pdbId, isBioUnit, fileLoadObservable);
            loadWebPDBProcess.start();

            // And another process to monitor the download process
            ProgressManager progressManager = 
                new ProgressManager(loadWebPDBProcess, this, "Downloading structure " + idField.getText());
            progressManager.start();
        }
        
        // Cancel
        else if ( e.getSource() == cancelButton ) {
            setVisible(false);
        }
    }
    
    public void finishLoad(boolean isSuccessful) {
        if (isSuccessful) {
            
        } else {
            
        }
    }

    // Respond to successful file load
    public void update(Observable observable, Object object) {
        if (observable == fileLoadObservable) {
            
            if (object == null) { // failed to load
                // TODO - error dialog
            }
            else if (object instanceof MoleculeCollection) {
                MoleculeCollection molecules = (MoleculeCollection) object;
                
                // Let derived classes do whatever they want with the molecule
                readStructureFromMoleculeCollection(molecules);

                setVisible(false); // close window on success
            }
        }
        
        // Reactivate dialog
        setButtonsAreEnabled(true);
        setCursor(defaultCursor);
        isLoadingFile = false; // Permit new loading operations
    }
}


class MoleculeFileChooser extends JFileChooser {
    static final long serialVersionUID = 01L;
    Frame parentFrame;
    
    MoleculeFileChooser(Frame frame, String[] extensions) {
        parentFrame = frame;
        setDialogTitle("Choose molecule structure file");
        FileNameFilter filter = new FileNameFilter("molecule structure files", extensions);
        setFileFilter(filter);
    }
    
    File getFile() {
        int returnVal = showOpenDialog(parentFrame);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File inputFile = getSelectedFile();
            return inputFile;
        }
        else return null;
    }
}

class LoadPDBProcess extends Thread implements MonitoredProcess {
    boolean m_isSuccessful = false; // Only set true in case of success
    private boolean m_isFailed = false;
    private SimpleObservable m_loadMoleculeObservable;
    protected volatile MoleculeCollection molecules = null;

    LoadPDBProcess(SimpleObservable loadMoleculeObservable) {
        m_loadMoleculeObservable = loadMoleculeObservable;
    }
    
    protected void loadMolecules() throws IOException, InterruptedException {}
    
    public void run() {
        try {
            loadMolecules();
            
            reportSuccess();
            
        } catch (IOException exc) {
            reportFailure();
        }
        catch (InterruptedException exc) {  
            reportFailure();
        }
    }
    
    private void reportSuccess() {
        m_isSuccessful = true;
        m_isFailed = false;        
        // Deliver molecules in the event thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                m_loadMoleculeObservable.setChanged();
                m_loadMoleculeObservable.notifyObservers(molecules);                    
            }
        });
    }
    
    private void reportFailure() {
        m_isFailed = true;
        m_isSuccessful = false;
        // Deliver molecules in the event thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                m_loadMoleculeObservable.setChanged();
                m_loadMoleculeObservable.notifyObservers(null);                    
            }
        });        
    }
    
    // MonitoredProcess interface
    public void abort() {
        interrupt();
    }    
    public int getProgress() {return 0;} // TODO
    public int getMinimum() {return 0;} // TODO
    public int getMaximum() {return 0;} // TODO
    public boolean isFailed() {return m_isFailed;}
    public boolean isSuccessful() {return m_isSuccessful;}
}

class LoadWebPDBProcess extends LoadPDBProcess {
    private boolean m_isBioUnit;
    private String m_pdbId;
    
    LoadWebPDBProcess(String pdbId, boolean isBioUnit, SimpleObservable loadMoleculeObservable) {
        super(loadMoleculeObservable);
        m_isBioUnit = isBioUnit;
        m_pdbId = pdbId;
    }
    
    protected void loadMolecules() throws IOException, InterruptedException {
        InputStream webStream = WebPDB.getWebPDBStream(m_pdbId, m_isBioUnit);

        
        // Remember file name
        URL pdbUrl = WebPDB.getWebPdbUrl(m_pdbId, m_isBioUnit);
        String fileName = pdbUrl.getFile();

        // strip off all but the file name (no path)
        int pathEnd = fileName.lastIndexOf("/");
        if (pathEnd >= 0) fileName = fileName.substring(pathEnd + 1);

        molecules = new MoleculeCollection();
        molecules.loadPDBFormat(webStream);            
        molecules.setInputStructureFileName(fileName);
        if (m_pdbId != null) molecules.setPdbId(m_pdbId);
    }
}

class LoadFilePDBProcess extends LoadPDBProcess {
    private File m_file;
    LoadFilePDBProcess(File file, SimpleObservable loadMoleculeObservable) {
        super(loadMoleculeObservable);
        m_file = file;
    }
    protected void loadMolecules() throws IOException, InterruptedException {
        molecules = new MoleculeCollection();
        molecules.loadPDBFormat(new FileInputStream(m_file));
        molecules.setInputStructureFileName(m_file.getName());
    }
}
