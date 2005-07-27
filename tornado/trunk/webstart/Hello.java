package webstart;

import java.awt.*;
import javax.swing.*;

public class Hello extends JFrame {
    static final long serialVersionUID = 1L;
  public Hello() {
    JLabel label = new JLabel("Hello");
    this.getContentPane().add(label, BorderLayout.CENTER);
    pack();
    setVisible(true);
  }
  public static void main(String[] args) {
    new Hello();
  }
}

