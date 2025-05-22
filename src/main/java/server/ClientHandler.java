package server;

import client.ChessBoardClient;
import common.Coder;
import model.*;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private final Tile[][] board = new Tile[ChessBoardClient.WIDTH][ChessBoardClient.HEIGHT];

    private final Socket socket1;
    private final BufferedWriter bufferedWriter1;
    private final BufferedReader bufferedReader1;
    private final Socket socket2;
    private final BufferedWriter bufferedWriter2;
    private final BufferedReader bufferedReader2;

    private int grayPieces = 0;
    private int whitePieces = 0;

    // Add AI for CPU mode
    private CheckersAI ai;
    private boolean isCpuMode = false;

    // Aggiungiamo un parametro per il ritardo della CPU (in millisecondi)
    private final long cpuMoveDelay = 1000; // 1 secondo di ritardo

    // NEW: Variables for advanced rules
    private boolean mustCapture = false;
    private boolean isInMultiJump = false;
    private int currentPlayer = -1; // -1 = GRAY turn, 1 = WHITE turn
    private Piece multiJumpPiece = null;
    private int movesWithoutCapture = 0;
    private final int MAX_MOVES_WITHOUT_CAPTURE = 40;

    public ClientHandler(Socket socket1, Socket socket2) throws IOException {
        try {
            this.socket1 = socket1;
            bufferedWriter1 = new BufferedWriter(new OutputStreamWriter(socket1.getOutputStream()));
            bufferedReader1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
            bufferedWriter1.write("1");
            bufferedWriter1.newLine();
            bufferedWriter1.flush();

            this.socket2 = socket2;
            if (socket2 != null) {
                bufferedWriter2 = new BufferedWriter(new OutputStreamWriter(socket2.getOutputStream()));
                bufferedReader2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
                bufferedWriter2.write("2");
                bufferedWriter2.newLine();
                bufferedWriter2.flush();
            } else {
                bufferedWriter2 = null;
                bufferedReader2 = null;
                // This is CPU mode if socket2 is null
                isCpuMode = true;
            }
        } catch (IOException e) {
            closeEverything();
            throw e;
        }
    }

    @Override
    public void run() {
        createContent();

        // Initialize AI if in CPU mode
        if (isCpuMode) {
            // CPU is always player 2 (WHITE)
            ai = new CheckersAI(board, true);
        }

        int i = 1;
        while (socket1.isConnected() && (socket2 == null || socket2.isConnected()) && whitePieces * grayPieces > 0 && movesWithoutCapture < MAX_MOVES_WITHOUT_CAPTURE) {
            try {
                currentPlayer = i % 2 * 2 - 1; // -1 for GRAY, 1 for WHITE
                updateMustCapture();

                if (processMove(currentPlayer)) {
                    if (!isInMultiJump) {
                        i++; // Solo se non siamo in multi-jump
                    }
                }
            } catch (IOException e) {
                closeEverything();
                e.printStackTrace();
            }
        }

        // Check for draw condition
        if (movesWithoutCapture >= MAX_MOVES_WITHOUT_CAPTURE) {
            try {
                String drawMessage = "1 2 3 4 DRAW";
                if (bufferedWriter1 != null) {
                    bufferedWriter1.write(drawMessage);
                    bufferedWriter1.newLine();
                    bufferedWriter1.flush();
                }
                if (bufferedWriter2 != null) {
                    bufferedWriter2.write(drawMessage);
                    bufferedWriter2.newLine();
                    bufferedWriter2.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createContent() {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                Tile tile = new Tile((x + y) % 2 == 0, x, y);
                board[x][y] = tile;

                Piece piece = null;
                if (y <= 2 && (x + y) % 2 != 0) {
                    piece = new Piece(PieceType.GRAY, x, y);
                    grayPieces++;
                } else if (y >= 5 && (x + y) % 2 != 0) {
                    piece = new Piece(PieceType.WHITE, x, y);
                    whitePieces++;
                }

                if (piece != null) {
                    tile.setPiece(piece);
                }
            }
        }
    }

    public static void ping(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write("PING");
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    private MoveResult tryMove(Piece piece, int newX, int newY) {
        if (board[newX][newY].hasPiece() || (newX + newY) % 2 == 0) {
            return new MoveResult(MoveType.NONE);
        }

        int oldX = Coder.pixelToBoard(piece.getOldX());
        int oldY = Coder.pixelToBoard(piece.getOldY());

        if (Math.abs(newX - oldX) == 1 && newY - oldY == piece.getPieceType().moveDir ) {
            return new MoveResult(MoveType.NORMAL);
        } else if (Math.abs(newX - oldX) == 2 && Math.abs(newY - oldY) == 2) {
            int middleX = oldX + (newX - oldX) / 2;
            int middleY = oldY + (newY - oldY) / 2;

            if (board[middleX][middleY].hasPiece() && board[middleX][middleY].getPiece().getPieceType() != piece.getPieceType()) {
                return new MoveResult(MoveType.KILL, board[middleX][middleY].getPiece());
            }
        } else if ((piece.getPieceType() == PieceType.GRAY_SUP || piece.getPieceType() == PieceType.WHITE_SUP ) && (Math.abs(newX - oldX) == 1 && Math.abs(newY - oldY) == 1)) {
            return new MoveResult(MoveType.NORMAL);
        }

        return new MoveResult(MoveType.NONE);
    }

    /**
     * Trova tutte le catture possibili per il giocatore corrente
     */
    private List<MoveResult> findAllPossibleCaptures(int moveDir) {
        List<MoveResult> allCaptures = new ArrayList<>();

        for (int y = 0; y < ChessBoardClient.HEIGHT; y++) {
            for (int x = 0; x < ChessBoardClient.WIDTH; x++) {
                if (board[x][y].hasPiece()) {
                    Piece piece = board[x][y].getPiece();
                    if (Math.signum(piece.getPieceType().moveDir) == Math.signum(moveDir)) {
                        allCaptures.addAll(findPossibleCaptures(piece));
                    }
                }
            }
        }

        return allCaptures;
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

            if (destX >= 0 && destX < ChessBoardClient.WIDTH && destY >= 0 && destY < ChessBoardClient.HEIGHT &&
                    middleX >= 0 && middleX < ChessBoardClient.WIDTH && middleY >= 0 && middleY < ChessBoardClient.HEIGHT &&
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
     * Verifica se una pedina è avversaria
     */
    private boolean isOpponentPiece(Piece piece, Piece otherPiece) {
        return Math.signum(piece.getPieceType().moveDir) != Math.signum(otherPiece.getPieceType().moveDir);
    }

    /**
     * Aggiorna la variabile mustCapture
     */
    private void updateMustCapture() {
        if (isInMultiJump && multiJumpPiece != null) {
            // Durante multi-jump, controlla solo la pedina che sta saltando
            List<MoveResult> captures = findPossibleCaptures(multiJumpPiece);
            mustCapture = !captures.isEmpty();
        } else {
            // Normale controllo per tutte le pedine del giocatore corrente
            List<MoveResult> possibleCaptures = findAllPossibleCaptures(currentPlayer);
            mustCapture = !possibleCaptures.isEmpty();
        }
    }

    public boolean processMove(int moveDir) throws IOException {
        try {
            BufferedReader fromBufferedReader;
            BufferedWriter fromBufferedWriter;
            BufferedWriter toBufferedWriter;

            if (moveDir == -1) {
                fromBufferedReader = bufferedReader1;
                fromBufferedWriter = bufferedWriter1;
                toBufferedWriter = bufferedWriter2;
            } else {
                fromBufferedReader = bufferedReader2;
                fromBufferedWriter = bufferedWriter2;
                toBufferedWriter = bufferedWriter1;
            }

            if (fromBufferedWriter != null) {
                ping(fromBufferedWriter);
            }

            String messageFrom;
            if (fromBufferedReader != null) {
                messageFrom = fromBufferedReader.readLine();
            } else {
                // Se siamo in modalità CPU, aggiungiamo un ritardo prima di eseguire la mossa
                try {
                    System.out.println("CPU sta pensando...");
                    Thread.sleep(cpuMoveDelay);
                    System.out.println("CPU ha completato il pensiero e sta eseguendo la mossa");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("La pausa della CPU è stata interrotta");
                }

                // Use AI to generate a move instead of random moves
                messageFrom = ai.generateBestMove();
            }
            System.out.println(messageFrom);

            // Verifica se è un messaggio di chat
            if (messageFrom.startsWith("CHAT ")) {
                try {
                    // Inoltra il messaggio di chat all'altro giocatore
                    forwardChatMessage(messageFrom, fromBufferedWriter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false; // Non è una mossa, quindi ritorna false
            }

            int fromX = Integer.parseInt(messageFrom.split(" ")[0]);
            int fromY = Integer.parseInt(messageFrom.split(" ")[1]);
            int newX = (int) Float.parseFloat(messageFrom.split(" ")[2]);
            int newY = (int) Float.parseFloat(messageFrom.split(" ")[3]);

            if (board[fromX][fromY].getPiece() == null) {
                return false;
            }

            Piece piece = board[fromX][fromY].getPiece();

            // Verifica se è il turno corretto
            if (Math.signum(piece.getPieceType().moveDir) != Math.signum(moveDir)) {
                if (fromBufferedReader != null) {
                    fromBufferedWriter.write(Coder.encode(piece, newX, newY, new MoveResult(MoveType.NONE)));
                    fromBufferedWriter.newLine();
                    fromBufferedWriter.flush();
                }
                return false;
            }

            // Verifica se è in multi-jump e la pedina è quella corretta
            if (isInMultiJump && piece != multiJumpPiece) {
                if (fromBufferedReader != null) {
                    fromBufferedWriter.write(Coder.encode(piece, newX, newY, new MoveResult(MoveType.NONE)));
                    fromBufferedWriter.newLine();
                    fromBufferedWriter.flush();
                }
                return false;
            }

            MoveResult moveResult = tryMove(piece, newX, newY);

            // Verifica mangiata obbligatoria
            if (mustCapture && moveResult.getMoveType() != MoveType.KILL) {
                if (fromBufferedReader != null) {
                    fromBufferedWriter.write(Coder.encode(piece, newX, newY, new MoveResult(MoveType.NONE)));
                    fromBufferedWriter.newLine();
                    fromBufferedWriter.flush();
                }
                return false;
            }

            String toMessage = Coder.encode(piece, newX, newY, moveResult);
            makeMove(piece, newX, newY, moveResult);

            // Gestisci multi-jump
            if (moveResult.getMoveType() == MoveType.KILL) {
                movesWithoutCapture = 0; // Reset contatore

                // Controlla se ci sono altre catture possibili
                List<MoveResult> additionalCaptures = findPossibleCaptures(piece);
                if (!additionalCaptures.isEmpty()) {
                    isInMultiJump = true;
                    multiJumpPiece = piece;
                    // Non inviare il messaggio ancora, continua il multi-jump
                    return false; // Non cambiare turno
                } else {
                    isInMultiJump = false;
                    multiJumpPiece = null;
                }
            } else {
                movesWithoutCapture++;
                isInMultiJump = false;
                multiJumpPiece = null;
            }

            // Invia il messaggio ai client
            if (toBufferedWriter != null) {
                toBufferedWriter.write(toMessage);
                toBufferedWriter.newLine();
                toBufferedWriter.flush();
            }

            if (fromBufferedReader != null) {
                fromBufferedWriter.write(toMessage);
                fromBufferedWriter.newLine();
                fromBufferedWriter.flush();
            }

            // Controlla condizioni di fine partita
            if (whitePieces == 0 || grayPieces == 0 || movesWithoutCapture >= MAX_MOVES_WITHOUT_CAPTURE) {
                String endOfGameMessage;
                if (movesWithoutCapture >= MAX_MOVES_WITHOUT_CAPTURE) {
                    endOfGameMessage = "1 2 3 4 DRAW";
                } else if (whitePieces == 0) {
                    endOfGameMessage = "1 2 3 4 END1";
                } else {
                    endOfGameMessage = "1 2 3 4 END2";
                }

                if (toBufferedWriter != null) {
                    toBufferedWriter.write(endOfGameMessage);
                    toBufferedWriter.newLine();
                    toBufferedWriter.flush();
                }

                if (fromBufferedReader != null) {
                    fromBufferedWriter.write(endOfGameMessage);
                    fromBufferedWriter.newLine();
                    fromBufferedWriter.flush();
                }
            }

            return moveResult.getMoveType() != MoveType.NONE;
        } catch (IOException e) {
            closeEverything();
            e.printStackTrace();
            throw e;
        }
    }

    public void makeMove(Piece piece, int newX, int newY, MoveResult moveResult) {
        MoveType moveType = moveResult.getMoveType();
        switch (moveType) {
            case NONE -> piece.abortMove();
            case NORMAL -> {
                board[Coder.pixelToBoard(piece.getOldX())][Coder.pixelToBoard(piece.getOldY())].setPiece(null);
                piece.move(newX, newY);
                board[newX][newY].setPiece(piece);
                if ((newY == 7 && piece.getPieceType() == PieceType.GRAY) || (newY == 0 && piece.getPieceType() == PieceType.WHITE)) {
                    piece.promote();
                }
            }
            case KILL -> {
                board[Coder.pixelToBoard(piece.getOldX())][Coder.pixelToBoard(piece.getOldY())].setPiece(null);
                piece.move(newX, newY);
                board[newX][newY].setPiece(piece);
                if (piece.getPieceType() == PieceType.GRAY || piece.getPieceType() == PieceType.GRAY_SUP) {
                    whitePieces--;
                } else {
                    grayPieces--;
                }

                Piece otherPiece = moveResult.getPiece();
                board[Coder.pixelToBoard(otherPiece.getOldX())][Coder.pixelToBoard(otherPiece.getOldY())].setPiece(null);
                if ((newY == 7 && piece.getPieceType() == PieceType.GRAY) || (newY == 0 && piece.getPieceType() == PieceType.WHITE)) {
                    piece.promote();
                }
            }
        }
    }

    private void forwardChatMessage(String message, BufferedWriter fromWriter) throws IOException {
        // Determina quale bufferedWriter è quello da cui arriva il messaggio
        // e invia il messaggio all'altro client

        if (fromWriter == bufferedWriter1) {
            // Invia il messaggio al client 2
            if (bufferedWriter2 != null) {
                bufferedWriter2.write(message);
                bufferedWriter2.newLine();
                bufferedWriter2.flush();
            }
        } else {
            // Invia il messaggio al client 1
            bufferedWriter1.write(message);
            bufferedWriter1.newLine();
            bufferedWriter1.flush();
        }
    }

    private void closeEverything() {
        try {
            if (bufferedReader1 != null) {
                bufferedReader1.close();
            }
            if (bufferedWriter1 != null) {
                bufferedWriter1.close();
            }
            if (socket1 != null) {
                socket1.close();
            }
            if (bufferedReader2 != null) {
                bufferedReader2.close();
            }
            if (bufferedWriter2 != null) {
                bufferedWriter2.close();
            }
            if (socket2 != null) {
                socket2.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}