package de.rosenau.simon.rbzchess;

import de.rosenau.simon.rbzchess.boards.Standard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Project created by Simon Rosenau.
 */

public class Main {

    // Testing

    public static void main(String[] args) {
        Standard standard = new Standard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        int depth = 0;
        int calculated = 0;
        /*HashMap<Standard, Double> set;
        HashMap<Standard, Double> newSet = new HashMap<>();
        newSet.put(standard, standard.evaluate());
        while(depth < 3) {
            set = newSet;
            newSet = new HashMap<>();
            for (Standard node : set.keySet()) {
                for (ChessMove move : node.getPossibleMoves()) {
                    Standard newNode = node.performMove(move);
                    double value = newNode.evaluate();
                    newSet.put(node.performMove(move), value);
                    calculated++;
                    System.out.println("Calculated: " + calculated);
                }
            }
            depth++;
            System.out.println("Depth: " + depth);
        }*/

    }

    public static void run(Standard standard, int depth) {
        System.out.println("Depth: " + depth);
        List<Standard> list = new ArrayList<>();
        for (ChessMove move : standard.getPossibleMoves()) {
            list.add(standard.performMove(move));
        }

    }

    public static void main1(String[] args) throws IOException {
        Standard standard = new Standard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        loop:
        while(true) {
            System.out.print("Which move to play? ");
            String input = new BufferedReader(new InputStreamReader(System.in)).readLine();
            Set<ChessMove> set = standard.getPossibleMoves();
            for (ChessMove move : set) {
                if (move.getName().equals(input)) {
                    standard = standard.performMove(move);
                    System.out.println("Updated to fen: " + standard.fen());
                    continue loop;
                }
            }
            System.out.println("Illegal move: " + input);
        }
    }
}
