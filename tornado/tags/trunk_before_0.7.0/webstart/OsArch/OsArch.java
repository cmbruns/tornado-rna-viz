package webstart.OsArch;

import java.awt.*;
import javax.swing.*;

public class OsArch extends JFrame {
    static final long serialVersionUID = 1L;
  public OsArch() {
    JLabel osLabel = new JLabel("os = " + System.getProperty("os.name"));
    JLabel archLabel = new JLabel("arch = " + System.getProperty("os.arch"));
    this.getContentPane().add(osLabel, BorderLayout.CENTER);
    this.getContentPane().add(archLabel, BorderLayout.SOUTH);
    pack();
    setVisible(true);
  }
  public static void main(String[] args) {
    new OsArch();
  }
}

