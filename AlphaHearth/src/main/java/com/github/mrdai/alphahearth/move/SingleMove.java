package com.github.mrdai.alphahearth.move;

public interface SingleMove {
    public default Move toMove() {
        return Move.EMPTY_MOVE.withNewMove(this);
    }
}
