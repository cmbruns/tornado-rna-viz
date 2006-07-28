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
import edu.stanford.ejalbert.BrowserLauncher;

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
    private ProgressDialog progressDialog = null;
    
    private LoadPdbUrlProcess loadPDBProcess = null;
    private ProgressManager progressManager = null;
    
    protected SimpleObservable fileLoadObservable = new SimpleObservable();
    private String currentPath = ".";
    
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
   
    public MoleculeAcquisitionMethodDialog(JFrame f, ProgressDialog p) {
        super(f);
        // System.out.println("MoleculeAcquisitionMethodDialog constructor");
        setMAMD_Params(f, p);
    }

	private void setMAMD_Params(JFrame f, ProgressDialog p) {
		parent = f;
        progressDialog = p;
        initializeDialog();
        fileLoadObservable.addObserver(this);
	}
    
    public MoleculeAcquisitionMethodDialog(JFrame f, ProgressDialog p, String curPath) {
        super(f);
        currentPath = curPath;
        setMAMD_Params(f, p);
    }
    
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

        //////////////// Load from File ///////////////////
        
        JPanel loadFromFilePanel = new JPanel();
        
        loadFromFilePanel.add(new JLabel("Option 1: "));
        
        loadFileButton = new JButton("Load file...");
        loadFileButton.addActionListener(this);
        loadFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadFromFilePanel.add(loadFileButton);

        contentPanel.add(loadFromFilePanel);

        contentPanel.add(Box.createRigidArea(new Dimension(0,8)));
        contentPanel.add(new JSeparator());
        contentPanel.add(Box.createRigidArea(new Dimension(0,8)));
        
        
        ////////////////// Load from PDB Web site ///////////////////
        
        JPanel webPDBPanel = new JPanel();
        webPDBPanel.setLayout(new BoxLayout(webPDBPanel, BoxLayout.X_AXIS));

        JPanel docPanel = new JPanel();
        docPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        docPanel.setLayout(new BoxLayout(docPanel, BoxLayout.Y_AXIS));
        docPanel.add(new JLabel("Option 2: "));
        docPanel.add(Box.createVerticalStrut(10));
        docPanel.add(new JLabel("From Protein"));
        docPanel.add(new JLabel("Data Bank (PDB)"));
        docPanel.add(new PdbLinkLabel("Web Site"));
        
        webPDBPanel.add(docPanel);
        
        JPanel formPanel = new JPanel();
        formPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.add(new JLabel("Enter PDB ID"));
        formPanel.add(new JLabel("(4 characters)"));
        
        // label = new JLabel(" PDB ID (4 characters): ");
        // webPDBPanel.add(label);

        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.X_AXIS));
        fieldPanel.add(Box.createHorizontalGlue());
        idField = new JTextField(defaultPdbId, 4);
        idField.addActionListener(this);
        idField.setMaximumSize(idField.getPreferredSize());
        idField.setMinimumSize(idField.getPreferredSize());
        idField.setAlignmentX(Component.CENTER_ALIGNMENT);
        fieldPanel.add(idField);
        fieldPanel.add(Box.createHorizontalGlue());

        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(fieldPanel);
        formPanel.add(Box.createVerticalStrut(5));

        webPDBButton = new JButton("Fetch");
        webPDBButton.addActionListener(this);
        formPanel.add(webPDBButton);
        
        webPDBPanel.add(formPanel);

        webPDBPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(webPDBPanel);

        String unitOptions[] = {"Biological Unit (recommended)", "Crystallographic Unit"};
        bioUnitList = new JComboBox(unitOptions);
        bioUnitList.setSelectedIndex(0); // biological unit
        // This option is unecessary -- advanced users can go straight to the PDB
        // contentPanel.add(bioUnitList);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0,8)));
        contentPanel.add(new JSeparator());

        contentPanel.add(Box.createVerticalGlue());

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
        
        // Tell everyone we are busy
        inactivate();

        // Load structure from file
        if ( e.getSource() == loadFileButton ) {
            // Load molecule from file using file browser dialog

            String [] extensions = {"pdb", "pdb1", "pdb2", "pdb3", "pqr"};
            if (moleculeFileChooser == null) moleculeFileChooser = new MoleculeFileChooser(parent, extensions, currentPath);

            // Hide first dialog while the file chooser is shown
            setVisible(false);

            File inputFile = moleculeFileChooser.getFile();
    	    currentPath = moleculeFileChooser.getCurrentDirectory().getPath();
	        currentPathUpdated();

            // Convert to URL and start the loading process
            
            if (inputFile != null) {
                try {
                    URL moleculeUrl = inputFile.toURI().toURL();
                    handleMoleculeUrl(moleculeUrl);
                }
                catch (IOException exc) {
                    // failure
    
                    // Show error dialog
                    JOptionPane.showMessageDialog(this, 
                            "Unexpected error: problem creating URL from file name: " +
                            inputFile.getName());
    
                    setVisible(true);
                }
            }
            
            // No file was loaded, give the user another chance by reactivating the method dialog
            else setVisible(true);
            
        }

        // Download structure from PDB web site
        else if ( (e.getSource() == webPDBButton) ||
             (e.getSource() == idField) ) {

            // Tell everyone we are busy
            inactivate();
            
            // Load PDB molecule from the internet
            String pdbId = idField.getText().trim().toLowerCase();
            setPdbId(pdbId.toUpperCase());
            
            // Get the PDB file over the internet
            boolean isBioUnit = true;
            if (bioUnitList.getSelectedIndex() != 0)
                isBioUnit = false;
            
            // Put call to handleMoleculeURL here
            try {
                URL moleculeUrl = WebPDB.getWebPdbUrl(pdbId, isBioUnit);
                handleMoleculeUrl(moleculeUrl);
            }
            catch (IOException exc) {
                // failure

                // Show error dialog
                JOptionPane.showMessageDialog(this, 
                        "Unexpected error: problem downloading PDB structure: " +
                        pdbId);

                setVisible(true);
            }
             
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
            setVisible(false); // hide dialog
        }
        
        reactivate();
    }

    protected void currentPathUpdated() {
    	//empty stub, for example use see Tornado.LoadStructureDialog
    }
    
    protected void handleMoleculeUrl(URL moleculeURL) throws IOException {
        
        loadPDBProcess = 
            new LoadPdbUrlProcess(moleculeURL, fileLoadObservable);
        loadPDBProcess.start();
        
        // And another process to monitor the download process
        if (progressDialog == null)
            progressManager = 
                new ProgressManager(
                        loadPDBProcess, 
                        this, 
                        "Loading structure from " + moleculeURL);
        else 
            progressManager = new ProgressManager(loadPDBProcess, progressDialog);
        
        progressManager.start();        
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
            
            if ( (molecules.getPdbId() == null) && (getPdbId() != null) )
                molecules.setPdbId(getPdbId());
            
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

	public String getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
	}
}


class MoleculeFileChooser extends JFileChooser {
    static final long serialVersionUID = 01L;
    Frame parentFrame;
    
    MoleculeFileChooser(Frame frame, String[] extensions) {
        setMFC_Params(frame, extensions);
    }

	private void setMFC_Params(Frame frame, String[] extensions) {
		parentFrame = frame;
        setDialogTitle("Choose molecule structure file");
        FileNameFilter filter = new FileNameFilter("molecule structure files", extensions);
        setFileFilter(filter);
	}

    MoleculeFileChooser(Frame frame, String[] extensions, String curPath) {
    	super(curPath);
        setMFC_Params(frame, extensions);
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

class PdbLinkLabel extends JLinkLabel {
    PdbLinkLabel(String text) {super(text);}
    
    @Override
    protected void respondToClick() {
        String urlString = "http://www.rcsb.org/pdb/";
        try {BrowserLauncher.openURL(urlString);}
        catch (IOException exc2) {
            JOptionPane.showMessageDialog(
                    this, 
                    "Error: failed to launch browser to PDB site "+urlString,
                    "Error: Browser launch error",
                    JOptionPane.ERROR_MESSAGE);
        }        
    }
}
