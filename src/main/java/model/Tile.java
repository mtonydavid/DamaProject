package model;

import common.GameConfig;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Represents a single tile on the checkers board.
 * Each tile can be light or dark and may contain a piece.
 *
 * @author DamaProject Team
 * @version 2.0 - Refactored to use GameConfig
 * @since 1.0
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
     *
     * @param isLight true if this is a light-colored tile
     * @param x the x coordinate on the board (0-7)
     * @param y the y coordinate on the board (0-7)
     * @throws IllegalArgumentException if coordinates are invalid
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

    /**
     * Gets the board X coordinate of this tile.
     *
     * @return the x coordinate (0-7)
     */
    public int getBoardX() {
        return x;
    }

    /**
     * Gets the board Y coordinate of this tile.
     *
     * @return the y coordinate (0-7)
     */
    public int getBoardY() {
        return y;
    }

    /**
     * Checks if this tile is a light-colored tile.
     *
     * @return true if this is a light tile
     */
    public boolean isLight() {
        return isLight;
    }

    /**
     * Checks if this tile is a dark-colored tile.
     *
     * @return true if this is a dark tile
     */
    public boolean isDark() {
        return !isLight;
    }

    /**
     * Checks if this tile contains a piece.
     *
     * @return true if a piece is on this tile
     */
    public boolean hasPiece() {
        return piece != null;
    }

    /**
     * Gets the piece on this tile.
     *
     * @return the piece on this tile, or null if empty
     */
    public Piece getPiece() {
        return piece;
    }

    /**
     * Sets the piece on this tile.
     *
     * @param piece the piece to place on this tile, or null to remove
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
     *
     * @return true if this tile is highlighted
     */
    public boolean isHighlighted() {
        return isHighlighted;
    }

    /**
     * Checks if this tile is a valid square for checkers pieces.
     * In checkers, only dark squares are used.
     *
     * @return true if this is a valid square for piece placement
     */
    public boolean isValidSquare() {
        return !isLight;
    }

    /**
     * Checks if this tile is on the edge of the board.
     *
     * @return true if this tile is on any edge
     */
    public boolean isEdgeTile() {
        return x == 0 || x == GameConfig.BOARD_WIDTH - 1 ||
                y == 0 || y == GameConfig.BOARD_HEIGHT - 1;
    }

    /**
     * Checks if this tile is a corner tile.
     *
     * @return true if this tile is in a corner
     */
    public boolean isCornerTile() {
        return (x == 0 || x == GameConfig.BOARD_WIDTH - 1) &&
                (y == 0 || y == GameConfig.BOARD_HEIGHT - 1);
    }

    /**
     * Gets the Manhattan distance to another tile.
     *
     * @param other the other tile
     * @return the Manhattan distance
     */
    public int distanceTo(Tile other) {
        if (other == null) {
            throw new IllegalArgumentException("Other tile cannot be null");
        }
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    /**
     * Checks if this tile is adjacent to another tile (distance 1).
     *
     * @param other the other tile
     * @return true if tiles are adjacent
     */
    public boolean isAdjacentTo(Tile other) {
        return distanceTo(other) == 1;
    }

    /**
     * Checks if this tile is diagonally adjacent to another tile.
     *
     * @param other the other tile
     * @return true if tiles are diagonally adjacent
     */
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
     *
     * @return string describing the tile position and state
     */
    @Override
    public String toString() {
        return String.format("Tile{pos=(%d,%d), light=%s, piece=%s, highlighted=%s}",
                x, y, isLight, (piece != null ? piece.getPieceType() : "none"), isHighlighted);
    }
}