package info.hearthsim.brazier.ui.jtable;

public interface ColumnDisplayFormat<DisplayType, ColumnData> {
    public Class<DisplayType> getDisplayDataClass();
    public DisplayType getDisplayData(ColumnData data);
}
