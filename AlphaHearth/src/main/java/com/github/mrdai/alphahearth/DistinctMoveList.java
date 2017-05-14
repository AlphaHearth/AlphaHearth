package com.github.mrdai.alphahearth;

import com.github.mrdai.alphahearth.move.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

class DistinctMoveList {
    private static final Logger LOG = LoggerFactory.getLogger(DistinctMoveList.class);

    private int size = 0;
    private Node head = null;
    private Node tail = null;

    private Set<Board> boardSet = new HashSet<>();

    public void add(Board parentBoard, Move move) {
        int orgValue = (int) parentBoard.getValue();
        Board copiedParentBoard = parentBoard.clone();
        copiedParentBoard.applyMoves(move);
        if (boardSet.contains(copiedParentBoard)) {
            LOG.debug("Not adding move \n{} as it leads to the same state as other added moves.",
                move.toString());
            return;
        }
        int value = (int) copiedParentBoard.getValue();
        if (orgValue > value) {
            LOG.debug("Not adding move \n{} as it decrease the board value from {} to {}.",
                move.toString(), orgValue, value);
            return;
        }

        boardSet.add(copiedParentBoard);
        Node node = new Node(move, value);
        if (head == null && tail == null) {
            head = node;
            tail = node;
        } else {
            // Adds it in the decreasing order of the value
            Node ptr = head;
            while (true) {
                assert ptr != null;
                if (ptr.next == null) {
                    ptr.next = node;
                    node.front = ptr;
                    tail = node;
                    break;
                }
                if (ptr.value > value && ptr.next.value <= value) {
                    node.next = ptr.next;
                    ptr.next.front = node;
                    node.front = ptr;
                    ptr.next = node;
                    break;
                }
                ptr = ptr.next;
            }
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
        return toMoveList((s) -> s < trimSize ? s : trimSize);
    }

    public List<Move> toMoveList(Function<Integer, Integer> trimSizeProducer) {
        int listSize = trimSizeProducer.apply(size);
        List<Move> result = new LinkedList<>();
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
        final int value;
        Node front = null;
        Node next = null;

        Node(Move move, int value) {
            this.move = move;
            this.value = value;
        }
    }
}
