package com.davidbracewell.hermes.driver;

import com.davidbracewell.Tag;
import com.davidbracewell.application.SwingApplication;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.EntityType;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author David B. Bracewell
 */
public class AnnotationEditor extends SwingApplication {
   private JTextPane editorPane;
   private Map<Tag, Color> types = new TreeMap<>();


   public static void main(String[] args) {
      new AnnotationEditor().run(args);
   }

   private void addTag(Tag tag, DataModel model) {
      int start = editorPane.getSelectionStart();
      int end = editorPane.getSelectionEnd();
      String txt = editorPane.getText();
      while (start < end && Character.isWhitespace(txt.charAt(start))) {
         start++;
      }
      while (end > start && Character.isWhitespace(txt.charAt(end - 1))) {
         end--;
      }

      if (start == end) {
         return;
      }
      editorPane.setSelectionStart(start);
      editorPane.setSelectionEnd(end);
      StyleContext context = StyleContext.getDefaultStyleContext();
      AttributeSet aset = context.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.WHITE);
      aset = context.addAttribute(aset, StyleConstants.Bold, true);
      aset = context.addAttribute(aset, StyleConstants.Background, types.get(tag));
      editorPane.setCharacterAttributes(aset, false);
      model.addRow(new Object[]{start, end, tag, editorPane.getSelectedText()});
   }

   @Override
   public void setup() throws Exception {
      EntityType
         .values()
         .forEach(e -> types.put(e, Color.BLUE));


      setTitle("Annotation Editor");
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      setLayout(new BorderLayout());
      setMinimumSize(new Dimension(800, 600));

      JMenuBar menuBar = new JMenuBar();
      JMenu menu = new JMenu("File");
      menu.setMnemonic(KeyEvent.VK_F);

      menu.add(new JMenuItem("Open", KeyEvent.VK_O));
      menu.addSeparator();
      JMenuItem exit = new JMenuItem("Quit", KeyEvent.VK_Q);
      menu.add(exit);
      menuBar.add(menu);
      setJMenuBar(menuBar);


      JPanel panel = new JPanel();
      panel.setLayout(new FlowLayout());
      add(panel, BorderLayout.NORTH);


      JSplitPane splitPane = new JSplitPane();
      splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
      splitPane.setDividerLocation(300);
      add(splitPane, BorderLayout.CENTER);

      DefaultStyledDocument document = new DefaultStyledDocument();
      editorPane = new JTextPane(document);
      editorPane.setEditable(false);
      editorPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
      editorPane.setText("Now is the time for all good men to come to the aid of their country.");
      splitPane.add(editorPane, JSplitPane.TOP);


      JTable table = new JTable(new DataModel());
      JComboBox<Tag> typeCombobox = new JComboBox<>(this.types.keySet().toArray(new Tag[this.types.size()]));
      table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(typeCombobox));
      table.getColumnModel().getColumn(0).setPreferredWidth(50);
      table.getColumnModel().getColumn(1).setPreferredWidth(50);
      table.getColumnModel().getColumn(2).setPreferredWidth(500);
      table.getColumnModel().getColumn(3).setPreferredWidth(200);
      table.setShowGrid(true);
      JScrollPane tblContainer = new JScrollPane(table);
      table.setFillsViewportHeight(true);
      splitPane.add(tblContainer, JSplitPane.BOTTOM);
      DataModel model = Cast.as(table.getModel());
      table.getTableHeader().setReorderingAllowed(false);
      table.setAutoCreateRowSorter(true);

      table.addMouseListener(new MouseListener() {
         @Override
         public void mouseClicked(MouseEvent e) {
            int row = table.getSelectedRow();
            int start = Integer.parseInt(model.getValueAt(row, 0).toString());
            int end = Integer.parseInt(model.getValueAt(row, 1).toString());
            editorPane.setSelectionStart(start);
            editorPane.setSelectionEnd(end);
         }

         @Override
         public void mouseEntered(MouseEvent e) {

         }

         @Override
         public void mouseExited(MouseEvent e) {

         }

         @Override
         public void mousePressed(MouseEvent e) {

         }

         @Override
         public void mouseReleased(MouseEvent e) {

         }
      });

      JPopupMenu popupMenu = new JPopupMenu();
      editorPane.setComponentPopupMenu(popupMenu);

      for (Tag tag : types.keySet()) {
         JButton button = new JButton(tag.name());
         panel.add(button);
         JMenuItem menuItem = new JMenuItem(tag.name());
         popupMenu.add(menuItem);
         button.addActionListener(a -> addTag(tag, model));
         menuItem.addActionListener(a -> addTag(tag, model));
      }

   }

   class DataModel extends AbstractTableModel {
      private java.util.List<Object[]> rows = new ArrayList<>();
      private int columns = 4;

      public void addRow(Object[] row) {
         rows.add(row);
         fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
      }

      @Override
      public Class<?> getColumnClass(int columnIndex) {
         switch (columnIndex) {
            case 0:
            case 1:
               return Integer.class;
            case 2:
               return Type.class;
            default:
               return String.class;
         }
      }

      @Override
      public int getColumnCount() {
         return columns;
      }

      @Override
      public String getColumnName(int column) {
         switch (column) {
            case 0:
               return "Start";
            case 1:
               return "End";
            case 2:
               return "Type";
            default:
               return "Surface";
         }
      }

      @Override
      public int getRowCount() {
         return rows.size();
      }

      @Override
      public Object getValueAt(int rowIndex, int columnIndex) {
         return rows.get(rowIndex)[columnIndex];
      }

      @Override
      public boolean isCellEditable(int rowIndex, int columnIndex) {
         return columnIndex == 2;
      }

      @Override
      public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
         rows.get(rowIndex)[columnIndex] = aValue;
         if (columnIndex == 2) {
            int start = (int) rows.get(rowIndex)[0];
            int end = (int) rows.get(rowIndex)[1];
            int oldS = editorPane.getSelectionStart();
            int oldE = editorPane.getSelectionEnd();

            editorPane.setSelectionStart(start);
            editorPane.setSelectionEnd(end);

            StyleContext context = StyleContext.getDefaultStyleContext();
            AttributeSet aset = context.addAttribute(SimpleAttributeSet.EMPTY,
                                                     StyleConstants.Foreground,
                                                     Color.WHITE);
            aset = context.addAttribute(aset,
                                        StyleConstants.Bold,
                                        true);
            aset = context.addAttribute(aset,
                                        StyleConstants.Background,
                                        types.get(aValue));
            editorPane.setCharacterAttributes(aset, false);

            editorPane.setSelectionStart(oldS);
            editorPane.setSelectionEnd(oldE);
            fireTableCellUpdated(rowIndex, columnIndex);
         }
      }
   }

}// END OF Swinger
