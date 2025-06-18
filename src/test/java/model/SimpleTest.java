package model;

import it.polimi.model.MoveResult;
import it.polimi.model.MoveType;
import it.polimi.model.Piece;
import it.polimi.model.PieceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test semplice per verificare che JUnit funzioni
 */
class SimpleTest {

    @Test
    @DisplayName("Test di base - verifica setup JUnit")
    void testBasicSetup() {
        // Test banale per verificare che tutto funzioni
        assertTrue(true, "Questo test dovrebbe sempre passare");
        assertEquals(4, 2 + 2, "2 + 2 dovrebbe fare 4");
        assertNotNull("Hello", "Una stringa non dovrebbe essere null");
    }

    @Test
    @DisplayName("Test creazione piece")
    void testPieceCreation() {
        // Test che una piece si crei correttamente
        Piece piece = new Piece(PieceType.GRAY, 0, 0);

        assertNotNull(piece, "Piece non dovrebbe essere null");
        assertEquals(PieceType.GRAY, piece.getPieceType(), "Piece dovrebbe essere GRAY");
        assertEquals(0, piece.getOldX(), "Position X dovrebbe essere 0");
        assertEquals(0, piece.getOldY(), "Position Y dovrebbe essere 0");
    }

    @Test
    @DisplayName("Test MoveResult")
    void testMoveResult() {
        // Test che MoveResult funzioni
        MoveResult normalMove = new MoveResult(MoveType.NORMAL);

        assertNotNull(normalMove, "MoveResult non dovrebbe essere null");
        assertEquals(MoveType.NORMAL, normalMove.getMoveType(), "Tipo mossa dovrebbe essere NORMAL");
        assertNull(normalMove.getPiece(), "Piece dovrebbe essere null per mossa normale");
    }
}