package model;


import client.ChessBoardClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

    public class Timer extends StackPane {
        public static final double width = ChessBoardClient.TILE_SIZE * 1.6;
        public static final double height = ChessBoardClient.TILE_SIZE * 0.4;

        private final StringProperty timeString = new SimpleStringProperty("Timer: 0s.");

        public Timer() {
            Rectangle rectangle = new Rectangle();
            rectangle.setWidth(width);
            rectangle.setHeight(height);
            rectangle.setFill(Color.valueOf("#C1A89F"));
            rectangle.setArcWidth(10.0);
            rectangle.setArcHeight(10.0);
            rectangle.setStroke(Color.BLACK);

            Label timeLabel = new Label();
            timeLabel.textProperty().bind(timeString);
            timeLabel.setFont(new Font(height * 0.5));
            timeLabel.setTextAlignment(TextAlignment.CENTER);
            timeLabel.setTextFill(Color.web("#4F4F4F"));

            getChildren().addAll(rectangle, timeLabel);

            relocate((ChessBoardClient.TILE_SIZE * ChessBoardClient.WIDTH - width) / 2f, 0);
        }

        public void set(String string) {
            timeString.set(string);
        }
    }

