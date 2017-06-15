package com.davidbracewell.hermes.driver;

import com.davidbracewell.Tag;
import com.davidbracewell.application.SwingApplication;
import com.davidbracewell.cli.Option;
import com.davidbracewell.collection.map.Maps;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.conversion.Convert;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.spi.TaggedFormat;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.json.Json;
import com.davidbracewell.reflection.Reflect;
import com.davidbracewell.reflection.ReflectionException;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Throwables;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * @author David B. Bracewell
 */
public class AnnotationEditor extends SwingApplication {
   private JTextPane editorPane;
   private Map<Tag, Color> attributeTypeColorMap = new TreeMap<>();
   private AnnotationType annotationType;
   private AttributeType attributeType;
   private JPopupMenu editorPopup;
   private JPanel toolbarPanel;
   private String taskName;
   private JTable annotationTable;
   private DataModel annotationTableModel;
   private boolean isTagged;

   @Option(description = "JSON file describing the annotation task.",
      defaultValue = "/home/dbb/prj/personal/hermes/hermes-core/config.json")
   private Resource task;

   public static void main(String[] args) {
      new AnnotationEditor().run(args);
   }

   private void addTag(Tag tag) {
      int start = editorPane.getSelectionStart();
      int end = editorPane.getSelectionEnd();
      if (!annotationTableModel.spanHasAnnotation(start, end)) {
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
         annotationTableModel.addRow(new Object[]{start, end, tag, editorPane.getSelectedText()});
         for (Component component : toolbarPanel.getComponents()) {
            component.setEnabled(false);
         }
      }
   }

   private JScrollPane createAnnotationTable() {
      annotationTable = new JTable(new DataModel());
      annotationTableModel = Cast.as(annotationTable.getModel());
      JComboBox<Tag> typeCombobox = new JComboBox<>(this.attributeTypeColorMap.keySet()
                                                                              .toArray(
                                                                                 new Tag[this.attributeTypeColorMap
                                                                                            .size()]));
      annotationTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(typeCombobox));
      annotationTable.getColumnModel().getColumn(0).setPreferredWidth(75);
      annotationTable.getColumnModel().getColumn(1).setPreferredWidth(75);
      annotationTable.getColumnModel().getColumn(2).setPreferredWidth(300);
      annotationTable.getColumnModel().getColumn(3).setPreferredWidth(350);
      annotationTable.setShowGrid(true);
      JScrollPane tblContainer = new JScrollPane(annotationTable);
      annotationTable.setFillsViewportHeight(true);
      annotationTable.getTableHeader().setReorderingAllowed(false);
      annotationTable.setAutoCreateRowSorter(true);
      annotationTable.getRowSorter().toggleSortOrder(0);
      annotationTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {

         @Override
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            row = table.getRowSorter().convertRowIndexToModel(row);
            Color c = attributeTypeColorMap.get(annotationTableModel.getValueAt(row, column));
            setForeground(getFontColor(c));
            setBackground(c);
            return this;
         }
      });

      annotationTable.addMouseListener(mousePressed(e -> {
         int row = annotationTable.getRowSorter().convertRowIndexToModel(annotationTable.getSelectedRow());
         System.err.println("row=" + row);
         if (row >= 0) {
            editorPane.setSelectionStart(annotationTableModel.getStart(row));
            editorPane.setSelectionEnd(annotationTableModel.getEnd(row));
         }
      }));

      JPopupMenu tablePopup = new JPopupMenu();
      JMenuItem deleteAnnotation = new JMenuItem("Delete");
      tablePopup.add(deleteAnnotation);
      annotationTable.setComponentPopupMenu(tablePopup);
      deleteAnnotation.addMouseListener(mouseReleased(e -> deleteAnnotation(annotationTable.getSelectedRows())));
      return tblContainer;
   }

   private JTextPane createEditor() {
      DefaultStyledDocument document = new DefaultStyledDocument();
      editorPane = new JTextPane(document);
      editorPane.setEditable(false);
      editorPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
      this.attributeTypeColorMap.forEach((t, c) -> {
         Style style = editorPane.addStyle(t.name(), null);
         StyleConstants.setForeground(style, getFontColor(c));
         StyleConstants.setBackground(style, c);
      });
      editorPane.setComponentPopupMenu(createEditorPopup());
      editorPane.addCaretListener(e -> {
         if (annotationTableModel.spanHasAnnotation(editorPane.getSelectionStart(), editorPane.getSelectionEnd())) {
            for (Component component : toolbarPanel.getComponents()) {
               component.setEnabled(false);
            }
         } else {
            for (Component component : toolbarPanel.getComponents()) {
               component.setEnabled(true);
            }
         }
      });
      editorPane.addMouseListener(mouseClicked(this::syncEditorSelection));
      editorPane.addMouseListener(mousePressed(this::syncEditorSelection));
      return editorPane;
   }

   private JPopupMenu createEditorPopup() {
      editorPopup = new JPopupMenu();
      JMenuItem delete = new JMenuItem("Delete");
      editorPopup.add(delete);
      delete.addActionListener(e -> deleteAnnotation());
      JMenu addAnnotation = new JMenu("Add...");
      editorPopup.add(addAnnotation);
      for (Tag tag : attributeTypeColorMap.keySet()) {
         JMenuItem menuItem = new JMenuItem(tag.name());
         addAnnotation.add(menuItem);
         menuItem.addActionListener(a -> addTag(tag));
      }

      editorPopup.addPopupMenuListener(popupMenuWillBecomeVisible(e -> {
         int start = editorPane.getSelectionStart();
         int end = editorPane.getSelectionEnd();
         if (annotationTableModel.spanHasAnnotation(start, end)) {
            editorPopup.getSubElements()[0].getComponent().setEnabled(true);
            editorPopup.getSubElements()[1].getComponent().setEnabled(false);
         } else {
            editorPopup.getSubElements()[0].getComponent().setEnabled(false);
            editorPopup.getSubElements()[1].getComponent().setEnabled(true);
         }
      }));

      return editorPopup;
   }

   private void createToolbar() {
      toolbarPanel = new JPanel();
      toolbarPanel.setLayout(new FlowLayout());
      add(toolbarPanel, BorderLayout.NORTH);
      for (Tag tag : attributeTypeColorMap.keySet()) {
         JButton button = new JButton(tag.name());
         button.addActionListener(a -> addTag(tag));
         toolbarPanel.add(button);
      }

      JButton save = new JButton("SAVE");
      toolbarPanel.add(save);
      save.addActionListener(e -> {
         try {
            Document d = DocumentFactory.getInstance().createRaw(editorPane.getText());
            annotationTableModel.annotations.forEach(a -> d.createAnnotation(a.getType(), a.start(), a.end(),
                                                                             Maps.map(attributeType,
                                                                                      a.getTag().orElse(null))));
            TaggedFormat format = new TaggedFormat();
            Resources.from("/home/dbb/out.txt").write(format.toString(d));
         } catch (Exception ee) {
            ee.printStackTrace();
         }
      });

      JButton load = new JButton("LOAD");
      toolbarPanel.add(load);
      load.addActionListener(e -> {
         try {
            loadDocument(Corpus.builder()
                               .source(Resources.from("/home/dbb/test.txt"))
                               .format("TAGGED")
                               .build()
                               .stream().first().orElse(null));

         } catch (Exception ee) {
            ee.printStackTrace();
         }
      });
   }

   private void deleteAnnotation() {
      int row = annotationTableModel.find(editorPane.getSelectionStart(), editorPane.getSelectionEnd());
      if (row >= 0) {
         deleteAnnotation(new int[]{
            annotationTable.getRowSorter().convertRowIndexToView(row)
         });
      }
   }

   private void deleteAnnotation(int[] rows) {
      for (int i = rows.length - 1; i >= 0; i--) {
         int r = annotationTable.getRowSorter().convertRowIndexToModel(rows[i]);
         if (r >= 0 && r < annotationTable.getRowCount()) {
            int start = annotationTableModel.getStart(r);
            int end = annotationTableModel.getEnd(r);
            annotationTableModel.removeRow(r);
            editorPane.getStyledDocument()
                      .setCharacterAttributes(start, end - start, editorPane.getStyle(StyleContext.DEFAULT_STYLE),
                                              true);
         }
      }
   }

   private Color generateRandomColor() {
      final Color baseColor = Color.WHITE;
      Random rnd = new Random();
      int red = (rnd.nextInt(256) + baseColor.getRed()) / 2;
      int green = (rnd.nextInt(256) + baseColor.getGreen()) / 2;
      int blue = (rnd.nextInt(256) + baseColor.getBlue()) / 2;
      return new Color(red, green, blue);
   }

   private Color getFontColor(Color background) {
      double a = 1 - (0.229 * background.getRed() + 0.587 * background.getGreen() + 0.114 * background.getBlue()) / 255;
      if (a < 0.5) {
         return Color.BLACK;
      }
      return Color.WHITE;
   }

   private void loadConfig() {
      try {
         Map<String, Object> map = Json.qloads(task);
         taskName = map.getOrDefault("task", "").toString();
         annotationType = AnnotationType.valueOf(map.get("annotation").toString());
         attributeType = annotationType.getTagAttribute();
         isTagged = true;
         final Reflect cReflect = Reflect.onClass(Color.class);
         Cast.<Map<String, String>>as(map.get("values")).forEach((type, color) -> {
            Tag tag = Cast.as(Convert.convert(type, attributeType.getValueType().getType()));
            Color c = null;
            if (StringUtils.isNullOrBlank(color)) {
               c = generateRandomColor();
            } else {
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
            }
            attributeTypeColorMap.put(tag, c);
         });
      } catch (IOException e) {
         throw Throwables.propagate(e);
      }
   }

   private void loadDocument(Document doc) {
      editorPane.setText(doc.toString());
      annotationTableModel.annotations.clear();
      annotationTableModel.rows.clear();
      annotationTableModel.fireTableDataChanged();
      doc.get(annotationType)
         .forEach(a -> {
            Object[] row = {
               a.start(),
               a.end(),
               a.getTag().orElse(null),
               a.toString()
            };
            annotationTableModel.addRow(row);
            editorPane.getStyledDocument()
                      .setCharacterAttributes(a.start(),
                                              a.length(),
                                              editorPane.getStyle(a.getTag().orElse(null).name()),
                                              true);
         });
   }

   @Override
   public void setup() throws Exception {
      loadConfig();
      if (StringUtils.isNullOrBlank(taskName)) {
         setTitle("Annotation Editor");
      } else {
         setTitle("Annotation Editor [" + taskName + "]");
      }
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

      JSplitPane splitPane = new JSplitPane();
      splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
      splitPane.setDividerLocation(300);
      add(splitPane, BorderLayout.CENTER);

      splitPane.add(createAnnotationTable(), JSplitPane.BOTTOM);
      splitPane.add(createEditor(), JSplitPane.TOP);
      createToolbar();


   }

   private void syncEditorSelection(MouseEvent e) {
      int start = editorPane.getSelectionStart();
      int end = editorPane.getSelectionEnd();
      boolean isSelection = (end > start);
      if (!isSelection) {
         end++;
      }
      if (start >= 0) {
         int modelRow = annotationTableModel.find(start, end);
         if (modelRow < 0) {
            return;
         }
         int viewRow = annotationTable.getRowSorter().convertRowIndexToView(modelRow);
         annotationTable.getSelectionModel().setSelectionInterval(viewRow, viewRow);
         editorPane.setSelectionStart(annotationTableModel.getStart(modelRow));
         editorPane.setSelectionEnd(annotationTableModel.getEnd(modelRow));
      }
   }

   class DataModel extends AbstractTableModel {
      private AnnotationTree annotations = new AnnotationTree();
      private java.util.List<Object[]> rows = new ArrayList<>();
      private int columns = 4;

      public void addRow(Object[] row) {
         rows.add(row);
         int start = (int) row[0];
         int end = (int) row[1];
         Annotation annotation = Fragments.detachedAnnotation(annotationType, start, end);
         if (isTagged) {
            Tag tag = (Tag) row[2];
            annotation.put(attributeType, tag);
         }
         annotations.add(annotation);
         fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
      }

      private int find(int start, int end) {
         java.util.List<Annotation> annotationList = annotations.overlapping(new Span(start, end));
         if (annotationList.isEmpty()) {
            return -1;
         }
         start = annotationList.get(0).start();
         end = annotationList.get(0).end();
         for (int i = 0; i < rows.size(); i++) {
            if (getStart(i) == start && getEnd(i) == end) {
               return i;
            }
         }
         return -1;
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
         int start = getStart(index);
         int end = getEnd(index);
         annotations.removeAll(annotations.overlapping(new Span(start, end)));
         rows.remove(index);
         fireTableDataChanged();
      }

      @Override
      public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
         rows.get(rowIndex)[columnIndex] = aValue;
         if (columnIndex == 2) {
            int start = (int) rows.get(rowIndex)[0];
            int end = (int) rows.get(rowIndex)[1];
            editorPane.getStyledDocument()
                      .setCharacterAttributes(start,
                                              end - start,
                                              editorPane.getStyle(((Tag) rows.get(rowIndex)[2]).name()),
                                              true);
            fireTableCellUpdated(rowIndex, columnIndex);
         }
      }

      public boolean spanHasAnnotation(int start, int end) {
         return annotations.overlapping(new Span(start, end)).size() > 0;
      }
   }

}// END OF Swinger
