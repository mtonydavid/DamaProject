package model;

import common.GameConfig;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;

/**
 * Represents a checker piece in the game.
 * This class extends StackPane to provide visual representation and handles
 * piece movement, promotion to king, and mouse interactions.
 *
 * <p>Each piece has a type (GRAY, WHITE, GRAY_SUP, WHITE_SUP) and maintains
 * its position on the board. The piece can be moved, promoted to a king,
 * or have its move aborted.</p>
 *
 * <p>Visual representation includes:
 * <ul>
 *   <li>Background ellipse with shadow effect</li>
 *   <li>Main colored ellipse based on piece type</li>
 *   <li>Additional small ellipse for promoted pieces (kings)</li>
 * </ul>
 * </p>
 *
 * @author DamaProject Team
 * @version 2.0 - Refactored to use GameConfig
 * @since 1.0
 */
public class Piece extends StackPane {

    /**
     * The type of this piece (GRAY, WHITE, GRAY_SUP, WHITE_SUP).
     */
    private PieceType pieceType;

    /**
     * Mouse position when piece is clicked (X coordinate).
     */
    private double mouseX;

    /**
     * Mouse position when piece is clicked (Y coordinate).
     */
    private double mouseY;

    /**
     * Previous X position of the piece in pixels.
     */
    private double oldX;

    /**
     * Previous Y position of the piece in pixels.
     */
    private double oldY;

    /**
     * Gets the current piece type.
     *
     * @return the current PieceType of this piece
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    /**
     * Creates a new checker piece at the specified board position.
     *
     * <p>The piece is created with visual components including a shadow background
     * and a colored main ellipse. Mouse event handlers are automatically set up
     * for drag and drop functionality.</p>
     *
     * @param pieceType the type of piece to create (GRAY, WHITE, GRAY_SUP, WHITE_SUP)
     * @param x the initial x coordinate on the board (0-7)
     * @param y the initial y coordinate on the board (0-7)
     * @throws IllegalArgumentException if coordinates are outside valid range
     */
    public Piece(PieceType pieceType, int x, int y) {
        if (pieceType == null) {
            throw new IllegalArgumentException("PieceType cannot be null");
        }
        if (!GameConfig.isValidCoordinate(x, y)) {
            throw new IllegalArgumentException("Invalid coordinates: " + x + ", " + y);
        }

        this.pieceType = pieceType;

        // Calculate pixel position from board coordinates using GameConfig
        oldX = GameConfig.boardToPixelCoordinate(x);
        oldY = GameConfig.boardToPixelCoordinate(y);
        relocate(oldX, oldY);

        createVisualComponents();
        setupMouseHandlers();
    }

    /**
     * Creates the visual components of the piece.
     */
    private void createVisualComponents() {
        // Create background shadow ellipse
        Ellipse ellipseBackground = new Ellipse(GameConfig.PIECE_RADIUS_X, GameConfig.PIECE_RADIUS_Y);
        ellipseBackground.setFill(Color.BLACK);
        ellipseBackground.setStroke(Color.BLACK);
        ellipseBackground.setStrokeWidth(GameConfig.PIECE_STROKE_WIDTH);
        ellipseBackground.setTranslateX((GameConfig.TILE_SIZE - GameConfig.PIECE_RADIUS_X * 2) / 2);
        ellipseBackground.setTranslateY((GameConfig.TILE_SIZE - GameConfig.PIECE_RADIUS_Y * 2) / 2 + GameConfig.PIECE_SHADOW_OFFSET);

        // Create main piece ellipse
        Ellipse ellipse = new Ellipse(GameConfig.PIECE_RADIUS_X, GameConfig.PIECE_RADIUS_Y);

        // Set color based on piece type using GameConfig colors
        Color pieceColor = isGrayPiece() ?
                Color.valueOf(GameConfig.GRAY_PIECE_COLOR) :
                Color.valueOf(GameConfig.WHITE_PIECE_COLOR);
        ellipse.setFill(pieceColor);
        ellipse.setStroke(Color.BLACK);
        ellipse.setStrokeWidth(GameConfig.PIECE_STROKE_WIDTH);
        ellipse.setTranslateX((GameConfig.TILE_SIZE - GameConfig.PIECE_RADIUS_X * 2) / 2);
        ellipse.setTranslateY((GameConfig.TILE_SIZE - GameConfig.PIECE_RADIUS_Y * 2) / 2);

        getChildren().addAll(ellipseBackground, ellipse);
    }

    /**
     * Sets up mouse event handlers for drag and drop functionality.
     */
    private void setupMouseHandlers() {
        setOnMousePressed(e -> {
            // Save exact click position within the piece
            mouseX = e.getSceneX() - getLayoutX();
            mouseY = e.getSceneY() - getLayoutY();

            // Bring piece to front for better visual experience during drag
            toFront();
        });

        setOnMouseDragged(e -> {
            // Calculate correct position considering initial click offset
            relocate(e.getSceneX() - mouseX, e.getSceneY() - mouseY);
        });
    }

    /**
     * Checks if this piece is a gray piece (including king).
     *
     * @return true if piece is gray or gray king
     */
    private boolean isGrayPiece() {
        return pieceType == PieceType.GRAY || pieceType == PieceType.GRAY_SUP;
    }

    /**
     * Checks if this piece is a king (promoted piece).
     *
     * @return true if piece is a king
     */
    public boolean isKing() {
        return pieceType == PieceType.GRAY_SUP || pieceType == PieceType.WHITE_SUP;
    }

    /**
     * Gets the previous X position in pixels.
     *
     * @return the previous X position of the piece
     */
    public double getOldX() {
        return oldX;
    }

    /**
     * Gets the previous Y position in pixels.
     *
     * @return the previous Y position of the piece
     */
    public double getOldY() {
        return oldY;
    }

    /**
     * Gets the current board X coordinate.
     *
     * @return current X coordinate (0-7)
     */
    public int getBoardX() {
        return GameConfig.pixelToBoardCoordinate(oldX);
    }

    /**
     * Gets the current board Y coordinate.
     *
     * @return current Y coordinate (0-7)
     */
    public int getBoardY() {
        return GameConfig.pixelToBoardCoordinate(oldY);
    }

    /**
     * Moves the piece to a new board position.
     *
     * <p>This method updates both the visual position and the stored
     * old position coordinates. The coordinates are converted from
     * board coordinates to pixel coordinates.</p>
     *
     * @param x the new x coordinate on the board (0-7)
     * @param y the new y coordinate on the board (0-7)
     * @throws IllegalArgumentException if coordinates are invalid
     */
    public void move(int x, int y) {
        if (!GameConfig.isValidCoordinate(x, y)) {
            throw new IllegalArgumentException("Invalid move coordinates: " + x + ", " + y);
        }

        oldX = GameConfig.boardToPixelCoordinate(x);
        oldY = GameConfig.boardToPixelCoordinate(y);
        relocate(oldX, oldY);
    }

    /**
     * Aborts the current move and returns the piece to its previous position.
     *
     * <p>This method is typically called when a move is invalid or rejected
     * by the game logic. The piece will visually return to its last valid position.</p>
     */
    public void abortMove() {
        relocate(oldX, oldY);
    }

    /**
     * Promotes this piece to a king.
     *
     * <p>Promotion changes the piece type from regular to king (SUP) variant
     * and adds visual indication by calling {@link #promoteImage()}.</p>
     *
     * <p>Promotion rules:
     * <ul>
     *   <li>GRAY pieces become GRAY_SUP</li>
     *   <li>WHITE pieces become WHITE_SUP</li>
     * </ul>
     * </p>
     *
     * @see #promoteImage()
     */
    public void promote() {
        promoteImage();
        pieceType = (pieceType == PieceType.GRAY) ? PieceType.GRAY_SUP : PieceType.WHITE_SUP;
    }

    /**
     * Adds visual indication that this piece has been promoted to a king.
     *
     * <p>This method adds a smaller ellipse on top of the existing piece
     * to visually distinguish kings from regular pieces. The small ellipse
     * uses the same color as the main piece.</p>
     *
     * @see #promote()
     */
    public void promoteImage() {
        Ellipse doubleEllipse = new Ellipse(
                GameConfig.PIECE_RADIUS_X * 0.5,
                GameConfig.PIECE_RADIUS_Y * 0.5
        );

        Color pieceColor = isGrayPiece() ?
                Color.valueOf(GameConfig.GRAY_PIECE_COLOR) :
                Color.valueOf(GameConfig.WHITE_PIECE_COLOR);
        doubleEllipse.setFill(pieceColor);
        doubleEllipse.setStroke(Color.BLACK);
        doubleEllipse.setStrokeWidth(GameConfig.PIECE_STROKE_WIDTH);
        doubleEllipse.setTranslateX((GameConfig.TILE_SIZE - GameConfig.PIECE_RADIUS_X * 2) / 2);
        doubleEllipse.setTranslateY((GameConfig.TILE_SIZE - GameConfig.PIECE_RADIUS_Y * 2) / 2);

        getChildren().add(doubleEllipse);
    }

    /**
     * Returns a string representation of this piece.
     *
     * @return string describing the piece type and position
     */
    @Override
    public String toString() {
        return String.format("Piece{type=%s, position=(%d,%d), isKing=%s}",
                pieceType, getBoardX(), getBoardY(), isKing());
    }

    /**
     * Checks if this piece can move in the given direction based on its type.
     *
     * @param deltaY the Y direction of movement
     * @return true if the piece can move in that direction
     */
    public boolean canMoveInDirection(int deltaY) {
        if (isKing()) {
            return true; // Kings can move in any direction
        }

        // Regular pieces can only move forward
        return deltaY == pieceType.moveDir;
    }
}