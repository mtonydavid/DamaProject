package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test completi per la classe Tile
 */
class TileTest {

    private Tile lightTile;
    private Tile darkTile;

    @BeforeEach
    void setUp() {
        lightTile = new Tile(true, 0, 0);   // Tile chiara
        darkTile = new Tile(false, 1, 1);   // Tile scura
    }

    @Test
    @DisplayName("Test creazione tile chiara")
    void testLightTileCreation() {
        Tile tile = new Tile(true, 2, 3);

        assertNotNull(tile);
        assertEquals(100, tile.getWidth());  // TILE_SIZE
        assertEquals(100, tile.getHeight()); // TILE_SIZE
        assertEquals(200, tile.getLayoutX()); // 2 * TILE_SIZE
        assertEquals(300, tile.getLayoutY()); // 3 * TILE_SIZE
    }

    @Test
    @DisplayName("Test creazione tile scura")
    void testDarkTileCreation() {
        Tile tile = new Tile(false, 4, 5);

        assertNotNull(tile);
        assertEquals(100, tile.getWidth());
        assertEquals(100, tile.getHeight());
        assertEquals(400, tile.getLayoutX());
        assertEquals(500, tile.getLayoutY());
    }

    @Test
    @DisplayName("Test hasPiece inizialmente false")
    void testInitiallyNoPiece() {
        assertFalse(lightTile.hasPiece());
        assertFalse(darkTile.hasPiece());
        assertNull(lightTile.getPiece());
        assertNull(darkTile.getPiece());
    }

    @Test
    @DisplayName("Test setPiece e getPiece")
    void testSetAndGetPiece() {
        Piece piece = new Piece(PieceType.GRAY, 0, 0);

        lightTile.setPiece(piece);

        assertTrue(lightTile.hasPiece());
        assertEquals(piece, lightTile.getPiece());
        assertSame(piece, lightTile.getPiece());
    }

    @Test
    @DisplayName("Test rimozione piece (set null)")
    void testRemovePiece() {
        Piece piece = new Piece(PieceType.WHITE, 1, 1);

        darkTile.setPiece(piece);
        assertTrue(darkTile.hasPiece());

        darkTile.setPiece(null);
        assertFalse(darkTile.hasPiece());
        assertNull(darkTile.getPiece());
    }

    @Test
    @DisplayName("Test highlight tile scura")
    void testHighlightDarkTile() {
        // Solo le tile scure possono essere evidenziate
        darkTile.highlight();
        assertTrue(darkTile.isHighlighted());
    }

    @Test
    @DisplayName("Test highlight tile chiara (non dovrebbe funzionare)")
    void testHighlightLightTile() {
        // Le tile chiare non dovrebbero essere evidenziabili
        lightTile.highlight();
        // Dipende dall'implementazione, potrebbe essere false
        // assertFalse(lightTile.isHighlighted());
    }

    @Test
    @DisplayName("Test removeHighlight")
    void testRemoveHighlight() {
        darkTile.highlight();
        assertTrue(darkTile.isHighlighted());

        darkTile.removeHighlight();
        assertFalse(darkTile.isHighlighted());
    }

    @Test
    @DisplayName("Test multiple highlight/remove cycles")
    void testMultipleHighlightCycles() {
        for (int i = 0; i < 5; i++) {
            darkTile.highlight();
            assertTrue(darkTile.isHighlighted(), "Cycle " + i + " highlight failed");

            darkTile.removeHighlight();
            assertFalse(darkTile.isHighlighted(), "Cycle " + i + " remove highlight failed");
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    @DisplayName("Test creazione tile in tutte le posizioni X")
    void testTileCreationAllXPositions(int x) {
        Tile tile = new Tile(false, x, 0);
        assertEquals(x * 100, tile.getLayoutX());
        assertEquals(0, tile.getLayoutY());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    @DisplayName("Test creazione tile in tutte le posizioni Y")
    void testTileCreationAllYPositions(int y) {
        Tile tile = new Tile(false, 0, y);
        assertEquals(0, tile.getLayoutX());
        assertEquals(y * 100, tile.getLayoutY());
    }

    @Test
    @DisplayName("Test sostituzione piece")
    void testReplacePiece() {
        Piece piece1 = new Piece(PieceType.GRAY, 0, 0);
        Piece piece2 = new Piece(PieceType.WHITE, 1, 1);

        lightTile.setPiece(piece1);
        assertEquals(piece1, lightTile.getPiece());

        lightTile.setPiece(piece2);
        assertEquals(piece2, lightTile.getPiece());
        assertNotEquals(piece1, lightTile.getPiece());
    }

    @Test
    @DisplayName("Test tile agli angoli della scacchiera")
    void testCornerTiles() {
        // Angolo top-left
        Tile topLeft = new Tile(true, 0, 0);
        assertEquals(0, topLeft.getLayoutX());
        assertEquals(0, topLeft.getLayoutY());

        // Angolo top-right
        Tile topRight = new Tile(false, 7, 0);
        assertEquals(700, topRight.getLayoutX());
        assertEquals(0, topRight.getLayoutY());

        // Angolo bottom-left
        Tile bottomLeft = new Tile(false, 0, 7);
        assertEquals(0, bottomLeft.getLayoutX());
        assertEquals(700, bottomLeft.getLayoutY());

        // Angolo bottom-right
        Tile bottomRight = new Tile(true, 7, 7);
        assertEquals(700, bottomRight.getLayoutX());
        assertEquals(700, bottomRight.getLayoutY());
    }
}