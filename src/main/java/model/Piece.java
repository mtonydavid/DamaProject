package model;

import common.GameConfig;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;

/**
 * Represents a checker piece in the game.
 * This class extends StackPane to provide visual representation and handles
 * piece movement, promotion to king, and mouse interactions.
 */
public class Piece extends StackPane {

    private PieceType pieceType;
    private double mouseX;
    private double mouseY;
    private double oldX;
    private double oldY;

    /**
     * Gets the current piece type.
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    /**
     * Creates a new checker piece at the specified board position.
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
     * Checks if this piece is a gray piece
     */
    private boolean isGrayPiece() {
        return pieceType == PieceType.GRAY || pieceType == PieceType.GRAY_SUP;
    }

    /**
     * Checks if this piece is a king
     */
    public boolean isKing() {
        return pieceType == PieceType.GRAY_SUP || pieceType == PieceType.WHITE_SUP;
    }
    public double getOldX() {
        return oldX;
    }
    public double getOldY() {
        return oldY;
    }
    public int getBoardX() {
        return GameConfig.pixelToBoardCoordinate(oldX);
    }
    public int getBoardY() {
        return GameConfig.pixelToBoardCoordinate(oldY);
    }

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
     */
    public void abortMove() {
        relocate(oldX, oldY);
    }
    public void promote() {
        promoteImage();
        pieceType = (pieceType == PieceType.GRAY) ? PieceType.GRAY_SUP : PieceType.WHITE_SUP;
    }

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
     */
    @Override
    public String toString() {
        return String.format("Piece{type=%s, position=(%d,%d), isKing=%s}",
                pieceType, getBoardX(), getBoardY(), isKing());
    }

    /**
     * Checks if this piece can move in the given direction based on its type.
     */
    public boolean canMoveInDirection(int deltaY) {
        if (isKing()) {
            return true; // Kings can move in any direction
        }

        // Regular pieces can only move forward
        return deltaY == pieceType.moveDir;
    }
}