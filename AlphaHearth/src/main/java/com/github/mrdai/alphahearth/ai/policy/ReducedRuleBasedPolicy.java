package com.github.mrdai.alphahearth.ai.policy;

import com.github.mrdai.alphahearth.Board;
import com.github.mrdai.alphahearth.move.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * A {@code DefaultPolicy} who produces {@link Move} based on predefined rules.
 */
public class ReducedRuleBasedPolicy implements DefaultPolicy {
    private static final Logger LOG = LoggerFactory.getLogger(ReducedRuleBasedPolicy.class);

    private final Random random = new Random();
    private final float p;
    private final RandomPolicy randomPolicy = new RandomPolicy();
    private final ExpertRuleBasedPolicy ruleBasedPolicy = new ExpertRuleBasedPolicy();

    public ReducedRuleBasedPolicy(float p) {
        this.p = p;
    }

    @Override
    public Move produceMode(Board board) {
        board = board.clone();
        Move.Builder builder = new Move.Builder();
        int i = 0;
        while (i < 30) {
            SingleMove move = produceSingleMove(board);
            if (move == null)
                break;
            move.applyTo(board);
            builder.addMove(move);
            i++;
        }
        return builder.build();
    }

    SingleMove produceSingleMove(Board board) {
        SingleMove move;

        // Generate `CardPlaying` move
        if (random.nextFloat() <= p)
            move = ruleBasedPolicy.cardPlaying(board);
        else
            move = randomPolicy.cardPlaying(board);
        if (move != null) {
            LOG.trace(move.toString(board));
            return move;
        }

        // Generate `DirectAttacking` move
        if (random.nextFloat() <= p)
            move = ruleBasedPolicy.minionAttack(board);
        else
            move = randomPolicy.minionAttack(board);
        if (move != null) {
            LOG.trace(move.toString(board));
            return move;
        }

        if (random.nextFloat() <= p)
            move = ruleBasedPolicy.heroAttack(board);
        else
            move = randomPolicy.heroAttack(board);
        if (move != null) {
            LOG.trace(move.toString(board));
            return move;
        }

        // Generate `HeroPowerPlaying` move
        if (random.nextFloat() <= p)
            return ruleBasedPolicy.heroPowerPlaying(board);
        else
            return randomPolicy.heroPowerPlaying(board);
    }

    public void close() {}
}
