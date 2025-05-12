package model;

import client.ChessBoardClient;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;

public class Piece extends StackPane {
    private PieceType pieceType;

    private double mouseX, mouseY;
    private double oldX, oldY;

    public PieceType getPieceType() {
        return pieceType;
    }

    public Piece(PieceType pieceType, int x, int y) {
        this.pieceType = pieceType;

        oldX = x * ChessBoardClient.TILE_SIZE;
        oldY = y * ChessBoardClient.TILE_SIZE;
        relocate(oldX, oldY);

        Ellipse ellipseBackground = new Ellipse(ChessBoardClient.TILE_SIZE * 0.3125, ChessBoardClient.TILE_SIZE * 0.26);

        ellipseBackground.setFill(Color.BLACK);

        ellipseBackground.setStroke(Color.BLACK);
        ellipseBackground.setStrokeWidth(ChessBoardClient.TILE_SIZE * 0.03);

        ellipseBackground.setTranslateX((ChessBoardClient.TILE_SIZE - ChessBoardClient.TILE_SIZE * 0.3125 * 2) / 2);
        ellipseBackground.setTranslateY((ChessBoardClient.TILE_SIZE - ChessBoardClient.TILE_SIZE * 0.26 * 2) / 2 + ChessBoardClient.TILE_SIZE * 0.07);

        Ellipse ellipse = new Ellipse(ChessBoardClient.TILE_SIZE * 0.3125, ChessBoardClient.TILE_SIZE * 0.26);

        ellipse.setFill(pieceType == PieceType.GRAY ? Color.valueOf("#4F4F4F") : Color.valueOf("#CEB087"));

        ellipse.setStroke(Color.BLACK);
        ellipse.setStrokeWidth(ChessBoardClient.TILE_SIZE * 0.03);

        ellipse.setTranslateX((ChessBoardClient.TILE_SIZE - ChessBoardClient.TILE_SIZE * 0.3125 * 2) / 2);
        ellipse.setTranslateY((ChessBoardClient.TILE_SIZE - ChessBoardClient.TILE_SIZE * 0.26 * 2) / 2);

        getChildren().addAll(ellipseBackground, ellipse);

        setOnMousePressed(e -> {
            // Salva la posizione esatta del click all'interno del pezzo
            mouseX = e.getSceneX() - getLayoutX();
            mouseY = e.getSceneY() - getLayoutY();

            // Porta il pezzo in primo piano per migliorare l'esperienza visiva durante il trascinamento
            toFront();
        });

        setOnMouseDragged(e -> {
            // Calcola la posizione corretta considerando l'offset iniziale del click
            relocate(e.getSceneX() - mouseX, e.getSceneY() - mouseY);
        });
    }

    public double getOldX() {
        return oldX;
    }

    public double getOldY() {
        return oldY;
    }

    public void move(int x, int y) {
        oldX = x * ChessBoardClient.TILE_SIZE;
        oldY = y * ChessBoardClient.TILE_SIZE;

        relocate(oldX, oldY);
    }

    public void abortMove() {
        relocate(oldX, oldY);
    }

    public void promote() {
        promoteImage();
        pieceType = (pieceType == PieceType.GRAY) ? PieceType.GRAY_SUP : PieceType.WHITE_SUP;
    }

    public void promoteImage() {
        Ellipse doubleEllipse = new Ellipse(ChessBoardClient.TILE_SIZE * 0.3125 * 0.5, ChessBoardClient.TILE_SIZE * 0.26 * 0.5);

        doubleEllipse.setFill(pieceType == PieceType.GRAY ? Color.valueOf("#4F4F4F") : Color.valueOf("#CEB087"));

        doubleEllipse.setStroke(Color.BLACK);
        doubleEllipse.setStrokeWidth(ChessBoardClient.TILE_SIZE * 0.03);

        doubleEllipse.setTranslateX((ChessBoardClient.TILE_SIZE - ChessBoardClient.TILE_SIZE * 0.3125 * 2) / 2);
        doubleEllipse.setTranslateY((ChessBoardClient.TILE_SIZE - ChessBoardClient.TILE_SIZE * 0.26 * 2) / 2);

        getChildren().addAll(doubleEllipse);
    }
}