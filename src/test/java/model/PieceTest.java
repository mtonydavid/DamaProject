package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test specifici per la classe Piece
 * Obiettivo: aumentare coverage testando tutti i metodi
 */
class PieceTest {

    @Test
    @DisplayName("Test creazione piece di tutti i tipi")
    void testPieceCreationAllTypes() {
        // Test GRAY
        Piece grayPiece = new Piece(PieceType.GRAY, 1, 2);
        assertEquals(PieceType.GRAY, grayPiece.getPieceType());
        assertEquals(100, grayPiece.getOldX()); // 1 * TILE_SIZE
        assertEquals(200, grayPiece.getOldY()); // 2 * TILE_SIZE

        // Test WHITE
        Piece whitePiece = new Piece(PieceType.WHITE, 3, 4);
        assertEquals(PieceType.WHITE, whitePiece.getPieceType());
        assertEquals(300, whitePiece.getOldX());
        assertEquals(400, whitePiece.getOldY());
    }

    @Test
    @DisplayName("Test movimento piece")
    void testPieceMovement() {
        Piece piece = new Piece(PieceType.GRAY, 0, 0);

        // Test move
        piece.move(5, 6);
        assertEquals(500, piece.getOldX());
        assertEquals(600, piece.getOldY());

        // Test altro movimento
        piece.move(2, 3);
        assertEquals(200, piece.getOldX());
        assertEquals(300, piece.getOldY());
    }

    @Test
    @DisplayName("Test abort move")
    void testAbortMove() {
        Piece piece = new Piece(PieceType.WHITE, 2, 3);
        double originalX = piece.getOldX();
        double originalY = piece.getOldY();

        // Simula spostamento (cambia posizione layout)
        piece.relocate(500, 600);

        // Abort dovrebbe riportare alla posizione originale
        piece.abortMove();
        assertEquals(originalX, piece.getLayoutX());
        assertEquals(originalY, piece.getLayoutY());
    }

    @Test
    @DisplayName("Test promozione GRAY")
    void testGrayPromotion() {
        Piece grayPiece = new Piece(PieceType.GRAY, 1, 1);
        assertEquals(PieceType.GRAY, grayPiece.getPieceType());

        grayPiece.promote();
        assertEquals(PieceType.GRAY_SUP, grayPiece.getPieceType());
    }

    @Test
    @DisplayName("Test promozione WHITE")
    void testWhitePromotion() {
        Piece whitePiece = new Piece(PieceType.WHITE, 1, 1);
        assertEquals(PieceType.WHITE, whitePiece.getPieceType());

        whitePiece.promote();
        assertEquals(PieceType.WHITE_SUP, whitePiece.getPieceType());
    }

    @Test
    @DisplayName("Test promozione mantiene posizione")
    void testPromotionKeepsPosition() {
        Piece piece = new Piece(PieceType.GRAY, 4, 5);
        double originalX = piece.getOldX();
        double originalY = piece.getOldY();

        piece.promote();

        // Posizione dovrebbe rimanere la stessa
        assertEquals(originalX, piece.getOldX());
        assertEquals(originalY, piece.getOldY());
    }

    @Test
    @DisplayName("Test coordinate ai bordi")
    void testEdgeCoordinates() {
        // Angolo top-left
        Piece topLeft = new Piece(PieceType.GRAY, 0, 0);
        assertEquals(0, topLeft.getOldX());
        assertEquals(0, topLeft.getOldY());

        // Angolo bottom-right
        Piece bottomRight = new Piece(PieceType.WHITE, 7, 7);
        assertEquals(700, bottomRight.getOldX());
        assertEquals(700, bottomRight.getOldY());
    }
}