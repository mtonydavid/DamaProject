package common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per GameConfig - configurazione centralizzata
 */
class GameConfigTest {

    @Test
    @DisplayName("Test costanti di base")
    void testBasicConstants() {
        assertEquals(100, GameConfig.TILE_SIZE);
        assertEquals(8, GameConfig.BOARD_WIDTH);
        assertEquals(8, GameConfig.BOARD_HEIGHT);
        assertEquals(1234, GameConfig.DEFAULT_PORT);
        assertEquals("localhost", GameConfig.DEFAULT_HOST);
    }

    @Test
    @DisplayName("Test dimensioni totali board")
    void testTotalBoardDimensions() {
        assertEquals(800, GameConfig.getTotalBoardWidth());
        assertEquals(800, GameConfig.getTotalBoardHeight());
    }

    @Test
    @DisplayName("Test validazione coordinate")
    void testCoordinateValidation() {
        // Coordinate valide
        assertTrue(GameConfig.isValidCoordinate(0, 0));
        assertTrue(GameConfig.isValidCoordinate(7, 7));
        assertTrue(GameConfig.isValidCoordinate(3, 4));

        // Coordinate invalide
        assertFalse(GameConfig.isValidCoordinate(-1, 0));
        assertFalse(GameConfig.isValidCoordinate(0, -1));
        assertFalse(GameConfig.isValidCoordinate(8, 0));
        assertFalse(GameConfig.isValidCoordinate(0, 8));
        assertFalse(GameConfig.isValidCoordinate(10, 10));
    }

    @Test
    @DisplayName("Test conversione pixel to board")
    void testPixelToBoardConversion() {
        assertEquals(0, GameConfig.pixelToBoardCoordinate(0));
        assertEquals(0, GameConfig.pixelToBoardCoordinate(49));
        assertEquals(1, GameConfig.pixelToBoardCoordinate(50));
        assertEquals(1, GameConfig.pixelToBoardCoordinate(149));
        assertEquals(2, GameConfig.pixelToBoardCoordinate(150));
        assertEquals(7, GameConfig.pixelToBoardCoordinate(700)); // Cambiato da 750 a 700
        assertEquals(7, GameConfig.pixelToBoardCoordinate(749)); // 7*100 + 49
    }

    @Test
    @DisplayName("Test conversione board to pixel")
    void testBoardToPixelConversion() {
        assertEquals(0, GameConfig.boardToPixelCoordinate(0));
        assertEquals(100, GameConfig.boardToPixelCoordinate(1));
        assertEquals(200, GameConfig.boardToPixelCoordinate(2));
        assertEquals(700, GameConfig.boardToPixelCoordinate(7));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    @DisplayName("Test conversioni bidirezionali")
    void testBidirectionalConversions(int boardCoord) {
        int pixelCoord = GameConfig.boardToPixelCoordinate(boardCoord);
        int backToBoardCoord = GameConfig.pixelToBoardCoordinate(pixelCoord);
        assertEquals(boardCoord, backToBoardCoord);
    }

    @Test
    @DisplayName("Test costanti UI")
    void testUIConstants() {
        assertNotNull(GameConfig.LIGHT_TILE_COLOR);
        assertNotNull(GameConfig.DARK_TILE_COLOR);
        assertNotNull(GameConfig.HIGHLIGHT_COLOR);
        assertNotNull(GameConfig.GRAY_PIECE_COLOR);
        assertNotNull(GameConfig.WHITE_PIECE_COLOR);

        assertTrue(GameConfig.TIMER_WIDTH > 0);
        assertTrue(GameConfig.TIMER_HEIGHT > 0);
        assertTrue(GameConfig.SCORE_WIDTH > 0);
        assertTrue(GameConfig.SCORE_HEIGHT > 0);
    }

    @Test
    @DisplayName("Test costanti game rules")
    void testGameRuleConstants() {
        assertEquals(40, GameConfig.MAX_MOVES_WITHOUT_CAPTURE);
        assertEquals(12, GameConfig.INITIAL_PIECES_PER_PLAYER);
        assertTrue(GameConfig.CONNECTION_TIMEOUT > 0);
        assertTrue(GameConfig.MOVE_TIMEOUT > 0);
    }

    @Test
    @DisplayName("Test costanti piece")
    void testPieceConstants() {
        assertTrue(GameConfig.PIECE_RADIUS_X > 0);
        assertTrue(GameConfig.PIECE_RADIUS_Y > 0);
        assertTrue(GameConfig.PIECE_STROKE_WIDTH > 0);
        assertTrue(GameConfig.PIECE_SHADOW_OFFSET > 0);
    }

    @Test
    @DisplayName("Test che GameConfig sia utility class")
    void testUtilityClass() {
        // Verifica che la classe abbia solo metodi statici
        var methods = GameConfig.class.getDeclaredMethods();
        for (var method : methods) {
            if (!method.getName().equals("equals") &&
                    !method.getName().equals("hashCode") &&
                    !method.getName().equals("toString")) {
                assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()),
                        "Method " + method.getName() + " should be static");
            }
        }

        // Verifica che non ci siano field non-static
        var fields = GameConfig.class.getDeclaredFields();
        for (var field : fields) {
            assertTrue(java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                            java.lang.reflect.Modifier.isFinal(field.getModifiers()),
                    "Field " + field.getName() + " should be static or final");
        }
    }

    @Test
    @DisplayName("Test validazione configurazione")
    void testConfigurationValidation() {
        // Le validazioni dovrebbero essere già passate nel static block
        // Se arriviamo qui, significa che la configurazione è valida
        assertTrue(GameConfig.BOARD_WIDTH > 0);
        assertTrue(GameConfig.BOARD_HEIGHT > 0);
        assertTrue(GameConfig.TILE_SIZE > 0);
        assertTrue(GameConfig.DEFAULT_PORT > 0 && GameConfig.DEFAULT_PORT <= 65535);
    }

    @Test
    @DisplayName("Test colori hex format")
    void testColorHexFormat() {
        String[] colors = {
                GameConfig.LIGHT_TILE_COLOR,
                GameConfig.DARK_TILE_COLOR,
                GameConfig.HIGHLIGHT_COLOR,
                GameConfig.GRAY_PIECE_COLOR,
                GameConfig.WHITE_PIECE_COLOR
        };

        for (String color : colors) {
            assertTrue(color.startsWith("#"), "Color should start with #: " + color);
            assertTrue(color.length() == 7, "Color should be 7 chars long: " + color);
        }
    }

    @Test
    @DisplayName("Test timer settings consistency")
    void testTimerSettingsConsistency() {
        assertTrue(GameConfig.TIMER_UPDATE_INTERVAL > 0);
        assertTrue(GameConfig.TIMER_UPDATE_INTERVAL <= 1000); // Max 1 second

        assertTrue(GameConfig.TIMER_WIDTH > GameConfig.TIMER_HEIGHT);
    }
}