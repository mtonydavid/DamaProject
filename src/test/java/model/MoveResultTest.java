package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test specifici per la classe MoveResult
 */
class MoveResultTest {

    @Test
    @DisplayName("Test MoveResult con solo tipo")
    void testMoveResultTypeOnly() {
        // Test NORMAL
        MoveResult normalMove = new MoveResult(MoveType.NORMAL);
        assertEquals(MoveType.NORMAL, normalMove.getMoveType());
        assertNull(normalMove.getPiece());

        // Test NONE
        MoveResult noneMove = new MoveResult(MoveType.NONE);
        assertEquals(MoveType.NONE, noneMove.getMoveType());
        assertNull(noneMove.getPiece());
    }

    @Test
    @DisplayName("Test MoveResult KILL con piece")
    void testKillMoveWithPiece() {
        Piece capturedPiece = new Piece(PieceType.WHITE, 3, 3);
        MoveResult killMove = new MoveResult(MoveType.KILL, capturedPiece);

        assertEquals(MoveType.KILL, killMove.getMoveType());
        assertNotNull(killMove.getPiece());
        assertEquals(capturedPiece, killMove.getPiece());
        assertEquals(PieceType.WHITE, killMove.getPiece().getPieceType());
    }

    @Test
    @DisplayName("Test MoveResult con piece null")
    void testMoveResultWithNullPiece() {
        MoveResult moveWithNull = new MoveResult(MoveType.KILL, null);

        assertEquals(MoveType.KILL, moveWithNull.getMoveType());
        assertNull(moveWithNull.getPiece());
    }

    @Test
    @DisplayName("Test tutti i tipi di MoveType")
    void testAllMoveTypes() {
        // Test NORMAL
        MoveResult normal = new MoveResult(MoveType.NORMAL);
        assertEquals(MoveType.NORMAL, normal.getMoveType());

        // Test KILL
        MoveResult kill = new MoveResult(MoveType.KILL);
        assertEquals(MoveType.KILL, kill.getMoveType());

        // Test NONE
        MoveResult none = new MoveResult(MoveType.NONE);
        assertEquals(MoveType.NONE, none.getMoveType());
    }

    @Test
    @DisplayName("Test immutabilità MoveResult")
    void testMoveResultImmutability() {
        Piece piece = new Piece(PieceType.GRAY, 1, 1);
        MoveResult result = new MoveResult(MoveType.KILL, piece);

        // Verifica che i getter restituiscano sempre gli stessi valori
        assertEquals(MoveType.KILL, result.getMoveType());
        assertEquals(MoveType.KILL, result.getMoveType()); // secondo call

        assertSame(piece, result.getPiece());
        assertSame(piece, result.getPiece()); // secondo call
    }
}