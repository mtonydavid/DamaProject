package model;

import client.ChessBoardClient;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class ScoreDisplay extends StackPane {
    public static final double width = ChessBoardClient.TILE_SIZE * 2.5;
    public static final double height = ChessBoardClient.TILE_SIZE * 2;

    private final StringProperty grayLiveCount = new SimpleStringProperty("12");
    private final StringProperty whiteLiveCount = new SimpleStringProperty("12");
    private final StringProperty grayKilledCount = new SimpleStringProperty("0");
    private final StringProperty whiteKilledCount = new SimpleStringProperty("0");

    private final Label grayLiveLabel = new Label();
    private final Label whiteLiveLabel = new Label();
    private final Label grayKilledLabel = new Label();
    private final Label whiteKilledLabel = new Label();

    private final Button toggleButton = new Button("◄");
    private boolean isVisible = true;
    private final TranslateTransition showTransition;
    private final TranslateTransition hideTransition;

    public ScoreDisplay() {
        Rectangle background = new Rectangle();
        background.setWidth(width);
        background.setHeight(height);
        background.setFill(Color.valueOf("#BEA084"));
        background.setArcWidth(10.0);
        background.setArcHeight(10.0);
        background.setStroke(Color.BLACK);
        background.setStrokeWidth(1.5);

        VBox scoreBox = new VBox(5);
        scoreBox.setMaxWidth(width * 0.9);
        scoreBox.setPadding(new Insets(5, 5, 5, 10));

        Label titleLabel = new Label("Score");
        titleLabel.setFont(new Font("Arial", height * 0.13));
        titleLabel.setTextFill(Color.valueOf("#5D5364"));
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        // Labels for GRAY player
        Label grayTitle = new Label("GRAY");
        grayTitle.setFont(new Font("Arial", height * 0.11));
        grayTitle.setTextFill(Color.valueOf("#4F4F4F"));

        grayLiveLabel.textProperty().bind(new SimpleStringProperty("Live: ").concat(grayLiveCount));
        grayLiveLabel.setFont(new Font("Arial", height * 0.09));
        grayLiveLabel.setTextFill(Color.valueOf("#4F4F4F"));

        grayKilledLabel.textProperty().bind(new SimpleStringProperty("Captured: ").concat(grayKilledCount));
        grayKilledLabel.setFont(new Font("Arial", height * 0.09));
        grayKilledLabel.setTextFill(Color.valueOf("#4F4F4F"));

        // Labels for WHITE player
        Label whiteTitle = new Label("WHITE");
        whiteTitle.setFont(new Font("Arial", height * 0.11));
        whiteTitle.setTextFill(Color.valueOf("#F8F8F8"));

        whiteLiveLabel.textProperty().bind(new SimpleStringProperty("Live: ").concat(whiteLiveCount));
        whiteLiveLabel.setFont(new Font("Arial", height * 0.09));
        whiteLiveLabel.setTextFill(Color.valueOf("#F8F8F8"));

        whiteKilledLabel.textProperty().bind(new SimpleStringProperty("Captured: ").concat(whiteKilledCount));
        whiteKilledLabel.setFont(new Font("Arial", height * 0.09));
        whiteKilledLabel.setTextFill(Color.valueOf("#F8F8F8"));

        scoreBox.getChildren().addAll(
                titleLabel,
                grayTitle, grayLiveLabel, grayKilledLabel,
                whiteTitle, whiteLiveLabel, whiteKilledLabel
        );

        // Configurazione del pulsante toggle
        toggleButton.setFont(new Font("Arial", 12));
        toggleButton.setPrefHeight(height / 4);
        toggleButton.setTranslateX(-width / 2 - 15); // Posiziona a sinistra del pannello

        // Imposta lo stile del pulsante
        toggleButton.setStyle("-fx-background-color: #BEA084; -fx-border-color: black; -fx-border-radius: 5; -fx-text-fill: white;");

        getChildren().addAll(background, scoreBox, toggleButton);

        // Posiziona il display del punteggio in basso a destra
        relocate(
                ChessBoardClient.TILE_SIZE * ChessBoardClient.WIDTH - width - 10,
                ChessBoardClient.TILE_SIZE * ChessBoardClient.HEIGHT - height - 30
        );

        // Configura le animazioni
        showTransition = new TranslateTransition(Duration.millis(300), this);
        showTransition.setToX(0);

        hideTransition = new TranslateTransition(Duration.millis(300), this);
        hideTransition.setToX(width + 10);

        // Configura l'azione del pulsante toggle
        toggleButton.setOnAction(event -> toggleVisibility());
    }

    public void toggleVisibility() {
        if (isVisible) {
            // Nascondi il pannello
            hideTransition.play();
            toggleButton.setText("►");
        } else {
            // Mostra il pannello
            showTransition.play();
            toggleButton.setText("◄");
        }
        isVisible = !isVisible;
    }

    // Metodi per aggiornare il conteggio
    public void updateCounts(int grayLive, int whiteLive, int grayKilled, int whiteKilled) {
        grayLiveCount.set(String.valueOf(grayLive));
        whiteLiveCount.set(String.valueOf(whiteLive));
        grayKilledCount.set(String.valueOf(grayKilled));
        whiteKilledCount.set(String.valueOf(whiteKilled));
    }
}