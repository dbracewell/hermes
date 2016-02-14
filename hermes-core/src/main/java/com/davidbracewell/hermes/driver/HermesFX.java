package com.davidbracewell.hermes.driver;

import com.davidbracewell.application.JavaFXApplication;
import com.davidbracewell.io.resource.FileResource;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

/**
 * @author David B. Bracewell
 */
public class HermesFX extends JavaFXApplication {

  private static final String STANDARD_BUTTON_STYLE = "-fx-text-fill: #666666;";
  private static final String HOVERED_BUTTON_STYLE = "-fx-text-fill: #000000;";
  private final Font TenPointBold = Font.font("System", FontWeight.BOLD, 10);
  private TextArea editor;
  private TreeView<String> annotationList;
  private VBox annotationPane;
  private TreeView<String> attributeList;
  private VBox attributePane;
  private HBox statusBar;
  private SplitPane workspace;
  private File currentDirectory = new File(".");

  public HermesFX() {
    super("HermesFX");
  }

  public static void main(String[] args) {
    launch(args);
  }


  Menu createViewMenu() {
    Menu viewMenu = new Menu("_View");
    CheckMenuItem annotationWindow = new CheckMenuItem("A_nnotation Window");
    annotationWindow.setOnAction(a -> {
      if (workspace.getItems().contains(annotationPane)) {
        workspace.getItems().remove(annotationPane);
      } else {
        workspace.getItems().add(0, annotationPane);
        workspace.setDividerPosition(0, 0.2);
      }
    });
    CheckMenuItem attributeWindow = new CheckMenuItem("A_ttribute Window");

    attributeWindow.setOnAction(a -> {
      if (workspace.getItems().contains(attributePane)) {
        workspace.getItems().remove(attributePane);
      } else {
        workspace.getItems().add(2, attributePane);
        workspace.setDividerPosition(2, 0.1);
      }
    });
    viewMenu.getItems().addAll(annotationWindow, attributeWindow);

    viewMenu.setOnShowing(e -> {
      attributeWindow.setSelected(workspace.getItems().contains(attributePane));
      annotationWindow.setSelected(workspace.getItems().contains(annotationPane));
    });
    return viewMenu;
  }

  Menu createFileMenu() {
    Menu menuFile = new Menu("_File");
    MenuItem fileOpen = new MenuItem("_Open");
    fileOpen.setAccelerator(KeyCombination.keyCombination("SHORTCUT+O"));
    fileOpen.setOnAction(a -> {
      FileChooser chooser = new FileChooser();
      chooser.setInitialDirectory(currentDirectory);
      File chose = chooser.showOpenDialog(null);
      if (chose != null) {
        currentDirectory = chose.getParentFile();
        try {
          editor.setText(new FileResource(chose).readToString());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
    MenuItem fileClose = new MenuItem("E_xit");
    fileClose.setAccelerator(KeyCombination.keyCombination("SHORTCUT+Q"));
    fileClose.setOnAction(e -> Platform.exit());
    menuFile.getItems().addAll(fileOpen, new SeparatorMenuItem(), fileClose);
    return menuFile;
  }

  private VBox createAnnotationPane() {
    annotationPane = new VBox();
    annotationPane.setFillWidth(true);
    HBox titleBar = new HBox();
    titleBar.setMinHeight(15);
    titleBar.setAlignment(Pos.CENTER_RIGHT);
    Button closeButton = new Button("X");
    closeButton.setFont(TenPointBold);
    closeButton.setBackground(Background.EMPTY);
    closeButton.styleProperty().bind(
      Bindings.when(closeButton.hoverProperty()).then(new SimpleStringProperty(HOVERED_BUTTON_STYLE)).otherwise(STANDARD_BUTTON_STYLE)
    );
    Label lbl = new Label("Annotations");
    lbl.setFont(TenPointBold);
    Region spacer = new Region();
    titleBar.getChildren().addAll(lbl, spacer, closeButton);
    HBox.setHgrow(spacer, Priority.ALWAYS);
    closeButton.setOnAction(a -> workspace.getItems().remove(annotationPane));
    annotationList = new TreeView<>();
    VBox.setVgrow(annotationList, Priority.ALWAYS);
    annotationPane.getChildren().addAll(titleBar, annotationList);
    return annotationPane;
  }

  private VBox createAttributePane() {
    attributePane = new VBox();
    attributePane.setFillWidth(true);
    HBox titleBar = new HBox();
    titleBar.setMinHeight(15);
    titleBar.setAlignment(Pos.CENTER_RIGHT);
    Button closeButton = new Button("X");
    closeButton.setFont(TenPointBold);
    closeButton.setBackground(Background.EMPTY);
    closeButton.styleProperty().bind(
      Bindings.when(closeButton.hoverProperty()).then(new SimpleStringProperty(HOVERED_BUTTON_STYLE)).otherwise(STANDARD_BUTTON_STYLE)
    );
    Label lbl = new Label("Attributes");
    lbl.setFont(TenPointBold);
    Region spacer = new Region();
    titleBar.getChildren().addAll(lbl, spacer, closeButton);
    HBox.setHgrow(spacer, Priority.ALWAYS);
    closeButton.setOnAction(a -> workspace.getItems().remove(attributePane));
    attributeList = new TreeView<>();
    VBox.setVgrow(attributeList, Priority.ALWAYS);
    attributePane.getChildren().addAll(titleBar, attributeList);
    return attributePane;
  }

  @Override
  public void setup() throws Exception {
    editor = new TextArea();
    editor.setEditable(false);

    BorderPane pane = new BorderPane();
    statusBar = new HBox();
    statusBar.setMinHeight(15);
    statusBar.setMinWidth(500);
    statusBar.setMaxHeight(15);
    pane.setBottom(statusBar);


    MenuBar menuBar = new MenuBar();
    menuBar.getMenus().addAll(createFileMenu(), createViewMenu());
    pane.setTop(menuBar);

    workspace = new SplitPane();
    workspace.getItems().addAll(createAnnotationPane(), editor, createAttributePane());
    workspace.setDividerPositions(0.2, 0.8, 0.1);


    SplitPane.setResizableWithParent(editor, true);
    SplitPane.setResizableWithParent(workspace.getItems().get(0), false);
    SplitPane.setResizableWithParent(workspace.getItems().get(2), false);

    pane.setCenter(workspace);
    Scene scene = new Scene(pane);
    getStage().setScene(scene);
    getStage().show();
  }


}// END OF HermesFX
