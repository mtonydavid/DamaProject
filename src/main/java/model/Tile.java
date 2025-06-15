package model;

import common.GameConfig;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Represents a single tile on the checkers board.
 * Each tile can be light or dark and may contain a piece.
 */
public class Tile extends Rectangle {
    private Piece piece;
    private boolean isHighlighted = false;
    private final boolean isLight;
    private final int x, y;

    // Colors using GameConfig
    private static final Color LIGHT_COLOR = Color.valueOf(GameConfig.LIGHT_TILE_COLOR);
    private static final Color DARK_COLOR = Color.valueOf(GameConfig.DARK_TILE_COLOR);
    private static final Color HIGHLIGHT_COLOR = Color.valueOf(GameConfig.HIGHLIGHT_COLOR);

    /**
     * Creates a new tile at the specified position.
     */
    public Tile(boolean isLight, int x, int y) {
        if (!GameConfig.isValidCoordinate(x, y)) {
            throw new IllegalArgumentException("Invalid tile coordinates: " + x + ", " + y);
        }

        this.isLight = isLight;
        this.x = x;
        this.y = y;

        // Set tile dimensions using GameConfig
        setWidth(GameConfig.TILE_SIZE);
        setHeight(GameConfig.TILE_SIZE);

        // Position tile using GameConfig coordinate conversion
        relocate(GameConfig.boardToPixelCoordinate(x), GameConfig.boardToPixelCoordinate(y));

        // Set initial color
        setFill(isLight ? LIGHT_COLOR : DARK_COLOR);
    }

    public int getBoardX() {
        return x;
    }
    public int getBoardY() {
        return y;
    }
    public boolean isLight() {
        return isLight;
    }
    public boolean isDark() {
        return !isLight;
    }

    /**
     * Checks if this tile contains a piece.
     */
    public boolean hasPiece() {
        return piece != null;
    }

    /**
     * Gets the piece on this tile.
     */
    public Piece getPiece() {
        return piece;
    }

    /**
     * Sets the piece on this tile.
     */
    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    /**
     * Highlights this tile as a possible move destination.
     * Only dark tiles can be highlighted (as they are the valid squares in checkers).
     */
    public void highlight() {
        if (!isLight) { // Only highlight dark tiles (valid for checkers)
            isHighlighted = true;
            setFill(HIGHLIGHT_COLOR);
            setStrokeWidth(2.0);
            setStroke(Color.BLACK);
        }
    }

    /**
     * Removes highlighting from this tile.
     */
    public void removeHighlight() {
        isHighlighted = false;
        setFill(isLight ? LIGHT_COLOR : DARK_COLOR);
        setStroke(null);
        setStrokeWidth(0.0);
    }

    /**
     * Checks if this tile is currently highlighted.
     */
    public boolean isHighlighted() {
        return isHighlighted;
    }

    public boolean isValidSquare() {
        return !isLight;
    }

    public boolean isEdgeTile() {
        return x == 0 || x == GameConfig.BOARD_WIDTH - 1 ||
                y == 0 || y == GameConfig.BOARD_HEIGHT - 1;
    }

    public boolean isCornerTile() {
        return (x == 0 || x == GameConfig.BOARD_WIDTH - 1) &&
                (y == 0 || y == GameConfig.BOARD_HEIGHT - 1);
    }

    /**
     * Gets the Manhattan distance to another tile.
     */
    public int distanceTo(Tile other) {
        if (other == null) {
            throw new IllegalArgumentException("Other tile cannot be null");
        }
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    public boolean isAdjacentTo(Tile other) {
        return distanceTo(other) == 1;
    }

    public boolean isDiagonalTo(Tile other) {
        if (other == null) {
            return false;
        }
        int deltaX = Math.abs(this.x - other.x);
        int deltaY = Math.abs(this.y - other.y);
        return deltaX == 1 && deltaY == 1;
    }

    /**
     * Returns a string representation of this tile.
     */
    @Override
    public String toString() {
        return String.format("Tile{pos=(%d,%d), light=%s, piece=%s, highlighted=%s}",
                x, y, isLight, (piece != null ? piece.getPieceType() : "none"), isHighlighted);
    }
}