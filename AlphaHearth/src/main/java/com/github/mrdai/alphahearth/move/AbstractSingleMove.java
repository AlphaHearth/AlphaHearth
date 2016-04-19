package com.github.mrdai.alphahearth.move;

import com.github.mrdai.alphahearth.Board;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSingleMove implements SingleMove {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSingleMove.class);

    protected StackTraceElement[] constructPoint;

    protected void setConstructPoint() {
        StackTraceElement[] currentPoint = (new Throwable()).getStackTrace();
        int i;
        for (i = 0; i < currentPoint.length; i++)
            if (!currentPoint[i].getClassName().startsWith("com.github.mrdai.alphahearth.mcts.move"))
                break;
        constructPoint = new StackTraceElement[currentPoint.length - i];
        for (int j = 0; i < currentPoint.length; j++, i++)
            constructPoint[j] = currentPoint[i];
    }

    public void applyTo(Board board) {
        applyTo(board, false);
    }

    public void applyTo(Board board, boolean logMove) {
        try {
            applyToUnsafe(board, logMove);
        } catch (Throwable thr) {
            StringBuilder builder = new StringBuilder("Exception occurred when applying move ")
                .append(toString()).append(" to board:\n").append(board.toString())
                .append("\nThe move is constructed at\n");
            for (StackTraceElement elem : constructPoint)
                builder.append(elem.getMethodName()).append("(")
                    .append(elem.getFileName()).append(":")
                    .append(elem.getLineNumber()).append(")\n");
            builder.append("=====================");
            builder.append("The underlying exception is: ").append(thr.getMessage()).append("\n");
            for (StackTraceElement elem : thr.getStackTrace())
                builder.append(elem.getMethodName()).append("(")
                    .append(elem.getFileName()).append(":")
                    .append(elem.getLineNumber()).append(")\n");
            LOG.error(builder.toString());
        }
    }

    protected abstract void applyToUnsafe(Board board, boolean logMove);
}
