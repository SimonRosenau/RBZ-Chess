package de.rosenau.simon.rbzchess.boards;

import de.rosenau.simon.rbzchess.ChessMove;
import de.rosenau.simon.rbzchess.ChessUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Project created by Simon Rosenau.
 */

public class Standard implements ChessBoard {

    private char[] board = new char[64];

    private char moveRight;

    private boolean whiteKingsideCastleable;
    private boolean whiteQueensideCastleable;
    private boolean blackKingideCastleable;
    private boolean blackQueensideCastleable;

    private byte entPassent;

    private int moveSinceCaptureOrPawn;
    private int move;

    private Standard() {
    }

    /**
     * Constructs the ChessBoard by a given FEN
     *
     * @param fen Forsyth-Edwards-Notation of Chessboard
     *            Reference: https://de.wikipedia.org/wiki/Forsyth-Edwards-Notation
     *            Default: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
     */

    public Standard(String fen) {
        String[] parts = fen.split(" ");
        // Board Setup
        {
            String boardNotation = parts[0];
            String[] rows = boardNotation.split("/");
            for (int i = 0; i < rows.length; i++) {
                String row = rows[i];
                int index = 0;
                for (char c : row.toCharArray()) {
                    if (c <= 56) {
                        index += c - '0';
                        continue;
                    }
                    board[i * 8 + index] = c;
                    index++;
                }
            }
        }

        moveRight = parts[1].toCharArray()[0];
        whiteKingsideCastleable = parts[2].contains("K");
        whiteQueensideCastleable = parts[2].contains("Q");
        blackKingideCastleable = parts[2].contains("k");
        blackQueensideCastleable = parts[2].contains("q");
        entPassent = parts[3].equals("-") ? -1 : ChessUtils.squareNameToIndex(parts[3]);
        moveSinceCaptureOrPawn = Short.valueOf(parts[4]);
        move = Short.valueOf(parts[5]);
    }

    /**
     * Calculates every possible move to make on constructed fen
     *
     * @return Set of ChessMove's
     */

    @Override
    public Set<ChessMove> getPossibleMoves() {
        boolean white = moveRight == 'w';
        Set<ChessMove> set = getPossibleMovesRaw(white, true);
        Set<ChessMove> finalSet = new HashSet<>();

        // Check check
        for (ChessMove move : set) {
            Standard standard = performMove(move);
            byte kingSquare = -1;
            for (int i = 0; i < standard.board.length; i++) {
                if (standard.board[i] != 'K' && standard.board[i] != 'k') continue;
                if (white == (standard.board[i] == 'K')) {
                    kingSquare = (byte) i;
                    break;
                }
            }
            boolean valid = true;
            loop:
            for (ChessMove nextMove : standard.getPossibleMovesRaw(!white, false)) {
                for (int i = 0; i < nextMove.getMoves().length; i++) {
                    byte b = nextMove.getMoves()[i];
                    if (b == kingSquare) {
                        valid = false;
                        break loop;
                    }
                }
            }
            if (valid) finalSet.add(move);
        }

        return finalSet;
    }

    private Set<ChessMove> getPossibleMovesRaw(boolean white, boolean includeCastle) {
        Set<ChessMove> set = new HashSet<>();
        // Pawn moves
        {
            for (byte i = 0; i < board.length; i++) {
                char c = board[i];
                if (c != 'P' && c != 'p') continue;
                if (white != (c == 'P')) continue;
                // reverse movement factor for white pawns
                int factor = c == 'p' ? 1 : -1;

                // Forward movement by 1
                if (board[i + 8 * factor] == 0) {
                    if ((factor < 0 && i < 16) || (factor > 0 && i >= 48)) {
                        // Promotion

                        int to = i + 8 * factor;
                        String name = ChessUtils.squareIndexToName(i) + ChessUtils.squareIndexToName(to);

                        for (char p : (factor < 0 ? new char[]{'N', 'B', 'R', 'Q'} : new char[]{'n', 'b', 'r', 'q'})) {
                            set.add(ChessMove.builder().name(name + (char) (p > 96 ? p : p + 32)).move(i, to).conversion(to, p).build());
                        }
                    } else {
                        // Default
                        set.add(ChessMove.builder()
                                .name(ChessUtils.squareIndexToName(i) + ChessUtils.squareIndexToName(i + 8 * factor))
                                .move(i, i + 8 * factor)
                                .build());
                    }
                }

                // Forward movement by 2
                if (
                    // Check on second row
                        ((factor > 0 && (i >= 8 && i < 16)) || (factor < 0 && (i >= 48 && i < 56)))
                                // Check squares empty
                                && board[i + 8 * factor] == 0 && board[i + 16 * factor] == 0
                        ) {
                    set.add(ChessMove.builder()
                            .name(ChessUtils.squareIndexToName(i) + ChessUtils.squareIndexToName(i + 16 * factor))
                            .move(i, i + 16 * factor)
                            .build());
                }

                // Capturing pieces
                if (factor < 0) {
                    // White
                    for (int o : new int[]{-7, -9}) {
                        // Check border
                        if (o == -7 && i + 1 % 8 == 0) continue;
                        if (o == -9 && i % 8 == 0) continue;

                        if (board[i + o] != 0 && board[i + o] > 96) {
                            if (i < 16) {
                                // Promotion
                                int to = i + o;
                                String name = ChessUtils.squareIndexToName(i) + ChessUtils.squareIndexToName(to);

                                for (char p : (factor < 0 ? new char[]{'N', 'B', 'R', 'Q'} : new char[]{'n', 'b', 'r', 'q'})) {
                                    set.add(ChessMove.builder().name(name + (char) (p > 96 ? p : p + 32)).move(i, to).conversion(to, p).build());
                                }
                            } else {
                                // Default
                                set.add(ChessMove.builder()
                                        .name(ChessUtils.squareIndexToName(i) + ChessUtils.squareIndexToName(i + o))
                                        .move(i, i + o)
                                        .build());
                            }
                        }
                    }
                } else {
                    // Black
                    for (int o : new int[]{7, 9}) {
                        // Check border
                        if (o == 7 && i % 8 == 0) continue;
                        if (o == 9 && i + 1 % 8 == 0) continue;

                        if (board[i + o] != 0 && board[i + o] < 96) {
                            if (i >= 48) {
                                // Promotion
                                int to = i + o;
                                String name = ChessUtils.squareIndexToName(i) + ChessUtils.squareIndexToName(to);

                                for (char p : (factor < 0 ? new char[]{'N', 'B', 'R', 'Q'} : new char[]{'n', 'b', 'r', 'q'})) {
                                    set.add(ChessMove.builder().name(name + (char) (p > 96 ? p : p + 32)).move(i, to).conversion(to, p).build());
                                }
                            } else {
                                // Default
                                set.add(ChessMove.builder()
                                        .name(ChessUtils.squareIndexToName(i) + ChessUtils.squareIndexToName(i + o))
                                        .move(i, i + o)
                                        .build());
                            }
                        }
                    }
                }
            }
        }
        // En passent
        if (entPassent != -1) {
            if (white) {
                // Square iteration
                for (int i : new int[]{7, 9}) {
                    // Check border
                    if (i == 7 && entPassent % 8 == 0) continue;
                    if (i == 9 && entPassent + 1 % 8 == 0) continue;
                    // Check if pawn is nearby
                    if (board[entPassent + i] == 'P') {
                        set.add(ChessMove.builder()
                                .name(ChessUtils.squareIndexToName(entPassent + i) + ChessUtils.squareIndexToName(entPassent))
                                .move(entPassent + i, entPassent)
                                .conversion(entPassent + 8, (char) 0)
                                .build());
                    }
                }
            } else {
                // Square iteration
                for (int i : new int[]{-7, -9}) {
                    // Check border
                    if (i == -9 && entPassent % 8 == 0) continue;
                    if (i == -7 && entPassent + 1 % 8 == 0) continue;
                    // Check if pawn is nearby
                    if (board[entPassent + i] == 'p') {
                        set.add(ChessMove.builder()
                                .name(ChessUtils.squareIndexToName(entPassent + i) + ChessUtils.squareIndexToName(entPassent))
                                .move(entPassent + i, entPassent)
                                .conversion(entPassent - 8, (char) 0)
                                .build());
                    }
                }
            }
        }
        // Knight moves
        {

            // Hard-coded becuase of performance improvements
            final byte[][] squaresToMove = new byte[][]{
                    new byte[]{10, 17},
                    new byte[]{11, 16, 18},
                    new byte[]{8, 12, 17, 19},
                    new byte[]{9, 13, 18, 20},
                    new byte[]{10, 14, 19, 21},
                    new byte[]{11, 15, 20, 22},
                    new byte[]{12, 21, 23},
                    new byte[]{13, 22},

                    new byte[]{2, 18, 25},
                    new byte[]{3, 19, 24, 26},
                    new byte[]{0, 4, 16, 20, 25, 27},
                    new byte[]{1, 5, 17, 21, 26, 28},
                    new byte[]{2, 6, 18, 22, 27, 29},
                    new byte[]{3, 7, 19, 23, 28, 30},
                    new byte[]{4, 20, 29, 31},
                    new byte[]{5, 21, 30},

                    new byte[]{1, 10, 26, 33},
                    new byte[]{0, 2, 11, 27, 32, 34},
                    new byte[]{1, 3, 8, 12, 24, 28, 33, 35},
                    new byte[]{2, 4, 9, 13, 25, 29, 34, 36},
                    new byte[]{3, 5, 10, 14, 26, 30, 35, 37},
                    new byte[]{4, 6, 11, 15, 27, 31, 36, 38},
                    new byte[]{5, 7, 12, 28, 37, 39},
                    new byte[]{6, 13, 29, 38},

                    new byte[]{9, 18, 34, 41},
                    new byte[]{8, 10, 19, 35, 40, 42},
                    new byte[]{9, 11, 16, 20, 32, 36, 41, 43},
                    new byte[]{10, 12, 17, 21, 33, 37, 42, 44},
                    new byte[]{11, 13, 18, 22, 34, 38, 43, 45},
                    new byte[]{12, 14, 19, 23, 35, 39, 44, 46},
                    new byte[]{13, 15, 20, 36, 45, 47},
                    new byte[]{14, 21, 37, 46},

                    new byte[]{17, 26, 42, 49},
                    new byte[]{16, 18, 27, 43, 48, 50},
                    new byte[]{17, 19, 24, 28, 40, 44, 49, 51},
                    new byte[]{18, 20, 25, 29, 41, 45, 50, 52},
                    new byte[]{19, 21, 26, 30, 42, 46, 51, 53},
                    new byte[]{20, 22, 27, 31, 43, 47, 52, 54},
                    new byte[]{21, 23, 28, 44, 53, 55},
                    new byte[]{22, 29, 45, 54},

                    new byte[]{25, 34, 50, 57},
                    new byte[]{24, 26, 35, 51, 56, 58},
                    new byte[]{25, 27, 32, 36, 48, 52, 57, 59},
                    new byte[]{26, 28, 33, 37, 49, 53, 58, 60},
                    new byte[]{27, 29, 34, 38, 50, 54, 59, 61},
                    new byte[]{28, 30, 35, 39, 51, 55, 60, 62},
                    new byte[]{29, 31, 36, 52, 61, 63},
                    new byte[]{30, 37, 53, 62},

                    new byte[]{33, 42, 58},
                    new byte[]{32, 34, 43, 59},
                    new byte[]{33, 35, 40, 44, 56, 60},
                    new byte[]{34, 36, 41, 45, 57, 61},
                    new byte[]{35, 37, 42, 46, 58, 62},
                    new byte[]{36, 38, 43, 47, 59, 63},
                    new byte[]{37, 39, 44, 60},
                    new byte[]{38, 45, 61},

                    new byte[]{41, 50},
                    new byte[]{40, 42, 51},
                    new byte[]{41, 43, 48, 52},
                    new byte[]{42, 44, 49, 53},
                    new byte[]{43, 45, 50, 54},
                    new byte[]{44, 46, 51, 55},
                    new byte[]{45, 47, 52},
                    new byte[]{46, 53}
            };

            // Iterate over squares
            for (byte i = 0; i < board.length; i++) {
                char c = board[i];
                // Check if night is on square
                if (c != 'N' && c != 'n') continue;
                // Check if night is of valid color
                if (white != (c == 'N')) continue;

                // Iterate over possible squares
                for (byte move : squaresToMove[i]) {
                    // Check if square is empty or of a captureable color
                    if (board[move] == 0 || board[move] > 96 == (c == 'N')) {
                        set.add(ChessMove.builder()
                                .name(ChessUtils.squareIndexToName(i) + ChessUtils.squareIndexToName(move))
                                .move(i, move)
                                .build());
                    }
                }
            }
        }
        // Rook & straight Queen moves
        {
            // Iterate over all squares
            for (byte i = 0; i < board.length; i++) {
                char c = board[i];
                // Check if piece is of type rook or queen
                if (c != 'R' && c != 'r' && c != 'Q' && c != 'q') continue;
                // Check if piece is of valid color
                if (white != (c == 'R' || c == 'Q')) continue;

                // Check squares down
                for (int o = 1; o < 8 - (i / 8); o++) {
                    int to = i + o * 8;
                    if (board[to] == 0 || white == (board[to] > 96)) {
                        set.add(ChessMove.builder()
                                .name(ChessUtils.squareIndexToName(i) + ChessUtils.squareIndexToName(to))
                                .move(i, to)
                                .build());
                        if (board[to] != 0) break;
                    } else break;
                }
                // Check squares up
                for (int o = 1; o < i / 8 + 1; o++) {
                    int to = i - o * 8;
                    if (board[to] == 0 || white == (board[to] > 96)) {
                        set.add(ChessMove.builder()
                                .name(ChessUtils.squareIndexToName(i) + ChessUtils.squareIndexToName(to))
                                .move(i, to)
                                .build());
                        if (board[to] != 0) break;
                    } else break;
                }
                // Check squares right
                for (int o = 1; o < 8 - (i % 8); o++) {
                    int to = i + o;
                    if (board[to] == 0 || white == (board[to] > 96)) {
                        set.add(ChessMove.builder()
                                .name(ChessUtils.squareIndexToName(i) + ChessUtils.squareIndexToName(to))
                                .move(i, to)
                                .build());
                        if (board[to] != 0) break;
                    } else break;
                }
                // Check squares left
                for (int o = 1; o < (i % 8) + 1; o++) {
                    int to = i - o;
                    if (board[to] == 0 || white == (board[to] > 96)) {
                        set.add(ChessMove.builder()
                                .name(ChessUtils.squareIndexToName(i) + ChessUtils.squareIndexToName(to))
                                .move(i, to)
                                .build());
                        if (board[to] != 0) break;
                    } else break;
                }
            }
        }
        // Bishop and diagonal Queen moves
        {
            // Iterate over possible squares
            for (byte i = 0; i < board.length; i++) {
                char c = board[i];
                // Check if piece on square is type if bishop or queen
                if (c != 'B' && c != 'b' && c != 'Q' && c != 'q') continue;
                // Check if piece is of valid color
                if (white != (c == 'B' || c == 'Q')) continue;

                // Check bottom left diagonal
                // Calculate boarder distance
                int d = Math.min((i % 8) + 1, 8 - (i / 8));
                for (int o = 1; o < d; o++) {
                    int to = i + o * 7;
                    if (board[to] == 0 || white == (board[to] > 96)) {
                        set.add(ChessMove.builder()
                                .name(ChessUtils.squareIndexToName(i) + ChessUtils.squareIndexToName(to))
                                .move(i, to)
                                .build());
                        if (board[to] != 0) break;
                    } else break;
                }

                // Check top right diagonal
                // Calculate boarder distance
                d = Math.min(8 - (i % 8), (i / 8) + 1);
                for (int o = 1; o < d; o++) {
                    int to = i - o * 7;
                    if (board[to] == 0 || white == (board[to] > 96)) {
                        set.add(ChessMove.builder()
                                .name(ChessUtils.squareIndexToName(i) + ChessUtils.squareIndexToName(to))
                                .move(i, to)
                                .build());
                        if (board[to] != 0) break;
                    } else break;
                }

                // Check bottom right diagonal
                // Calculate boarder distance
                d = Math.min(8 - (i % 8), 8 - (i / 8));
                for (int o = 1; o < d; o++) {
                    int to = i + o * 9;
                    if (board[to] == 0 || white == (board[to] > 96)) {
                        set.add(ChessMove.builder()
                                .name(ChessUtils.squareIndexToName(i) + ChessUtils.squareIndexToName(to))
                                .move(i, to)
                                .build());
                        if (board[to] != 0) break;
                    } else break;
                }

                // Check top left diagonal
                // Calculate boarder distance
                d = Math.min((i % 8) + 1, (i / 8) + 1);
                for (int o = 1; o < d; o++) {
                    int to = i - o * 9;
                    if (board[to] == 0 || white == (board[to] > 96)) {
                        set.add(ChessMove.builder()
                                .name(ChessUtils.squareIndexToName(i) + ChessUtils.squareIndexToName(to))
                                .move(i, to)
                                .build());
                        if (board[to] != 0) break;
                    } else break;
                }
            }
        }
        // King moves
        {
            // Hard-coded becuase of performance improvements
            final byte[][] squaresToMove = new byte[][]{
                    new byte[]{1, 8, 9},
                    new byte[]{0, 2, 8, 9, 10},
                    new byte[]{1, 3, 9, 10, 11},
                    new byte[]{2, 4, 10, 11, 12},
                    new byte[]{3, 5, 11, 12, 13},
                    new byte[]{4, 6, 12, 13, 14},
                    new byte[]{5, 7, 13, 14, 15},
                    new byte[]{6, 14, 15},

                    new byte[]{0, 1, 9, 16, 17},
                    new byte[]{0, 1, 2, 8, 10, 16, 17, 18},
                    new byte[]{1, 2, 3, 9, 11, 17, 18, 19},
                    new byte[]{2, 3, 4, 10, 12, 18, 19, 20},
                    new byte[]{3, 4, 5, 11, 13, 19, 20, 21},
                    new byte[]{4, 5, 6, 12, 14, 20, 21, 22},
                    new byte[]{5, 6, 7, 13, 15, 21, 22, 23},
                    new byte[]{6, 7, 14, 22, 23},

                    new byte[]{8, 9, 17, 24, 25},
                    new byte[]{8, 9, 10, 16, 18, 24, 25, 26},
                    new byte[]{9, 10, 11, 17, 19, 25, 26, 27},
                    new byte[]{10, 11, 12, 18, 20, 26, 27, 28},
                    new byte[]{11, 12, 13, 19, 21, 27, 28, 29},
                    new byte[]{12, 13, 14, 20, 22, 28, 29, 30},
                    new byte[]{13, 14, 15, 21, 23, 29, 30, 31},
                    new byte[]{14, 15, 22, 30, 31},

                    new byte[]{16, 17, 25, 32, 33},
                    new byte[]{16, 17, 18, 24, 26, 32, 33, 34},
                    new byte[]{17, 18, 19, 25, 27, 33, 34, 35},
                    new byte[]{18, 19, 20, 26, 28, 34, 35, 36},
                    new byte[]{19, 20, 21, 27, 29, 35, 36, 37},
                    new byte[]{20, 21, 22, 28, 30, 36, 37, 38},
                    new byte[]{21, 22, 23, 29, 31, 37, 38, 39},
                    new byte[]{22, 23, 30, 38, 39},

                    new byte[]{24, 25, 33, 40, 41},
                    new byte[]{24, 25, 26, 32, 34, 40, 41, 42},
                    new byte[]{25, 26, 27, 33, 35, 41, 42, 43},
                    new byte[]{26, 27, 28, 34, 36, 42, 43, 44},
                    new byte[]{27, 28, 29, 35, 37, 43, 44, 45},
                    new byte[]{28, 29, 30, 36, 38, 44, 45, 46},
                    new byte[]{29, 30, 31, 37, 39, 45, 46, 47},
                    new byte[]{30, 31, 38, 46, 47},

                    new byte[]{32, 33, 41, 48, 49},
                    new byte[]{32, 33, 34, 40, 42, 48, 49, 50},
                    new byte[]{33, 34, 35, 41, 43, 49, 50, 51},
                    new byte[]{34, 35, 36, 42, 44, 50, 51, 52},
                    new byte[]{35, 36, 37, 43, 45, 51, 52, 53},
                    new byte[]{36, 37, 38, 44, 46, 52, 53, 54},
                    new byte[]{37, 38, 39, 45, 47, 53, 54, 55},
                    new byte[]{38, 39, 46, 54, 55},

                    new byte[]{40, 41, 49, 56, 57},
                    new byte[]{40, 41, 42, 48, 50, 56, 57, 58},
                    new byte[]{41, 42, 43, 49, 51, 57, 58, 59},
                    new byte[]{42, 43, 44, 50, 52, 58, 59, 60},
                    new byte[]{43, 44, 45, 51, 53, 59, 60, 61},
                    new byte[]{44, 45, 46, 52, 54, 60, 61, 62},
                    new byte[]{45, 46, 47, 53, 55, 61, 62, 63},
                    new byte[]{46, 47, 54, 62, 63},

                    new byte[]{48, 49, 57},
                    new byte[]{48, 49, 50, 56, 58},
                    new byte[]{49, 50, 51, 57, 59},
                    new byte[]{50, 51, 52, 58, 60},
                    new byte[]{51, 52, 53, 59, 61},
                    new byte[]{52, 53, 54, 60, 62},
                    new byte[]{53, 54, 55, 61, 63},
                    new byte[]{54, 55, 62}
            };

            for (byte i = 0; i < board.length; i++) {
                char c = board[i];
                if (c != 'K' && c != 'k') continue;
                if (white != (c == 'K')) continue;

                for (byte move : squaresToMove[i]) {
                    if (board[move] == 0 || board[move] > 96 == (c == 'K')) {
                        set.add(ChessMove.builder()
                                .name(ChessUtils.squareIndexToName(i) + ChessUtils.squareIndexToName(move))
                                .move(i, move)
                                .build());
                    }
                }
            }
        }

        // Check castle
        if (includeCastle) {
            if (white) {
                if (whiteQueensideCastleable || whiteKingsideCastleable) {
                    // Check in check
                    boolean inCheck = false;
                    loop:
                    for (ChessMove move : getPossibleMovesRaw(false, false)) {
                        for (byte b : move.getMoves()) {
                            if (b == 60) {
                                inCheck = true;
                                break loop;
                            }
                        }
                    }
                    if (!inCheck) {
                        if (whiteQueensideCastleable) {
                            if (board[56] == 'R' && board[57] == 0 && board[58] == 0 && board[59] == 0 && board[60] == 'K') {
                                ChessMove move = ChessMove.builder().name("e1c1").move(60, 58).move(56, 59).build();
                                Standard standard = performMove(move);
                                boolean valid = true;
                                loop:
                                for (ChessMove nextMove : standard.getPossibleMovesRaw(false, false)) {
                                    for (byte b : nextMove.getMoves()) {
                                        if (b == 59 || b == 58) {
                                            valid = false;
                                            break loop;
                                        }
                                    }
                                }
                                if (valid) set.add(move);
                            }
                        }
                        if (whiteKingsideCastleable) {
                            if (board[63] == 'R' && board[62] == 0 && board[61] == 0 && board[60] == 'K') {
                                ChessMove move = ChessMove.builder().name("e1g1").move(60, 62).move(63, 61).build();
                                Standard standard = performMove(move);
                                boolean valid = true;
                                loop:
                                for (ChessMove nextMove : standard.getPossibleMovesRaw(false, false)) {
                                    for (byte b : nextMove.getMoves()) {
                                        if (b == 61 || b == 62) {
                                            valid = false;
                                            break loop;
                                        }
                                    }
                                }
                                if (valid) set.add(move);
                            }
                        }
                    }
                }
            } else {
                if (blackQueensideCastleable || blackKingideCastleable) {
                    // Check in check
                    boolean inCheck = false;
                    loop:
                    for (ChessMove move : getPossibleMovesRaw(true, false)) {
                        for (byte b : move.getMoves()) {
                            if (b == 4) {
                                inCheck = true;
                                break loop;
                            }
                        }
                    }
                    if (!inCheck) {
                        if (blackQueensideCastleable) {
                            if (board[0] == 'r' && board[1] == 0 && board[2] == 0 && board[3] == 0 && board[4] == 'k') {
                                ChessMove move = ChessMove.builder().name("e8c8").move(4, 2).move(0, 3).build();
                                Standard standard = performMove(move);
                                boolean valid = true;
                                loop:
                                for (ChessMove nextMove : standard.getPossibleMovesRaw(true, false)) {
                                    for (byte b : nextMove.getMoves()) {
                                        if (b == 3 || b == 2) {
                                            valid = false;
                                            break loop;
                                        }
                                    }
                                }
                                if (valid) set.add(move);
                            }
                        }
                        if (blackKingideCastleable) {
                            if (board[7] == 'r' && board[6] == 0 && board[5] == 0 && board[4] == 'k') {
                                ChessMove move = ChessMove.builder().name("e8g8").move(4, 6).move(7, 5).build();
                                Standard standard = performMove(move);
                                boolean valid = true;
                                loop:
                                for (ChessMove nextMove : standard.getPossibleMovesRaw(true, false)) {
                                    for (byte b : nextMove.getMoves()) {
                                        if (b == 5 || b == 6) {
                                            valid = false;
                                            break loop;
                                        }
                                    }
                                }
                                if (valid) set.add(move);
                            }
                        }
                    }
                }
            }
        }

        return set;
    }

    /**
     * Creates a new Board with pieces and settings after performed passed move
     *
     * @param move Move to make
     * @return ChessBoard
     */

    @Override
    public Standard performMove(ChessMove move) {
        // Construct new Object based on current
        Standard standard = new Standard();
        standard.board = Arrays.copyOf(board, board.length);
        standard.moveRight = (moveRight == 'w' ? 'b' : 'w');
        standard.whiteKingsideCastleable = whiteKingsideCastleable;
        standard.whiteQueensideCastleable = whiteQueensideCastleable;
        standard.blackKingideCastleable = blackKingideCastleable;
        standard.blackQueensideCastleable = blackQueensideCastleable;
        standard.entPassent = -1;
        standard.moveSinceCaptureOrPawn = moveSinceCaptureOrPawn + 1;
        standard.move = this.move + (moveRight == 'b' ? 1 : 0);

        // Perform moves
        for (int i = 0; i < move.getMoves().length; i++) {
            byte b = move.getMoves()[i];
            if (b == -1) continue;

            // Move since capture or pawn
            if (standard.board[b] != 0 || standard.board[i] == 'p' || standard.board[i] == 'P') {
                standard.moveSinceCaptureOrPawn = 0;
            }

            standard.board[b] = standard.board[i];
            standard.board[i] = 0;

            // Castle
            if (standard.board[b] == 'K') {
                standard.whiteKingsideCastleable = false;
                standard.whiteQueensideCastleable = false;
            } else if (standard.board[b] == 'k') {
                standard.blackKingideCastleable = false;
                standard.blackQueensideCastleable = false;
            }
            if (standard.board[b] == 'R' && b == 56) {
                standard.whiteQueensideCastleable = false;
            } else if (standard.board[b] == 'R' && b == 63) {
                standard.whiteKingsideCastleable = false;
            } else if (standard.board[b] == 'r' && b == 0) {
                standard.blackQueensideCastleable = false;
            } else if (standard.board[b] == 'r' && b == 7) {
                standard.blackKingideCastleable = false;
            }

            // En passent
            if (Math.abs(i - b) == 16 && (standard.board[b] == 'P' || standard.board[b] == 'p')) {
                standard.entPassent = (byte) ((i + b) / 2);
            }
        }

        // Perform conversions
        for (int i = 0; i < move.getConversions().length; i++) {
            char c = move.getConversions()[i];
            if (c == 0) continue;
            standard.board[i] = c;
        }

        return standard;
    }

    /**
     * Generates FEN of current state
     *
     * @return FEN as String
     */

    @Override
    public String fen() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            if (builder.length() != 0) builder.append("/");
            int empty = 0;
            for (int o = 0; o < 8; o++) {
                char c = board[i * 8 + o];
                if (c != 0) {
                    if (empty != 0) {
                        builder.append(empty);
                        empty = 0;
                    }
                    builder.append(c);
                } else {
                    empty++;
                }
            }
            if (empty != 0) builder.append(empty);
        }

        builder.append(' ');
        builder.append(moveRight);
        builder.append(' ');

        if (whiteKingsideCastleable || whiteQueensideCastleable || blackKingideCastleable || blackQueensideCastleable) {
            if (whiteKingsideCastleable) builder.append('K');
            if (whiteQueensideCastleable) builder.append('Q');
            if (blackKingideCastleable) builder.append('k');
            if (blackQueensideCastleable) builder.append('q');
        } else {
            builder.append('-');
        }
        builder.append(' ');

        if (entPassent != -1) {
            builder.append(ChessUtils.squareIndexToName(entPassent));
        } else {
            builder.append('-');
        }
        builder.append(' ');
        builder.append(moveSinceCaptureOrPawn);
        builder.append(' ');
        builder.append(move);
        return builder.toString();
    }

    @Override
    public double evaluate() {
        double value = 0;

        for (char c : board) {
            switch (c) {
                case 'P':
                    value += 1;
                    break;
                case 'B':
                case 'K':
                    value += 3;
                    break;
                case 'R':
                    value += 5;
                    break;
                case 'Q':
                    value += 9;
                    break;
                case 'p':
                    value -= 1;
                    break;
                case 'b':
                case 'k':
                    value -= 3;
                    break;
                case 'r':
                    value -= 5;
                    break;
                case 'q':
                    value -= 9;
                    break;
                default:
                    break;
            }
        }

        return value;
    }

}
