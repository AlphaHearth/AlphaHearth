package info.hearthsim.brazier.ui;

import org.jtrim.utils.ExceptionHelper;

import javax.swing.*;
import java.awt.*;
import java.util.function.IntSupplier;

public final class JHorizontallyScrollablePanel extends JComponent implements Scrollable {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_SCROLL_UNIT = 10;

    private final IntSupplier scrollUnit;

    public JHorizontallyScrollablePanel(LayoutManager layout) {
        this(layout, () -> DEFAULT_SCROLL_UNIT);
    }

    public JHorizontallyScrollablePanel() {
        this(() -> DEFAULT_SCROLL_UNIT);
    }

    public JHorizontallyScrollablePanel(LayoutManager layout, IntSupplier scrollUnit) {
        ExceptionHelper.checkNotNullArgument(scrollUnit, "scrollUnit");
        this.scrollUnit = scrollUnit;

        setLayout(layout);
    }

    public JHorizontallyScrollablePanel(IntSupplier scrollUnit) {
        ExceptionHelper.checkNotNullArgument(scrollUnit, "scrollUnit");
        this.scrollUnit = scrollUnit;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return null;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return scrollUnit.getAsInt();
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return scrollUnit.getAsInt();
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void paintComponent(Graphics g) {
        Color prevColor = g.getColor();

        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(prevColor);
    }
}
