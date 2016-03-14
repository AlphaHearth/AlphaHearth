package com.github.mrdai.alphahearth.move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@code Move} represents a sequence of actions a player does in a game turn, which are represented
 * as {@link List} of {@link SingleMove}.
 * <p>
 * {@code Move} instances are immutable. To construct a {@code Move}, one should start from {@link Move#EMPTY_MOVE}
 * and use {@link #withNewMove(SingleMove)} to add customized {@link SingleMove}s.
 */
public class Move {
    /** Empty {@code Move}. */
    public static final Move EMPTY_MOVE = new Move();

    private final List<SingleMove> actualMoves;

    private Move() {
        this(Collections.emptyList());
    }

    private Move(List<SingleMove> actualMoves) {
        this.actualMoves = actualMoves;
    }

    /**
     * Creates a new {@code Move} with the given {@link SingleMove} added.
     */
    public Move withNewMove(SingleMove move) {
        List<SingleMove> newMoves = new ArrayList<>(actualMoves.size() + 1);
        actualMoves.forEach(newMoves::add);
        return new Move(Collections.unmodifiableList(newMoves));
    }

    public List<SingleMove> getActualMoves() {
        return actualMoves;
    }
}
