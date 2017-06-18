package com.davidbracewell.hermes.driver;

import com.davidbracewell.SystemInfo;
import com.davidbracewell.Tag;
import com.davidbracewell.application.SwingApplication;
import com.davidbracewell.cli.Option;
import com.davidbracewell.collection.Span;
import com.davidbracewell.collection.map.Maps;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.conversion.Convert;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.spi.TaggedFormat;
import com.davidbracewell.io.CSV;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.json.Json;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author David B. Bracewell
 */
public class AnnotationEditor extends SwingApplication {
   private Deque<JMenuItem> mru = new LinkedList<>();
   private Properties properties;
   private JTextPane editorPane;
   private Types validTypes = new Types();
   private AnnotationType annotationType;
   private AttributeType attributeType;
   private JPopupMenu editorPopup;
   private JPanel toolbarPanel;
   private String taskName;
   private JTable annotationTable;
   private DataModel annotationTableModel;
   private List<Color> pallete = Arrays.asList(
      Color.BLUE,
      Color.RED,
      Color.GREEN,
      Color.YELLOW,
      Color.ORANGE,
      Color.PINK,
      Color.GRAY,
      Color.MAGENTA,
      Color.CYAN);
   private boolean isTagged;
   @Option(description = "JSON file describing the annotation task.",
      defaultValue = "classpath:com/davidbracewell/hermes/editor/entity.json")
   private Resource task;
   private Resource propertiesFile;

   public static void main(String[] args) {
      new AnnotationEditor().run(args);
   }

   private void addTag(Tag tag) {
      int start = editorPane.getSelectionStart();
      int end = editorPane.getSelectionEnd();
      if (annotationTableModel.spanHasAnnotation(start, end)) {
         if (JOptionPane.showConfirmDialog(null, "Delete existing annotations on span?") == JOptionPane.OK_OPTION) {
            annotationTableModel.annotations.overlapping(new Span(start, end)).forEach(a -> {
               int r = annotationTableModel.find(a.start(), a.end());
               System.err.println(r + " : " + Arrays.toString(annotationTableModel.rows.get(r)));
               annotationTableModel.removeRow(r);
            });
         } else {
            return;
         }
      }
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
   }

   private JScrollPane createAnnotationTable() {
      annotationTable = new JTable(new DataModel());
      annotationTableModel = Cast.as(annotationTable.getModel());
      annotationTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(validTypes.typeCombobox));
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
            Color c = validTypes.colorMap.get(annotationTableModel.getValueAt(row, column));
            if (isSelected) {
               setForeground(table.getSelectionForeground());
               setBackground(table.getSelectionBackground());
            } else {
               setForeground(getFontColor(c));
               setBackground(c);
            }
            validTypes.typeCombobox.setSelectedItem(value);
            return this;
         }
      });

      annotationTable.addMouseListener(mousePressed(e -> {
         int row = annotationTable.getRowSorter().convertRowIndexToModel(annotationTable.getSelectedRow());
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
      editorPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
      editorPane.getCaret().setVisible(true);
      this.validTypes.forEach(t -> {
         Style style = editorPane.addStyle(t.name(), null);
         StyleConstants.setForeground(style, getFontColor(validTypes.getColor(t)));
         StyleConstants.setBackground(style, validTypes.getColor(t));
      });
      editorPane.setComponentPopupMenu(createEditorPopup());
      editorPane.addMouseListener(mouseClicked(this::syncEditorSelection));
      editorPane.addMouseListener(mousePressed(this::syncEditorSelection));

      editorPane.addKeyListener(new KeyAdapter() {
         @Override
         public void keyReleased(KeyEvent e) {
            if (e.isControlDown() && e.getKeyChar() == '+') {
               Font font = editorPane.getFont();
               if (font.getSize() < 24) {
                  Font f2 = new Font(font.getName(), font.getStyle(), font.getSize() + 2);
                  editorPane.setFont(f2);
               }
            } else if (e.isControlDown() && e.getKeyChar() == '-') {
               Font font = editorPane.getFont();
               if (font.getSize() > 12) {
                  Font f2 = new Font(font.getName(), font.getStyle(), font.getSize() - 2);
                  editorPane.setFont(f2);
               }
            }
            super.keyPressed(e);
         }
      });


      editorPane.getCaret().setVisible(true);

      return editorPane;
   }

   private JPopupMenu createEditorPopup() {
      editorPopup = new JPopupMenu();
      JMenuItem delete = new JMenuItem("Delete");
      editorPopup.add(delete);
      delete.addActionListener(e -> deleteAnnotation());
      JMenu addAnnotation = new JMenu("Add...");
      editorPopup.add(addAnnotation);
      for (Tag tag : validTypes) {
         JMenuItem menuItem = new JMenuItem(tag.name());
         addAnnotation.add(menuItem);
         menuItem.addActionListener(a -> addTag(tag));
      }

      editorPopup.addPopupMenuListener(popupMenuWillBecomeVisible(e -> {
         int start = editorPane.getSelectionStart();
         int end = editorPane.getSelectionEnd();
         if (annotationTableModel.spanHasAnnotation(start, end)) {
            editorPopup.getSubElements()[0].getComponent().setEnabled(true);
         } else {
            editorPopup.getSubElements()[0].getComponent().setEnabled(false);
         }
      }));

      return editorPopup;
   }

   private JMenuItem createMRUItem(String file) {
      JMenuItem item = new JMenuItem(file);
      item.addActionListener(a -> loadDocument(Corpus.builder()
                                                     .source(Resources.fromFile(file))
                                                     .format("TAGGED")
                                                     .build()
                                                     .stream().first().orElse(null)));
      return item;
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
         isTagged = map.containsKey("types");
         if (map.containsKey("types")) {
            List<String> list = Cast.as(map.get("types"));
            list.forEach(st -> validTypes.add(
               Cast.as(Convert.convert(st, attributeType.getValueType().getType()))));
         }
         validTypes.sort();
      } catch (IOException e) {
         throw Throwables.propagate(e);
      }
   }

   private void loadDocument(Document doc) {
      editorPane.setText(doc.toString());
      annotationTableModel.annotations.clear();
      annotationTableModel.rows.clear();
      annotationTableModel.fireTableDataChanged();
      editorPane.getStyledDocument()
                .setCharacterAttributes(0, editorPane.getText().length(),
                                        editorPane.getStyle(StyleContext.DEFAULT_STYLE)
                                                  .copyAttributes(),
                                        true);
      doc.get(annotationType)
         .forEach(a -> {
            if (validTypes.isValid(a.getTag().orElse(null))) {
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
            }
         });
      editorPane.setCaretPosition(0);
      editorPane.getCaret().setVisible(true);
   }

   @Override
   public void setup() throws Exception {
      properties = new Properties();
      propertiesFile = Resources.from(SystemInfo.USER_HOME)
                                .getChild(".aeditor")
                                .getChild("properties.xml");
      if (propertiesFile.exists()) {
         properties.loadFromXML(propertiesFile.inputStream());
      }
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
      menuBar.add(Box.createRigidArea(new Dimension(5, 25)));
      JMenu menu = new JMenu("File");
      menu.setMnemonic(KeyEvent.VK_F);

      JMenuItem fileOpen = new JMenuItem("Open...", KeyEvent.VK_O);
      menu.add(fileOpen);

      JMenu recent = new JMenu("Recent");
      menu.add(recent);


      if (properties.containsKey("mru")) {
         StringUtils.split(properties.get("mru").toString(), ',')
                    .forEach(file -> {
                       JMenuItem ii = createMRUItem(file);
                       recent.add(ii);
                       mru.addFirst(ii);
                    });
      }


      final JFileChooser fileChooser = new JFileChooser();
      fileOpen.addActionListener(e -> {
         int returnValue = fileChooser.showOpenDialog(AnnotationEditor.this);
         if (returnValue == JFileChooser.APPROVE_OPTION) {
            mru.addFirst(createMRUItem(fileChooser.getSelectedFile().getAbsolutePath()));
            recent.insert(mru.getFirst(), 0);

            if (mru.size() > 10) {
               recent.remove(mru.removeLast());
            }

            properties.setProperty("mru", CSV.builder()
                                             .formatter()
                                             .format(mru.stream()
                                                        .map(JMenuItem::getText)
                                                        .collect(Collectors.toList())));
            try {
               propertiesFile.getParent().mkdirs();
               properties.storeToXML(propertiesFile.outputStream(), "");
            } catch (IOException e1) {
               e1.printStackTrace();
            }

         }

         loadDocument(Corpus.builder()
                            .source(Resources.fromFile(fileChooser.getSelectedFile()))
                            .format("TAGGED")
                            .build()
                            .stream().first().orElse(null));
      });


      JMenuItem fileSave = new JMenuItem("Save As...", KeyEvent.VK_S);
      menu.add(fileSave);
      fileSave.addActionListener(e -> {
         int returnValue = fileChooser.showSaveDialog(AnnotationEditor.this);
         if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
               Document d = DocumentFactory.getInstance().createRaw(editorPane.getText());
               annotationTableModel.annotations.forEach(a -> d.createAnnotation(a.getType(), a.start(), a.end(),
                                                                                Maps.map(attributeType,
                                                                                         a.getTag().orElse(null))));
               TaggedFormat format = new TaggedFormat();
               Resources.fromFile(fileChooser.getSelectedFile()).write(format.toString(d));
            } catch (Exception ee) {
               ee.printStackTrace();
            }
         }
      });

      menu.addSeparator();
      JMenuItem exit = new JMenuItem("Quit", KeyEvent.VK_Q);
      exit.addActionListener(e -> System.exit(0));
      menu.add(exit);
      menuBar.add(menu);
      setJMenuBar(menuBar);

      JMenu addAnnotation = new JMenu("Tag");
      addAnnotation.setMnemonic(KeyEvent.VK_T);
      menuBar.add(addAnnotation);
      int i = 0;
      for (Tag tag : validTypes) {
         int adj = i % 10;
         JMenuItem button = new JMenuItem(tag.name());
         button.addActionListener(a -> addTag(tag));

         addAnnotation.add(button);
         String code = "";
         if (i >= 30) {
            code += "control alt";
         } else if (i >= 20) {
            code += "control";
         } else if (i >= 10) {
            code += " alt";
         }
         code += " pressed " + adj;
         i++;
         button.setAccelerator(KeyStroke.getKeyStroke(code));
      }

      JSplitPane splitPane = new JSplitPane();
      splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
      splitPane.setDividerLocation(300);
      add(splitPane, BorderLayout.CENTER);

      splitPane.add(createAnnotationTable(), JSplitPane.BOTTOM);
      JPanel editorPanel = new JPanel();
      editorPanel.setLayout(new BorderLayout());
      editorPanel.add(createEditor());
      splitPane.add(new JScrollPane(editorPanel), JSplitPane.TOP);

      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);


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

   class Types implements Iterable<Tag> {
      final Set<Tag> tags = new TreeSet<>();
      final Map<Tag, Color> colorMap = new TreeMap<>(Comparator.comparing(Tag::name));
      final JComboBox<Tag> typeCombobox = new JComboBox<>();
      int colorIndex = 0;

      public Types() {
         typeCombobox.setEditable(false);
      }

      void add(Tag tag) {
         if (!tags.contains(tag)) {
            tags.add(tag);
            Color c;
            if (colorIndex < pallete.size()) {
               c = pallete.get(colorIndex);
            } else {
               c = generateRandomColor();
            }
            colorMap.put(tag, c);
            typeCombobox.addItem(tag);
            colorIndex++;
         }
      }

      void addAll(List<Tag> tags) {
         tags.forEach(this::add);
         sort();
      }

      void clear() {
         tags.clear();
         colorMap.clear();
      }

      private Color getColor(Tag tag) {
         return colorMap.getOrDefault(tag, Color.WHITE);
      }

      boolean isValid(Tag tag) {
         return tags.contains(tag);
      }

      @Override
      public Iterator<Tag> iterator() {
         return tags.iterator();
      }

      void sort() {
         typeCombobox.removeAllItems();
         tags.forEach(typeCombobox::addItem);
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
         editorPane.getStyledDocument()
                   .setCharacterAttributes(start, end - start,
                                           editorPane.getStyle(StyleContext.DEFAULT_STYLE)
                                                     .copyAttributes(),
                                           true);
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
