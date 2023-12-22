package plugins;

import net.dongliu.commons.Sys;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static tableInit.GetjTableHeader.adjustColumnWidths;
import static tableInit.GetjTableHeader.getjTableHeader;

public class CommonTemplate {

    private static String pluginName = "";

    // 动态新增 tab 页
    public static void addTabbedPaneFromFile(JTabbedPane tabbedPane) {
        EventQueue.invokeLater(() -> {
            //读取文件并为每一行创建一个新的标签页
            Path file = Paths.get("./plugins/AllPlugins.txt");

            try (BufferedReader reader = Files.newBufferedReader(file)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        String pluginName = parts[0]; //获取插件名称

                        //ActionListener在选择“关闭”时关闭标签页
                        ActionListener closeListener = event -> tabbedPane.removeTabAt(tabbedPane.indexOfTab(pluginName));

                        JPanel panel1 = new JPanel(); //创建面板
                        JPanel panel2 = new JPanel(); //创建面板
                        panel1.add(addBanner(pluginName), BorderLayout.NORTH);
                        panel2.add(new JScrollPane(createTable()), BorderLayout.CENTER); //添加表格到中心

                        JPanel mainPanel = new JPanel();
                        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
                        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                        mainPanel.add(panel1);
                        mainPanel.add(panel2);

                        //为标签添加了一个鼠标监听器，显示弹出菜单以关闭标签页
                        mainPanel.addMouseListener(new MouseAdapter() {
                            public void mousePressed(MouseEvent e) {
                                if (SwingUtilities.isRightMouseButton(e)) {
                                    JPopupMenu menu = new JPopupMenu();
                                    JMenuItem closeItem = new JMenuItem("关闭");
                                    closeItem.addActionListener(closeListener);
                                    menu.add(closeItem);
                                    menu.show(e.getComponent(), e.getX(), e.getY());
                                }
                            }
                        });

                        //添加标签页
                        tabbedPane.addTab(pluginName, mainPanel);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    public static JLabel addBanner(String banner) {
        JLabel labelIcon = new JLabel(banner);
        labelIcon.setForeground(new Color(48, 49, 52));
        Font iconFont = new Font("Times New Roman", Font.BOLD, 60);
        labelIcon.setFont(iconFont);
        return labelIcon;
    }

    // 创建表格
    public static JTable createTable() {
        // Define column headers
        String[] columnNames = {"Current No Data"," "};
        // Use the default table model
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        //  columnNames: 这是一个Object数组，它包含了将要显示在JTable表头的列名。这些列名将成为表格的列标题。
        //  rowCount: 这是一个整数，它指定了模型在初始化时应该有多少行。这个数值为0意味着表格将以零行开始，你可以在之后根据需要动态地添加行。

        JTable table = new JTable(model);

        JTableHeader header = getjTableHeader(table);
        table.setTableHeader(header);
        adjustColumnWidths(table);
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);

        return table;
    }

    public static void addMenuModel(String banner) {
        JLabel labelIcon = new JLabel(banner);
        labelIcon.setForeground(new Color(48, 49, 52));
        Font iconFont = new Font("Times New Roman", Font.BOLD, 60);
        labelIcon.setFont(iconFont);

    }

    // 在给定面板中查找 JTable
    public static JTable findTableInPanel(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                JViewport viewport = scrollPane.getViewport();
                if (viewport.getView() instanceof JTable) {
                    return (JTable) viewport.getView();
                }
            }
        }
        return null;
    }

    public static void saveTableData(JTabbedPane tabbedPane) {
        EventQueue.invokeLater(() -> {
            // 检查或创建coredata文件夹
            File directory = new File("coredata");
            if (!directory.exists()) {
                directory.mkdir();
            }

            // 获取当前选中的标签索引
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == -1) {
                // 如果没有选中的标签，不执行任何操作
                return;
            }

            // 获取当前选中的标签的标题
            String tabName = tabbedPane.getTitleAt(selectedIndex);

            // 获取选中的标签对应的组件
            Component selectedComponent = tabbedPane.getComponentAt(selectedIndex);
            if (!(selectedComponent instanceof JPanel)) {
                System.err.println("The selected component is not a JPanel.");
                return;
            }
            JPanel panel = (JPanel) selectedComponent;

            // 查找JScrollPane组件
            JScrollPane scrollPane = findScrollPane(panel);

            if (scrollPane == null) {
                System.err.println("No JScrollPane found in the selected tab.");
                return;
            }

            // 获取JTable
            JTable table = (JTable) scrollPane.getViewport().getView();

            // 转换表格数据为JSON
            JSONArray jsonArray = new JSONArray();
            for (int row = 0; row < table.getRowCount(); row++) {
                JSONObject rowJson = new JSONObject();
                for (int col = 0; col < table.getColumnCount(); col++) {
                    rowJson.put(table.getColumnName(col), table.getValueAt(row, col));
                }
                jsonArray.put(rowJson);
            }

            // 写入JSON文件
            String filename = "coredata/" + tabName + "data.json";
            try (FileWriter file = new FileWriter(filename)) {
                file.write(jsonArray.toString(4)); // 缩进为4个空格
                System.out.println("[+] Successfully saved JSON data to " + filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    // 动态创建子菜单
    public static void addMenuItemsFromFile(JMenu pluginMenu,JTabbedPane tabbedPane) {
        EventQueue.invokeLater(() -> {
            Path file = Paths.get("./plugins/AllPlugins.txt");

            if (!Files.exists(file)) {
                try {
                    Files.createDirectories(file.getParent());
                    Files.createFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try (BufferedReader reader = Files.newBufferedReader(file)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        // 创建插件的菜单项
                        JMenu submenu = new JMenu(parts[0]);

                        // 为插件添加"运行"、"设置"、"关于"三个选项
                        JMenuItem runItem = new JMenuItem("运行");
                        JMenuItem settingItem = new JMenuItem("设置");
                        JMenuItem aboutItem = new JMenuItem("关于");

                        // 对菜单项添加相应的事件处理器
                        runItem.addActionListener(event -> {
                            addPluginTab(tabbedPane, parts[0]);
                            addPluginFrame(parts[0]);
                        });
                        settingItem.addActionListener(event -> {
                            // 这里添加设置的事件处理代码
                        });
                        aboutItem.addActionListener(event -> {
                            // 这里添加关于的事件处理代码
                        });

                        // 把菜单项添加到子菜单中
                        submenu.add(runItem);
                        submenu.add(settingItem);
                        submenu.add(aboutItem);

                        // 把子菜单添加到主菜单中
                        pluginMenu.add(submenu);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    // 点击运行新增 tab 页
    public static void addPluginTab(JTabbedPane tabbedPane, String pluginName) {
        //ActionListener在选择“关闭”时关闭标签页
        ActionListener closeListener = event -> tabbedPane.removeTabAt(tabbedPane.indexOfTab(pluginName));

        JPanel panel1 = new JPanel(); //创建面板
        JPanel panel2 = new JPanel(); //创建面板
        panel1.add(addBanner(pluginName), BorderLayout.NORTH);
        panel2.add(new JScrollPane(createTable()), BorderLayout.CENTER); //添加表格到中心

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(panel1);
        mainPanel.add(panel2);

        //为标签添加了一个鼠标监听器，显示弹出菜单以关闭标签页
        mainPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem closeItem = new JMenuItem("关闭");
                    closeItem.addActionListener(closeListener);
                    menu.add(closeItem);
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        //添加标签页
        tabbedPane.addTab(pluginName, mainPanel);
    }

    public static void addPluginFrame(String frameName) {
        // 建立新的窗口Frame
        JFrame newFrame = new JFrame(frameName);
        newFrame.setSize(300, 200);  // 设置窗口大小
        newFrame.setLocationRelativeTo(null); // 窗口位置居中
        newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 关闭窗口时只关闭当前窗口，不影响其他窗口

        // 创建面板Panel，并添加按钮Button
        JPanel panel = new JPanel();
        JButton execButton = new JButton("执行");
        execButton.setFocusPainted(false); // 取消焦点边框的绘制
        execButton.setFocusable(false);

        // 向按钮添加事件处理器
        execButton.addActionListener(e -> {
            // 这里添加按钮动作，下面代码仅作示例，实际操作需按需修改
            System.out.println("执行按钮已按下");
        });

        panel.add(execButton);

        // 把面板添加到窗口中
        newFrame.add(panel);

        // 显示窗口
        newFrame.setVisible(true);
    }

    // 用与保存 table 数据
    private static JScrollPane findScrollPane(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JScrollPane) {
                return (JScrollPane) comp;
            } else if (comp instanceof Container) {
                JScrollPane scrollPane = findScrollPane((Container) comp);
                if (scrollPane != null) {
                    return scrollPane;
                }
            }
        }
        return null;
    }
    public static String localCurrentTab(JTabbedPane tabbedPane){
        int selectedIndex = tabbedPane.getSelectedIndex();
        String tabTitle ="";
        if (selectedIndex != -1) {
            tabTitle = tabbedPane.getTitleAt(selectedIndex);
            System.out.println("当前标签："+tabTitle);
        }
        return tabTitle;
    }
    public static void switchTab(JTabbedPane tabbedPane, String tabName){
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(tabName)) {
                // 找到后切换到该标签
                tabbedPane.setSelectedIndex(i);
                break;
            }
        }
    }
    public static void main(String[] args) {
    }
}
