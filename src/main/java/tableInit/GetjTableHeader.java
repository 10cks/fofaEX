package tableInit;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;

public class GetjTableHeader {
    public static JTableHeader getjTableHeader(JTable table) {
        JTableHeader header = table.getTableHeader();
        // 自定义列标题的渲染器
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                // 设置渲染器返回的组件类型为JLabel
                JLabel headerLabel = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                // 检查排序键是否与当前列匹配
                if(table.getRowSorter() != null) {
                    java.util.List<? extends RowSorter.SortKey> sortKeys = table.getRowSorter().getSortKeys();

                    if (sortKeys.size() > 0 && sortKeys.get(0).getColumn() == table.convertColumnIndexToView(column)) {
                        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD));
                        headerLabel.setForeground(Color.CYAN);
                    } else {
                        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.PLAIN));
                        headerLabel.setForeground(Color.WHITE);
                    }
                }

                // 设置背景色为灰色
                headerLabel.setBackground(Color.GRAY);
                // 设置文字居中
                headerLabel.setHorizontalAlignment(JLabel.CENTER);
                // 设置标题加粗和大小
                headerLabel.setFont(new Font(headerLabel.getFont().getFamily(), Font.BOLD, 15));
                // 设置边框，其中颜色设置为白色
                headerLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.WHITE));
                // 设置数据文字大小
                table.setFont(new Font("Serif", Font.PLAIN, 15)); // 设置数据单元格的字体和大小

                return headerLabel;
            }
        });
        return header;
    }

    public static void adjustColumnWidths(JTable table) {
        SwingUtilities.invokeLater(new Runnable() { // 修复多线程并发问题
            public void run() {
                TableColumnModel columnModel = table.getColumnModel();
                for (int column = 0; column < table.getColumnCount(); column++) {
                    int width = 15; // Min width
                    for (int row = 0; row < table.getRowCount(); row++) {
                        TableCellRenderer renderer = table.getCellRenderer(row, column);
                        Component comp = table.prepareRenderer(renderer, row, column);
                        width = Math.max(comp.getPreferredSize().width + 1, width);
                    }
                    if (width > 300)
                        width = 300;
                    columnModel.getColumn(column).setPreferredWidth(width);
                }
            }
        });

    }
}
