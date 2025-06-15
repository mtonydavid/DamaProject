package client;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class StartScreen extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Creazione del layout principale
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #ecdac9;");

        // Titolo del gioco
        Label titleLabel = new Label("Italian Checkers");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setStyle("-fx-text-fill: #5D5364;");

        // Sottotitolo
        Label subtitleLabel = new Label("Select Game Mode");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        subtitleLabel.setStyle("-fx-text-fill: #5D5364;");

        // Pulsanti per le modalità di gioco
        Button localButton = createButton("Local");
        Button cpuButton = createButton("CPU");
        Button onlineButton = createButton("Online");

        // Aggiunta di tutti gli elementi al layout
        root.getChildren().addAll(titleLabel, subtitleLabel, localButton, cpuButton, onlineButton);

        // Gestione degli eventi dei pulsanti
        localButton.setOnAction(e -> {
            launchGame(primaryStage, "local");
        });

        cpuButton.setOnAction(e -> {
            launchGame(primaryStage, "cpu");
        });

        onlineButton.setOnAction(e -> {
            launchGame(primaryStage, "wait");
        });

        // Creazione della scena
        Scene scene = new Scene(root, 400, 500);

        // Impostazione della scena sullo stage
        primaryStage.setTitle("Checkers-Main Menu");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(200);
        button.setPrefHeight(50);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setStyle("-fx-background-color: #C1A89F; -fx-text-fill: #5D5364; -fx-background-radius: 5;");

        // Effetto hover
        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: #5D5364; -fx-text-fill: #C1A89F; -fx-background-radius: 5;"));
        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: #C1A89F; -fx-text-fill: #5D5364; -fx-background-radius: 5;"));

        return button;
    }

    private void launchGame(Stage primaryStage, String mode) {
        primaryStage.close();
        String[] args = {mode};
        try {
            ChessBoardClient chessBoardClient = new ChessBoardClient();
            chessBoardClient.setMode(mode);
            chessBoardClient.start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}