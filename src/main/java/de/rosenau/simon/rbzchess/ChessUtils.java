package de.rosenau.simon.rbzchess;

/**
 * Project created by Simon Rosenau.
 */

public class ChessUtils {

    /**
     * Converts the square name to the index in board notated from 0 (top left) to 63 (bottom right) line by line
     *
     * @param name Square name notated as e.g. h3
     * @return index
     */

    public static byte squareNameToIndex(String name) {
        int column = name.charAt(0) - 'a';
        int row = 8 - name.charAt(1) + '0';
        return (byte) (row * 8 + column);
    }

    /**
     * Converts the square index in board notated from 0 (top left) to 63 (bottom right) line by line to the name
     *
     * @param index Square name notated as e.g. h3
     * @return name
     */

    public static String squareIndexToName(int index) {
        char column = (char) (index % 8 + 'a');
        char row = (char) (8 - index / 8 + '0');
        return new String(new char[]{column, row});
    }

    /**
     * Pretty prints the board in the console
     *
     * @param board as char array
     */

    public static void printBoard(char[] board) {
        for (int i = 0; i < 8; i++) {
            StringBuilder builder = new StringBuilder();
            for (int o = 0; o < 8; o++) {
                char c = board[i * 8 + o];
                if (builder.length() != 0) builder.append(" | ");
                if (c != 0) builder.append(c);
                else builder.append('-');
            }
            System.out.println(builder.toString());
        }
    }

}
