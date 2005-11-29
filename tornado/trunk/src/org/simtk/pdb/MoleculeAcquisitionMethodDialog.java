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
    
    private LoadPDBProcess loadPDBProcess = null;
    private ProgressManager progressManager = null;
    
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

       // The cancel button should be left enabled to stop the load process
       // cancelButton.setEnabled(buttonsAreEnabled);

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

        setPdbId(null);
        
        // Load structure from file
        if ( e.getSource() == loadFileButton ) {
            // Load molecule from file using file browser dialog

            // Tell everyone we are busy
            inactivate();

            String [] extensions = {"pdb", "pqr"};
            if (moleculeFileChooser == null) moleculeFileChooser = new MoleculeFileChooser(parent, extensions);

            // Hide first dialog while the file chooser is shown
            setVisible(false);

            File inputFile = moleculeFileChooser.getFile();

            loadPDBProcess = 
                new LoadFilePDBProcess(inputFile, fileLoadObservable);
            loadPDBProcess.start();
            
            // And another process to monitor the download process
            progressManager = 
                new ProgressManager(loadPDBProcess, this, "Loading structure " + inputFile.getName());
            progressManager.start();

        }

        // Download structure from PDB web site
        else if ( (e.getSource() == webPDBButton) ||
             (e.getSource() == idField) ) {

            // Tell everyone we are busy
            inactivate();
            
            // Load PDB molecule from the internet
            String pdbId = idField.getText().trim().toLowerCase();
            
            // Get the PDB file over the internet
            boolean isBioUnit = true;
            if (bioUnitList.getSelectedIndex() != 0)
                isBioUnit = false;
            
            // Start the background download process
            loadPDBProcess = new LoadWebPDBProcess(pdbId, isBioUnit, fileLoadObservable);
            loadPDBProcess.start();

            // And another process to monitor the download process
            progressManager = 
                new ProgressManager(loadPDBProcess, this, "Downloading structure " + idField.getText());
            progressManager.start();
            
            setVisible(false);
        }
        
        // Cancel
        else if ( e.getSource() == cancelButton ) {
            System.out.println("cancel");
            
            // allow process to continue, but ignore it, because
            //  this cancel routine seems to block
            cancelDownload(); // try to stop thread

            // forget that the process was ever our friend
            loadPDBProcess = null;
            reactivate();
            setVisible(false); // hide dialog
        }
    }

    // Respond to successful file load
    public void update(Observable observable, Object object) {
        // System.out.println("update load PDB");
        // System.out.println("  object = " + object);
        // System.out.println("  loadPDBProcess = " + loadPDBProcess);
        
        // Don't respond to stale processes
        if (object != loadPDBProcess) return;

        if (loadPDBProcess.isSuccessful()) {
            MoleculeCollection molecules = loadPDBProcess.getMolecules();
            readStructureFromMoleculeCollection(molecules);
            setVisible(false); // Hide dialog after success
        }
        
        reactivate();
    }
    
    /**
     * Cancel download in progress
     */
    void cancelDownload() {

        if ( (progressManager != null) &&
             (progressManager.isAlive()) ) {
            progressManager.abort();
        }
        
        // if ( (loadPDBProcess != null) &&
        //    (loadPDBProcess.isAlive()) ) {
        //   loadPDBProcess.abort();
        //}
    }
    
    public void reactivate() {
        setButtonsAreEnabled(true);
        setCursor(defaultCursor);
    }
    
    public void inactivate() {
        setButtonsAreEnabled(false);
        setCursor(waitCursor);
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

abstract class LoadPDBProcess extends Thread implements MonitoredProcess {
    private volatile boolean m_isSuccessful = false; // Only set true in case of success
    private volatile boolean m_isFailed = false;
    private volatile SimpleObservable m_loadMoleculeObservable;
    protected volatile MoleculeCollection molecules = null;
    private volatile InputStream m_inputStream;
    private volatile boolean isCancelled = false;
    
    LoadPDBProcess(SimpleObservable loadMoleculeObservable) {
        m_loadMoleculeObservable = loadMoleculeObservable;
    }
    
    abstract InputStream getInputStream() throws IOException;

    public synchronized void cancelLoad() {
        isCancelled = true;
        try {
            getInputStream().close();
        } catch (IOException exc) {}
        interrupt();
    }
    
    protected void loadMolecules() throws IOException, InterruptedException {
        molecules = new MoleculeCollection();
        molecules.loadPDBFormat(getInputStream());
    }
    
    public synchronized MoleculeCollection getMolecules() {return molecules;}
    
    public void run() {
        try {
            loadMolecules();
            
            if (isCancelled)
                reportFailure();
            else
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
                m_loadMoleculeObservable.notifyObservers(LoadPDBProcess.this);                    
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
                m_loadMoleculeObservable.notifyObservers(LoadPDBProcess.this);                    
            }
        });        
    }
    
    // MonitoredProcess interface
    public synchronized void abort() {
        cancelLoad();
    }   
    
    public int getProgress() {return 0;} // TODO
    public int getMinimum() {return 0;} // TODO
    public int getMaximum() {return 0;} // TODO
    public boolean isFailed() {return m_isFailed;}
    public boolean isSuccessful() {return ( (m_isSuccessful) && (!isCancelled) );}
}

class LoadWebPDBProcess extends LoadPDBProcess {
    private boolean m_isBioUnit;
    private String m_pdbId;
    private volatile InputStream m_inputStream;
    
    LoadWebPDBProcess(String pdbId, boolean isBioUnit, SimpleObservable loadMoleculeObservable) {
        super(loadMoleculeObservable);
        m_isBioUnit = isBioUnit;
        m_pdbId = pdbId;
    }
    
    InputStream getInputStream() throws IOException {
        if (m_inputStream == null) m_inputStream = WebPDB.getWebPDBStream(m_pdbId, m_isBioUnit);
        return m_inputStream;
    }
    
    protected void loadMolecules() throws IOException, InterruptedException {
        super.loadMolecules();
        
        // Remember file name
        URL pdbUrl = WebPDB.getWebPdbUrl(m_pdbId, m_isBioUnit);
        String fileName = pdbUrl.getFile();

        // strip off all but the file name (no path)
        int pathEnd = fileName.lastIndexOf("/");
        if (pathEnd >= 0) fileName = fileName.substring(pathEnd + 1);

        molecules.setInputStructureFileName(fileName);
        if (m_pdbId != null) molecules.setPdbId(m_pdbId);
    }
}

class LoadFilePDBProcess extends LoadPDBProcess {
    private File m_file;
    private InputStream m_inputStream = null;

    LoadFilePDBProcess(File file, SimpleObservable loadMoleculeObservable) {
        super(loadMoleculeObservable);
        m_file = file;
    }
    InputStream getInputStream() throws IOException {
        if (m_inputStream == null) m_inputStream = new FileInputStream(m_file);
        return m_inputStream;
    }
    protected void loadMolecules() throws IOException, InterruptedException {
        super.loadMolecules();
        molecules.setInputStructureFileName(m_file.getName());
    }
}
