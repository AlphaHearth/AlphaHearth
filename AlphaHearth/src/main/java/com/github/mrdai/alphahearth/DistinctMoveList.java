package com.github.mrdai.alphahearth;

import com.github.mrdai.alphahearth.move.Move;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class DistinctMoveList {
    private int size = 0;
    private Node head = null;
    private Node tail = null;

    private List<Board> boardSet = new LinkedList<>();

    public void add(Board copiedParentBoard, Move move) {
        copiedParentBoard.applyMoves(move);
        for (Board board : boardSet)
            if (board.equals(copiedParentBoard))
                return;
        boardSet.add(copiedParentBoard);

        Node node = new Node(move);
        if (head == null && tail == null) {
            head = node;
            tail = node;
        } else {
            node.next = head;
            head.front = node;
            head = node;
        }
        size++;
    }

    public Move get(int index) {
        if (index >= size || index < 0)
            throw new IllegalArgumentException("The given index " + index + " is invalid.");
        Node ptr = tail;
        for (int i = 0; i < index; i++) {
            ptr = ptr.front;
            assert ptr != null;
        }
        return ptr.move;
    }

    public List<Move> toMoveList(int trimSize) {
        int listSize = size < trimSize ? size : trimSize;
        List<Move> result = new ArrayList<>(listSize);
        Node ptr = head;
        for (int i = 0; i < listSize; i++) {
            assert ptr != null;
            result.add(ptr.move);
            ptr = ptr.next;
        }
        return result;
    }

    public int size() {
        return size;
    }

    private static final class Node {
        final Move move;
        Node front = null;
        Node next = null;

        Node(Move move) {
            this.move = move;
        }
    }
}
