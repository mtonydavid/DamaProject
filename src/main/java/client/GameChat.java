package client;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.BufferedWriter;
import java.io.IOException;

public class GameChat extends VBox {
    private VBox messageArea;
    private TextField messageInput;
    private BufferedWriter writer;
    private String playerName;
    private VBox chatContainer;
    private VBox chatIcon;
    private boolean hasUnreadMessages = false;

    public GameChat(BufferedWriter writer, String playerName) {
        this.writer = writer;
        this.playerName = playerName;

        // Container principale trasparente
        setPadding(new Insets(10));
        setSpacing(5);
        setMaxWidth(250);
        setStyle("-fx-background-color: transparent;");
        setAlignment(Pos.TOP_RIGHT);

        // Icona chat
        Text iconText = new Text("💬");
        iconText.setFont(Font.font("System", 18));

        chatIcon = new VBox(iconText);
        chatIcon.setAlignment(Pos.CENTER);
        chatIcon.setPrefWidth(40);
        chatIcon.setPrefHeight(40);
        chatIcon.setStyle("-fx-background-color: rgba(200, 200, 200, 0.7); -fx-background-radius: 20;");

        // Area messaggi
        messageArea = new VBox();
        messageArea.setSpacing(5);
        messageArea.setPadding(new Insets(5));

        // ScrollPane per messaggi
        ScrollPane scrollPane = new ScrollPane(messageArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);
        scrollPane.setStyle("-fx-background: rgba(255, 255, 255, 0.7); -fx-background-radius: 5;");
        scrollPane.vvalueProperty().bind(messageArea.heightProperty());

        // Campo input
        messageInput = new TextField();
        messageInput.setPromptText("Scrivi un messaggio...");
        messageInput.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); -fx-background-radius: 5;");
        messageInput.setOnAction(e -> sendMessage());

        // Container per messaggi e input
        chatContainer = new VBox(5, scrollPane, messageInput);
        chatContainer.setStyle("-fx-background-color: rgba(240, 240, 240, 0.5); -fx-background-radius: 10; " +
                "fx-border-radius: 10; -fx-border-color: rgba(200, 200, 200, 0.8); -fx-border-width: 1;");
        chatContainer.setPadding(new Insets(10));
        chatContainer.setVisible(false);
        chatContainer.setOpacity(0);

        // Gestione hover
        chatIcon.setOnMouseEntered(e -> showChat());
        chatContainer.setOnMouseExited(e -> hideChat());

        getChildren().addAll(chatIcon, chatContainer);

        // Inizializza con un messaggio di sistema
        addMessage("Sistema", "Chat connessa", false);
    }

    private void showChat() {
        hasUnreadMessages = false;
        chatIcon.setStyle("-fx-background-color: rgba(200, 200, 200, 0.7); -fx-background-radius: 20;");

        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), chatContainer);
        chatContainer.setVisible(true);
        fadeIn.setFromValue(chatContainer.getOpacity());
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void hideChat() {
        // Piccolo ritardo prima di nascondere
        new Thread(() -> {
            try {
                Thread.sleep(500);
                if (!chatContainer.isHover() && !chatIcon.isHover()) {
                    Platform.runLater(() -> {
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), chatContainer);
                        fadeOut.setFromValue(chatContainer.getOpacity());
                        fadeOut.setToValue(0.0);
                        fadeOut.setOnFinished(e -> chatContainer.setVisible(false));
                        fadeOut.play();
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            try {
                // Invia messaggio al server
                writer.write("CHAT " + playerName + " " + message);
                writer.newLine();
                writer.flush();

                // Visualizza il messaggio inviato
                addMessage("Tu", message, true);

                // Pulisci input
                messageInput.clear();
            } catch (IOException e) {
                addMessage("Sistema", "Errore nell'invio del messaggio", false);
            }
        }
    }

    public void receiveMessage(String sender, String message) {
        Platform.runLater(() -> {
            addMessage(sender, message, false);

            // Se la chat è nascosta, evidenzia l'icona
            if (!chatContainer.isVisible()) {
                hasUnreadMessages = true;
                chatIcon.setStyle("-fx-background-color: rgba(100, 180, 100, 0.8); -fx-background-radius: 20;");
            }
        });
    }

    private void addMessage(String sender, String message, boolean isOwnMessage) {
        HBox messageBox = new HBox(5);

        Text nameText = new Text(sender + ":");
        nameText.setFont(Font.font("System", 10));
        nameText.setFill(isOwnMessage ? Color.DARKBLUE : Color.DARKGREEN);

        Text messageText = new Text(message);
        messageText.setFont(Font.font("System", 12));
        messageText.setWrappingWidth(180);

        VBox textContainer = new VBox(nameText, messageText);
        textContainer.setStyle("-fx-background-color: " +
                (isOwnMessage ? "rgba(220, 240, 255, 0.8)" : "rgba(240, 255, 240, 0.8)") +
                "; -fx-background-radius: 5; -fx-padding: 5;");

        messageBox.getChildren().add(textContainer);
        messageBox.setPadding(new Insets(2));

        messageArea.getChildren().add(messageBox);
    }
}