package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test completi per la classe Timer
 */
class TimerTest {

    @Test
    @DisplayName("Test creazione timer default (online mode)")
    void testDefaultTimerCreation() {
        Timer timer = new Timer();

        assertNotNull(timer);
        assertEquals(160, timer.getPrefWidth(), 0.1); // TILE_SIZE * 1.6
        assertEquals(40, timer.getPrefHeight(), 0.1);  // TILE_SIZE * 0.4
    }

    @Test
    @DisplayName("Test creazione timer GRAY")
    void testGrayTimerCreation() {
        Timer grayTimer = new Timer("GRAY");

        assertNotNull(grayTimer);
        assertEquals(160, grayTimer.getPrefWidth(), 0.1);
        assertEquals(40, grayTimer.getPrefHeight(), 0.1);

        // Verifica posizione GRAY (sinistra)
        assertEquals(20, grayTimer.getLayoutX(), 0.1); // TILE_SIZE * 0.2
        assertEquals(20, grayTimer.getLayoutY(), 0.1); // TILE_SIZE * 0.2
    }

    @Test
    @DisplayName("Test creazione timer WHITE")
    void testWhiteTimerCreation() {
        Timer whiteTimer = new Timer("WHITE");

        assertNotNull(whiteTimer);
        assertEquals(160, whiteTimer.getPrefWidth(), 0.1);
        assertEquals(40, whiteTimer.getPrefHeight(), 0.1);

        // Verifica posizione WHITE (destra)
        assertEquals(600, whiteTimer.getLayoutX(), 0.1); // TILE_SIZE * (WIDTH - 2)
        assertEquals(20, whiteTimer.getLayoutY(), 0.1);
    }

    @Test
    @DisplayName("Test set timer text")
    void testSetTimerText() {
        Timer timer = new Timer();

        timer.set("Test: 30s");
        // Non possiamo facilmente testare il testo interno senza accesso al Label
        // Ma possiamo verificare che il metodo non lanci eccezioni
        assertDoesNotThrow(() -> timer.set("Another text"));
    }

    @Test
    @DisplayName("Test set con testi diversi")
    void testSetVariousTexts() {
        Timer timer = new Timer("GRAY");

        // Test vari formati di testo
        assertDoesNotThrow(() -> timer.set("GRAY: 0s"));
        assertDoesNotThrow(() -> timer.set("GRAY: 123s"));
        assertDoesNotThrow(() -> timer.set("Timer: 5m 30s"));
        assertDoesNotThrow(() -> timer.set(""));
        assertDoesNotThrow(() -> timer.set(null));
    }

    @Test
    @DisplayName("Test timer con player name null")
    void testTimerWithNullPlayerName() {
        Timer timer = new Timer(null);

        assertNotNull(timer);
        // Dovrebbe usare posizione default (centro)
        assertEquals(300, timer.getLayoutX(), 0.1); // (TILE_SIZE * WIDTH - width) / 2
        assertEquals(0, timer.getLayoutY(), 0.1);
    }

    @Test
    @DisplayName("Test timer con player name vuoto")
    void testTimerWithEmptyPlayerName() {
        Timer timer = new Timer("");

        assertNotNull(timer);
        // Dovrebbe usare posizione default
        assertEquals(300, timer.getLayoutX(), 0.1);
        assertEquals(0, timer.getLayoutY(), 0.1);
    }

    @Test
    @DisplayName("Test timer con player name non riconosciuto")
    void testTimerWithUnknownPlayerName() {
        Timer timer = new Timer("UNKNOWN");

        assertNotNull(timer);
        // Dovrebbe usare posizione default
        assertEquals(300, timer.getLayoutX(), 0.1);
        assertEquals(0, timer.getLayoutY(), 0.1);
    }

    @Test
    @DisplayName("Test dimensioni timer costanti")
    void testTimerDimensionsConsistent() {
        Timer timer1 = new Timer();
        Timer timer2 = new Timer("GRAY");
        Timer timer3 = new Timer("WHITE");

        // Tutti i timer dovrebbero avere le stesse dimensioni
        assertEquals(timer1.getPrefWidth(), timer2.getPrefWidth());
        assertEquals(timer1.getPrefWidth(), timer3.getPrefWidth());
        assertEquals(timer1.getPrefHeight(), timer2.getPrefHeight());
        assertEquals(timer1.getPrefHeight(), timer3.getPrefHeight());
    }

    @Test
    @DisplayName("Test case sensitivity player names")
    void testPlayerNameCaseSensitivity() {
        Timer grayUpper = new Timer("GRAY");
        Timer grayLower = new Timer("gray");
        Timer grayMixed = new Timer("Gray");

        // Solo "GRAY" esatto dovrebbe essere riconosciuto
        assertEquals(20, grayUpper.getLayoutX(), 0.1);
        assertEquals(300, grayLower.getLayoutX(), 0.1); // Default position
        assertEquals(300, grayMixed.getLayoutX(), 0.1); // Default position
    }

    @Test
    @DisplayName("Test multiple set calls")
    void testMultipleSetCalls() {
        Timer timer = new Timer("WHITE");

        // Test chiamate multiple senza eccezioni
        for (int i = 0; i < 10; i++) {
            final int seconds = i;
            assertDoesNotThrow(() -> timer.set("WHITE: " + seconds + "s"));
        }
    }
}