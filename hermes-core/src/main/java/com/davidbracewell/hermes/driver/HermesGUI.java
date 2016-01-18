package com.davidbracewell.hermes.driver;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * @author David B. Bracewell
 */
public class HermesGUI extends JFrame {

  private JMenuBar menuBar;
  private JSplitPane basePane;
  private JTextPane textPane;
  private JTree annotationList;

  public HermesGUI() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }
    menuBar = new JMenuBar();
    menuBar.add(createFileMenu());
    textPane = new JTextPane();
    annotationList = new JTree();
    annotationList.setModel(null);
    JSplitPane contentPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(textPane), new JScrollPane(new JPanel()));
    contentPane.setDividerLocation(400);
    basePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(annotationList), contentPane);
    basePane.setDividerLocation(200);
    setSize(800, 600);
    setJMenuBar(menuBar);
    add(basePane);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      new HermesGUI().setVisible(true);
    });
  }

  JMenu createFileMenu() {
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);

    JMenuItem open = new JMenuItem("Open", KeyEvent.VK_O);
    open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
    open.addActionListener(this::fileOpen);
    fileMenu.add(open);

    JMenuItem select = new JMenuItem("Select");
    select.addActionListener(this::fileSelect);
    fileMenu.add(select);

    fileMenu.addSeparator();
    JMenuItem exit = new JMenuItem("Exit", KeyEvent.VK_X);
    exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
    exit.addActionListener(this::fileExit);
    fileMenu.add(exit);


    return fileMenu;
  }

  void fileSelect(ActionEvent e) {
    StyledDocument doc = textPane.getStyledDocument();
    Style s = textPane.addStyle("BLUE", null);
    StyleConstants.setBackground(s, Color.BLUE);
    StyleConstants.setForeground(s, Color.WHITE);
    doc.setCharacterAttributes(textPane.getSelectionStart(), textPane.getSelectionEnd() - textPane.getSelectionStart(), s, true);
  }

  void fileOpen(ActionEvent e) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.showOpenDialog(null);
  }

  void fileExit(ActionEvent e) {
    dispose();
  }

}// END OF HermesGUI
