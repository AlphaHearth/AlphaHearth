package info.hearthsim.brazier.ui.jtable;

public interface ColumnDataGetter<RowData, ColumnData> {
    public ColumnData getColumnData(RowData data);
}
