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

import java.io.*;
import java.net.Socket;
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

    private float time = 0;
    private final Timer timer = new Timer();

    // Contatori per tenere traccia delle pedine
    private int grayLivePieces = 12;
    private int whiteLivePieces = 12;
    private int grayKilledPieces = 0;
    private int whiteKilledPieces = 0;
    private final ScoreDisplay scoreDisplay = new ScoreDisplay();

    private boolean isItMyTurn = false;

    private Piece selectedPiece = null;

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

            countTime();
            listenToServer();
        } else {
            // Modalità locale, senza connessione
            player = 1; // Il giocatore 1 inizia sempre
            countTime();
            isItMyTurn = true; // Il primo giocatore inizia
        }

        Scene scene = new Scene(createContent());
        stage.setTitle("Dama - Partita in corso");
        stage.setScene(scene);
        stage.show();
    }

    private Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        root.getChildren().addAll(tileGroup, pieceGroup, colorLabel, timer, scoreDisplay);

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
            colorLabel.setText("Modalità Locale - Turno: GRAY");
        } else {
            colorLabel.setText("Tu giochi" + ((player == 1) ? " GRAY" : " WHITE"));
        }

        // Aggiorna il display del punteggio iniziale
        scoreDisplay.updateCounts(grayLivePieces, whiteLivePieces, grayKilledPieces, whiteKilledPieces);

        return root;
    }

    private Piece makePiece(PieceType pieceType, int x, int y) {
        Piece piece = new Piece(pieceType, x, y);

        piece.setOnMousePressed(e -> {
            // Se è il turno del giocatore, seleziona la pedina e mostra le mosse disponibili
            if (("local".equals(mode) &&
                    ((isItMyTurn && (pieceType == PieceType.GRAY || pieceType == PieceType.GRAY_SUP)) ||
                            (!isItMyTurn && (pieceType == PieceType.WHITE || pieceType == PieceType.WHITE_SUP)))) ||
                    ((!mode.equals("local")) && isItMyTurn &&
                            ((player == 1 && (pieceType == PieceType.GRAY || pieceType == PieceType.GRAY_SUP)) ||
                                    (player == 2 && (pieceType == PieceType.WHITE || pieceType == PieceType.WHITE_SUP))))) {

                // Rimuovi l'evidenziazione precedente se esiste
                removeAllHighlights();

                // Seleziona la nuova pedina
                selectedPiece = piece;

                // Mostra le mosse disponibili
                highlightPossibleMoves(piece);
            }
        });

        piece.setOnMouseReleased(e -> {
            // Calcola le coordinate di board arrotondando alla casella più vicina
            int newX = (int) Math.round(piece.getLayoutX() / TILE_SIZE);
            int newY = (int) Math.round(piece.getLayoutY() / TILE_SIZE);

            // Assicurati che le coordinate siano all'interno dei limiti della scacchiera
            newX = Math.max(0, Math.min(WIDTH - 1, newX));
            newY = Math.max(0, Math.min(HEIGHT - 1, newY));

            if ("local".equals(mode)) {
                // In modalità locale, gestiamo le mosse direttamente
                handleLocalMove(piece, newX, newY);
            } else {
                // Nelle altre modalità, inviamo la richiesta al server
                requestMove(piece, newX, newY);
            }

            // Rimuovi evidenziazioni dopo la mossa
            removeAllHighlights();
            selectedPiece = null;
        });

        return piece;
    }
    // Modifica il metodo handleLocalMove per tenere conto dell'evidenziazione
    private void handleLocalMove(Piece piece, int newX, int newY) {
        // Verifica se è il turno del giocatore corretto
        boolean isGrayTurn = isItMyTurn;
        boolean isGrayPiece = piece.getPieceType() == PieceType.GRAY || piece.getPieceType() == PieceType.GRAY_SUP;

        if (isGrayTurn != isGrayPiece) {
            // Non è il turno di questo pezzo
            piece.abortMove();
            return;
        }

        // Verifica se la cella di destinazione è evidenziata
        if (!isValidCoordinate(newX, newY) || !board[newX][newY].isHighlighted()) {
            piece.abortMove();
            return;
        }

        MoveResult result = tryMove(piece, newX, newY);
        makeMove(piece, newX, newY, result);

        // Cambio turno solo se la mossa è valida
        if (result.getMoveType() != MoveType.NONE) {
            isItMyTurn = !isItMyTurn;
            colorLabel.setText("Modalità Locale - Turno: " + (isItMyTurn ? "GRAY" : "WHITE"));
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


    // Modifica il metodo requestMove per tenere conto dell'evidenziazione
    public void requestMove(Piece piece, int newX, int newY) {
        if (!isItMyTurn) {
            makeMove(piece, newX, newY, new MoveResult(MoveType.NONE));
            return;
        }

        // Verifica se la cella di destinazione è evidenziata
        if (!isValidCoordinate(newX, newY) || !board[newX][newY].isHighlighted()) {
            piece.abortMove();
            return;
        }

        try {
            int oldBoardX = Coder.pixelToBoard(piece.getOldX());
            int oldBoardY = Coder.pixelToBoard(piece.getOldY());
            bufferedWriter.write(oldBoardX + " " + oldBoardY + " " + newX + " " + newY);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeMove(Piece piece, int newX, int newY, MoveResult moveResult) {
        MoveType moveType = moveResult.getMoveType();
        switch (moveType) {
            case NONE -> piece.abortMove();
            case NORMAL -> {
                board[Coder.pixelToBoard(piece.getOldX())][Coder.pixelToBoard(piece.getOldY())].setPiece(null);
                piece.move(newX, newY);
                board[newX][newY].setPiece(piece);
                if (!mode.equals("local")) {
                    isItMyTurn = false;
                }
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

                if (!mode.equals("local")) {
                    isItMyTurn = false;
                }
                if ((newY == 7 && piece.getPieceType() == PieceType.GRAY) || (newY == 0 && piece.getPieceType() == PieceType.WHITE)) {
                    Platform.runLater(piece::promote);
                }

                // Verifica se la partita è finita
                checkForWinner();
            }
        }
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
            Platform.runLater(() -> timer.set("WHITE ha vinto!"));
        } else if (!whiteExists) {
            winner = 1;
            Platform.runLater(() -> timer.set("GRAY ha vinto!"));
        }
    }

    public void countTime() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            if (winner != 0) {
                if ("local".equals(mode)) {
                    Platform.runLater(() -> timer.set((winner == 1) ? "GRAY ha vinto!" : "WHITE ha vinto!"));
                } else {
                    Platform.runLater(() -> timer.set((winner == player) ? "Hai vinto!" : "Hai perso!"));
                }
                executor.shutdown();
            }

            if (isItMyTurn) {
                time += 0.1;
                if ("local".equals(mode)) {
                    String currentPlayer = isItMyTurn ? "GRAY" : "WHITE";
                    Platform.runLater(() -> timer.set("Tempo " + currentPlayer + ": " + (int)time + "s."));
                } else {
                    Platform.runLater(() -> timer.set("Timer: " + (int)time + "s."));
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void listenToServer() {
        new Thread(() -> {
            String message;

            while (socket != null && socket.isConnected() && winner == 0) {
                try {
                    message = bufferedReader.readLine();
                    System.out.println(message);
                    if (message.startsWith("PING")) {
                        isItMyTurn = true;
                    } else {
                        String[] partsOfMessage = message.split(" ");
                        int fromX = Integer.parseInt(partsOfMessage[0]);
                        int fromY = Integer.parseInt(partsOfMessage[1]);
                        int newX = Integer.parseInt(partsOfMessage[2]);
                        int newY = Integer.parseInt(partsOfMessage[3]);

                        Piece piece = board[fromX][fromY].getPiece();

                        switch(partsOfMessage[4]) {
                            case "NONE" -> makeMove(piece, newX, newY, new MoveResult(MoveType.NONE));
                            case "NORMAL" -> makeMove(piece, newX, newY, new MoveResult(MoveType.NORMAL));
                            case "KILL" -> {
                                int killX = Integer.parseInt(partsOfMessage[5]);
                                int killY = Integer.parseInt(partsOfMessage[6]);
                                Piece killedPiece = board[killX][killY].getPiece();

                                makeMove(piece, newX, newY, new MoveResult(MoveType.KILL, killedPiece));
                            }
                            case "END1" -> winner = 1;
                            case "END2" -> winner = 2;
                        }
                    }
                } catch (IOException e) {
                    closeEverything();
                }
            }
            closeEverything();
        }).start();
    }

    /**
     * Evidenzia tutte le celle in cui è possibile muoversi con la pedina selezionata.
     */
    private void highlightPossibleMoves(Piece piece) {
        if (piece == null) return;

        int x = Coder.pixelToBoard(piece.getOldX());
        int y = Coder.pixelToBoard(piece.getOldY());

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
        if (isValidCoordinate(x, y) && !board[x][y].hasPiece()) {
            board[x][y].highlight();
        }
    }
    /**
     * Verifica se una cella è valida per una mossa di cattura e la evidenzia.
     */
    private void checkAndHighlightCaptureTile(Piece piece, int destX, int destY, int middleX, int middleY) {
        if (isValidCoordinate(destX, destY) && isValidCoordinate(middleX, middleY) &&
                !board[destX][destY].hasPiece() && board[middleX][middleY].hasPiece()) {

            PieceType pieceType = piece.getPieceType();
            PieceType middlePieceType = board[middleX][middleY].getPiece().getPieceType();

            // Controlla se la pedina di mezzo è di colore opposto
            boolean isOpponent = false;
            if ((pieceType == PieceType.GRAY || pieceType == PieceType.GRAY_SUP) &&
                    (middlePieceType == PieceType.WHITE || middlePieceType == PieceType.WHITE_SUP)) {
                isOpponent = true;
            } else if ((pieceType == PieceType.WHITE || pieceType == PieceType.WHITE_SUP) &&
                    (middlePieceType == PieceType.GRAY || middlePieceType == PieceType.GRAY_SUP)) {
                isOpponent = true;
            }

            if (isOpponent) {
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