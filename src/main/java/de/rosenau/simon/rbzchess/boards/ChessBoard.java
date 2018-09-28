package de.rosenau.simon.rbzchess.boards;

import de.rosenau.simon.rbzchess.ChessMove;

import java.util.Set;

/**
 * Project created by Simon Rosenau.
 */

public interface ChessBoard {

    Set<ChessMove> getPossibleMoves();

    ChessBoard performMove(ChessMove move);

    String fen();

    double evaluate();

}
