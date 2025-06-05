package common;

/**
 * Configurazione centralizzata del gioco.
 * Contiene tutte le costanti condivise tra i vari componenti.
 *
 * @author DamaProject Team
 * @version 1.0
 */
public final class GameConfig {

    // === BOARD SETTINGS ===
    /** Dimensione di ogni tile in pixel */
    public static final int TILE_SIZE = 100;

    /** Larghezza della scacchiera (numero di colonne) */
    public static final int BOARD_WIDTH = 8;

    /** Altezza della scacchiera (numero di righe) */
    public static final int BOARD_HEIGHT = 8;

    // === NETWORK SETTINGS ===
    /** Host di default per connessioni */
    public static final String DEFAULT_HOST = "localhost";

    /** Porta di default del server */
    public static final int DEFAULT_PORT = 1234;

    /** Timeout connessione in millisecondi */
    public static final int CONNECTION_TIMEOUT = 5000;

    /** Timeout per mosse in secondi */
    public static final int MOVE_TIMEOUT = 10;

    // === GAME RULES ===
    /** Numero massimo di mosse senza cattura prima della patta */
    public static final int MAX_MOVES_WITHOUT_CAPTURE = 40;

    /** Numero iniziale di pedine per giocatore */
    public static final int INITIAL_PIECES_PER_PLAYER = 12;

    // === UI SETTINGS ===
    /** Colore delle tile chiare */
    public static final String LIGHT_TILE_COLOR = "#C1A89F";

    /** Colore delle tile scure */
    public static final String DARK_TILE_COLOR = "#5D5364";

    /** Colore evidenziazione mosse possibili */
    public static final String HIGHLIGHT_COLOR = "#BADA55";

    /** Colore pedine grigie */
    public static final String GRAY_PIECE_COLOR = "#4F4F4F";

    /** Colore pedine bianche */
    public static final String WHITE_PIECE_COLOR = "#CEB087";

    // === TIMER SETTINGS ===
    /** Intervallo aggiornamento timer in millisecondi */
    public static final int TIMER_UPDATE_INTERVAL = 100;

    /** Larghezza timer */
    public static final double TIMER_WIDTH = TILE_SIZE * 1.6;

    /** Altezza timer */
    public static final double TIMER_HEIGHT = TILE_SIZE * 0.4;

    // === SCORE DISPLAY SETTINGS ===
    /** Larghezza score display */
    public static final double SCORE_WIDTH = TILE_SIZE * 2.5;

    /** Altezza score display */
    public static final double SCORE_HEIGHT = TILE_SIZE * 2.0;

    // === PIECE SETTINGS ===
    /** Raggio orizzontale delle pedine */
    public static final double PIECE_RADIUS_X = TILE_SIZE * 0.3125;

    /** Raggio verticale delle pedine */
    public static final double PIECE_RADIUS_Y = TILE_SIZE * 0.26;

    /** Spessore bordo pedine */
    public static final double PIECE_STROKE_WIDTH = TILE_SIZE * 0.03;

    /** Offset ombra pedine */
    public static final double PIECE_SHADOW_OFFSET = TILE_SIZE * 0.07;

    // === VALIDATION ===
    static {
        // Validazione configurazione al caricamento classe
        if (BOARD_WIDTH <= 0 || BOARD_HEIGHT <= 0) {
            throw new IllegalStateException("Board dimensions must be positive");
        }
        if (TILE_SIZE <= 0) {
            throw new IllegalStateException("Tile size must be positive");
        }
        if (DEFAULT_PORT <= 0 || DEFAULT_PORT > 65535) {
            throw new IllegalStateException("Port must be between 1 and 65535");
        }
    }

    /**
     * Costruttore privato per impedire istanziazione.
     * Questa è una utility class con solo costanti statiche.
     */
    private GameConfig() {
        throw new UnsupportedOperationException("GameConfig is a utility class and cannot be instantiated");
    }

    /**
     * Calcola la dimensione totale della scacchiera in pixel (larghezza).
     * @return larghezza totale in pixel
     */
    public static int getTotalBoardWidth() {
        return BOARD_WIDTH * TILE_SIZE;
    }

    /**
     * Calcola la dimensione totale della scacchiera in pixel (altezza).
     * @return altezza totale in pixel
     */
    public static int getTotalBoardHeight() {
        return BOARD_HEIGHT * TILE_SIZE;
    }

    /**
     * Verifica se le coordinate sono valide.
     * @param x coordinata x
     * @param y coordinata y
     * @return true se le coordinate sono dentro i limiti della scacchiera
     */
    public static boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < BOARD_WIDTH && y >= 0 && y < BOARD_HEIGHT;
    }

    /**
     * Converte coordinate pixel in coordinate scacchiera.
     * @param pixelCoordinate coordinata in pixel
     * @return coordinata scacchiera (0-7)
     */
    public static int pixelToBoardCoordinate(double pixelCoordinate) {
        // Stessa logica dell'originale Coder.pixelToBoard()
        return (int) (pixelCoordinate + TILE_SIZE / 2) / TILE_SIZE;
    }

    /**
     * Converte coordinate scacchiera in coordinate pixel.
     * @param boardCoordinate coordinata scacchiera (0-7)
     * @return coordinata in pixel
     */
    public static int boardToPixelCoordinate(int boardCoordinate) {
        return boardCoordinate * TILE_SIZE;
    }
}