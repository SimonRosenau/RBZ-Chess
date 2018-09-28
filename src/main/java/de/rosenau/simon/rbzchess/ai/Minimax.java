package de.rosenau.simon.rbzchess.ai;

import de.rosenau.simon.rbzchess.ChessMove;
import de.rosenau.simon.rbzchess.boards.ChessBoard;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Project created by Simon Rosenau.
 */

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Minimax {

    private final MinimaxNode node;

    public Minimax(ChessBoard board) {
        node = new MinimaxNode(null, board, board.evaluate());
    }

    public ChessMove getBestMove() {
        // TODO
        return null;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static class MinimaxNode {
        private final MinimaxNode root;
        private final Set<MinimaxNode> children = new HashSet<>();

        private final ChessBoard board;
        private final double evaluation;
    }

}
