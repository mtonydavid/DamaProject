package it.polimi.common;

/**
 * Configurazione centralizzata del gioco.
 * Contiene tutte le costanti condivise tra i vari componenti.
 */
public final class GameConfig {

    public static final int TILE_SIZE = 100;
    public static final int BOARD_WIDTH = 8;
    public static final int BOARD_HEIGHT = 8;
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 1234;
    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int MOVE_TIMEOUT = 10;
    public static final int MAX_MOVES_WITHOUT_CAPTURE = 40;
    public static final int INITIAL_PIECES_PER_PLAYER = 12;
    public static final String LIGHT_TILE_COLOR = "#C1A89F";
    public static final String DARK_TILE_COLOR = "#5D5364";
    public static final String HIGHLIGHT_COLOR = "#BADA55";
    public static final String GRAY_PIECE_COLOR = "#4F4F4F";
    public static final String WHITE_PIECE_COLOR = "#CEB087";
    public static final int TIMER_UPDATE_INTERVAL = 100;
    public static final double TIMER_WIDTH = TILE_SIZE * 1.6;
    public static final double TIMER_HEIGHT = TILE_SIZE * 0.4;
    public static final double SCORE_WIDTH = TILE_SIZE * 2.5;
    public static final double SCORE_HEIGHT = TILE_SIZE * 2.0;
    public static final double PIECE_RADIUS_X = TILE_SIZE * 0.3125;
    public static final double PIECE_RADIUS_Y = TILE_SIZE * 0.26;
    public static final double PIECE_STROKE_WIDTH = TILE_SIZE * 0.03;
    public static final double PIECE_SHADOW_OFFSET = TILE_SIZE * 0.07;


    static {

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
     * @return true se le coordinate sono dentro i limiti della scacchiera
     */
    public static boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < BOARD_WIDTH && y >= 0 && y < BOARD_HEIGHT;
    }

    /**
     * Converte coordinate pixel in coordinate scacchiera.
     * @return coordinata scacchiera (0-7)
     */
    public static int pixelToBoardCoordinate(double pixelCoordinate) {
        // Stessa logica dell'originale Coder.pixelToBoard()
        return (int) (pixelCoordinate + TILE_SIZE / 2) / TILE_SIZE;
    }

    /**
     * Converte coordinate scacchiera in coordinate pixel.
     * @return coordinata in pixel
     */
    public static int boardToPixelCoordinate(int boardCoordinate) {
        return boardCoordinate * TILE_SIZE;
    }
}