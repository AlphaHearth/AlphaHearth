package com.github.mrdai.alphahearth.mcts.policy;

import com.github.mrdai.alphahearth.Board;
import com.github.mrdai.alphahearth.move.Move;

import java.util.List;
import java.util.Random;

/**
 * A {@link DefaultPolicy} which produces {@link Move} by randomly selecting from all the
 * valid moves of the given {@link Board}.
 */
public class RandomDefaultPolicy implements DefaultPolicy {
    private final Random random = new Random();

    @Override
    public Move produceMode(Board board) {
        List<Move> moves = board.getAvailableMoves();
        return moves.get(random.nextInt(moves.size()));
    }
}
