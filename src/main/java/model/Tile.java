package model;

import client.ChessBoardClient;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Tile extends Rectangle {
    private Piece piece;
    private boolean isHighlighted = false;
    private final boolean isLight;
    private final int x, y;

    // Colori per le celle
    private static final Color LIGHT_COLOR = Color.valueOf("#FFFACD");
    private static final Color DARK_COLOR = Color.valueOf("#8B4513");
    private static final Color HIGHLIGHT_COLOR = Color.valueOf("#55FF55");
    private static final Color HIGHLIGHT_BORDER_COLOR = Color.valueOf("#00AA00");

    public Tile(boolean isLight, int x, int y) {
        this.isLight = isLight;
        this.x = x;
        this.y = y;

        setWidth(ChessBoardClient.TILE_SIZE);
        setHeight(ChessBoardClient.TILE_SIZE);
        relocate(x * ChessBoardClient.TILE_SIZE, y * ChessBoardClient.TILE_SIZE);

        // Imposta il colore iniziale della cella
        setFill(isLight ? LIGHT_COLOR : DARK_COLOR);
    }

    public boolean hasPiece() {
        return piece != null;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    /**
     * Evidenzia la cella come mossa possibile.
     */
    public void highlight() {
        if (!isLight) { // Evidenziamo solo le celle scure (quelle valide per la dama)
            isHighlighted = true;
            setFill(HIGHLIGHT_COLOR);
            setStroke(HIGHLIGHT_BORDER_COLOR);
            setStrokeWidth(2.0);
        }
    }

    /**
     * Rimuove l'evidenziazione.
     */
    public void removeHighlight() {
        isHighlighted = false;
        setFill(isLight ? LIGHT_COLOR : DARK_COLOR);
        setStroke(null);
    }

    /**
     * Verifica se la cella è evidenziata.
     */
    public boolean isHighlighted() {
        return isHighlighted;
    }
}