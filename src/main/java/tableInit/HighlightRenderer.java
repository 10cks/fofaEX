package tableInit;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class HighlightRenderer extends DefaultTableCellRenderer {
    private String searchText;

    public void setSearchText(String searchText) {
        this.searchText = searchText.toLowerCase();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (searchText != null && !searchText.isEmpty() && value != null) { // 此设置需要成对设置，全局渲染
            String cellText = value.toString().toLowerCase();
            if (cellText.contains(searchText)) {
                //comp.setBackground(Color.RED); // highlight background
                comp.setForeground(new Color(201, 79, 79)); // highlight background
            } else {
                if (!isSelected) {
                    // comp.setBackground(Color.WHITE); // or table's default background
                    comp.setForeground(Color.BLACK); // highlight background
                }
            }
        }

        return comp;
    }
}
