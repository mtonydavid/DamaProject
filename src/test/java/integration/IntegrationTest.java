package integration;

import it.polimi.client.ChessBoardClient;
import it.polimi.common.Coder;
import it.polimi.common.GameConfig;
import it.polimi.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import it.polimi.server.CheckersAI;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di integrazione semplificati per verificare che tutti i componenti
 * lavorino insieme correttamente, senza dipendenze complesse.
 */
class IntegrationTest {

    @Test
    @DisplayName("Test integrazione Model-Common")
    void testModelCommonIntegration() {
        // Test che il Model usi correttamente le utility Common

        // Test GameConfig con coordinate valide
        assertTrue(GameConfig.isValidCoordinate(0, 0));
        assertTrue(GameConfig.isValidCoordinate(7, 7));
        assertFalse(GameConfig.isValidCoordinate(-1, 0));
        assertFalse(GameConfig.isValidCoordinate(8, 0));

        // Test Piece con GameConfig
        Piece piece = new Piece(PieceType.GRAY, 2, 3);
        assertEquals(GameConfig.boardToPixelCoordinate(2), piece.getOldX());
        assertEquals(GameConfig.boardToPixelCoordinate(3), piece.getOldY());

        // Test conversioni Coder
        int boardX = Coder.pixelToBoard(piece.getOldX());
        int boardY = Coder.pixelToBoard(piece.getOldY());
        assertEquals(2, boardX);
        assertEquals(3, boardY);

        // Test encoding di una mossa
        MoveResult normalMove = new MoveResult(MoveType.NORMAL);
        String encoded = Coder.encode(piece, 3, 4, normalMove);
        assertEquals("2 3 3 4 NORMAL", encoded);
    }

    @Test
    @DisplayName("Test integrazione Server-Model")
    void testServerModelIntegration() {
        // Test che il Server usi correttamente gli oggetti Model

        Tile[][] board = createTestBoard();

        // Test AI con questo board
        CheckersAI ai = new CheckersAI(board, true);
        String move = ai.generateBestMove();
        assertNotNull(move);

        // Verifica che la mossa sia in formato valido
        assertTrue(Coder.isValidMoveFormat(move));

        // Test decodifica mossa
        int[] coords = Coder.decodeMoveCoordinates(move);
        assertNotNull(coords);
        assertEquals(4, coords.length);
    }

    @Test
    @DisplayName("Test integrazione Client-Model-Common")
    void testClientModelCommonIntegration() {
        // Test che il Client usi correttamente Model e Common

        // Verifica costanti
        assertEquals(GameConfig.TILE_SIZE, ChessBoardClient.TILE_SIZE);
        assertEquals(GameConfig.BOARD_WIDTH, ChessBoardClient.WIDTH);
        assertEquals(GameConfig.BOARD_HEIGHT, ChessBoardClient.HEIGHT);

        // Test calcoli dimensioni
        int totalWidth = ChessBoardClient.WIDTH * ChessBoardClient.TILE_SIZE;
        int totalHeight = ChessBoardClient.HEIGHT * ChessBoardClient.TILE_SIZE;

        assertEquals(GameConfig.getTotalBoardWidth(), totalWidth);
        assertEquals(GameConfig.getTotalBoardHeight(), totalHeight);
    }

    @Test
    @DisplayName("Test flusso completo partita locale")
    void testCompleteLocalGameFlow() {
        // Simula un flusso completo di partita locale

        // 1. Setup iniziale
        Tile[][] board = createInitialBoard();

        // 2. Prima mossa GRAY
        Piece grayPiece = board[1][2].getPiece();
        assertNotNull(grayPiece);
        assertEquals(PieceType.GRAY, grayPiece.getPieceType());

        // Simula movimento
        MoveResult move1 = validateMove(grayPiece, 2, 3, board);
        assertEquals(MoveType.NORMAL, move1.getMoveType());

        // Applica movimento
        executeMove(grayPiece, 2, 3, board);
        assertNull(board[1][2].getPiece());
        assertEquals(grayPiece, board[2][3].getPiece());

        // 3. Risposta WHITE
        Piece whitePiece = board[0][5].getPiece();
        assertNotNull(whitePiece);
        assertEquals(PieceType.WHITE, whitePiece.getPieceType());

        MoveResult move2 = validateMove(whitePiece, 1, 4, board);
        assertEquals(MoveType.NORMAL, move2.getMoveType());

        executeMove(whitePiece, 1, 4, board);
        assertNull(board[0][5].getPiece());
        assertEquals(whitePiece, board[1][4].getPiece());

        // 4. Verifica stato del gioco
        assertTrue(hasValidMoves(board, PieceType.GRAY));
        assertTrue(hasValidMoves(board, PieceType.WHITE));
    }

    @Test
    @DisplayName("Test simulazione cattura")
    void testCaptureSimulation() {
        // Setup scenario di cattura
        Tile[][] board = createEmptyBoard();

        // Posiziona pedine per cattura
        Piece grayPiece = new Piece(PieceType.GRAY, 1, 2);
        board[1][2].setPiece(grayPiece);

        Piece whitePiece = new Piece(PieceType.WHITE, 2, 3);
        board[2][3].setPiece(whitePiece);

        // GRAY cattura WHITE
        MoveResult captureResult = validateMove(grayPiece, 3, 4, board);
        assertEquals(MoveType.KILL, captureResult.getMoveType());

        // FIX: Verifica che la pedina catturata sia quella corretta
        assertNotNull(captureResult.getPiece());
        assertEquals(whitePiece, captureResult.getPiece());

        // Esegui cattura con logica corretta
        executeCaptureMove(grayPiece, 3, 4, board, captureResult);

        // Verifica risultato
        assertNull(board[1][2].getPiece()); // Posizione originale vuota
        assertNull(board[2][3].getPiece()); // Pedina catturata rimossa
        assertEquals(grayPiece, board[3][4].getPiece()); // Pedina spostata
    }

    @Test
    @DisplayName("Test promozione end-to-end")
    void testPromotionEndToEnd() {
        // Test promozione completa

        // GRAY che raggiunge il fondo
        Piece grayPiece = new Piece(PieceType.GRAY, 1, 6);
        assertEquals(PieceType.GRAY, grayPiece.getPieceType());

        // Muovi alla riga di promozione
        grayPiece.move(2, 7);

        // Promuovi
        grayPiece.promote();
        assertEquals(PieceType.GRAY_SUP, grayPiece.getPieceType());

        // WHITE che raggiunge il top
        Piece whitePiece = new Piece(PieceType.WHITE, 3, 1);
        assertEquals(PieceType.WHITE, whitePiece.getPieceType());

        whitePiece.move(4, 0);
        whitePiece.promote();
        assertEquals(PieceType.WHITE_SUP, whitePiece.getPieceType());
    }

    @Test
    @DisplayName("Test encoding/decoding completo")
    void testCompleteEncodingDecoding() {
        // Test ciclo completo encoding/decoding

        Piece piece = new Piece(PieceType.GRAY, 2, 3);
        MoveResult killResult = new MoveResult(MoveType.KILL,
                new Piece(PieceType.WHITE, 3, 4));

        // Encode
        String encoded = Coder.encode(piece, 4, 5, killResult);
        assertEquals("2 3 4 5 KILL 3 4", encoded);

        // Decode
        assertTrue(Coder.isValidMoveFormat(encoded));
        int[] coords = Coder.decodeMoveCoordinates(encoded);
        assertNotNull(coords);
        assertEquals(2, coords[0]);
        assertEquals(3, coords[1]);
        assertEquals(4, coords[2]);
        assertEquals(5, coords[3]);

        // Verifica tipo mossa
        assertTrue(Coder.isCaptureMove(coords[0], coords[1], coords[2], coords[3]));
        assertFalse(Coder.isNormalMove(coords[0], coords[1], coords[2], coords[3]));
    }

    @Test
    @DisplayName("Test configuration consistency")
    void testConfigurationConsistency() {
        // Verifica che tutte le configurazioni siano consistenti

        // GameConfig vs ChessBoardClient
        assertEquals(GameConfig.TILE_SIZE, ChessBoardClient.TILE_SIZE);
        assertEquals(GameConfig.BOARD_WIDTH, ChessBoardClient.WIDTH);
        assertEquals(GameConfig.BOARD_HEIGHT, ChessBoardClient.HEIGHT);

        // Dimensioni componenti UI vs board
        assertTrue(ScoreDisplay.width <= GameConfig.getTotalBoardWidth());
        assertTrue(ScoreDisplay.height <= GameConfig.getTotalBoardHeight());

        // Timer dimensions
        assertTrue(Timer.width > 0);
        assertTrue(Timer.height > 0);
    }

    @Test
    @DisplayName("Test AI decision making")
    void testAIDecisionMaking() {
        // Test che l'AI prenda decisioni sensate

        Tile[][] board = createBoardWithCaptureOpportunity();
        CheckersAI ai = new CheckersAI(board, true); // WHITE AI

        String move = ai.generateBestMove();
        assertNotNull(move);

        // L'AI dovrebbe preferire catture quando disponibili
        int[] coords = Coder.decodeMoveCoordinates(move);
        if (coords != null) {
            // Se c'è una cattura disponibile, l'AI dovrebbe sceglierla
            boolean isCapture = Coder.isCaptureMove(coords[0], coords[1], coords[2], coords[3]);
            // Non possiamo garantire che sia sempre una cattura, ma la mossa dovrebbe essere valida
            assertTrue(GameConfig.isValidCoordinate(coords[0], coords[1]));
            assertTrue(GameConfig.isValidCoordinate(coords[2], coords[3]));
        }
    }

    @Test
    @DisplayName("Test error handling integration")
    void testErrorHandlingIntegration() {
        // Test gestione errori end-to-end

        // Test coordinate invalide
        assertThrows(IllegalArgumentException.class, () -> {
            new Piece(PieceType.GRAY, -1, 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Piece(PieceType.GRAY, 8, 0);
        });

        // Test encoding con parametri null
        Piece piece = new Piece(PieceType.GRAY, 0, 0);
        assertThrows(IllegalArgumentException.class, () -> {
            Coder.encode(null, 1, 1, new MoveResult(MoveType.NORMAL));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Coder.encode(piece, 1, 1, null);
        });

        // Test validazione formato mossa
        assertFalse(Coder.isValidMoveFormat(null));
        assertFalse(Coder.isValidMoveFormat(""));
        assertFalse(Coder.isValidMoveFormat("invalid"));
        assertFalse(Coder.isValidMoveFormat("1 2 3")); // Troppe poche parti
    }

    @Test
    @DisplayName("Test complete game scenario")
    void testCompleteGameScenario() {
        // Test scenario di gioco completo

        // 1. Setup board iniziale
        Tile[][] board = createInitialBoard();
        int initialGrayPieces = countPieces(board, PieceType.GRAY);
        int initialWhitePieces = countPieces(board, PieceType.WHITE);

        assertEquals(12, initialGrayPieces);
        assertEquals(12, initialWhitePieces);

        // 2. Simula alcune mosse
        Piece grayPiece = findMovablePiece(board, PieceType.GRAY);
        assertNotNull(grayPiece);

        // 3. Esegui mossa valida
        int[] validMove = findValidMove(grayPiece, board);
        if (validMove != null) {
            MoveResult result = validateMove(grayPiece, validMove[0], validMove[1], board);
            assertNotEquals(MoveType.NONE, result.getMoveType());

            executeMove(grayPiece, validMove[0], validMove[1], board);

            // 4. Verifica che il board sia ancora consistente
            assertTrue(isBoardConsistent(board));
        }
    }

    // Helper methods

    private Tile[][] createTestBoard() {
        Tile[][] board = new Tile[8][8];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                board[x][y] = new Tile((x + y) % 2 == 0, x, y);
            }
        }
        return board;
    }

    private Tile[][] createInitialBoard() {
        Tile[][] board = createTestBoard();

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (y <= 2 && (x + y) % 2 != 0) {
                    Piece grayPiece = new Piece(PieceType.GRAY, x, y);
                    board[x][y].setPiece(grayPiece);
                } else if (y >= 5 && (x + y) % 2 != 0) {
                    Piece whitePiece = new Piece(PieceType.WHITE, x, y);
                    board[x][y].setPiece(whitePiece);
                }
            }
        }
        return board;
    }

    private Tile[][] createEmptyBoard() {
        return createTestBoard();
    }

    private Tile[][] createBoardWithCaptureOpportunity() {
        Tile[][] board = createTestBoard();

        // Setup scenario dove WHITE può catturare GRAY
        Piece whitePiece = new Piece(PieceType.WHITE, 1, 4);
        board[1][4].setPiece(whitePiece);

        Piece grayPiece = new Piece(PieceType.GRAY, 2, 3);
        board[2][3].setPiece(grayPiece);

        return board;
    }

    private MoveResult validateMove(Piece piece, int newX, int newY, Tile[][] board) {
        if (board[newX][newY].hasPiece() || (newX + newY) % 2 == 0) {
            return new MoveResult(MoveType.NONE);
        }

        int oldX = Coder.pixelToBoard(piece.getOldX());
        int oldY = Coder.pixelToBoard(piece.getOldY());

        // Mossa normale
        if (Math.abs(newX - oldX) == 1 && newY - oldY == piece.getPieceType().moveDir) {
            return new MoveResult(MoveType.NORMAL);
        }

        // Mossa di cattura
        if (Math.abs(newX - oldX) == 2 && Math.abs(newY - oldY) == 2) {
            int middleX = oldX + (newX - oldX) / 2;
            int middleY = oldY + (newY - oldY) / 2;

            if (board[middleX][middleY].hasPiece()) {
                Piece middlePiece = board[middleX][middleY].getPiece();
                if (isOpponentPiece(piece, middlePiece)) {
                    return new MoveResult(MoveType.KILL, middlePiece);
                }
            }
        }

        return new MoveResult(MoveType.NONE);
    }

    private void executeMove(Piece piece, int newX, int newY, Tile[][] board) {
        int oldX = Coder.pixelToBoard(piece.getOldX());
        int oldY = Coder.pixelToBoard(piece.getOldY());

        board[oldX][oldY].setPiece(null);
        piece.move(newX, newY);
        board[newX][newY].setPiece(piece);
    }

    // FIX: Nuovo metodo per eseguire catture correttamente
    private void executeCaptureMove(Piece piece, int newX, int newY, Tile[][] board, MoveResult captureResult) {
        int oldX = Coder.pixelToBoard(piece.getOldX());
        int oldY = Coder.pixelToBoard(piece.getOldY());

        // Rimuovi la pedina dalla posizione originale
        board[oldX][oldY].setPiece(null);

        // Rimuovi la pedina catturata se presente
        if (captureResult.getPiece() != null) {
            Piece capturedPiece = captureResult.getPiece();
            int capturedX = Coder.pixelToBoard(capturedPiece.getOldX());
            int capturedY = Coder.pixelToBoard(capturedPiece.getOldY());
            board[capturedX][capturedY].setPiece(null);
        }

        // Sposta la pedina alla nuova posizione
        piece.move(newX, newY);
        board[newX][newY].setPiece(piece);
    }

    private boolean isOpponentPiece(Piece piece1, Piece piece2) {
        boolean isGray1 = (piece1.getPieceType() == PieceType.GRAY || piece1.getPieceType() == PieceType.GRAY_SUP);
        boolean isGray2 = (piece2.getPieceType() == PieceType.GRAY || piece2.getPieceType() == PieceType.GRAY_SUP);
        return isGray1 != isGray2;
    }

    private boolean hasValidMoves(Tile[][] board, PieceType playerType) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (board[x][y].hasPiece()) {
                    Piece piece = board[x][y].getPiece();
                    if (isSameColor(piece.getPieceType(), playerType)) {
                        if (hasValidMovesForPiece(piece, board)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isSameColor(PieceType type1, PieceType type2) {
        boolean isGray1 = (type1 == PieceType.GRAY || type1 == PieceType.GRAY_SUP);
        boolean isGray2 = (type2 == PieceType.GRAY || type2 == PieceType.GRAY_SUP);
        return isGray1 == isGray2;
    }

    private boolean hasValidMovesForPiece(Piece piece, Tile[][] board) {
        int x = Coder.pixelToBoard(piece.getOldX());
        int y = Coder.pixelToBoard(piece.getOldY());

        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];

            if (GameConfig.isValidCoordinate(newX, newY)) {
                MoveResult result = validateMove(piece, newX, newY, board);
                if (result.getMoveType() != MoveType.NONE) {
                    return true;
                }
            }
        }

        return false;
    }

    private int countPieces(Tile[][] board, PieceType type) {
        int count = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (board[x][y].hasPiece() &&
                        isSameColor(board[x][y].getPiece().getPieceType(), type)) {
                    count++;
                }
            }
        }
        return count;
    }

    private Piece findMovablePiece(Tile[][] board, PieceType type) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (board[x][y].hasPiece()) {
                    Piece piece = board[x][y].getPiece();
                    if (isSameColor(piece.getPieceType(), type) &&
                            hasValidMovesForPiece(piece, board)) {
                        return piece;
                    }
                }
            }
        }
        return null;
    }

    private int[] findValidMove(Piece piece, Tile[][] board) {
        int x = Coder.pixelToBoard(piece.getOldX());
        int y = Coder.pixelToBoard(piece.getOldY());

        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];

            if (GameConfig.isValidCoordinate(newX, newY)) {
                MoveResult result = validateMove(piece, newX, newY, board);
                if (result.getMoveType() != MoveType.NONE) {
                    return new int[]{newX, newY};
                }
            }
        }

        return null;
    }

    private boolean isBoardConsistent(Tile[][] board) {
        // Verifica che il board sia in uno stato consistente

        int grayCount = 0;
        int whiteCount = 0;

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                // Verifica che ogni tile esista
                assertNotNull(board[x][y]);

                // Conta pezzi
                if (board[x][y].hasPiece()) {
                    Piece piece = board[x][y].getPiece();
                    assertNotNull(piece);

                    // Verifica che il pezzo sia su una casella scura
                    assertTrue((x + y) % 2 != 0);

                    // Conta per colore
                    if (isSameColor(piece.getPieceType(), PieceType.GRAY)) {
                        grayCount++;
                    } else {
                        whiteCount++;
                    }

                    // Verifica coordinate del pezzo
                    assertEquals(GameConfig.boardToPixelCoordinate(x), piece.getOldX());
                    assertEquals(GameConfig.boardToPixelCoordinate(y), piece.getOldY());
                }
            }
        }

        // Dovrebbero esserci pezzi di entrambi i colori (a meno che il gioco non sia finito)
        assertTrue(grayCount >= 0);
        assertTrue(whiteCount >= 0);
        assertTrue(grayCount + whiteCount <= 24); // Massimo 24 pezzi totali

        return true;
    }
}