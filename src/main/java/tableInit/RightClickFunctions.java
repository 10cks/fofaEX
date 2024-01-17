package tableInit;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.net.URI;

public class RightClickFunctions {
    // 在类的成员变量中创建弹出菜单
    private JPopupMenu popupMenu = new JPopupMenu();
    private JMenuItem itemSelectColumn = new JMenuItem("选择当前整列");
    private JMenuItem itemDeselectColumn = new JMenuItem("取消选择整列");
    private JMenuItem itemOpenLink = new JMenuItem("打开链接");
    private JMenuItem itemCopy = new JMenuItem("复制当前单元格");
    private JMenuItem itemSearch = new JMenuItem("表格搜索");
    private File lastOpenedPath; // 添加一个成员变量来保存上次打开的文件路径
    static TableCellRenderer highlightRenderer = new HighlightRenderer();
    private static TableCellRenderer defaultRenderer;

    private boolean isInitialized = false; // 新添加的字段，用于跟踪是否已初始化

    private JTable table;
    // 检查对话框是否已经存在
    private static JDialog searchDialog = null;

    // 添加一个setter方法
    public void setTable(JTable newTable) {
        table = newTable;
    }

    public void initializeTable() {
        // 添加菜单项到弹出菜单
        popupMenu.add(itemOpenLink);
        popupMenu.add(itemCopy);
        popupMenu.add(itemSearch);
        popupMenu.add(itemSelectColumn);
        popupMenu.add(itemDeselectColumn);
        defaultRenderer = table.getDefaultRenderer(Object.class);
        // 为右键菜单项添加全选当前列监听器
        itemSelectColumn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (table != null) {
                    int col = table.getSelectedColumn();
                    if (col >= 0) {
                        table.setColumnSelectionAllowed(true);
                        table.setRowSelectionAllowed(false);
                        table.clearSelection();
                        table.addColumnSelectionInterval(col, col);
                    }
                }
            }
        });
        // 设置表格的默认渲染器
        table.setDefaultRenderer(Object.class, new SelectedCellBorderHighlighter());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (row >= 0 && col >= 0) {
                        // 右键显示弹出菜单
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        // 为取消选择列的菜单项添加事件监听器
        itemDeselectColumn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 取消选择当前列
                if (table != null) {
                    table.clearSelection();
                    // 恢复默认的行选择模式
                    table.setRowSelectionAllowed(true);
                    table.setColumnSelectionAllowed(false);
                }
            }
        });
        // 为打开链接的菜单项添加事件监听器
        itemOpenLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 执行打开链接的操作
                if (table != null) {
                    int selectedRow = table.getSelectedRow();
                    int selectedCol = table.getSelectedColumn();
                    if (selectedRow >= 0 && selectedCol >= 0) {
                        Object cellContent = table.getValueAt(selectedRow, selectedCol);
                        if (cellContent != null && cellContent.toString().startsWith("http")) {
                            try {
                                Desktop desktop = Desktop.getDesktop();
                                URI uri = new URI(cellContent.toString());
                                desktop.browse(uri);
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(popupMenu, "无法打开链接：" + cellContent, "错误", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(popupMenu, "当前单元格不包含有效链接", "警告", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            }
        });

        // 为复制的菜单项添加事件监听器
        itemCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 执行复制操作
                if (table != null) {
                    int[] selectedRows = table.getSelectedRows();
                    int[] selectedColumns = table.getSelectedColumns();
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < selectedRows.length; i++) {
                        for (int j = 0; j < selectedColumns.length; j++) {
                            Object value = table.getValueAt(selectedRows[i], selectedColumns[j]);
                            sb.append(value == null ? "" : value.toString());
                            if (j < selectedColumns.length - 1) {
                                sb.append("\t"); // 列之间添加制表符分隔
                            }
                        }
                        if (i < selectedRows.length - 1) {
                            sb.append("\n"); // 行之间添加换行符分隔
                        }
                    }
                    StringSelection stringSelection = new StringSelection(sb.toString());
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);
                }
            }
        });

        itemSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createSearchDialog();
            }
        });
    }

    private void createSearchDialog() {

        if (searchDialog != null) {
            // 对话框已经存在，可能需要将其带到前面
            searchDialog.toFront();
            searchDialog.requestFocus();
        } else {
            // 创建一个新的JDialog
            searchDialog = new JDialog((Frame) null, "搜索", false);
            searchDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // 点击关闭按钮时释放窗口资源

            searchDialog.setLayout(new FlowLayout());
            searchDialog.setAlwaysOnTop(true);
            JLabel label = new JLabel("输入搜索内容：");
            JTextField searchField = new JTextField(20);
            JButton searchButton = new JButton("搜索");
            JButton closeButton = new JButton("退出高亮");

            // 添加组件到对话框
            searchDialog.add(label);
            searchDialog.add(searchField);
            searchDialog.add(searchButton);
            searchDialog.add(closeButton);

            // 显示对话框
            searchDialog.pack();
            searchDialog.setLocationRelativeTo(null); // 在屏幕中央显示
            searchDialog.setVisible(true);

            // 搜索按钮监听器
            searchButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String searchText = searchField.getText();
                    if (searchText != null && !searchText.isEmpty()) {
                        // 执行搜索并高亮显示匹配的单元格
                        searchTable(searchText);
                    }
                }
            });
            // 退出按钮监听器
            JDialog finalSearchDialog = searchDialog;
            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    finalSearchDialog.dispose(); // 关闭对话框
                    resetSearch(); // 重置搜索结果
                    searchDialog = null; // 重置searchDialog引用
                }
            });
            // 设置窗口关闭监听器
            searchDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    searchDialog = null; // 窗口关闭时重置searchDialog引用
                }
            });
        }
    }

    private void searchTable(String searchText) {
        if (!(highlightRenderer instanceof HighlightRenderer)) {
            highlightRenderer = new HighlightRenderer();
        }
        // Update the search text in the highlight renderer
        ((HighlightRenderer) highlightRenderer).setSearchText(searchText);

        // Apply the highlight renderer to all columns
        for (int col = 0; col < table.getColumnCount(); col++) {
            table.getColumnModel().getColumn(col).setCellRenderer(highlightRenderer);
        }

        // Repaint the table to show the changes
        table.repaint();
    }

    private void resetSearch() {
        // Reset the renderer to the default for all columns
        for (int col = 0; col < table.getColumnCount(); col++) {
            table.getColumnModel().getColumn(col).setCellRenderer(defaultRenderer);
        }
        // 退出时恢复表格颜色
        table.repaint();
    }
}
