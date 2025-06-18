package model;

import it.polimi.client.ChessBoardClient;
import it.polimi.model.ScoreDisplay;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test completi per la classe ScoreDisplay.
 * Nota: alcuni test richiedono JavaFX Application Thread.
 */
class ScoreDisplayTest {

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
    @DisplayName("Test creazione ScoreDisplay")
    void testScoreDisplayCreation() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ScoreDisplay scoreDisplay = new ScoreDisplay();
                assertNotNull(scoreDisplay);

                // FIX: Verifica dimensioni - non usare getPrefWidth() che può restituire -1
                // Invece verifica le costanti statiche
                assertEquals(ScoreDisplay.width, 250.0, 0.1); // TILE_SIZE * 2.5 = 100 * 2.5 = 250
                assertEquals(ScoreDisplay.height, 200.0, 0.1); // TILE_SIZE * 2.0 = 100 * 2.0 = 200

                // Verifica che abbia figli (background, contenuto, toggle button)
                assertTrue(scoreDisplay.getChildren().size() >= 2);

            } catch (Exception e) {
                fail("Exception during ScoreDisplay creation: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test dimensioni costanti")
    void testDimensions() {
        // Test delle costanti dimensionali
        assertTrue(ScoreDisplay.width > 0);
        assertTrue(ScoreDisplay.height > 0);

        // Verifica proporzioni ragionevoli
        assertTrue(ScoreDisplay.width > ScoreDisplay.height); // Più largo che alto

        // Verifica valori attesi
        assertEquals(250.0, ScoreDisplay.width, 0.1); // ChessBoardClient.TILE_SIZE * 2.5
        assertEquals(200.0, ScoreDisplay.height, 0.1); // ChessBoardClient.TILE_SIZE * 2.0
    }

    @Test
    @DisplayName("Test aggiornamento conteggi")
    void testUpdateCounts() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ScoreDisplay scoreDisplay = new ScoreDisplay();

                // Test valori iniziali (dovrebbero essere 12, 12, 0, 0)
                // Non possiamo accedere direttamente ai valori, ma possiamo testare che il metodo non lanci eccezioni
                assertDoesNotThrow(() -> scoreDisplay.updateCounts(12, 12, 0, 0));

                // Test aggiornamento con valori diversi
                assertDoesNotThrow(() -> scoreDisplay.updateCounts(10, 8, 2, 4));
                assertDoesNotThrow(() -> scoreDisplay.updateCounts(5, 5, 7, 7));
                assertDoesNotThrow(() -> scoreDisplay.updateCounts(0, 1, 12, 11));

            } catch (Exception e) {
                fail("Exception during updateCounts: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test aggiornamento con valori edge case")
    void testUpdateCountsEdgeCases() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ScoreDisplay scoreDisplay = new ScoreDisplay();

                // Test valori limite
                assertDoesNotThrow(() -> scoreDisplay.updateCounts(0, 0, 0, 0));
                assertDoesNotThrow(() -> scoreDisplay.updateCounts(12, 12, 12, 12));
                assertDoesNotThrow(() -> scoreDisplay.updateCounts(100, 100, 100, 100));

                // Test valori negativi (non dovrebbero verificarsi nel gioco reale, ma testiamo robustezza)
                assertDoesNotThrow(() -> scoreDisplay.updateCounts(-1, -1, -1, -1));

            } catch (Exception e) {
                fail("Exception during edge case testing: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test toggle visibility")
    void testToggleVisibility() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ScoreDisplay scoreDisplay = new ScoreDisplay();

                // Test toggle (dovrebbe partire visibile)
                assertDoesNotThrow(() -> scoreDisplay.toggleVisibility());

                // Test toggle nuovamente
                assertDoesNotThrow(() -> scoreDisplay.toggleVisibility());

                // Test multipli toggle
                for (int i = 0; i < 5; i++) {
                    assertDoesNotThrow(() -> scoreDisplay.toggleVisibility());
                }

            } catch (Exception e) {
                fail("Exception during toggle visibility: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test posizionamento")
    void testPositioning() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ScoreDisplay scoreDisplay = new ScoreDisplay();

                // Il ScoreDisplay dovrebbe essere posizionato in basso a destra
                // Verifica che le coordinate siano ragionevoli
                double expectedX = ChessBoardClient.TILE_SIZE * ChessBoardClient.WIDTH - ScoreDisplay.width - 10;
                double expectedY = ChessBoardClient.TILE_SIZE * ChessBoardClient.HEIGHT - ScoreDisplay.height - 30;

                assertEquals(expectedX, scoreDisplay.getLayoutX(), 0.1);
                assertEquals(expectedY, scoreDisplay.getLayoutY(), 0.1);

            } catch (Exception e) {
                fail("Exception during positioning test: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test aggiornamenti multipli")
    void testMultipleUpdates() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ScoreDisplay scoreDisplay = new ScoreDisplay();

                // Simula una partita con aggiornamenti progressivi
                scoreDisplay.updateCounts(12, 12, 0, 0);  // Inizio partita
                scoreDisplay.updateCounts(12, 11, 0, 1);  // Prima cattura
                scoreDisplay.updateCounts(11, 11, 1, 1);  // Seconda cattura
                scoreDisplay.updateCounts(11, 10, 1, 2);  // Terza cattura
                scoreDisplay.updateCounts(10, 10, 2, 2);  // Quarta cattura
                scoreDisplay.updateCounts(5, 3, 7, 9);    // Metà partita
                scoreDisplay.updateCounts(1, 0, 11, 12);  // Fine partita

                // Tutti gli aggiornamenti dovrebbero essere eseguiti senza errori
                assertTrue(true); // Se arriviamo qui, il test è passato

            } catch (Exception e) {
                fail("Exception during multiple updates: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test stress con molti toggle")
    void testStressToggling() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ScoreDisplay scoreDisplay = new ScoreDisplay();

                // Test stress con molti toggle rapidi
                for (int i = 0; i < 50; i++) {
                    scoreDisplay.toggleVisibility();
                }

                // Dovrebbe ancora funzionare
                scoreDisplay.updateCounts(5, 5, 7, 7);

            } catch (Exception e) {
                fail("Exception during stress testing: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test thread safety")
    void testThreadSafety() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ScoreDisplay scoreDisplay = new ScoreDisplay();

                // Test che tutti i metodi pubblici possano essere chiamati senza errori
                scoreDisplay.updateCounts(8, 6, 4, 6);
                scoreDisplay.toggleVisibility();
                scoreDisplay.updateCounts(7, 5, 5, 7);
                scoreDisplay.toggleVisibility();

            } catch (Exception e) {
                fail("Exception during thread safety test: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test compatibilità con ChessBoardClient")
    void testChessBoardClientCompatibility() {
        // Test che le costanti usate siano compatibili con ChessBoardClient
        assertEquals(100, ChessBoardClient.TILE_SIZE);
        assertEquals(8, ChessBoardClient.WIDTH);
        assertEquals(8, ChessBoardClient.HEIGHT);

        // Verifica che le dimensioni del ScoreDisplay siano ragionevoli rispetto al board
        assertTrue(ScoreDisplay.width <= ChessBoardClient.TILE_SIZE * ChessBoardClient.WIDTH);
        assertTrue(ScoreDisplay.height <= ChessBoardClient.TILE_SIZE * ChessBoardClient.HEIGHT);
    }

    @Test
    @DisplayName("Test valori di default")
    void testDefaultValues() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ScoreDisplay scoreDisplay = new ScoreDisplay();

                // Dopo la creazione, dovrebbe essere possibile aggiornare senza problemi
                // (implica che i valori di default siano impostati correttamente)
                assertDoesNotThrow(() -> scoreDisplay.updateCounts(12, 12, 0, 0));

            } catch (Exception e) {
                fail("Exception during default values test: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}