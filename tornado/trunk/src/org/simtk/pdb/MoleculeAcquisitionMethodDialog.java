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

public class MoleculeAcquisitionMethodDialog extends JDialog implements ActionListener {
    static final long serialVersionUID = 01L;
    JButton loadFileButton = null;
    JButton webPDBButton = null;
    JButton cancelButton = null;
    private JTextField idField = null;            
    JComboBox bioUnitList;
    Frame parent = null;
    MoleculeFileChooser moleculeFileChooser = null;
    String defaultPdbId = "1MRP";
    private String pdbId = null;
    private String structureFileName = null;
    
    Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
    Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    
    /**
     * Overload this method to read the structure from a stream
     * @param structureStream
     * @return true if the read was successful, false if the dialog should keep prodding the user to load a structure
     */
   protected void readStructureFromStream(InputStream structureStream) throws IOException {
       if (structureStream == null) throw new IOException("Attempt to load structure from null stream");
   }
   
   public void setDefaultPdbId(String id) {
       defaultPdbId = id;
       idField.setText(id);
   }
   public String getDefaultPdbId() {return defaultPdbId;}

//   MoleculeAcquisitionMethodDialog() {
//        initializeDialog();
//   }
    
    public MoleculeAcquisitionMethodDialog(JFrame f) {
        super(f);
        // System.out.println("MoleculeAcquisitionMethodDialog constructor");
        parent = f;
        initializeDialog();
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
        setStructureFileName(null);
        
        // Load structure from file
        if ( e.getSource() == loadFileButton ) {
            // Load molecule from file using file browser dialog

            setCursor(waitCursor);
            setEnabled(false);

            InputStream fileStream = loadMoleculeFromFile();
            try {
                readStructureFromStream(fileStream);
                setVisible(false); // hide dialog after successful load
            } catch (IOException exc) {
                // TODO report problem
                setVisible(true);
            }
        }

        // Download structure from PDB web site
        else if ( (e.getSource() == webPDBButton) ||
             (e.getSource() == idField) ) {

            setCursor(waitCursor);
            setEnabled(false);

            // Load PDB molecule from the internet
            String pdbId = idField.getText().trim().toLowerCase();
            
            // Get the PDB file over the internet
            boolean isBioUnit = true;
            if (bioUnitList.getSelectedIndex() != 0)
                isBioUnit = false;
            
            try {
                InputStream webStream = WebPDB.getWebPDBStream(pdbId, isBioUnit);

                // Remember PDBId used
                setPdbId(idField.getText());

                // Remember file name
                URL pdbUrl = WebPDB.getWebPdbUrl(pdbId, isBioUnit);
                String fileName = pdbUrl.getFile();

                // strip off all but the file name (no path)
                int pathEnd = fileName.lastIndexOf("/");
                if (pathEnd >= 0) fileName = fileName.substring(pathEnd + 1);

                setStructureFileName(fileName);
                
                readStructureFromStream(webStream);
                setVisible(false);
            } catch (IOException exc) {
                // TODO report problem
                setVisible(true);
            }
        }
        
        // Cancel
        else if ( e.getSource() == cancelButton ) {
            setVisible(false);
        }

        setEnabled(true);
        setCursor(defaultCursor);
    }

    protected String getStructureFileName() {return structureFileName;}
    protected void setStructureFileName(String fName) {structureFileName = fName;}
    
    InputStream loadMoleculeFromFile() {
        String [] extensions = {"pdb", "pqr"};
        if (moleculeFileChooser == null) moleculeFileChooser = new MoleculeFileChooser(parent, extensions);

        File inputFile = moleculeFileChooser.getFile();
        if (null == inputFile) return null;
        
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(inputFile);
            setStructureFileName(inputFile.getName());
        }
        catch (FileNotFoundException exc) {
            String[] options = {"Bummer!"};
            JOptionPane.showOptionDialog(null, "No such file: " + inputFile, "File Error!",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                    null, options, options[0]);
            return null;
        }
        return inStream;
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

