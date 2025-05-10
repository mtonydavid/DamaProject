package model;

import client.ChessBoardClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

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

    public ScoreDisplay() {
        Rectangle background = new Rectangle();
        background.setWidth(width);
        background.setHeight(height);
        background.setFill(Color.valueOf("#ECD8C6"));
        background.setArcWidth(10.0);
        background.setArcHeight(10.0);
        background.setStroke(Color.BLACK);
        background.setStrokeWidth(1.5);

        VBox scoreBox = new VBox(5);
        scoreBox.setMaxWidth(width * 0.9);

        Label titleLabel = new Label("Score");
        titleLabel.setFont(new Font("Arial", height * 0.15));
        titleLabel.setTextFill(Color.valueOf("#5D5364"));
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        // Labels for GRAY player
        Label grayTitle = new Label("GRAY");
        grayTitle.setFont(new Font("Arial", height * 0.12));
        grayTitle.setTextFill(Color.valueOf("#4F4F4F"));

        grayLiveLabel.textProperty().bind(new SimpleStringProperty("Live: ").concat(grayLiveCount));
        grayLiveLabel.setFont(new Font("Arial", height * 0.1));
        grayLiveLabel.setTextFill(Color.valueOf("#4F4F4F"));

        grayKilledLabel.textProperty().bind(new SimpleStringProperty("Captured: ").concat(grayKilledCount));
        grayKilledLabel.setFont(new Font("Arial", height * 0.1));
        grayKilledLabel.setTextFill(Color.valueOf("#4F4F4F"));

        // Labels for WHITE player
        Label whiteTitle = new Label("WHITE");
        whiteTitle.setFont(new Font("Arial", height * 0.12));
        whiteTitle.setTextFill(Color.valueOf("#CEB087"));

        whiteLiveLabel.textProperty().bind(new SimpleStringProperty("Live: ").concat(whiteLiveCount));
        whiteLiveLabel.setFont(new Font("Arial", height * 0.1));
        whiteLiveLabel.setTextFill(Color.valueOf("#CEB087"));

        whiteKilledLabel.textProperty().bind(new SimpleStringProperty("Captured: ").concat(whiteKilledCount));
        whiteKilledLabel.setFont(new Font("Arial", height * 0.1));
        whiteKilledLabel.setTextFill(Color.valueOf("#CEB087"));

        scoreBox.getChildren().addAll(
                titleLabel,
                grayTitle, grayLiveLabel, grayKilledLabel,
                whiteTitle, whiteLiveLabel, whiteKilledLabel
        );

        getChildren().addAll(background, scoreBox);

        // Posiziona il display del punteggio in basso a destra
        relocate(
                ChessBoardClient.TILE_SIZE * ChessBoardClient.WIDTH - width - 10,
                ChessBoardClient.TILE_SIZE * ChessBoardClient.HEIGHT - height - 10
        );
    }

    // Metodi per aggiornare il conteggio
    public void updateCounts(int grayLive, int whiteLive, int grayKilled, int whiteKilled) {
        grayLiveCount.set(String.valueOf(grayLive));
        whiteLiveCount.set(String.valueOf(whiteLive));
        grayKilledCount.set(String.valueOf(grayKilled));
        whiteKilledCount.set(String.valueOf(whiteKilled));
    }
}