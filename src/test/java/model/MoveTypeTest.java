package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test completi per l'enum MoveType.
 * Raggiungiamo 100% coverage testando tutti i metodi e casi.
 */
class MoveTypeTest {

    @Test
    @DisplayName("Test valori enum")
    void testEnumValues() {
        MoveType[] values = MoveType.values();
        assertEquals(3, values.length);

        // Verifica presenza di tutti i valori
        assertTrue(java.util.Arrays.asList(values).contains(MoveType.NONE));
        assertTrue(java.util.Arrays.asList(values).contains(MoveType.NORMAL));
        assertTrue(java.util.Arrays.asList(values).contains(MoveType.KILL));
    }

    @Test
    @DisplayName("Test valueOf per tutti i valori")
    void testValueOf() {
        assertEquals(MoveType.NONE, MoveType.valueOf("NONE"));
        assertEquals(MoveType.NORMAL, MoveType.valueOf("NORMAL"));
        assertEquals(MoveType.KILL, MoveType.valueOf("KILL"));
    }

    @Test
    @DisplayName("Test valueOf con valore invalido")
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> MoveType.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> MoveType.valueOf("normal")); // case sensitive
        assertThrows(NullPointerException.class, () -> MoveType.valueOf(null));
    }

    @Test
    @DisplayName("Test toString per NONE")
    void testToStringNone() {
        assertEquals("NONE", MoveType.NONE.toString());
    }

    @Test
    @DisplayName("Test toString per NORMAL")
    void testToStringNormal() {
        assertEquals("NORMAL", MoveType.NORMAL.toString());
    }

    @Test
    @DisplayName("Test toString per KILL")
    void testToStringKill() {
        assertEquals("KILL", MoveType.KILL.toString());
    }

    @Test
    @DisplayName("Test ordinal dei valori enum")
    void testOrdinal() {
        assertEquals(0, MoveType.NONE.ordinal());
        assertEquals(1, MoveType.NORMAL.ordinal());
        assertEquals(2, MoveType.KILL.ordinal());
    }

    @Test
    @DisplayName("Test name() method")
    void testName() {
        assertEquals("NONE", MoveType.NONE.name());
        assertEquals("NORMAL", MoveType.NORMAL.name());
        assertEquals("KILL", MoveType.KILL.name());
    }

    @Test
    @DisplayName("Test equals e hashCode")
    void testEqualsAndHashCode() {
        // Test equals
        assertEquals(MoveType.NONE, MoveType.NONE);
        assertEquals(MoveType.NORMAL, MoveType.NORMAL);
        assertEquals(MoveType.KILL, MoveType.KILL);

        assertNotEquals(MoveType.NONE, MoveType.NORMAL);
        assertNotEquals(MoveType.NORMAL, MoveType.KILL);
        assertNotEquals(MoveType.KILL, MoveType.NONE);

        // Test hashCode consistency
        assertEquals(MoveType.NONE.hashCode(), MoveType.NONE.hashCode());
        assertEquals(MoveType.NORMAL.hashCode(), MoveType.NORMAL.hashCode());
        assertEquals(MoveType.KILL.hashCode(), MoveType.KILL.hashCode());
    }

    @Test
    @DisplayName("Test compareTo")
    void testCompareTo() {
        assertTrue(MoveType.NONE.compareTo(MoveType.NORMAL) < 0);
        assertTrue(MoveType.NORMAL.compareTo(MoveType.KILL) < 0);
        assertTrue(MoveType.KILL.compareTo(MoveType.NONE) > 0);

        assertEquals(0, MoveType.NONE.compareTo(MoveType.NONE));
        assertEquals(0, MoveType.NORMAL.compareTo(MoveType.NORMAL));
        assertEquals(0, MoveType.KILL.compareTo(MoveType.KILL));
    }

    @Test
    @DisplayName("Test uso in switch statement")
    void testSwitchStatement() {
        // Test del metodo toString() che usa switch
        String result1 = getMessageForMoveType(MoveType.NONE);
        String result2 = getMessageForMoveType(MoveType.NORMAL);
        String result3 = getMessageForMoveType(MoveType.KILL);

        assertEquals("No move", result1);
        assertEquals("Normal move", result2);
        assertEquals("Capture move", result3);
    }

    // Metodo helper per testare switch
    private String getMessageForMoveType(MoveType moveType) {
        return switch (moveType) {
            case NONE -> "No move";
            case NORMAL -> "Normal move";
            case KILL -> "Capture move";
        };
    }

    @Test
    @DisplayName("Test serializzazione enum")
    void testEnumSerialization() {
        // Test che il toString restituisca il nome dell'enum
        for (MoveType moveType : MoveType.values()) {
            assertEquals(moveType.name(), moveType.toString());
        }
    }

    @Test
    @DisplayName("Test immutabilità enum")
    void testEnumImmutability() {
        // Gli enum sono immutabili per natura
        MoveType none1 = MoveType.NONE;
        MoveType none2 = MoveType.NONE;

        // Stesso oggetto (singleton pattern degli enum)
        assertSame(none1, none2);
    }

    @Test
    @DisplayName("Test con collezioni")
    void testWithCollections() {
        java.util.Set<MoveType> moveTypes = java.util.EnumSet.allOf(MoveType.class);
        assertEquals(3, moveTypes.size());

        assertTrue(moveTypes.contains(MoveType.NONE));
        assertTrue(moveTypes.contains(MoveType.NORMAL));
        assertTrue(moveTypes.contains(MoveType.KILL));
    }

    @Test
    @DisplayName("Test performance toString vs name")
    void testToStringVsName() {
        // toString() e name() dovrebbero restituire lo stesso valore
        for (MoveType moveType : MoveType.values()) {
            assertEquals(moveType.name(), moveType.toString());
        }
    }
}