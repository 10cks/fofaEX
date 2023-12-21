package plugins;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
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
    public static void addTabbedPane(JTabbedPane tabbedPane) {
        EventQueue.invokeLater(() -> {
            // 读取文件并为每一行创建一个新的标签页
            Path file = Paths.get("./plugins/AllPlugins.txt");
            try (BufferedReader reader = Files.newBufferedReader(file)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        pluginName = parts[0]; // 获取插件名称
                        // 以下是根据插件名称创建组件
                        JPanel panel1 = new JPanel(); // 创建面板
                        JPanel panel2 = new JPanel(); // 创建面板
                        panel1.add(addBanner(pluginName), BorderLayout.NORTH);
                        panel2.add(new JScrollPane(createTable()), BorderLayout.CENTER); // 添加表格到中心

                        JPanel mainPanel = new JPanel();
                        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
                        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                        mainPanel.add(panel1);
                        mainPanel.add(panel2);

                        tabbedPane.addTab(pluginName, mainPanel); // 添加标签页
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

    public static void main(String[] args) {
    }
}
