package it.polimi.common;

import it.polimi.model.MoveResult;
import it.polimi.model.MoveType;
import it.polimi.model.Piece;

import java.util.Random;

/**
 * Utility class per encoding/decoding delle mosse e conversioni coordinate.
 * Versione refactored che usa GameConfig invece di dipendenze circolari.
 */
public final class Coder {

    private static final Random random = new Random();

    /**
     * Costruttore privato per impedire istanziazione.
     */
    private Coder() {
        throw new UnsupportedOperationException("Coder is a utility class and cannot be instantiated");
    }

    /**
     * Converte coordinate pixel in coordinate scacchiera.
     */
    public static int pixelToBoard(double pixelCoordinate) {
        return GameConfig.pixelToBoardCoordinate(pixelCoordinate);
    }

    /**
     * Converte coordinate scacchiera in coordinate pixel.
     */
    public static int boardToPixel(int boardCoordinate) {
        return GameConfig.boardToPixelCoordinate(boardCoordinate);
    }

    /**
     * Codifica una mossa in formato stringa per invio al server.
     */
    public static String encode(Piece piece, int newX, int newY, MoveResult moveResult) {
        if (piece == null) {
            throw new IllegalArgumentException("Piece cannot be null");
        }
        if (moveResult == null) {
            throw new IllegalArgumentException("MoveResult cannot be null");
        }
        if (!GameConfig.isValidCoordinate(newX, newY)) {
            throw new IllegalArgumentException("Invalid coordinates: " + newX + ", " + newY);
        }

        // Coordinate di partenza della pedina
        int oldX = pixelToBoard(piece.getOldX());
        int oldY = pixelToBoard(piece.getOldY());

        // Base della mossa: "oldX oldY newX newY moveType"
        StringBuilder encoded = new StringBuilder();
        encoded.append(oldX).append(" ")
                .append(oldY).append(" ")
                .append(newX).append(" ")
                .append(newY).append(" ")
                .append(moveResult.getMoveType());

        // Se è una cattura, aggiungi coordinate della pedina catturata
        if (moveResult.getMoveType() == MoveType.KILL && moveResult.getPiece() != null) {
            Piece capturedPiece = moveResult.getPiece();
            int capturedX = pixelToBoard(capturedPiece.getOldX());
            int capturedY = pixelToBoard(capturedPiece.getOldY());
            encoded.append(" ").append(capturedX).append(" ").append(capturedY);
        }

        return encoded.toString();
    }

    /**
     * Genera una mossa casuale valida per fallback dell'AI.
     */
    public static String generateMove() {
        // Genera coordinate casuali valide
        int fromX = random.nextInt(GameConfig.BOARD_WIDTH);
        int fromY = random.nextInt(GameConfig.BOARD_HEIGHT);
        int toX = random.nextInt(GameConfig.BOARD_WIDTH);
        int toY = random.nextInt(GameConfig.BOARD_HEIGHT);

        return fromX + " " + fromY + " " + toX + " " + toY;
    }

    /**
     * Valida il formato di una stringa mossa.
     */
    public static boolean isValidMoveFormat(String moveString) {
        if (moveString == null || moveString.trim().isEmpty()) {
            return false;
        }

        String[] parts = moveString.trim().split("\\s+");
        if (parts.length < 4) {
            return false;
        }

        try {
            for (int i = 0; i < 4; i++) {
                int coord = Integer.parseInt(parts[i]);
                if (coord < 0 || coord >= GameConfig.BOARD_WIDTH) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Decodifica una stringa mossa in componenti separate.
     */
    public static int[] decodeMoveCoordinates(String moveString) {
        if (!isValidMoveFormat(moveString)) {
            return null;
        }

        String[] parts = moveString.trim().split("\\s+");
        return new int[] {
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3])
        };
    }

    /**
     * Calcola la distanza Manhattan tra due punti.
     */
    public static int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }

    /**
     * Verifica se una mossa è di tipo cattura (distanza 2 in diagonale).
     */
    public static boolean isCaptureMove(int fromX, int fromY, int toX, int toY) {
        int deltaX = Math.abs(toX - fromX);
        int deltaY = Math.abs(toY - fromY);
        return deltaX == 2 && deltaY == 2;
    }

    /**
     * Verifica se una mossa è normale (distanza 1 in diagonale).
     */
    public static boolean isNormalMove(int fromX, int fromY, int toX, int toY) {
        int deltaX = Math.abs(toX - fromX);
        int deltaY = Math.abs(toY - fromY);
        return deltaX == 1 && deltaY == 1;
    }
}