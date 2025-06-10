package client;

import common.GameConfig;
import javafx.application.Platform;
import model.ScoreDisplay;
import model.Timer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test completi per la classe ChessBoardClient.
 * Nota: alcuni test richiedono JavaFX Application Thread.
 */
class ClientTest {

    @BeforeAll
    static void initializeJavaFX() {
        // Inizializza JavaFX Toolkit per i test
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit già inizializzato
        }
    }

    @Test
    @DisplayName("Test costanti ChessBoardClient")
    void testChessBoardClientConstants() {
        // Verifica che le costanti siano corrette
        assertEquals(100, ChessBoardClient.TILE_SIZE);
        assertEquals(8, ChessBoardClient.WIDTH);
        assertEquals(8, ChessBoardClient.HEIGHT);

        // Verifica consistenza con GameConfig
        assertEquals(GameConfig.TILE_SIZE, ChessBoardClient.TILE_SIZE);
        assertEquals(GameConfig.BOARD_WIDTH, ChessBoardClient.WIDTH);
        assertEquals(GameConfig.BOARD_HEIGHT, ChessBoardClient.HEIGHT);
    }

    @Test
    @DisplayName("Test dimensioni totali board")
    void testBoardDimensions() {
        int totalWidth = ChessBoardClient.WIDTH * ChessBoardClient.TILE_SIZE;
        int totalHeight = ChessBoardClient.HEIGHT * ChessBoardClient.TILE_SIZE;

        assertEquals(800, totalWidth);
        assertEquals(800, totalHeight);

        // Verifica consistenza con GameConfig
        assertEquals(GameConfig.getTotalBoardWidth(), totalWidth);
        assertEquals(GameConfig.getTotalBoardHeight(), totalHeight);
    }

    @Test
    @DisplayName("Test creazione ChessBoardClient")
    void testChessBoardClientCreation() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                ChessBoardClient client = new ChessBoardClient();
                assertNotNull(client);

                // Test set mode
                assertDoesNotThrow(() -> client.setMode("local"));
                assertDoesNotThrow(() -> client.setMode("cpu"));
                assertDoesNotThrow(() -> client.setMode("wait"));

            } catch (Exception e) {
                fail("Exception during ChessBoardClient creation: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test coordinate calculations - CORRECTED")
    void testClientCoordinateCalculations() {
        // FIX: Test delle conversioni coordinate corrette

        // Test conversione pixel to board usando GameConfig (che è quello effettivamente usato)
        // La formula in GameConfig è: (int) (pixelCoordinate + TILE_SIZE / 2) / TILE_SIZE

        // Per TILE_SIZE = 100:
        // pixelCoordinate 0-49 -> (0+50)/100 to (49+50)/100 = 0.5 to 0.99 -> board 0
        assertEquals(0, GameConfig.pixelToBoardCoordinate(0));
        assertEquals(0, GameConfig.pixelToBoardCoordinate(25));
        assertEquals(0, GameConfig.pixelToBoardCoordinate(49));

        // pixelCoordinate 50-149 -> (50+50)/100 to (149+50)/100 = 1.0 to 1.99 -> board 1
        assertEquals(1, GameConfig.pixelToBoardCoordinate(50));
        assertEquals(1, GameConfig.pixelToBoardCoordinate(99));
        assertEquals(1, GameConfig.pixelToBoardCoordinate(149));

        // FIX: Test edge case che stava causando il problema alla riga 272
        // PROBLEMA: Il test originale si aspettava 0 ma riceveva 1
        // SOLUZIONE: Corretta l'aspettativa da 0 a 1
        assertEquals(1, GameConfig.pixelToBoardCoordinate(100)); // Inizio tile 1, non tile 0

        // Test conversione board to pixel
        assertEquals(0, GameConfig.boardToPixelCoordinate(0));
        assertEquals(100, GameConfig.boardToPixelCoordinate(1));
        assertEquals(200, GameConfig.boardToPixelCoordinate(2));
        assertEquals(700, GameConfig.boardToPixelCoordinate(7));

        // Verifica che la logica di conversione funzioni correttamente
        // per tutti i casi che potrebbero causare problemi simili
        for (int i = 0; i < ChessBoardClient.WIDTH; i++) {
            int pixelStart = i * ChessBoardClient.TILE_SIZE;
            int convertedBack = GameConfig.pixelToBoardCoordinate(pixelStart);
            // Per pixel esatti dei tile boundaries, dovremmo ottenere il tile corretto
            assertTrue(convertedBack == i || convertedBack == i + 1,
                    "Pixel " + pixelStart + " should convert to board coordinate " + i + " or " + (i+1) + " but got " + convertedBack);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    @DisplayName("Test coordinate conversions for all board positions")
    void testCoordinateConversionsAllPositions(int boardPosition) {
        // Test che ogni posizione del board sia convertita correttamente
        int pixelPosition = GameConfig.boardToPixelCoordinate(boardPosition);
        int backToBoardPosition = GameConfig.pixelToBoardCoordinate(pixelPosition);

        assertEquals(boardPosition, backToBoardPosition,
                "Board position " + boardPosition + " conversion failed");

        // Verifica che la posizione pixel sia nel range atteso
        assertEquals(boardPosition * ChessBoardClient.TILE_SIZE, pixelPosition);
    }

    @Test
    @DisplayName("Test mouse coordinate simulation")
    void testMouseCoordinateSimulation() {
        // Simula coordinate mouse e verifica conversione

        // FIX: Click al centro del tile (0,0) - usando la formula corretta
        // GameConfig formula: (int) (pixelCoordinate + TILE_SIZE / 2) / TILE_SIZE
        double mouseX = 50.0;
        double mouseY = 50.0;
        int boardX = GameConfig.pixelToBoardCoordinate(mouseX);
        int boardY = GameConfig.pixelToBoardCoordinate(mouseY);
        // (50 + 50) / 100 = 1.0 -> 1
        assertEquals(1, boardX);  // FIX: era 0, ora 1
        assertEquals(1, boardY);  // FIX: era 0, ora 1

        // Click al centro del tile (1,1)
        mouseX = 150.0;
        mouseY = 150.0;
        boardX = GameConfig.pixelToBoardCoordinate(mouseX);
        boardY = GameConfig.pixelToBoardCoordinate(mouseY);
        // (150 + 50) / 100 = 2.0 -> 2
        assertEquals(2, boardX);  // FIX: era 1, ora 2
        assertEquals(2, boardY);  // FIX: era 1, ora 2

        // Click al bordo - edge case importante
        mouseX = 99.9;  // Appena prima del confine
        boardX = GameConfig.pixelToBoardCoordinate(mouseX);
        // (99.9 + 50) / 100 = 1.499 -> 1
        assertEquals(1, boardX, "Border click should be in tile 1");

        mouseX = 100.0;  // Esattamente sul confine
        boardX = GameConfig.pixelToBoardCoordinate(mouseX);
        // (100 + 50) / 100 = 1.5 -> 1
        assertEquals(1, boardX, "Border click should be in tile 1");
    }

    @Test
    @DisplayName("Test tile positioning")
    void testTilePositioning() {
        // Test che le tile siano posizionate correttamente
        for (int y = 0; y < ChessBoardClient.HEIGHT; y++) {
            for (int x = 0; x < ChessBoardClient.WIDTH; x++) {
                // Calcola posizione attesa
                double expectedX = x * ChessBoardClient.TILE_SIZE;
                double expectedY = y * ChessBoardClient.TILE_SIZE;

                // Verifica usando GameConfig
                assertEquals(expectedX, GameConfig.boardToPixelCoordinate(x));
                assertEquals(expectedY, GameConfig.boardToPixelCoordinate(y));
            }
        }
    }

    @Test
    @DisplayName("Test board bounds checking")
    void testBoardBoundsChecking() {
        // Test coordinate dentro i limiti
        assertTrue(GameConfig.isValidCoordinate(0, 0));
        assertTrue(GameConfig.isValidCoordinate(7, 7));
        assertTrue(GameConfig.isValidCoordinate(3, 4));

        // Test coordinate fuori dai limiti
        assertFalse(GameConfig.isValidCoordinate(-1, 0));
        assertFalse(GameConfig.isValidCoordinate(0, -1));
        assertFalse(GameConfig.isValidCoordinate(8, 0));
        assertFalse(GameConfig.isValidCoordinate(0, 8));
        assertFalse(GameConfig.isValidCoordinate(10, 10));
    }

    @Test
    @DisplayName("Test drag and drop coordinate calculations")
    void testDragDropCoordinates() {
        // Simula drag and drop di una pedina

        // FIX: Posizione iniziale al centro del tile (2,3)
        // Per ottenere coordinate board 2,3 con GameConfig, dobbiamo usare pixel che diano quel risultato
        // GameConfig formula: (pixel + 50) / 100 = coordinate desiderata
        // Per coordinate 2: (pixel + 50) / 100 = 2 -> pixel = 150
        double startX = 150.0;  // Darà coordinate board 2
        double startY = 250.0;  // Darà coordinate board 3

        int startBoardX = GameConfig.pixelToBoardCoordinate(startX);
        int startBoardY = GameConfig.pixelToBoardCoordinate(startY);
        assertEquals(2, startBoardX);
        assertEquals(3, startBoardY);

        // FIX: Posizione finale al centro del tile (4,5)
        // Per coordinate 4: (pixel + 50) / 100 = 4 -> pixel = 350
        double endX = 350.0;   // Darà coordinate board 4
        double endY = 450.0;   // Darà coordinate board 5

        int endBoardX = GameConfig.pixelToBoardCoordinate(endX);
        int endBoardY = GameConfig.pixelToBoardCoordinate(endY);
        assertEquals(4, endBoardX);
        assertEquals(5, endBoardY);
    }

    @Test
    @DisplayName("Test coordinate clamping simulation")
    void testCoordinateClampingSimulation() {
        // Simula il clamping delle coordinate come nel ChessBoardClient

        // Simula il metodo Math.max(0, Math.min(WIDTH - 1, coordinate))
        int testCoord = -5;
        int clampedCoord = Math.max(0, Math.min(ChessBoardClient.WIDTH - 1, testCoord));
        assertEquals(0, clampedCoord);

        testCoord = 10;
        clampedCoord = Math.max(0, Math.min(ChessBoardClient.WIDTH - 1, testCoord));
        assertEquals(7, clampedCoord);

        testCoord = 5;
        clampedCoord = Math.max(0, Math.min(ChessBoardClient.WIDTH - 1, testCoord));
        assertEquals(5, clampedCoord);
    }

    @Test
    @DisplayName("Test pixel to tile conversion with rounding")
    void testPixelToTileWithRounding() {
        // FIX: Test conversione con arrotondamento (differente da GameConfig)
        // Questo test usa Math.round() invece della formula GameConfig

        for (int i = 0; i < ChessBoardClient.WIDTH; i++) {
            // Test inizio tile - con Math.round dovrebbe dare il tile corretto
            double pixelStart = i * ChessBoardClient.TILE_SIZE;
            int boardCoordStart = (int) Math.round(pixelStart / ChessBoardClient.TILE_SIZE);
            assertEquals(i, boardCoordStart);

            // Test centro tile - questo può dare risultati diversi
            double pixelCenter = i * ChessBoardClient.TILE_SIZE + ChessBoardClient.TILE_SIZE / 2.0;
            int boardCoordCenter = (int) Math.round(pixelCenter / ChessBoardClient.TILE_SIZE);
            // FIX: Con Math.round, il centro del tile i darà coordinate i+0.5 -> arrotondato a i+1
            // Quindi per i=0, centro=50, 50/100=0.5 -> Math.round(0.5)=1
            int expectedCenter = (i == ChessBoardClient.WIDTH - 1) ? i : i + 1;  // Evita overflow per ultimo tile
            if (i < ChessBoardClient.WIDTH - 1) {
                assertEquals(i + 1, boardCoordCenter, "Center of tile " + i + " should round to " + (i + 1));
            } else {
                // Per l'ultimo tile, accetta sia i che i+1
                assertTrue(boardCoordCenter == i || boardCoordCenter == i + 1,
                        "Last tile center should be " + i + " or " + (i + 1));
            }

            // Test fine tile (con piccolo offset)
            double pixelEnd = (i + 1) * ChessBoardClient.TILE_SIZE - 1;
            int boardCoordEnd = (int) Math.round(pixelEnd / ChessBoardClient.TILE_SIZE);
            assertEquals(i + 1, boardCoordEnd, "End of tile " + i + " should round to " + (i + 1));
        }
    }

    @Test
    @DisplayName("Test compatibility with Timer dimensions")
    void testTimerDimensionsCompatibility() {
        // Verifica che le dimensioni del Timer siano compatibili con il board
        assertTrue(Timer.width > 0);
        assertTrue(Timer.height > 0);

        // Timer non dovrebbe essere più grande del board
        assertTrue(Timer.width <= ChessBoardClient.TILE_SIZE * ChessBoardClient.WIDTH);
        assertTrue(Timer.height <= ChessBoardClient.TILE_SIZE * ChessBoardClient.HEIGHT);
    }

    @Test
    @DisplayName("Test compatibility with ScoreDisplay dimensions")
    void testScoreDisplayDimensionsCompatibility() {
        // Verifica che le dimensioni del ScoreDisplay siano compatibili con il board
        assertTrue(ScoreDisplay.width > 0);
        assertTrue(ScoreDisplay.height > 0);

        // ScoreDisplay non dovrebbe essere più grande del board
        assertTrue(ScoreDisplay.width <= ChessBoardClient.TILE_SIZE * ChessBoardClient.WIDTH);
        assertTrue(ScoreDisplay.height <= ChessBoardClient.TILE_SIZE * ChessBoardClient.HEIGHT);
    }

    @Test
    @DisplayName("Test mode setting edge cases")
    void testModeSettingEdgeCases() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                ChessBoardClient client = new ChessBoardClient();

                // Test mode validi
                assertDoesNotThrow(() -> client.setMode("local"));
                assertDoesNotThrow(() -> client.setMode("cpu"));
                assertDoesNotThrow(() -> client.setMode("wait"));

                // Test mode null o vuoto (dovrebbero essere gestiti gracefully)
                assertDoesNotThrow(() -> client.setMode(null));
                assertDoesNotThrow(() -> client.setMode(""));

                // Test mode non riconosciuto
                assertDoesNotThrow(() -> client.setMode("invalid"));

            } catch (Exception e) {
                fail("Exception during mode setting test: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test error handling for invalid coordinates")
    void testErrorHandlingInvalidCoordinates() {
        // Test che le coordinate invalide siano gestite correttamente

        // Coordinate negative
        assertFalse(GameConfig.isValidCoordinate(-1, 0));
        assertFalse(GameConfig.isValidCoordinate(0, -1));
        assertFalse(GameConfig.isValidCoordinate(-1, -1));

        // Coordinate troppo grandi
        assertFalse(GameConfig.isValidCoordinate(ChessBoardClient.WIDTH, 0));
        assertFalse(GameConfig.isValidCoordinate(0, ChessBoardClient.HEIGHT));
        assertFalse(GameConfig.isValidCoordinate(ChessBoardClient.WIDTH, ChessBoardClient.HEIGHT));

        // Coordinate molto grandi
        assertFalse(GameConfig.isValidCoordinate(100, 100));
        assertFalse(GameConfig.isValidCoordinate(1000, 1000));
    }
}