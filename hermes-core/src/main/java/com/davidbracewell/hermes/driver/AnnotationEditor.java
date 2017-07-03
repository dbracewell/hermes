package com.davidbracewell.hermes.driver;

import com.davidbracewell.SystemInfo;
import com.davidbracewell.Tag;
import com.davidbracewell.application.SwingApplication;
import com.davidbracewell.cli.Option;
import com.davidbracewell.collection.IntervalTree;
import com.davidbracewell.collection.Span;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.conversion.Convert;
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.AttributeType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.spi.TaggedFormat;
import com.davidbracewell.io.CSV;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.json.Json;
import com.davidbracewell.string.StringUtils;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
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
   private Resource currentFile;
   private boolean isTagged;
   private boolean dirty = false;
   @Option(description = "JSON file describing the annotation task.",
      defaultValue = "classpath:com/davidbracewell/hermes/editor/entity.json")
   private Resource task;
   private Resource propertiesFile;
   private JMenu mruMenu;

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
               annotationTableModel.removeRow(r);
            });
         } else {
            return;
         }
      }
      if (start == end) {
         return;
      }
      dirty = true;
      editorPane.getStyledDocument()
                .setCharacterAttributes(start, end - start, editorPane.getStyle(tag.name()), true);
      annotationTableModel.addRow(new Object[]{start, end, tag, editorPane.getSelectedText()});
   }

   private void autoExpandSelection() {
      int start = editorPane.getSelectionStart();
      int end = editorPane.getSelectionEnd();
      String txt = editorPane.getText();
      while (start > 0 && !Character.isWhitespace(txt.charAt(start - 1)) && !StringUtils.isPunctuation(
         txt.charAt(start - 1))) {
         start--;
      }

      while (start < end && Character.isWhitespace(txt.charAt(start))) {
         start++;
      }

      while (end < txt.length() && !Character.isWhitespace(txt.charAt(end)) && !StringUtils.isPunctuation(
         txt.charAt(end))) {
         end++;
      }

      while (end > start && Character.isWhitespace(txt.charAt(end - 1))) {
         end--;
      }
      if (start == end) {
         return;
      }
      editorPane.setSelectionEnd(end);
      editorPane.setSelectionStart(start);
   }

   private void checkDirty() {
      if (dirty) {
         int r = JOptionPane.showConfirmDialog(this, "Save Changes?", "Save Changes?", JOptionPane.YES_NO_OPTION);
         if (r == JOptionPane.YES_OPTION) {
            save(currentFile);
         }
      }
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
      annotationTable.getColumnModel().getColumn(2)
                     .setCellRenderer(new DefaultTableCellRenderer() {
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

      annotationTable.getSelectionModel().addListSelectionListener(e -> {
         int row = annotationTable.getRowSorter().convertRowIndexToModel(e.getLastIndex());
         if (row >= 0) {
            editorPane.setSelectionStart(annotationTableModel.getStart(row));
            editorPane.setSelectionEnd(annotationTableModel.getEnd(row));
         }
      });
//      annotationTable.addMouseListener(mousePressed(e -> {
//         int row = annotationTable.getRowSorter().convertRowIndexToModel(annotationTable.getSelectedRow());
//         if (row >= 0) {
//            editorPane.setSelectionStart(annotationTableModel.getStart(row));
//            editorPane.setSelectionEnd(annotationTableModel.getEnd(row));
//         }
//      }));

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

      int fontSize = 14;
      if (properties.containsKey("font_size")) {
         fontSize = Integer.parseInt(properties.getProperty("font_size"));
      }
      editorPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
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
            savePreferences();
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

      editorPane.addMouseListener(mouseReleased(e -> {
         if (!e.isAltDown()) {
            autoExpandSelection();
         }
      }));

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

   private JMenu createFileMenu() {
      JMenu menu = new JMenu("File");
      menu.setMnemonic(KeyEvent.VK_F);
      JMenuItem fileOpen = new JMenuItem("Open...", KeyEvent.VK_O);
      menu.add(fileOpen);


      mruMenu = new JMenu("Recent");
      menu.add(mruMenu);

      if (properties.containsKey("mru")) {
         StringUtils.split(properties.get("mru").toString(), ',')
                    .forEach(file -> {
                       JMenuItem ii = createMRUItem(file);
                       mruMenu.add(ii);
                       mru.addFirst(ii);
                    });
      }

      final JFileChooser fileChooser = new JFileChooser();
      fileOpen.addActionListener(e -> {
         fileChooser.setCurrentDirectory(new File(properties.getProperty("current_directory")));
         int returnValue = fileChooser.showOpenDialog(AnnotationEditor.this);
         if (returnValue == JFileChooser.APPROVE_OPTION) {
            currentFile = Resources.fromFile(fileChooser.getSelectedFile());
            properties.setProperty("current_directory", fileChooser.getSelectedFile().getAbsolutePath());
            savePreferences();
            loadDocument(Resources.fromFile(fileChooser.getSelectedFile()));
         }
      });


      JMenuItem fileSave = new JMenuItem("Save", KeyEvent.VK_S);
      menu.add(fileSave);
      fileSave.addActionListener(pe -> save(currentFile));

      JMenuItem fileSaveAs = new JMenuItem("Save As...");
      menu.add(fileSaveAs);
      fileSaveAs.addActionListener(e -> {
         int returnValue = fileChooser.showSaveDialog(AnnotationEditor.this);
         if (returnValue == JFileChooser.APPROVE_OPTION) {
            currentFile = Resources.fromFile(fileChooser.getSelectedFile());
            setTitle(getTitle().replaceFirst(" \\(.*\\)$", "") + " (" + currentFile.baseName() + ")");
            updateMRU(fileChooser.getSelectedFile().getAbsolutePath());
            properties.setProperty("current_directory", fileChooser.getSelectedFile().getAbsolutePath());
            savePreferences();
            save(currentFile);
         }
      });

      menu.addSeparator();
      JMenuItem exit = new JMenuItem("Quit", KeyEvent.VK_Q);
      exit.addActionListener(e -> dispatchEvent(new WindowEvent(AnnotationEditor.this, WindowEvent.WINDOW_CLOSING)));
      menu.add(exit);

      menu.addMenuListener(new MenuListener() {
         @Override
         public void menuCanceled(MenuEvent e) {

         }

         @Override
         public void menuDeselected(MenuEvent e) {

         }

         @Override
         public void menuSelected(MenuEvent e) {
            if (currentFile == null) {
               fileSave.setEnabled(false);
            } else {
               fileSave.setEnabled(true);
            }
            if (editorPane.getText().length() == 0) {
               fileSaveAs.setEnabled(false);
            } else {
               fileSaveAs.setEnabled(true);
            }
         }
      });

      return menu;
   }

   private JMenuItem createMRUItem(String file) {
      JMenuItem item = new JMenuItem(file);
      item.addActionListener(a -> loadDocument(Resources.fromFile(file)));
      return item;
   }

   private void deleteAnnotation() {
      int row = annotationTableModel.find(editorPane.getSelectionStart(), editorPane.getSelectionEnd());
      if (row >= 0) {
         dirty = true;
         deleteAnnotation(new int[]{
            annotationTable.getRowSorter().convertRowIndexToView(row)
         });
      }
   }

   private void deleteAnnotation(int[] rows) {
      for (int i = rows.length - 1; i >= 0; i--) {
         int r = annotationTable.getRowSorter().convertRowIndexToModel(rows[i]);
         if (r >= 0 && r < annotationTable.getRowCount()) {
            dirty = true;
            int start = annotationTableModel.getStart(r);
            int end = annotationTableModel.getEnd(r);
            annotationTableModel.removeRow(r);
            editorPane.getStyledDocument()
                      .setCharacterAttributes(start, end - start, DEFAULT, true);
         }
      }
   }

   private void loadDocument(Resource docResource) {
      checkDirty();
      dirty = false;
      currentFile = docResource;
      updateMRU(docResource.path());
      List<Object[]> rows = new ArrayList<>();
      StringBuilder text = new StringBuilder();

      Corpus.builder().source(docResource)
            .inMemory()
            .format("TAGGED_OPL")
            .build()
            .forEach(doc -> {
               int offset = text.length();
               text.append(doc.toString().trim());
               text.append(SystemInfo.LINE_SEPARATOR);
               doc.get(annotationType)
                  .forEach(a -> {
                     if (tagSet.isValid(a.getTag().orElse(null))) {
                        rows.add(new Object[]{
                           a.start() + offset,
                           a.end() + offset,
                           a.getTag().orElse(null),
                           a.toString()
                        });
                     }
                  });
            });

      setTitle(getTitle().replaceFirst(" \\(.*\\)$", "") + " (" + currentFile.baseName() + ")");
      editorPane.setText(text.toString());
      annotationTableModel.annotations.clear();
      annotationTableModel.rows.clear();
      annotationTableModel.fireTableDataChanged();
      editorPane.getStyledDocument().setCharacterAttributes(0, editorPane.getText().length(), DEFAULT, true);
      rows.forEach(row -> {
         annotationTableModel.addRow(row);
         int start = (int) row[0];
         int length = (int) row[1] - (int) row[0];
         editorPane.getStyledDocument()
                   .setCharacterAttributes(start,
                                           length,
                                           editorPane.getStyle(((Tag) row[2]).name()),
                                           true);
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

   private void save(Resource out) {
      TaggedFormat format = new TaggedFormat();
      Document d = DocumentFactory.getInstance().createRaw(editorPane.getText());
//      annotationTableModel.annotations.forEach(a -> d.createAnnotation(a.getType(), a.start(), a.end(),
//                                                                       Maps.map(attributeType,
//                                                                                a.getTag().orElse(null))));
      try {
         out.write(format.toString(d));
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void savePreferences() {
      properties.setProperty("font_size", Integer.toString(editorPane.getFont().getSize()));
      try {
         propertiesFile.getParent().mkdirs();
         properties.storeToXML(propertiesFile.outputStream(), "");
      } catch (IOException e1) {
         e1.printStackTrace();
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
      if (!properties.containsKey("current_directory")) {
         properties.setProperty("current_directory", new File(".").getAbsolutePath());
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
      setJMenuBar(menuBar);


      menuBar.add(createFileMenu());
      menuBar.add(tagSet.createTagMenu(this::addTag));


      JMenu searchMenu = new JMenu("Search");
      menuBar.add(searchMenu);
      searchMenu.setMnemonic(KeyEvent.VK_S);
      JMenuItem nextAnnotation = new JMenuItem("Next Annotation", KeyEvent.VK_N);
      searchMenu.add(nextAnnotation);
      nextAnnotation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.ALT_DOWN_MASK));
      nextAnnotation.addActionListener(e -> {
         int start = editorPane.getSelectionEnd();
         int end = start + 10;
         System.out.println(start + " : " + end);
         Span next = annotationTableModel.annotations.ceiling(Span.of(start, end));
         System.out.println(next);
         if (next != null) {
            int r = annotationTableModel.find(next.start(), next.end());
            if (r != -1) {
               annotationTable.getSelectionModel()
                              .setSelectionInterval(annotationTable.getRowSorter().convertRowIndexToView(r),
                                                    annotationTable.getRowSorter().convertRowIndexToView(r));

            }
         }
      });

      JMenuItem prevAnnotation = new JMenuItem("Previous Annotation", KeyEvent.VK_N);
      searchMenu.add(prevAnnotation);
      prevAnnotation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.ALT_DOWN_MASK));
      prevAnnotation.addActionListener(e -> {
         int start = editorPane.getSelectionStart() - 5;
         int end = editorPane.getSelectionStart();
         Span next = annotationTableModel.annotations.floor(Span.of(start, end));
         System.out.println(next);
         if (next != null) {
            int r = annotationTableModel.find(next.start(), next.end());
            if (r != -1) {
               annotationTable.getSelectionModel()
                              .setSelectionInterval(annotationTable.getRowSorter().convertRowIndexToView(r),
                                                    annotationTable.getRowSorter().convertRowIndexToView(r));
            }
         }
      });

      JSplitPane splitPane = new JSplitPane();
      splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
      splitPane.setDividerLocation(300);
      add(splitPane, BorderLayout.CENTER);

      splitPane.add(createAnnotationTable(), JSplitPane.BOTTOM);
      splitPane.add(createEditor(), JSplitPane.TOP);

      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

      addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            checkDirty();
            savePreferences();
            super.windowClosing(e);
         }
      });
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

   private void updateMRU(String path) {
      if (mru.stream().noneMatch(a -> a.getText().equals(path))) {
         mru.addFirst(createMRUItem(path));
         mruMenu.insert(mru.getFirst(), 0);
         if (mru.size() > 10) {
            mruMenu.remove(mru.removeLast());
         }
      } else {
         for (Component component : mruMenu.getMenuComponents()) {
            JMenuItem ii = Cast.as(component);
            if (ii.getText().equals(path)) {
               mruMenu.remove(ii);
               mruMenu.insert(ii, 0);
               break;
            }
         }
      }
      properties.setProperty("mru", CSV.builder()
                                       .formatter()
                                       .format(mru.stream()
                                                  .map(JMenuItem::getText)
                                                  .collect(Collectors.toList())));
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
      private IntervalTree<Span> annotations = new IntervalTree<>();
      private java.util.List<Object[]> rows = new ArrayList<>();
      private int columns = 4;

      public int addRow(Object[] row) {
         rows.add(row);
         int start = (int) row[0];
         int end = (int) row[1];
         annotations.add(Span.of(start, end));
         fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
         return rows.size() - 1;
      }

      private int find(int start, int end) {
         java.util.List<Span> annotationList = annotations.overlapping(new Span(start, end));
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
