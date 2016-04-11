package com.github.mrdai.alphahearth.move;

import com.github.mrdai.alphahearth.Board;

public interface SingleMove {
    public default Move toMove() {
        return Move.EMPTY_MOVE.withNewMove(this);
    }
    public String toString(Board board);
}
