package model;

import it.polimi.model.PieceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test completi per l'enum PieceType.
 * Testa tutte le proprietà e comportamenti dell'enum.
 */
class PieceTypeTest {

    @Test
    @DisplayName("Test valori enum e direzioni movimento")
    void testEnumValuesAndMoveDirections() {
        PieceType[] values = PieceType.values();
        assertEquals(4, values.length);

        // Test direzioni movimento
        assertEquals(1, PieceType.GRAY.moveDir);      // GRAY muove verso il basso (+1)
        assertEquals(-1, PieceType.WHITE.moveDir);    // WHITE muove verso l'alto (-1)
        assertEquals(2, PieceType.GRAY_SUP.moveDir);  // GRAY_SUP (dama)
        assertEquals(-2, PieceType.WHITE_SUP.moveDir); // WHITE_SUP (dama)
    }

    @Test
    @DisplayName("Test valueOf per tutti i valori")
    void testValueOf() {
        assertEquals(PieceType.GRAY, PieceType.valueOf("GRAY"));
        assertEquals(PieceType.WHITE, PieceType.valueOf("WHITE"));
        assertEquals(PieceType.GRAY_SUP, PieceType.valueOf("GRAY_SUP"));
        assertEquals(PieceType.WHITE_SUP, PieceType.valueOf("WHITE_SUP"));
    }

    @Test
    @DisplayName("Test valueOf con valori invalidi")
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> PieceType.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> PieceType.valueOf("gray")); // case sensitive
        assertThrows(NullPointerException.class, () -> PieceType.valueOf(null));
    }

    @ParameterizedTest
    @EnumSource(PieceType.class)
    @DisplayName("Test name() per tutti i valori")
    void testName(PieceType pieceType) {
        assertNotNull(pieceType.name());
        assertFalse(pieceType.name().isEmpty());
    }

    @Test
    @DisplayName("Test ordinal dei valori")
    void testOrdinal() {
        assertEquals(0, PieceType.GRAY.ordinal());
        assertEquals(1, PieceType.WHITE.ordinal());
        assertEquals(2, PieceType.GRAY_SUP.ordinal());
        assertEquals(3, PieceType.WHITE_SUP.ordinal());
    }

    @Test
    @DisplayName("Test direzioni movimento logiche")
    void testMovementDirectionsLogic() {
        // GRAY dovrebbe muovere verso il basso (coordinate Y crescenti)
        assertTrue(PieceType.GRAY.moveDir > 0);

        // WHITE dovrebbe muovere verso l'alto (coordinate Y decrescenti)
        assertTrue(PieceType.WHITE.moveDir < 0);

        // Le dame hanno direzioni "potenziate" (valore assoluto maggiore)
        assertTrue(Math.abs(PieceType.GRAY_SUP.moveDir) > Math.abs(PieceType.GRAY.moveDir));
        assertTrue(Math.abs(PieceType.WHITE_SUP.moveDir) > Math.abs(PieceType.WHITE.moveDir));
    }

    @Test
    @DisplayName("Test relazioni tra pedine normali e dame")
    void testNormalVsKingRelationships() {
        // Direzioni opposte per colori opposti
        assertTrue(PieceType.GRAY.moveDir * PieceType.WHITE.moveDir < 0);
        assertTrue(PieceType.GRAY_SUP.moveDir * PieceType.WHITE_SUP.moveDir < 0);

        // Stesso segno per stesso colore
        assertTrue(PieceType.GRAY.moveDir * PieceType.GRAY_SUP.moveDir > 0);
        assertTrue(PieceType.WHITE.moveDir * PieceType.WHITE_SUP.moveDir > 0);
    }

    @Test
    @DisplayName("Test equals e hashCode")
    void testEqualsAndHashCode() {
        // Test equals
        assertEquals(PieceType.GRAY, PieceType.GRAY);
        assertEquals(PieceType.WHITE, PieceType.WHITE);
        assertEquals(PieceType.GRAY_SUP, PieceType.GRAY_SUP);
        assertEquals(PieceType.WHITE_SUP, PieceType.WHITE_SUP);

        // Test inequality
        assertNotEquals(PieceType.GRAY, PieceType.WHITE);
        assertNotEquals(PieceType.GRAY, PieceType.GRAY_SUP);
        assertNotEquals(PieceType.WHITE, PieceType.WHITE_SUP);

        // Test hashCode consistency
        assertEquals(PieceType.GRAY.hashCode(), PieceType.GRAY.hashCode());
        assertEquals(PieceType.WHITE.hashCode(), PieceType.WHITE.hashCode());
        assertEquals(PieceType.GRAY_SUP.hashCode(), PieceType.GRAY_SUP.hashCode());
        assertEquals(PieceType.WHITE_SUP.hashCode(), PieceType.WHITE_SUP.hashCode());
    }

    @Test
    @DisplayName("Test compareTo")
    void testCompareTo() {
        assertTrue(PieceType.GRAY.compareTo(PieceType.WHITE) < 0);
        assertTrue(PieceType.WHITE.compareTo(PieceType.GRAY_SUP) < 0);
        assertTrue(PieceType.GRAY_SUP.compareTo(PieceType.WHITE_SUP) < 0);

        assertEquals(0, PieceType.GRAY.compareTo(PieceType.GRAY));
        assertEquals(0, PieceType.WHITE.compareTo(PieceType.WHITE));
        assertEquals(0, PieceType.GRAY_SUP.compareTo(PieceType.GRAY_SUP));
        assertEquals(0, PieceType.WHITE_SUP.compareTo(PieceType.WHITE_SUP));
    }

    @Test
    @DisplayName("Test comportamento in switch")
    void testSwitchBehavior() {
        // Test switch per identificare colore
        for (PieceType type : PieceType.values()) {
            String color = switch (type) {
                case GRAY, GRAY_SUP -> "GRAY";
                case WHITE, WHITE_SUP -> "WHITE";
            };

            if (type == PieceType.GRAY || type == PieceType.GRAY_SUP) {
                assertEquals("GRAY", color);
            } else {
                assertEquals("WHITE", color);
            }
        }
    }

    @Test
    @DisplayName("Test identificazione dame")
    void testKingIdentification() {
        assertTrue(isKing(PieceType.GRAY_SUP));
        assertTrue(isKing(PieceType.WHITE_SUP));
        assertFalse(isKing(PieceType.GRAY));
        assertFalse(isKing(PieceType.WHITE));
    }

    @Test
    @DisplayName("Test identificazione colore")
    void testColorIdentification() {
        assertTrue(isGrayPiece(PieceType.GRAY));
        assertTrue(isGrayPiece(PieceType.GRAY_SUP));
        assertFalse(isGrayPiece(PieceType.WHITE));
        assertFalse(isGrayPiece(PieceType.WHITE_SUP));

        assertTrue(isWhitePiece(PieceType.WHITE));
        assertTrue(isWhitePiece(PieceType.WHITE_SUP));
        assertFalse(isWhitePiece(PieceType.GRAY));
        assertFalse(isWhitePiece(PieceType.GRAY_SUP));
    }

    @Test
    @DisplayName("Test promozione logica")
    void testPromotionLogic() {
        assertEquals(PieceType.GRAY_SUP, promote(PieceType.GRAY));
        assertEquals(PieceType.WHITE_SUP, promote(PieceType.WHITE));

        // Dame rimangono dame
        assertEquals(PieceType.GRAY_SUP, promote(PieceType.GRAY_SUP));
        assertEquals(PieceType.WHITE_SUP, promote(PieceType.WHITE_SUP));
    }

    @Test
    @DisplayName("Test con collezioni")
    void testWithCollections() {
        java.util.Set<PieceType> pieceTypes = java.util.EnumSet.allOf(PieceType.class);
        assertEquals(4, pieceTypes.size());

        // Test filtering
        java.util.Set<PieceType> grayPieces = java.util.EnumSet.of(PieceType.GRAY, PieceType.GRAY_SUP);
        java.util.Set<PieceType> whitePieces = java.util.EnumSet.of(PieceType.WHITE, PieceType.WHITE_SUP);
        java.util.Set<PieceType> kings = java.util.EnumSet.of(PieceType.GRAY_SUP, PieceType.WHITE_SUP);

        assertEquals(2, grayPieces.size());
        assertEquals(2, whitePieces.size());
        assertEquals(2, kings.size());
    }

    @Test
    @DisplayName("Test immutabilità moveDir")
    void testMoveDirImmutability() {
        // moveDir è final, non può essere modificato
        int grayDir = PieceType.GRAY.moveDir;
        // Non possiamo modificarlo, ma possiamo verificare che sia costante
        assertEquals(grayDir, PieceType.GRAY.moveDir);

        // Verifica che i valori siano quelli attesi
        assertEquals(1, PieceType.GRAY.moveDir);
        assertEquals(-1, PieceType.WHITE.moveDir);
        assertEquals(2, PieceType.GRAY_SUP.moveDir);
        assertEquals(-2, PieceType.WHITE_SUP.moveDir);
    }

    @Test
    @DisplayName("Test toString consistency")
    void testToStringConsistency() {
        for (PieceType type : PieceType.values()) {
            assertEquals(type.name(), type.toString());
        }
    }

    @ParameterizedTest
    @EnumSource(PieceType.class)
    @DisplayName("Test serializzazione per tutti i tipi")
    void testSerialization(PieceType pieceType) {
        // Test che valueOf(name()) ritorni lo stesso oggetto
        assertEquals(pieceType, PieceType.valueOf(pieceType.name()));
    }

    // Metodi helper per i test
    private boolean isKing(PieceType type) {
        return type == PieceType.GRAY_SUP || type == PieceType.WHITE_SUP;
    }

    private boolean isGrayPiece(PieceType type) {
        return type == PieceType.GRAY || type == PieceType.GRAY_SUP;
    }

    private boolean isWhitePiece(PieceType type) {
        return type == PieceType.WHITE || type == PieceType.WHITE_SUP;
    }

    private PieceType promote(PieceType type) {
        return switch (type) {
            case GRAY -> PieceType.GRAY_SUP;
            case WHITE -> PieceType.WHITE_SUP;
            case GRAY_SUP -> PieceType.GRAY_SUP;
            case WHITE_SUP -> PieceType.WHITE_SUP;
        };
    }
}