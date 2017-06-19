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
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.spi.TaggedFormat;
import com.davidbracewell.io.CSV;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.json.Json;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Throwables;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author David B. Bracewell
 */
public class AnnotationEditor extends SwingApplication {

   private Deque<JMenuItem> mru = new LinkedList<>();
   private Properties properties;
   private JTextPane editorPane;
   private TagSet tagSet;
   private AnnotationType annotationType;
   private AttributeType attributeType;
   private JPopupMenu editorPopup;
   private String taskName;
   private JTable annotationTable;
   private DataModel annotationTableModel;
   private Style DEFAULT;
   private boolean isTagged;
   @Option(description = "JSON file describing the annotation task.",
      defaultValue = "classpath:com/davidbracewell/hermes/editor/entity.json")
   private Resource task;
   private Resource propertiesFile;

   public static void main(String[] args) {
      new AnnotationEditor().run(args);
   }

   private void addTag(int start, int end, Tag tag) {
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

   private void addTag(Tag tag) {
      addTag(editorPane.getSelectionStart(), editorPane.getSelectionEnd(), tag);
   }

   private JComponent createAnnotationTable() {
      annotationTable = new JTable(new DataModel());
      annotationTableModel = Cast.as(annotationTable.getModel());
      annotationTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(tagSet));
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
            if (isSelected) {
               setForeground(table.getSelectionForeground());
               setBackground(table.getSelectionBackground());
            } else {
               setForeground(tagSet.getForegroundColor(annotationTableModel.getValueAt(row, column)));
               setBackground(tagSet.getBackgroundColor(annotationTableModel.getValueAt(row, column)));
            }
            tagSet.setSelectedItem(value);
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

   private JComponent createEditor() {
      JTextArea lines = new JTextArea("1");
      editorPane = new JTextPane(new DefaultStyledDocument());
      editorPane.setEditable(false);
      editorPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
      editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
      editorPane.getCaret().setVisible(true);
      DEFAULT = editorPane.addStyle("DEFAULT", null);
      editorPane.setCharacterAttributes(DEFAULT, true);
      tagSet.forEach(t -> {
         Style style = editorPane.addStyle(t.name(), null);
         StyleConstants.setForeground(style, tagSet.getForegroundColor(t));
         StyleConstants.setBackground(style, tagSet.getBackgroundColor(t));
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
                  StyleConstants.setFontSize(editorPane.getStyle(StyleContext.DEFAULT_STYLE), f2.getSize());
               }
            } else if (e.isControlDown() && e.getKeyChar() == '-') {
               Font font = editorPane.getFont();
               if (font.getSize() > 12) {
                  Font f2 = new Font(font.getName(), font.getStyle(), font.getSize() - 2);
                  editorPane.setFont(f2);
               }
            }
            lines.setFont(new Font(Font.MONOSPACED, Font.PLAIN, editorPane.getFont().getSize()));
            super.keyPressed(e);
         }
      });

      editorPane.addCaretListener(e -> {
         JMenuBar menuBar = AnnotationEditor.this.getJMenuBar();
         if (menuBar == null) {
            return;
         }
         if (editorPane.getSelectionStart() == editorPane.getSelectionEnd()) {
            menuBar.getMenu(2).setEnabled(false);
         } else {
            menuBar.getMenu(2).setEnabled(true);
         }
      });


      editorPane.getCaret().setVisible(true);

      JPanel editorPanel = new JPanel();
      editorPanel.setLayout(new BorderLayout());
      editorPanel.add(editorPane);
      JScrollPane jsp = new JScrollPane(editorPanel);


      lines.setMargin(editorPane.getMargin());
      lines.setEnabled(false);
      lines.setDisabledTextColor(Color.DARK_GRAY);
      lines.setFont(new Font(Font.MONOSPACED, Font.PLAIN, editorPane.getFont().getSize()));
      editorPane.getStyledDocument().addDocumentListener(new DocumentListener() {
         @Override
         public void changedUpdate(DocumentEvent de) {
            lines.setText(getText());
         }

         public String getText() {
            int caretPosition = editorPane.getStyledDocument().getLength();
            Element root = editorPane.getStyledDocument().getDefaultRootElement();
            String text = "1" + System.getProperty("line.separator");
            for (int i = 2; i < root.getElementIndex(caretPosition) + 2; i++) {
               text += i + System.getProperty("line.separator");
            }
            return text;
         }

         @Override
         public void insertUpdate(DocumentEvent de) {
            lines.setText(getText());
         }

         @Override
         public void removeUpdate(DocumentEvent de) {
            lines.setText(getText());
         }
      });


      jsp.setRowHeaderView(lines);

      return jsp;
   }

   private JPopupMenu createEditorPopup() {
      editorPopup = new JPopupMenu();
      JMenuItem delete = new JMenuItem("Delete");
      editorPopup.add(delete);
      delete.addActionListener(e -> deleteAnnotation());
      editorPopup.add(tagSet.createTagMenu(this::addTag));
      editorPopup.addPopupMenuListener(new PopupMenuListener() {
         @Override
         public void popupMenuCanceled(PopupMenuEvent e) {
            editorPane.getCaret().setVisible(true);
         }

         @Override
         public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            editorPane.getCaret().setVisible(true);
         }

         @Override
         public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            int start = editorPane.getSelectionStart();
            int end = editorPane.getSelectionEnd();
            if (annotationTableModel.spanHasAnnotation(start, end)) {
               editorPopup.getSubElements()[0].getComponent().setEnabled(true);
               editorPopup.getSubElements()[1].getComponent().setEnabled(false);
            } else {
               editorPopup.getSubElements()[0].getComponent().setEnabled(false);
               editorPopup.getSubElements()[1].getComponent().setEnabled(true);
            }
         }
      });

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
                      .setCharacterAttributes(start, end - start, DEFAULT, true);
         }
      }
   }

   private void loadDocument(Document doc) {
      editorPane.setText(doc.toString());
      annotationTableModel.annotations.clear();
      annotationTableModel.rows.clear();
      annotationTableModel.fireTableDataChanged();
      editorPane.setCharacterAttributes(DEFAULT, true);
      doc.get(annotationType)
         .forEach(a -> {
            if (tagSet.isValid(a.getTag().orElse(null))) {
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

   private void loadTaskFile() {
      try {
         Map<String, Object> map = Json.qloads(task);
         taskName = map.getOrDefault("task", "").toString();
         annotationType = AnnotationType.valueOf(map.get("annotation").toString());
         attributeType = annotationType.getTagAttribute();
         isTagged = map.containsKey("types");
         if (map.containsKey("types")) {
            tagSet = new TagSet(Cast.as(map.get("types")), attributeType);
         }
      } catch (IOException e) {
         throw Throwables.propagate(e);
      }
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
      loadTaskFile();
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

      if (!properties.containsKey("current_directory")) {
         properties.setProperty("current_directory", new File(".").getAbsolutePath());
      }
      final JFileChooser fileChooser = new JFileChooser();
      fileOpen.addActionListener(e -> {
         fileChooser.setCurrentDirectory(new File(properties.getProperty("current_directory")));
         int returnValue = fileChooser.showOpenDialog(AnnotationEditor.this);
         if (returnValue == JFileChooser.APPROVE_OPTION) {
            mru.addFirst(createMRUItem(fileChooser.getSelectedFile().getAbsolutePath()));
            recent.insert(mru.getFirst(), 0);
            properties.setProperty("current_directory", fileChooser.getSelectedFile().getAbsolutePath());
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

      menuBar.add(tagSet.createTagMenu(this::addTag));

      JSplitPane splitPane = new JSplitPane();
      splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
      splitPane.setDividerLocation(300);
      add(splitPane, BorderLayout.CENTER);

      splitPane.add(createAnnotationTable(), JSplitPane.BOTTOM);
      splitPane.add(createEditor(), JSplitPane.TOP);

      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
   }

   private void syncEditorSelection(MouseEvent e) {
      if (e.isPopupTrigger()) {
         return;
      }
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

   private static class TagSet extends JComboBox<Tag> implements Serializable, Iterable<Tag> {
      private static final long serialVersionUID = 1L;
      private static List<Color> pallete = Arrays.asList(
         Color.BLUE,
         Color.RED,
         Color.GREEN,
         Color.YELLOW,
         Color.ORANGE,
         Color.PINK,
         Color.GRAY,
         Color.MAGENTA,
         Color.CYAN);
      private Map<Tag, Color> backgroundColors = new TreeMap<>(Comparator.comparing(Tag::name));
      private Map<Tag, Color> foregroundColors = new TreeMap<>(Comparator.comparing(Tag::name));

      public TagSet(List<String> tagNames, AttributeType attributeType) {
         setEditable(false);
         int i = 0;
         for (String tagName : tagNames) {
            Tag tag = (Tag) Convert.convert(tagName, attributeType.getValueType().getType());
            Color bg = (i < pallete.size()) ? pallete.get(i) : generateRandomColor();
            Color fg = getFontColor(bg);
            backgroundColors.put(tag, bg);
            foregroundColors.put(tag, fg);
            i++;
         }
         backgroundColors.keySet().forEach(this::addItem);
      }

      public void clear() {
         backgroundColors.clear();
         foregroundColors.clear();
         removeAllItems();
      }

      public JMenu createTagMenu(Consumer<Tag> action) {
         JMenu tagMenu = new JMenu("Tag");
         tagMenu.setEnabled(false);
         tagMenu.setMnemonic(KeyEvent.VK_T);

         int i = 0;
         for (Tag tag : backgroundColors.keySet()) {
            int adj = i % 10;
            JMenuItem button = new JMenuItem(tag.name());
            button.addActionListener(a -> action.accept(tag));

            tagMenu.add(button);
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
         return tagMenu;
      }

      private Color generateRandomColor() {
         final Color baseColor = Color.WHITE;
         Random rnd = new Random();
         int red = (rnd.nextInt(256) + baseColor.getRed()) / 2;
         int green = (rnd.nextInt(256) + baseColor.getGreen()) / 2;
         int blue = (rnd.nextInt(256) + baseColor.getBlue()) / 2;
         return new Color(red, green, blue);
      }

      public Color getBackgroundColor(Object tag) {
         return backgroundColors.getOrDefault(tag, Color.WHITE);
      }

      private Color getFontColor(Color background) {
         double a = 1 - (0.229 * background.getRed() + 0.587 * background.getGreen() + 0.114 * background.getBlue()) / 255;
         if (a < 0.5) {
            return Color.BLACK;
         }
         return Color.WHITE;
      }

      public Color getForegroundColor(Object tag) {
         return foregroundColors.getOrDefault(tag, Color.WHITE);
      }

      public boolean isValid(Tag tag) {
         return backgroundColors.containsKey(tag);
      }

      @Override
      public Iterator<Tag> iterator() {
         return backgroundColors.keySet().iterator();
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
                   .setCharacterAttributes(start, end - start, DEFAULT, true);
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
