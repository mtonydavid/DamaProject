package it.polimi.server;

import it.polimi.client.ChessBoardClient;
import it.polimi.common.Coder;
import it.polimi.model.Piece;
import it.polimi.model.PieceType;
import it.polimi.model.Tile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * AI implementation for the CPU player in the checkers game.
 * Makes intelligent moves based on game strategy
 */
public class CheckersAI {
    private final Tile[][] board;
    private final PieceType aiColor;
    private final PieceType opponentColor;
    private final PieceType aiKing;
    private final PieceType opponentKing;
    private final int moveDirection;
    private final Random random = new Random();

    /**
     * Creates a new CheckersAI with the specified color and board state.
     */
    public CheckersAI(Tile[][] board, boolean isWhite) {
        this.board = board;

        if (isWhite) {
            this.aiColor = PieceType.WHITE;
            this.aiKing = PieceType.WHITE_SUP;
            this.opponentColor = PieceType.GRAY;
            this.opponentKing = PieceType.GRAY_SUP;
            this.moveDirection = -1; // White moves up the board (decreasing y)
        } else {
            this.aiColor = PieceType.GRAY;
            this.aiKing = PieceType.GRAY_SUP;
            this.opponentColor = PieceType.WHITE;
            this.opponentKing = PieceType.WHITE_SUP;
            this.moveDirection = 1; // Gray moves down the board (increasing y)
        }
    }

    /**
     * Generates the best move based on current board state.
     */
    public String generateBestMove() {
        // Find all possible moves
        List<Move> possibleMoves = findAllPossibleMoves();

        if (possibleMoves.isEmpty()) {
            // No moves possible - game should be over, but return a random move to avoid crash
            return Coder.generateMove();
        }

        // Sort moves by priority (highest to lowest)
        possibleMoves.sort(Comparator.comparingInt(Move::getPriority).reversed());

        // Get the highest priority move or a random one among equal highest priority
        List<Move> bestMoves = new ArrayList<>();
        int highestPriority = possibleMoves.get(0).getPriority();

        for (Move move : possibleMoves) {
            if (move.getPriority() == highestPriority) {
                bestMoves.add(move);
            } else {
                break;
            }
        }

        // Select a random move from the best options
        Move selectedMove = bestMoves.get(random.nextInt(bestMoves.size()));
        return selectedMove.fromX + " " + selectedMove.fromY + " " + selectedMove.toX + " " + selectedMove.toY;
    }

    /**
     * Finds all possible moves for the AI player.
     */
    private List<Move> findAllPossibleMoves() {
        List<Move> moves = new ArrayList<>();

        // Look for capture moves first (these are mandatory in checkers)
        for (int y = 0; y < ChessBoardClient.HEIGHT; y++) {
            for (int x = 0; x < ChessBoardClient.WIDTH; x++) {
                if (hasAIPieceAt(x, y)) {
                    // Add capture moves for regular pieces
                    addCaptureMoves(moves, x, y);
                }
            }
        }

        // If there are capture moves, we must take one of them
        if (!moves.isEmpty()) {
            return moves;
        }

        // No captures available, look for regular moves
        for (int y = 0; y < ChessBoardClient.HEIGHT; y++) {
            for (int x = 0; x < ChessBoardClient.WIDTH; x++) {
                if (hasAIPieceAt(x, y)) {
                    // Add regular moves
                    addRegularMoves(moves, x, y);
                }
            }
        }

        return moves;
    }

    /**
     * Adds possible capture moves for the piece at the specified position.
     */
    private void addCaptureMoves(List<Move> moves, int x, int y) {
        Piece piece = board[x][y].getPiece();
        boolean isKing = piece.getPieceType() == aiKing;

        // Directions to check for captures
        int[][] directions;
        if (isKing) {
            // Kings can move in all diagonal directions
            directions = new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        } else {
            directions = new int[][]{{1, moveDirection}, {-1, moveDirection}};
        }

        for (int[] dir : directions) {
            int midX = x + dir[0];
            int midY = y + dir[1];
            int targetX = x + 2 * dir[0];
            int targetY = y + 2 * dir[1];

            // Check if the target square is on the board
            if (targetX >= 0 && targetX < ChessBoardClient.WIDTH &&
                    targetY >= 0 && targetY < ChessBoardClient.HEIGHT) {

                // Check if there's an opponent's piece to capture
                if (hasOpponentPieceAt(midX, midY) && !board[targetX][targetY].hasPiece() &&
                        (targetX + targetY) % 2 != 0) { // Valid dark square

                    // Calculate priority: captures are highest priority (10)
                    // Add bonus for capturing kings (+2) and for moving toward promotion (+1)
                    int priority = 10;

                    if (board[midX][midY].getPiece().getPieceType() == opponentKing) {
                        priority += 2; // Bonus for capturing kings
                    }

                    // Bonus for moving toward promotion
                    if (!isKing && ((aiColor == PieceType.WHITE && targetY == 0) ||
                            (aiColor == PieceType.GRAY && targetY == 7))) {
                        priority += 1; // About to be promoted
                    }

                    moves.add(new Move(x, y, targetX, targetY, priority));
                }
            }
        }
    }

    /**
     * Adds possible regular moves for the piece at the specified position.
     */
    private void addRegularMoves(List<Move> moves, int x, int y) {
        Piece piece = board[x][y].getPiece();
        boolean isKing = piece.getPieceType() == aiKing;

        // Directions to check for regular moves
        int[][] directions;
        if (isKing) {
            // Kings can move in all diagonal directions
            directions = new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        } else {
            // Regular pieces can only move forward (and diagonally)
            directions = new int[][]{{1, moveDirection}, {-1, moveDirection}};
        }

        for (int[] dir : directions) {
            int targetX = x + dir[0];
            int targetY = y + dir[1];

            // Check if the target square is on the board
            if (targetX >= 0 && targetX < ChessBoardClient.WIDTH &&
                    targetY >= 0 && targetY < ChessBoardClient.HEIGHT) {

                // Check if the target square is empty and valid (dark square)
                if (!board[targetX][targetY].hasPiece() && (targetX + targetY) % 2 != 0) {

                    // Calculate move priority
                    int priority = 0;

                    // Highest priority for promotion moves
                    if (!isKing && ((aiColor == PieceType.WHITE && targetY == 0) ||
                            (aiColor == PieceType.GRAY && targetY == 7))) {
                        priority = 8; // Almost as good as a capture
                    }
                    // High priority for advancing pieces toward promotion
                    else if (!isKing) {
                        if (aiColor == PieceType.WHITE) {
                            priority = 5 - targetY; // Higher priority as we get closer to the promotion row (0)
                        } else {
                            priority = targetY; // Higher priority as we get closer to the promotion row (7)
                        }
                    }
                    // Kings get moderate priority
                    else {
                        priority = 3;

                        // Small bonus for kings that move toward enemy pieces (aggressive)
                        if (hasNearbyOpponentPieces(targetX, targetY)) {
                            priority += 1;
                        }
                    }

                    // Safe moves get a bonus
                    if (!isVulnerablePosition(targetX, targetY)) {
                        priority += 2;
                    }

                    moves.add(new Move(x, y, targetX, targetY, priority));
                }
            }
        }
    }

    /**
     * Checks if there are opponent pieces near the given position.
     */
    private boolean hasNearbyOpponentPieces(int x, int y) {
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        for (int[] dir : directions) {
            int nearX = x + dir[0];
            int nearY = y + dir[1];

            if (nearX >= 0 && nearX < ChessBoardClient.WIDTH &&
                    nearY >= 0 && nearY < ChessBoardClient.HEIGHT) {
                if (hasOpponentPieceAt(nearX, nearY)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if moving a piece to the given position would make it vulnerable to capture.
     */
    private boolean isVulnerablePosition(int x, int y) {
        // Check if an opponent piece could capture our piece if we move to (x,y)
        int[][] opponentDirections;

        if (aiColor == PieceType.WHITE) {
            // Gray pieces can capture from these directions
            opponentDirections = new int[][]{{1, 1}, {-1, 1}};
        } else {
            // White pieces can capture from these directions
            opponentDirections = new int[][]{{1, -1}, {-1, -1}};
        }

        for (int[] dir : opponentDirections) {
            int opponentX = x + dir[0];
            int opponentY = y + dir[1];
            int landingX = x - dir[0];
            int landingY = y - dir[1];

            // Check if opponent piece exists and landing spot is empty
            if (opponentX >= 0 && opponentX < ChessBoardClient.WIDTH &&
                    opponentY >= 0 && opponentY < ChessBoardClient.HEIGHT &&
                    landingX >= 0 && landingX < ChessBoardClient.WIDTH &&
                    landingY >= 0 && landingY < ChessBoardClient.HEIGHT) {

                if (hasOpponentPieceAt(opponentX, opponentY) &&
                        !board[landingX][landingY].hasPiece() &&
                        (landingX + landingY) % 2 != 0) {
                    return true;
                }
            }
        }

        // Check for opponent kings that could capture from any direction
        int[][] kingDirections = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        for (int[] dir : kingDirections) {
            int opponentX = x + dir[0];
            int opponentY = y + dir[1];
            int landingX = x - dir[0];
            int landingY = y - dir[1];

            // Check if opponent king exists and landing spot is empty
            if (opponentX >= 0 && opponentX < ChessBoardClient.WIDTH &&
                    opponentY >= 0 && opponentY < ChessBoardClient.HEIGHT &&
                    landingX >= 0 && landingX < ChessBoardClient.WIDTH &&
                    landingY >= 0 && landingY < ChessBoardClient.HEIGHT) {

                if (board[opponentX][opponentY].hasPiece() &&
                        board[opponentX][opponentY].getPiece().getPieceType() == opponentKing &&
                        !board[landingX][landingY].hasPiece() &&
                        (landingX + landingY) % 2 != 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if there is an AI piece at the given position.
     */
    private boolean hasAIPieceAt(int x, int y) {
        return board[x][y].hasPiece() &&
                (board[x][y].getPiece().getPieceType() == aiColor ||
                        board[x][y].getPiece().getPieceType() == aiKing);
    }

    /**
     * Checks if there is an opponent's piece at the given position.
     */
    private boolean hasOpponentPieceAt(int x, int y) {
        return board[x][y].hasPiece() &&
                (board[x][y].getPiece().getPieceType() == opponentColor ||
                        board[x][y].getPiece().getPieceType() == opponentKing);
    }

    /**
     * Represents a possible move with its priority.
     */
    private static class Move {
        private final int fromX;
        private final int fromY;
        private final int toX;
        private final int toY;
        private final int priority;

        public Move(int fromX, int fromY, int toX, int toY, int priority) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }
}