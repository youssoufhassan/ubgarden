package fr.ubx.poo.ubgarden.game.view;

import fr.ubx.poo.ubgarden.game.engine.GameEngine;
import fr.ubx.poo.ubgarden.game.Game;
import fr.ubx.poo.ubgarden.game.launcher.GameLauncher;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

public class GameLauncherView extends BorderPane {
    private final FileChooser fileChooser = new FileChooser();
    private final Stage stage;

    private GameEngine currentEngine = null;

    public GameLauncherView(Stage stage) {
        this.stage = stage;

        // Create menu
        MenuBar menuBar = new MenuBar();

        Menu menuGame = new Menu("Game");
        MenuItem loadItem = new MenuItem("Load from file ...");
        MenuItem defaultItem = new MenuItem("Load default configuration");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        menuGame.getItems().addAll(loadItem, defaultItem, new SeparatorMenuItem(), exitItem);

        Menu menuEditor = new Menu("Editor");
        MenuItem editorOpen = new MenuItem("Open map editor");

        menuEditor.getItems().addAll(editorOpen);

        menuBar.getMenus().addAll(menuGame, menuEditor);
        this.setTop(menuBar);

        Text text = new Text("UBGarden 2025");
        text.getStyleClass().add("message");
        VBox scene = new VBox();
        scene.getChildren().add(text);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/application.css")).toExternalForm());
        scene.getStyleClass().add("message");
        this.setCenter(scene);
        stage.setResizable(false);

        // Load from file
        loadItem.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                try (Reader in = new FileReader(file.getAbsolutePath())) {
                    Game game = GameLauncher.getInstance().load(in);

                    if (currentEngine != null) {
                        currentEngine.stop();
                    }

                    GameEngine engine = new GameEngine(game, stage.getScene());
                    this.setCenter(engine.getRoot());
                    engine.getRoot().requestFocus();
                    engine.start();

                    currentEngine = engine;

                    resizeStage();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Load default configuration
        defaultItem.setOnAction(e -> {
            Game game = GameLauncher.getInstance().load();

            if (currentEngine != null) {
                currentEngine.stop();
            }

            GameEngine engine = new GameEngine(game, stage.getScene());

            this.setCenter(engine.getRoot());
            engine.getRoot().requestFocus();
            engine.start();

            currentEngine = engine;

            resizeStage();
        });

        editorOpen.setOnAction(e -> {
            System.err.println("Not implemented");
        });

        // Exit
        exitItem.setOnAction(e -> System.exit(0));
    }


    private void resizeStage() {
        stage.sizeToScene();
        stage.hide();
        stage.show();
    }
}