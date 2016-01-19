package com.davidbracewell.hermes.driver;

import com.davidbracewell.application.JavaFXApplication;
import com.davidbracewell.io.resource.FileResource;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

/**
 * @author David B. Bracewell
 */
public class HermesFX extends JavaFXApplication {

  public HermesFX() {
    super("HermesFX");
  }

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void setup() throws Exception {
    TextArea textArea = new TextArea();
    textArea.setEditable(false);

    BorderPane pane = new BorderPane();
    HBox bbox = new HBox();
    pane.setBottom(bbox);
    bbox.setMinHeight(25);
    bbox.setMinWidth(500);
    bbox.setMaxHeight(25);

    GridPane grid = new GridPane();

    HBox leftBar = new HBox();
    leftBar.setMinHeight(20);
    leftBar.setAlignment(Pos.CENTER_RIGHT);
    Button closeButton = new Button("X");
    closeButton.setStyle("-fx-border-radius: 0;");
    leftBar.getChildren().addAll(closeButton);

    BorderPane leftPane = new BorderPane();
    TitledPane annotations = new TitledPane("Annotations", new TreeView<String>());
    annotations.setCollapsible(false);
    grid.add(annotations, 0, 1);
    TitledPane attributes = new TitledPane("Attributes", new TreeView<String>());
    attributes.setCollapsible(false);
    grid.add(attributes, 0, 2);


    RowConstraints top = new RowConstraints();
    top.setValignment(VPos.TOP);
    top.setPercentHeight(50);
    RowConstraints middle = new RowConstraints();
    middle.setValignment(VPos.CENTER);
    top.setPercentHeight(0);
    RowConstraints bottom = new RowConstraints();
    bottom.setValignment(VPos.BOTTOM);
    bottom.setPercentHeight(50);
    grid.getRowConstraints().setAll(top, middle, bottom);

    ColumnConstraints cc = new ColumnConstraints();
    cc.setFillWidth(true);
    cc.setPercentWidth(100);
    grid.getColumnConstraints().setAll(cc);


    MenuBar menuBar = new MenuBar();
    Menu menuFile = new Menu("_File");
    MenuItem fileOpen = new MenuItem("_Open");
    fileOpen.setAccelerator(KeyCombination.keyCombination("SHORTCUT+O"));
    fileOpen.setOnAction(a -> {
      FileChooser chooser = new FileChooser();
      File chose = chooser.showOpenDialog(null);
      if (chose != null) {
        try {
          textArea.setText(new FileResource(chose).readToString());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

    MenuItem fileClose = new MenuItem("E_xit");
    fileClose.setAccelerator(KeyCombination.keyCombination("SHORTCUT+Q"));
    fileClose.setOnAction(e -> Platform.exit());
    menuFile.getItems().addAll(fileOpen, new SeparatorMenuItem(), fileClose);


    menuBar.getMenus().addAll(menuFile);

    pane.setTop(menuBar);

    SplitPane splitPane = new SplitPane();
    leftPane.setTop(leftBar);
    leftPane.setCenter(grid);

    closeButton.setOnAction(a -> {
      splitPane.getItems().remove(leftPane);
    });

    Menu viewMenu = new Menu("_View");
    CheckMenuItem properties = new CheckMenuItem("_Side Bar");
    viewMenu.setOnShowing(e -> {
      if (splitPane.getItems().size() == 1) {
        properties.setSelected(false);
      } else {
        properties.setSelected(true);
      }
    });

    properties.setOnAction(a -> {
      if (splitPane.getItems().size() == 1) {
        splitPane.getItems().add(0, leftPane);
      } else {
        splitPane.getItems().remove(leftPane);
      }
    });
    viewMenu.getItems().addAll(properties);
    menuBar.getMenus().addAll(viewMenu);

    splitPane.getItems().addAll(leftPane, textArea);

    pane.setCenter(splitPane);
    Scene scene = new Scene(pane);
    getStage().setScene(scene);
    getStage().show();
  }

  @Override
  public void run() {

  }

}// END OF HermesFX
