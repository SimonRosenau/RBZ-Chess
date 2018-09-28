package de.rosenau.simon.rbzchess;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Project created by Simon Rosenau.
 */

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class ChessMove {

    private String name;

    private byte[] moves;
    private char[] conversions;

    public static ChessMoveBuilder builder() {
        return new ChessMoveBuilder();
    }

    public static class ChessMoveBuilder {

        private String name;
        private byte[] moves;
        private char[] conversions;

        ChessMoveBuilder() {
            moves = new byte[64];
            conversions = new char[64];
            for (int i = 0; i < moves.length; i++) moves[i] = -1;
        }

        public ChessMoveBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ChessMoveBuilder move(int from, int to) {
            moves[from] = (byte) to;
            return this;
        }

        public ChessMoveBuilder conversion(int square, char piece) {
            conversions[square] = piece;
            return this;
        }

        public ChessMove build() {
            return new ChessMove(name, moves, conversions);
        }

    }

}
