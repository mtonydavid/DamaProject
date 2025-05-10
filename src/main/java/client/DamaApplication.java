package client;

import javafx.application.Application;

/**
 * Classe principale per avviare l'applicazione della Dama.
 * Questa classe semplicemente avvia la schermata iniziale,
 * che successivamente gestirà l'avvio del gioco vero e proprio.
 */
public class DamaApplication {

    public static void main(String[] args) {
        // Avvia la schermata iniziale
        Application.launch(StartScreen.class, args);
    }
}