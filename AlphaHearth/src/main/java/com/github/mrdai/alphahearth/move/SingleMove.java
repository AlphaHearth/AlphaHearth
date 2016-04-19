package com.github.mrdai.alphahearth.move;

import com.github.mrdai.alphahearth.Board;

public interface SingleMove {

    public String toString(Board board);

    public void applyTo(Board board);
    public void applyTo(Board board, boolean logMove);

}
