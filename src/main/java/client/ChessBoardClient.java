package client;

import common.Coder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.*;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChessBoardClient extends Application {
    public static final int TILE_SIZE = 100;
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;

    private String mode = null;
    private final Tile[][] board = new Tile[WIDTH][HEIGHT];

    private final Group tileGroup = new Group();
    private final Group pieceGroup = new Group();

    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;

    private int player;
    private int winner = 0;
    private final Label colorLabel = new Label();

    // Separate timers for local mode
    private float grayTime = 0;
    private float whiteTime = 0;
    private float time = 0; // Keep for online mode compatibility
    private final Timer grayTimer = new Timer("GRAY");
    private final Timer whiteTimer = new Timer("WHITE");
    private final Timer timer = new Timer(); // Keep for online mode

    // Reference to the game stage
    private Stage gameStage;

    // Contatori per tenere traccia delle pedine
    private int grayLivePieces = 12;
    private int whiteLivePieces = 12;
    private int grayKilledPieces = 0;
    private int whiteKilledPieces = 0;
    private final ScoreDisplay scoreDisplay = new ScoreDisplay();

    // FIXED: Gestione turni migliorata per modalità online
    private boolean isItMyTurn = false;
    private boolean waitingForServerResponse = false;

    // Add separate variable for local mode turn management
    private boolean isWhiteTurn = true; // WHITE always starts first in local mode

    private Piece selectedPiece = null;

    // NEW: Variables for advanced rules
    private boolean mustCapture = false; // Mangiata obbligatoria
    private boolean isInMultiJump = false; // Multi-jump in corso
    private Piece multiJumpPiece = null; // Pedina che sta facendo multi-jump
    private int movesWithoutCapture = 0; // Contatore per regola dei 40 turni
    private final int MAX_MOVES_WITHOUT_CAPTURE = 40; // Limite per patta

    // FIXED: Timeout per evitare blocchi
    private ScheduledExecutorService timeoutExecutor;

    public static void main(String[] args) {
        if (args.length > 0) {
            // Se ci sono argomenti, avvia direttamente la partita
            launch(args);
        } else {
            // Altrimenti, avvia la schermata iniziale
            StartScreen.main(args);
        }
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        this.gameStage = stage;

        if (mode == null || mode.isEmpty()) {
            // Se il modo non è stato impostato, ma abbiamo args, usa quello
            if (getParameters() != null && !getParameters().getRaw().isEmpty()) {
                mode = getParameters().getRaw().get(0);
            } else {
                // Fallback a local se non c'è nulla
                mode = "local";
            }
        }

        // Gestisci la connessione solo per modalità "wait" (online) o "cpu"
        if ("wait".equals(mode) || "cpu".equals(mode)) {
            try {
                socket = new Socket("localhost", 1234);
            } catch (IOException e) {
                Thread.sleep(5000);
                socket = new Socket("localhost", 1234);
            }

            try {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                bufferedWriter.write(mode);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                if (bufferedReader.readLine().equals("1")) {
                    player = 1;
                } else {
                    player = 2;
                }
            } catch (IOException e) {
                closeEverything();
            }

            // FIXED: Inizializza timeout executor
            timeoutExecutor = Executors.newSingleThreadScheduledExecutor();
            countTime();
            listenToServer();
        } else {
            // Modalità locale, senza connessione
            player = 1; // Keep this for compatibility, but use isWhiteTurn for actual turn management
            countTimeLocal();
            isWhiteTurn = true; // WHITE starts first in local mode
        }

        Scene scene = new Scene(createContent());
        stage.setTitle("Checkers Game");
        stage.setScene(scene);
        stage.show();
    }

    private Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);

        if ("local".equals(mode)) {
            // Add separate timers for local mode
            root.getChildren().addAll(tileGroup, pieceGroup, colorLabel, grayTimer, whiteTimer, scoreDisplay);
        } else {
            // Use single timer for online mode
            root.getChildren().addAll(tileGroup, pieceGroup, colorLabel, timer, scoreDisplay);
        }

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                Tile tile = new Tile((x + y) % 2 == 0, x, y);
                board[x][y] = tile;

                tileGroup.getChildren().add(tile);

                Piece piece = null;
                if (y <= 2 && (x + y) % 2 != 0) {
                    piece = makePiece(PieceType.GRAY, x, y);
                } else if (y >= 5 && (x + y) % 2 != 0) {
                    piece = makePiece(PieceType.WHITE, x, y);
                }

                if (piece != null) {
                    tile.setPiece(piece);
                    pieceGroup.getChildren().add(piece);
                }
            }
        }

        colorLabel.relocate(0, 0);

        if ("local".equals(mode)) {
            updateTurnLabel();
        } else {
            updateOnlineLabel();
        }

        // Aggiorna il display del punteggio iniziale
        scoreDisplay.updateCounts(grayLivePieces, whiteLivePieces, grayKilledPieces, whiteKilledPieces);

        return root;
    }

    private Piece makePiece(PieceType pieceType, int x, int y) {
        Piece piece = new Piece(pieceType, x, y);

        piece.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if ("local".equals(mode)) {
                handleLocalPieceSelection(piece, pieceType);
            } else {
                handleOnlinePieceSelection(piece, pieceType);
            }
        });

        piece.setOnMouseReleased(e -> {
            int newX = (int) Math.round(piece.getLayoutX() / TILE_SIZE);
            int newY = (int) Math.round(piece.getLayoutY() / TILE_SIZE);

            newX = Math.max(0, Math.min(WIDTH - 1, newX));
            newY = Math.max(0, Math.min(HEIGHT - 1, newY));

            if ("local".equals(mode)) {
                handleLocalMove(piece, newX, newY);
            } else {
                requestMove(piece, newX, newY);
            }

            removeAllHighlights();
            selectedPiece = null;
        });

        return piece;
    }

    // FIXED: Separata logica per selezione pezzi locale
    private void handleLocalPieceSelection(Piece piece, PieceType pieceType) {
        boolean isWhitePiece = pieceType == PieceType.WHITE || pieceType == PieceType.WHITE_SUP;

        if (isInMultiJump && piece != multiJumpPiece) {
            return;
        }

        if (isWhiteTurn == isWhitePiece) {
            removeAllHighlights();
            selectedPiece = piece;
            highlightPossibleMoves(piece);
        }
    }

    // FIXED: Separata logica per selezione pezzi online
    private void handleOnlinePieceSelection(Piece piece, PieceType pieceType) {
        if (!isItMyTurn || waitingForServerResponse) {
            return;
        }

        // Verifica che sia il mio pezzo
        boolean isMyPiece = ((player == 1 && (pieceType == PieceType.GRAY || pieceType == PieceType.GRAY_SUP)) ||
                (player == 2 && (pieceType == PieceType.WHITE || pieceType == PieceType.WHITE_SUP)));

        if (!isMyPiece) {
            return;
        }

        // Se siamo in multi-jump, solo quella pedina può muoversi
        if (isInMultiJump && piece != multiJumpPiece) {
            return;
        }

        // Aggiorna mustCapture prima di evidenziare
        updateMustCapture();

        removeAllHighlights();
        selectedPiece = piece;
        highlightPossibleMoves(piece);
    }

    private void handleLocalMove(Piece piece, int newX, int newY) {
        // Check if it's the correct player's turn
        boolean isWhitePiece = piece.getPieceType() == PieceType.WHITE || piece.getPieceType() == PieceType.WHITE_SUP;

        if (isWhiteTurn != isWhitePiece) {
            // Wrong player trying to move
            piece.abortMove();
            return;
        }

        // Se siamo in multi-jump, solo quella pedina può muoversi
        if (isInMultiJump && piece != multiJumpPiece) {
            piece.abortMove();
            return;
        }

        // Verifica se la cella di destinazione è evidenziata
        if (!isValidCoordinate(newX, newY) || !board[newX][newY].isHighlighted()) {
            piece.abortMove();
            return;
        }

        MoveResult result = tryMove(piece, newX, newY);

        // Verifica mangiata obbligatoria
        if (mustCapture && result.getMoveType() != MoveType.KILL) {
            piece.abortMove();
            return;
        }

        makeMove(piece, newX, newY, result);

        // Gestisci il cambio turno e multi-jump
        if (result.getMoveType() != MoveType.NONE) {
            if (result.getMoveType() == MoveType.KILL) {
                // Reset contatore mosse senza cattura
                movesWithoutCapture = 0;

                // Controlla se ci sono altre catture possibili con la stessa pedina
                List<MoveResult> possibleCaptures = findPossibleCaptures(piece);
                if (!possibleCaptures.isEmpty()) {
                    // Multi-jump: la stessa pedina deve continuare a catturare
                    isInMultiJump = true;
                    multiJumpPiece = piece;
                    updateTurnLabel(); // Aggiorna la label per mostrare il multi-jump
                    return; // Non cambiare turno
                } else {
                    // Nessuna cattura aggiuntiva possibile
                    isInMultiJump = false;
                    multiJumpPiece = null;
                }
            } else {
                // Mossa normale
                movesWithoutCapture++;
                isInMultiJump = false;
                multiJumpPiece = null;
            }

            // Cambia turno
            isWhiteTurn = !isWhiteTurn;

            // Controlla la regola dei 40 turni
            if (movesWithoutCapture >= MAX_MOVES_WITHOUT_CAPTURE) {
                winner = 0; // Patta
                showVictoryScreen("DRAW - 40 moves without capture!");
                return;
            }

            // Aggiorna mustCapture per il prossimo turno
            updateMustCapture();
            updateTurnLabel();
        }
    }

    public void requestMove(Piece piece, int newX, int newY) {
        // FIXED: Controlli più rigorosi
        if (!isItMyTurn || waitingForServerResponse) {
            System.out.println("Cannot move: myTurn=" + isItMyTurn + ", waiting=" + waitingForServerResponse);
            piece.abortMove();
            return;
        }

        // Verifica se la cella di destinazione è evidenziata (validazione lato client)
        if (!isValidCoordinate(newX, newY) || !board[newX][newY].isHighlighted()) {
            System.out.println("Invalid destination - aborting move");
            piece.abortMove();
            return;
        }

        try {
            int oldBoardX = Coder.pixelToBoard(piece.getOldX());
            int oldBoardY = Coder.pixelToBoard(piece.getOldY());
            String moveMessage = oldBoardX + " " + oldBoardY + " " + newX + " " + newY;
            System.out.println("Sending move: " + moveMessage);

            bufferedWriter.write(moveMessage);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // FIXED: Imposta stato e timeout
            waitingForServerResponse = true;
            startMoveTimeout();

            System.out.println("Move sent, waiting for server response...");
        } catch (IOException e) {
            System.out.println("Error sending move: " + e.getMessage());
            piece.abortMove();
            waitingForServerResponse = false;
            e.printStackTrace();
        }
    }

    // FIXED: Timeout per evitare blocchi
    private void startMoveTimeout() {
        if (timeoutExecutor != null) {
            timeoutExecutor.schedule(() -> {
                if (waitingForServerResponse) {
                    System.out.println("Move timeout - resetting state");
                    Platform.runLater(() -> {
                        waitingForServerResponse = false;
                        // Non resettiamo isItMyTurn qui, aspettiamo il prossimo PING
                    });
                }
            }, 10, TimeUnit.SECONDS);
        }
    }

    private void makeMove(Piece piece, int newX, int newY, MoveResult moveResult) {
        MoveType moveType = moveResult.getMoveType();
        switch (moveType) {
            case NONE -> {
                System.out.println("Move rejected - aborting");
                piece.abortMove();
                // FIXED: Reset completo stato per modalità online
                if (!mode.equals("local")) {
                    waitingForServerResponse = false;
                    isItMyTurn = false;
                    isInMultiJump = false;
                    multiJumpPiece = null;
                    updateOnlineLabel();
                }
            }
            case NORMAL -> {
                board[Coder.pixelToBoard(piece.getOldX())][Coder.pixelToBoard(piece.getOldY())].setPiece(null);
                piece.move(newX, newY);
                board[newX][newY].setPiece(piece);

                // FIXED: Per modalità online, reset stato dopo mossa normale
                if (!mode.equals("local")) {
                    waitingForServerResponse = false;
                    isItMyTurn = false; // Il turno finisce dopo una mossa normale
                    isInMultiJump = false;
                    multiJumpPiece = null;
                    updateOnlineLabel();
                    System.out.println("Normal move completed - turn ended");
                }

                // Promozione
                if ((newY == 7 && piece.getPieceType() == PieceType.GRAY) || (newY == 0 && piece.getPieceType() == PieceType.WHITE)) {
                    Platform.runLater(piece::promote);
                }
            }
            case KILL -> {
                board[Coder.pixelToBoard(piece.getOldX())][Coder.pixelToBoard(piece.getOldY())].setPiece(null);
                piece.move(newX, newY);
                board[newX][newY].setPiece(piece);

                Piece otherPiece = moveResult.getPiece();
                board[Coder.pixelToBoard(otherPiece.getOldX())][Coder.pixelToBoard(otherPiece.getOldY())].setPiece(null);
                Platform.runLater(() -> pieceGroup.getChildren().remove(otherPiece));

                // Aggiorna il conteggio delle pedine catturate
                if (otherPiece.getPieceType() == PieceType.GRAY || otherPiece.getPieceType() == PieceType.GRAY_SUP) {
                    grayLivePieces--;
                    whiteKilledPieces++;
                } else {
                    whiteLivePieces--;
                    grayKilledPieces++;
                }

                // Aggiorna il display del punteggio
                Platform.runLater(() -> scoreDisplay.updateCounts(grayLivePieces, whiteLivePieces, grayKilledPieces, whiteKilledPieces));

                // FIXED: Per modalità online, gestione più accurata post-cattura
                if (!mode.equals("local")) {
                    waitingForServerResponse = false;

                    // Controlla se ci sono altre catture possibili
                    List<MoveResult> additionalCaptures = findPossibleCaptures(piece);
                    if (!additionalCaptures.isEmpty()) {
                        // Potenziale multi-jump - aspetta conferma server
                        isInMultiJump = true;
                        multiJumpPiece = piece;
                        System.out.println("Potential multi-jump - waiting for server PING");
                        // NON impostare isItMyTurn = false qui - aspetta il server
                    } else {
                        // Nessuna cattura possibile - turno finito
                        isItMyTurn = false;
                        isInMultiJump = false;
                        multiJumpPiece = null;
                        System.out.println("No more captures - turn ended");
                    }
                    updateOnlineLabel();
                }

                // Promozione
                if ((newY == 7 && piece.getPieceType() == PieceType.GRAY) || (newY == 0 && piece.getPieceType() == PieceType.WHITE)) {
                    Platform.runLater(piece::promote);
                }

                // Verifica se la partita è finita
                checkForWinner();
            }
        }
    }

    // FIXED: Label aggiornata per modalità online
    private void updateOnlineLabel() {
        String status = isItMyTurn ? "YOUR TURN" : "OPPONENT'S TURN";
        String playerColor = (player == 1) ? "GRAY" : "WHITE";
        String extra = "";

        if (waitingForServerResponse) {
            extra = " (Sending move...)";
        } else if (isInMultiJump) {
            extra = " (Multi-Jump!)";
        } else if (mustCapture && isItMyTurn) {
            extra = " (Must Capture!)";
        }

        colorLabel.setText("You are " + playerColor + " - " + status + extra);
    }

    /**
     * Trova tutte le catture possibili per una pedina specifica
     */
    private List<MoveResult> findPossibleCaptures(Piece piece) {
        List<MoveResult> captures = new ArrayList<>();
        int x = Coder.pixelToBoard(piece.getOldX());
        int y = Coder.pixelToBoard(piece.getOldY());

        // Direzioni da controllare in base al tipo di pedina
        int[][] directions;
        if (piece.getPieceType() == PieceType.GRAY_SUP || piece.getPieceType() == PieceType.WHITE_SUP) {
            // Dame: tutte le direzioni
            directions = new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        } else {
            // Pedine normali: solo avanti
            int moveDir = piece.getPieceType().moveDir;
            directions = new int[][]{{-1, moveDir}, {1, moveDir}};
        }

        for (int[] dir : directions) {
            int middleX = x + dir[0];
            int middleY = y + dir[1];
            int destX = x + dir[0] * 2;
            int destY = y + dir[1] * 2;

            if (isValidCoordinate(destX, destY) && isValidCoordinate(middleX, middleY) &&
                    !board[destX][destY].hasPiece() && board[middleX][middleY].hasPiece()) {

                Piece middlePiece = board[middleX][middleY].getPiece();
                if (isOpponentPiece(piece, middlePiece)) {
                    captures.add(new MoveResult(MoveType.KILL, middlePiece));
                }
            }
        }

        return captures;
    }

    /**
     * Trova tutte le catture possibili per il giocatore corrente
     */
    private List<MoveResult> findAllPossibleCaptures() {
        List<MoveResult> allCaptures = new ArrayList<>();

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                if (board[x][y].hasPiece()) {
                    Piece piece = board[x][y].getPiece();
                    boolean isCurrentPlayerPiece = false;

                    if ("local".equals(mode)) {
                        // Logica locale
                        boolean isWhitePiece = piece.getPieceType() == PieceType.WHITE || piece.getPieceType() == PieceType.WHITE_SUP;
                        isCurrentPlayerPiece = (isWhiteTurn == isWhitePiece);
                    } else {
                        // Logica per online/CPU
                        isCurrentPlayerPiece = ((player == 1 && (piece.getPieceType() == PieceType.GRAY || piece.getPieceType() == PieceType.GRAY_SUP)) ||
                                (player == 2 && (piece.getPieceType() == PieceType.WHITE || piece.getPieceType() == PieceType.WHITE_SUP)));
                    }

                    if (isCurrentPlayerPiece) {
                        allCaptures.addAll(findPossibleCaptures(piece));
                    }
                }
            }
        }

        return allCaptures;
    }

    /**
     * Aggiorna la variabile mustCapture in base alle catture disponibili
     */
    private void updateMustCapture() {
        if (isInMultiJump && multiJumpPiece != null) {
            // Durante multi-jump, controlla solo la pedina corrente
            List<MoveResult> captures = findPossibleCaptures(multiJumpPiece);
            mustCapture = !captures.isEmpty();
        } else {
            // Controllo normale per tutte le pedine
            List<MoveResult> possibleCaptures = findAllPossibleCaptures();
            mustCapture = !possibleCaptures.isEmpty();
        }
    }

    /**
     * Verifica se una pedina è avversaria
     */
    private boolean isOpponentPiece(Piece piece, Piece otherPiece) {
        PieceType pieceType = piece.getPieceType();
        PieceType otherType = otherPiece.getPieceType();

        boolean isGrayPiece = (pieceType == PieceType.GRAY || pieceType == PieceType.GRAY_SUP);
        boolean isOtherGray = (otherType == PieceType.GRAY || otherType == PieceType.GRAY_SUP);

        return isGrayPiece != isOtherGray;
    }

    /**
     * Aggiorna la label del turno con informazioni aggiuntive
     */
    private void updateTurnLabel() {
        if ("local".equals(mode)) {
            String currentPlayer = isWhiteTurn ? "WHITE" : "GRAY";
            String extra = "";

            if (isInMultiJump) {
                extra = " (Multi-Jump!)";
            } else if (mustCapture) {
                extra = " (Must Capture!)";
            }

            colorLabel.setText("Local Mode - Turn: " + currentPlayer + extra + " | Moves without capture: " + movesWithoutCapture);
        }
    }

    private MoveResult tryMove(Piece piece, int newX, int newY) {
        if (board[newX][newY].hasPiece() || (newX + newY) % 2 == 0) {
            return new MoveResult(MoveType.NONE);
        }

        int oldX = Coder.pixelToBoard(piece.getOldX());
        int oldY = Coder.pixelToBoard(piece.getOldY());

        // Regole di movimento normali
        if (piece.getPieceType() == PieceType.GRAY || piece.getPieceType() == PieceType.WHITE) {
            if (Math.abs(newX - oldX) == 1 && newY - oldY == piece.getPieceType().moveDir) {
                return new MoveResult(MoveType.NORMAL);
            } else if (Math.abs(newX - oldX) == 2 && Math.abs(newY - oldY) == 2) {
                int middleX = oldX + (newX - oldX) / 2;
                int middleY = oldY + (newY - oldY) / 2;

                if (board[middleX][middleY].hasPiece() &&
                        ((piece.getPieceType() == PieceType.GRAY &&
                                (board[middleX][middleY].getPiece().getPieceType() == PieceType.WHITE ||
                                        board[middleX][middleY].getPiece().getPieceType() == PieceType.WHITE_SUP)) ||
                                (piece.getPieceType() == PieceType.WHITE &&
                                        (board[middleX][middleY].getPiece().getPieceType() == PieceType.GRAY ||
                                                board[middleX][middleY].getPiece().getPieceType() == PieceType.GRAY_SUP)))) {
                    return new MoveResult(MoveType.KILL, board[middleX][middleY].getPiece());
                }
            }
        }
        // Regole per la dama (pedina promossa)
        else if (piece.getPieceType() == PieceType.GRAY_SUP || piece.getPieceType() == PieceType.WHITE_SUP) {
            if (Math.abs(newX - oldX) == 1 && Math.abs(newY - oldY) == 1) {
                return new MoveResult(MoveType.NORMAL);
            } else if (Math.abs(newX - oldX) == 2 && Math.abs(newY - oldY) == 2) {
                int middleX = oldX + (newX - oldX) / 2;
                int middleY = oldY + (newY - oldY) / 2;

                if (board[middleX][middleY].hasPiece() &&
                        ((piece.getPieceType() == PieceType.GRAY_SUP &&
                                (board[middleX][middleY].getPiece().getPieceType() == PieceType.WHITE ||
                                        board[middleX][middleY].getPiece().getPieceType() == PieceType.WHITE_SUP)) ||
                                (piece.getPieceType() == PieceType.WHITE_SUP &&
                                        (board[middleX][middleY].getPiece().getPieceType() == PieceType.GRAY ||
                                                board[middleX][middleY].getPiece().getPieceType() == PieceType.GRAY_SUP)))) {
                    return new MoveResult(MoveType.KILL, board[middleX][middleY].getPiece());
                }
            }
        }

        return new MoveResult(MoveType.NONE);
    }

    private void checkForWinner() {
        boolean grayExists = false;
        boolean whiteExists = false;

        // Controlla se ci sono ancora pezzi di entrambi i colori
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                if (board[x][y].hasPiece()) {
                    PieceType type = board[x][y].getPiece().getPieceType();
                    if (type == PieceType.GRAY || type == PieceType.GRAY_SUP) {
                        grayExists = true;
                    } else if (type == PieceType.WHITE || type == PieceType.WHITE_SUP) {
                        whiteExists = true;
                    }
                }
            }
        }

        // Se un giocatore non ha più pezzi, l'altro ha vinto
        if (!grayExists) {
            winner = 2;
            showVictoryScreen("WHITE WON!");
        } else if (!whiteExists) {
            winner = 1;
            showVictoryScreen("GRAY WON!");
        }
    }

    private void showVictoryScreen(String winnerText) {
        // Get the total time for the winner in local mode
        int gameTimeInSeconds;
        if ("local".equals(mode)) {
            // In local mode, show the time of the winning player
            gameTimeInSeconds = (winner == 1) ? (int) grayTime : (winner == 2) ? (int) whiteTime : Math.max((int) grayTime, (int) whiteTime);
        } else {
            gameTimeInSeconds = (int) time;
        }

        VictoryScreen victoryScreen = new VictoryScreen(
                winnerText,
                gameTimeInSeconds,
                grayLivePieces,
                whiteLivePieces,
                grayKilledPieces,
                whiteKilledPieces,
                mode
        );

        Platform.runLater(victoryScreen::show);
    }

    // FIXED: Timer locale con gestione corretta
    public void countTimeLocal() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            if (winner != 0) {
                String winnerText;
                if (winner == 1) {
                    winnerText = "GRAY WON!";
                } else if (winner == 2) {
                    winnerText = "WHITE WON!";
                } else {
                    winnerText = "DRAW!";
                }

                Platform.runLater(() -> {
                    grayTimer.set("GRAY: " + (int) grayTime + "s");
                    whiteTimer.set("WHITE: " + (int) whiteTime + "s");
                });
                Platform.runLater(() -> showVictoryScreen(winnerText));
                executor.shutdown();
                return;
            }

            // Update the timer for the current player
            if (isWhiteTurn) {
                whiteTime += 0.1;
                Platform.runLater(() -> whiteTimer.set("WHITE: " + (int) whiteTime + "s"));
            } else {
                grayTime += 0.1;
                Platform.runLater(() -> grayTimer.set("GRAY: " + (int) grayTime + "s"));
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    // FIXED: Timer online con controlli rigorosi
    public void countTime() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            if (winner != 0) {
                String winnerText = (winner == player) ? "YOU WON!" : "YOU LOST!";
                Platform.runLater(() -> timer.set(winnerText));

                Platform.runLater(() -> {
                    if (winner == 1) {
                        showVictoryScreen("GRAY WON!");
                    } else if (winner == 2) {
                        showVictoryScreen("WHITE WON!");
                    } else {
                        showVictoryScreen("DRAW!");
                    }
                });
                executor.shutdown();
                return;
            }

            // FIXED: Timer continua SOLO se è il mio turno E non sto aspettando risposta
            if (isItMyTurn && !waitingForServerResponse) {
                time += 0.1;
                Platform.runLater(() -> timer.set("Timer: " + (int) time + "s."));
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    // FIXED: Listener server completamente rivisto
    public void listenToServer() {
        new Thread(() -> {
            String message;

            while (socket != null && socket.isConnected() && winner == 0) {
                try {
                    message = bufferedReader.readLine();
                    if (message == null) {
                        System.out.println("Server disconnected");
                        break;
                    }

                    System.out.println("Received from server: " + message);

                    if (message.startsWith("PING")) {
                        handleServerPing();
                    } else if (message.startsWith("CHAT ")) {
                        handleChatMessage(message);
                    } else {
                        handleGameMessage(message);
                    }
                } catch (IOException e) {
                    System.out.println("Connection lost: " + e.getMessage());
                    closeEverything();
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid message format: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Unexpected error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            closeEverything();
        }).start();
    }

    // FIXED: Gestione PING separata e accurata
    private void handleServerPing() {
        System.out.println("PING received - It's my turn!");

        // Reset completo dello stato per nuovo turno
        isItMyTurn = true;
        waitingForServerResponse = false;

        // Se non eravamo in multi-jump, reset completo
        if (!isInMultiJump) {
            multiJumpPiece = null;
        }

        // Aggiorna mustCapture per il nuovo turno
        updateMustCapture();

        Platform.runLater(this::updateOnlineLabel);

        System.out.println("Turn state: myTurn=" + isItMyTurn +
                ", multiJump=" + isInMultiJump +
                ", mustCapture=" + mustCapture);
    }

    // FIXED: Gestione messaggi chat separata
    private void handleChatMessage(String message) {
        // Per ora ignoriamo i messaggi chat lato client
        // Potrebbero essere implementati in futuro
        System.out.println("Chat message ignored: " + message);
    }

    // FIXED: Gestione messaggi di gioco separata e migliorata
    private void handleGameMessage(String message) {
        String[] partsOfMessage = message.split(" ");
        if (partsOfMessage.length < 5) {
            System.out.println("Invalid game message format: " + message);
            return;
        }

        // Parse delle coordinate
        int fromX, fromY, newX, newY;
        try {
            fromX = Integer.parseInt(partsOfMessage[0]);
            fromY = Integer.parseInt(partsOfMessage[1]);
            newX = Integer.parseInt(partsOfMessage[2]);
            newY = Integer.parseInt(partsOfMessage[3]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid coordinates in message: " + message);
            return;
        }

        Piece piece = board[fromX][fromY].getPiece();
        if (piece == null) {
            System.out.println("No piece at " + fromX + "," + fromY);
            return;
        }

        String moveType = partsOfMessage[4];
        System.out.println("Processing server response: " + moveType);

        switch (moveType) {
            case "NONE" -> handleMoveRejected(piece, newX, newY);
            case "NORMAL" -> handleNormalMove(piece, newX, newY);
            case "KILL" -> handleKillMove(piece, newX, newY, partsOfMessage);
            case "END1" -> handleGameEnd(1, "GRAY WON!");
            case "END2" -> handleGameEnd(2, "WHITE WON!");
            case "DRAW" -> handleGameEnd(0, "DRAW - 40 moves without capture!");
            default -> System.out.println("Unknown move type: " + moveType);
        }
    }

    private void handleMoveRejected(Piece piece, int newX, int newY) {
        System.out.println("Server rejected move");
        makeMove(piece, newX, newY, new MoveResult(MoveType.NONE));
    }

    private void handleNormalMove(Piece piece, int newX, int newY) {
        System.out.println("Server confirmed normal move");
        makeMove(piece, newX, newY, new MoveResult(MoveType.NORMAL));
    }

    private void handleKillMove(Piece piece, int newX, int newY, String[] parts) {
        if (parts.length < 7) {
            System.out.println("Invalid KILL message format");
            return;
        }

        try {
            int killX = Integer.parseInt(parts[5]);
            int killY = Integer.parseInt(parts[6]);
            Piece killedPiece = board[killX][killY].getPiece();

            if (killedPiece == null) {
                System.out.println("No piece to kill at " + killX + "," + killY);
                return;
            }

            System.out.println("Server confirmed capture move");
            makeMove(piece, newX, newY, new MoveResult(MoveType.KILL, killedPiece));

        } catch (NumberFormatException e) {
            System.out.println("Invalid kill coordinates");
        }
    }

    private void handleGameEnd(int winnerPlayer, String message) {
        winner = winnerPlayer;
        Platform.runLater(() -> showVictoryScreen(message));
    }

    /**
     * Evidenzia tutte le celle in cui è possibile muoversi con la pedina selezionata.
     */
    private void highlightPossibleMoves(Piece piece) {
        if (piece == null) return;

        int x = Coder.pixelToBoard(piece.getOldX());
        int y = Coder.pixelToBoard(piece.getOldY());

        // Se mustCapture è true, evidenzia solo le catture
        if (mustCapture) {
            highlightOnlyCaptures(piece, x, y);
            return;
        }

        // Altrimenti evidenzia tutte le mosse possibili
        highlightAllMoves(piece, x, y);
    }

    /**
     * Evidenzia solo le mosse di cattura per una pedina
     */
    private void highlightOnlyCaptures(Piece piece, int x, int y) {
        // Direzioni da controllare in base al tipo di pedina
        int[][] directions;
        if (piece.getPieceType() == PieceType.GRAY_SUP || piece.getPieceType() == PieceType.WHITE_SUP) {
            // Dame: tutte le direzioni
            directions = new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        } else {
            // Pedine normali: solo avanti
            int moveDir = piece.getPieceType().moveDir;
            directions = new int[][]{{-1, moveDir}, {1, moveDir}};
        }

        for (int[] dir : directions) {
            checkAndHighlightCaptureTile(piece, x + dir[0] * 2, y + dir[1] * 2, x + dir[0], y + dir[1]);
        }
    }

    /**
     * Evidenzia tutte le mosse possibili (normale e catture)
     */
    private void highlightAllMoves(Piece piece, int x, int y) {
        // Verifica le mosse per le pedine normali
        if (piece.getPieceType() == PieceType.GRAY || piece.getPieceType() == PieceType.WHITE) {
            // Direzione di movimento (in base al colore)
            int moveDir = piece.getPieceType().moveDir;

            // Controlla mosse normali (diagonali adiacenti)
            checkAndHighlightTile(x - 1, y + moveDir);
            checkAndHighlightTile(x + 1, y + moveDir);

            // Controlla mosse di cattura (diagonali a distanza 2)
            checkAndHighlightCaptureTile(piece, x - 2, y + moveDir * 2, x - 1, y + moveDir);
            checkAndHighlightCaptureTile(piece, x + 2, y + moveDir * 2, x + 1, y + moveDir);
        }
        // Verifica le mosse per le dame
        else if (piece.getPieceType() == PieceType.GRAY_SUP || piece.getPieceType() == PieceType.WHITE_SUP) {
            // Le dame possono muoversi in tutte le direzioni diagonali
            checkAndHighlightTile(x - 1, y - 1);
            checkAndHighlightTile(x + 1, y - 1);
            checkAndHighlightTile(x - 1, y + 1);
            checkAndHighlightTile(x + 1, y + 1);

            // Controlla mosse di cattura in tutte le direzioni
            checkAndHighlightCaptureTile(piece, x - 2, y - 2, x - 1, y - 1);
            checkAndHighlightCaptureTile(piece, x + 2, y - 2, x + 1, y - 1);
            checkAndHighlightCaptureTile(piece, x - 2, y + 2, x - 1, y + 1);
            checkAndHighlightCaptureTile(piece, x + 2, y + 2, x + 1, y + 1);
        }
    }

    /**
     * Verifica se una cella è valida per una mossa normale e la evidenzia.
     */
    private void checkAndHighlightTile(int x, int y) {
        if (isValidCoordinate(x, y) && !board[x][y].hasPiece() && (x + y) % 2 != 0) {
            board[x][y].highlight();
        }
    }

    /**
     * Verifica se una cella è valida per una mossa di cattura e la evidenzia.
     */
    private void checkAndHighlightCaptureTile(Piece piece, int destX, int destY, int middleX, int middleY) {
        if (isValidCoordinate(destX, destY) && isValidCoordinate(middleX, middleY) &&
                !board[destX][destY].hasPiece() && board[middleX][middleY].hasPiece() &&
                (destX + destY) % 2 != 0) {

            Piece middlePiece = board[middleX][middleY].getPiece();
            if (isOpponentPiece(piece, middlePiece)) {
                board[destX][destY].highlight();
            }
        }
    }

    /**
     * Rimuove tutte le evidenziazioni dalla scacchiera.
     */
    private void removeAllHighlights() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                board[x][y].removeHighlight();
            }
        }
    }

    /**
     * Verifica se le coordinate sono valide.
     */
    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    private void closeEverything() {
        try {
            if (timeoutExecutor != null) {
                timeoutExecutor.shutdown();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}