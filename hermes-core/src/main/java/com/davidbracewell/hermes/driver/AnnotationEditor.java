package com.davidbracewell.hermes.driver;

import com.davidbracewell.Tag;
import com.davidbracewell.application.SwingApplication;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.conversion.Convert;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.AttributeType;
import com.davidbracewell.io.Resources;
import com.davidbracewell.json.Json;
import com.davidbracewell.reflection.Reflect;
import com.davidbracewell.reflection.ReflectionException;
import com.google.common.base.Throwables;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author David B. Bracewell
 */
public class AnnotationEditor extends SwingApplication {
   private JTextPane editorPane;
   private Map<Tag, Color> types = new TreeMap<>();
   private AnnotationType annotationType;
   private AttributeType attributeType;

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
      editorPane.getStyledDocument()
                .setCharacterAttributes(start,
                                        end - start,
                                        editorPane.getStyle(tag.name()),
                                        true);
      model.addRow(new Object[]{start, end, tag, editorPane.getSelectedText()});
   }

   private void loadConfig() {
      try {
         Map<String, Object> map = Json.qloads(Resources.from("/home/dbb/prj/personal/hermes/hermes-core/config.json"));
         annotationType = AnnotationType.valueOf(map.get("annotation").toString());
         attributeType = annotationType.getTagAttribute();
         final Reflect cReflect = Reflect.onClass(Color.class);
         Cast.<Map<String, String>>as(map.get("values")).forEach((type, color) -> {
            Tag tag = Cast.as(Convert.convert(type, attributeType.getValueType().getType()));
            Color c = null;
            try {
               c = cReflect.get(color.toLowerCase()).get();
            } catch (ReflectionException e) {
               e.printStackTrace();
            }
            if (c == null) {
               int r = Integer.parseInt(color.substring(0, 2), 16);
               int g = Integer.parseInt(color.substring(2, 4), 16);
               int b = Integer.parseInt(color.substring(4, 6), 16);
               c = new Color(r, g, b);
            }
            types.put(tag, c);
         });
      } catch (IOException e) {
         throw Throwables.propagate(e);
      }
   }

   @Override
   public void setup() throws Exception {
      loadConfig();
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

      this.types.forEach((t, c) -> {
         Style style = editorPane.addStyle(t.name(), null);
         StyleConstants.setForeground(style, Color.WHITE);
         StyleConstants.setBackground(style, c);
      });


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

      table.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            int row = table.getSelectedRow();
            if (row >= 0) {
               int start = Integer.parseInt(model.getValueAt(row, 0).toString());
               int end = Integer.parseInt(model.getValueAt(row, 1).toString());
               editorPane.setSelectionStart(start);
               editorPane.setSelectionEnd(end);
            }
         }
      });

      JPopupMenu tablePopup = new JPopupMenu();
      JMenuItem deleteAnnotation = new JMenuItem("Delete");
      tablePopup.add(deleteAnnotation);
      table.setComponentPopupMenu(tablePopup);
      deleteAnnotation.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseReleased(MouseEvent e) {
            int r = table.rowAtPoint(e.getPoint());
            if (r >= 0 && r < table.getRowCount()) {
               int start = model.getStart(r);
               int end = model.getEnd(r);
               model.removeRow(r);
               editorPane.getStyledDocument()
                         .setCharacterAttributes(start,
                                                 end - start,
                                                 editorPane.getStyle(StyleContext.DEFAULT_STYLE),
                                                 true);
            }
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

      public int getEnd(int row) {
         return (int) rows.get(row)[1];
      }

      @Override
      public int getRowCount() {
         return rows.size();
      }

      public int getStart(int row) {
         return (int) rows.get(row)[0];
      }

      @Override
      public Object getValueAt(int rowIndex, int columnIndex) {
         if (rowIndex < 0 || rowIndex > rows.size()) {
            return null;
         }
         return rows.get(rowIndex)[columnIndex];
      }

      @Override
      public boolean isCellEditable(int rowIndex, int columnIndex) {
         return columnIndex == 2;
      }

      public void removeRow(int index) {
         rows.remove(index);
         fireTableDataChanged();
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
