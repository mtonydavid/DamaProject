package client;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import model.PieceType;

public class VictoryScreen {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 400;

    private Stage stage;
    private Scene scene;
    private String winnerText;
    private int gameTime;
    private int grayLivePieces;
    private int whiteLivePieces;
    private int grayKilledPieces;
    private int whiteKilledPieces;
    private String mode;

    public VictoryScreen(String winnerText, int gameTime, int grayLivePieces, int whiteLivePieces,
                         int grayKilledPieces, int whiteKilledPieces, String mode) {
        this.winnerText = winnerText;
        this.gameTime = gameTime;
        this.grayLivePieces = grayLivePieces;
        this.whiteLivePieces = whiteLivePieces;
        this.grayKilledPieces = grayKilledPieces;
        this.whiteKilledPieces = whiteKilledPieces;
        this.mode = mode;

        initialize();
    }

    private void initialize() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Game Over");
        stage.initStyle(StageStyle.DECORATED);
        stage.setResizable(false);

        // Create the root layout
        Pane root = new Pane();
        root.setPrefSize(WIDTH, HEIGHT);
        root.setStyle("-fx-background-color: #ecdac9;");

        // Create a container for content
        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPrefWidth(WIDTH);
        contentBox.setPadding(new Insets(30, 20, 30, 20));

        // Create the victory banner
        Text victoryText = new Text(winnerText);
        victoryText.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        PieceType winnerType = winnerText.contains("GRAY") ? PieceType.GRAY : PieceType.WHITE;
        victoryText.setFill(winnerType == PieceType.GRAY ? Color.valueOf("#4F4F4F") : Color.valueOf("#CEB087"));

        // Add drop shadow effect to text
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(3.0);
        dropShadow.setColor(Color.color(0.4, 0.4, 0.4));
        victoryText.setEffect(dropShadow);

        // Game statistics
        Label timeLabel = new Label("Game Duration: " + formatTime(gameTime));
        timeLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        // Create score display
        VBox statsBox = createStatsBox();

        // Create action buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button playAgainButton = createButton("Play Again");
        Button mainMenuButton = createButton("Main Menu");

        buttonBox.getChildren().addAll(playAgainButton, mainMenuButton);

        // Add all components to content box
        contentBox.getChildren().addAll(victoryText, timeLabel, statsBox, buttonBox);
        root.getChildren().add(contentBox);

        // Add celebration animations
        addCelebrationEffects(root);

        // Set up button actions
        playAgainButton.setOnAction(e -> {
            stage.close();
            restartGame();
        });

        mainMenuButton.setOnAction(e -> {
            stage.close();
            returnToMainMenu();
        });

        scene = new Scene(root);
        stage.setScene(scene);
    }

    private VBox createStatsBox() {
        VBox statsBox = new VBox(10);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle("-fx-background-color: #BEA084; -fx-background-radius: 10;");

        Label statsTitle = new Label("Game Statistics");
        statsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        HBox grayStatsBox = new HBox(15);
        grayStatsBox.setAlignment(Pos.CENTER);

        Circle grayCircle = new Circle(15);
        grayCircle.setFill(Color.valueOf("#4F4F4F"));
        grayCircle.setStroke(Color.BLACK);

        VBox grayDetails = new VBox(5);
        grayDetails.setAlignment(Pos.CENTER_LEFT);
        Label grayLabel = new Label("GRAY");
        grayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label grayStats = new Label("Remaining: " + grayLivePieces + " | Captured: " + grayKilledPieces);
        grayStats.setFont(Font.font("Arial", 14));
        grayDetails.getChildren().addAll(grayLabel, grayStats);

        grayStatsBox.getChildren().addAll(grayCircle, grayDetails);

        HBox whiteStatsBox = new HBox(15);
        whiteStatsBox.setAlignment(Pos.CENTER);

        Circle whiteCircle = new Circle(15);
        whiteCircle.setFill(Color.valueOf("#CEB087"));
        whiteCircle.setStroke(Color.BLACK);

        VBox whiteDetails = new VBox(5);
        whiteDetails.setAlignment(Pos.CENTER_LEFT);
        Label whiteLabel = new Label("WHITE");
        whiteLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label whiteStats = new Label("Remaining: " + whiteLivePieces + " | Captured: " + whiteKilledPieces);
        whiteStats.setFont(Font.font("Arial", 14));
        whiteDetails.getChildren().addAll(whiteLabel, whiteStats);

        whiteStatsBox.getChildren().addAll(whiteCircle, whiteDetails);


        statsBox.getChildren().addAll(statsTitle, grayStatsBox, whiteStatsBox);
        return statsBox;
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(150);
        button.setPrefHeight(40);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setStyle("-fx-background-color: #C1A89F; -fx-text-fill: #5D5364; -fx-background-radius: 5;");

        // Hover effect
        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: #5D5364; -fx-text-fill: #C1A89F; -fx-background-radius: 5;"));
        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: #C1A89F; -fx-text-fill: #5D5364; -fx-background-radius: 5;"));

        return button;
    }

    private void addCelebrationEffects(Pane root) {
        // Add confetti particles with animation
        for (int i = 0; i < 30; i++) {
            final Circle confetti = new Circle(Math.random() * 5 + 3);
            confetti.setFill(Color.color(Math.random(), Math.random(), Math.random()));
            confetti.setOpacity(0.7);

            // Position around the top
            confetti.setCenterX(Math.random() * WIDTH);
            confetti.setCenterY(-20);

            // Create a path for the confetti to follow
            Path path = new Path();
            path.getElements().add(new MoveTo(confetti.getCenterX(), confetti.getCenterY()));
            path.getElements().add(new LineTo(Math.random() * WIDTH, HEIGHT + 50));

            // Create the path transition
            PathTransition pathTransition = new PathTransition();
            pathTransition.setDuration(Duration.seconds(Math.random() * 3 + 2));
            pathTransition.setPath(path);
            pathTransition.setNode(confetti);
            pathTransition.setCycleCount(Timeline.INDEFINITE);

            // Create a rotation animation
            RotateTransition rotateTransition = new RotateTransition(Duration.seconds(Math.random() * 2 + 1), confetti);
            rotateTransition.setByAngle(360);
            rotateTransition.setCycleCount(Timeline.INDEFINITE);

            // Play the animations
            ParallelTransition transition = new ParallelTransition(pathTransition, rotateTransition);

            root.getChildren().add(confetti);

            // Start with slight delay for each particle
            Timeline delayedStart = new Timeline(
                    new KeyFrame(Duration.seconds(Math.random() * 1.5), e -> transition.play())
            );
            delayedStart.play();
        }

        // Add checker piece spinning at the corners
        for (int i = 0; i < 4; i++) {
            Ellipse checkerPiece = new Ellipse(25, 20);
            boolean isGray = winnerText.contains("GRAY");
            checkerPiece.setFill(isGray ? Color.valueOf("#4F4F4F") : Color.valueOf("#CEB087"));
            checkerPiece.setStroke(Color.BLACK);
            checkerPiece.setStrokeWidth(2);

            // Position at corners
            switch (i) {
                case 0: checkerPiece.setCenterX(40); checkerPiece.setCenterY(40); break;
                case 1: checkerPiece.setCenterX(WIDTH - 40); checkerPiece.setCenterY(40); break;
                case 2: checkerPiece.setCenterX(40); checkerPiece.setCenterY(HEIGHT - 40); break;
                case 3: checkerPiece.setCenterX(WIDTH - 40); checkerPiece.setCenterY(HEIGHT - 40); break;
            }

            // Create a rotation animation
            RotateTransition rotateTransition = new RotateTransition(Duration.seconds(3), checkerPiece);
            rotateTransition.setByAngle(360);
            rotateTransition.setCycleCount(Timeline.INDEFINITE);
            rotateTransition.setAutoReverse(true);
            rotateTransition.play();

            // Add pulsating effect
            Timeline pulsate = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(checkerPiece.scaleXProperty(), 1)),
                    new KeyFrame(Duration.ZERO, new KeyValue(checkerPiece.scaleYProperty(), 1)),
                    new KeyFrame(Duration.seconds(1), new KeyValue(checkerPiece.scaleXProperty(), 1.2)),
                    new KeyFrame(Duration.seconds(1), new KeyValue(checkerPiece.scaleYProperty(), 1.2)),
                    new KeyFrame(Duration.seconds(2), new KeyValue(checkerPiece.scaleXProperty(), 1))
            );
            pulsate.setCycleCount(Timeline.INDEFINITE);
            pulsate.play();

            root.getChildren().add(checkerPiece);
        }
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    public void show() {
        stage.show();
    }

    private void restartGame() {
        try {
            // Close the current game window
            Platform.runLater(() -> {
                Stage gameStage = (Stage) scene.getWindow();
                gameStage.close();

                // Start a new game with the same mode
                try {
                    ChessBoardClient chessBoardClient = new ChessBoardClient();
                    chessBoardClient.setMode(mode);
                    chessBoardClient.start(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void returnToMainMenu() {
        Platform.runLater(() -> {
            try {
                // Close game window
                Stage gameStage = (Stage) scene.getWindow();
                gameStage.close();

                // Start the main menu
                StartScreen startScreen = new StartScreen();
                startScreen.start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}