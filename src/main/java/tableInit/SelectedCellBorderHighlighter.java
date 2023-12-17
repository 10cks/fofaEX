package tableInit;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class SelectedCellBorderHighlighter extends DefaultTableCellRenderer {
    private static final Border SELECTED_BORDER = BorderFactory.createLineBorder(new Color(201, 79, 79), 3);
    private static final Border UNSELECTED_BORDER = BorderFactory.createEmptyBorder(1, 1, 1, 1);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (table.getSelectedRow() == row && table.getSelectedColumn() == column) {
            setBorder(SELECTED_BORDER);
        } else {
            setBorder(UNSELECTED_BORDER);
        }
        return this;
    }
}
