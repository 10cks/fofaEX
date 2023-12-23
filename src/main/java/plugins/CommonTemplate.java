package plugins;

import com.google.gson.Gson;
import net.dongliu.commons.Sys;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static tableInit.GetjTableHeader.adjustColumnWidths;
import static tableInit.GetjTableHeader.getjTableHeader;

public class CommonTemplate {

    private static String pluginName = "";
    private static String allPluginsPath = "./plugins/";

    // 动态新增 tab 页
    public static void addTabbedPaneFromFile(JTabbedPane tabbedPane) {
        EventQueue.invokeLater(() -> {
            //读取文件并为每一行创建一个新的标签页
            Path file = Paths.get(allPluginsPath + "AllPlugins.json");

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
        String[] columnNames = {"Current No Data", " "};
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
    public static void addMenuItemsFromFile(JMenu pluginMenu, JTabbedPane tabbedPane) {
        EventQueue.invokeLater(() -> {
            Path file = Paths.get(allPluginsPath + "AllPlugins.json");

            if (!Files.exists(file)) {  // 如果不存在这个文件，则新建这个文件
                try {
                    Files.createDirectories(file.getParent());
                    Files.createFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                // Create a new Gson
                Gson gson = new Gson();

                if (Files.size(file) == 0) {
                    System.out.println("[!] 当前无第三方插件");
                    return;
                }

                // Read the JSON from the file into a Map
                Map<String, Boolean> plugins = gson.fromJson(
                        new FileReader(file.toFile()), Map.class);

                // Process each plugin
                for (Map.Entry<String, Boolean> plugin : plugins.entrySet()) {
                    // Only process plugins that are enabled
                    if (plugin.getValue()) {

                        // 检查对应的插件的文件夹和json文件是否都存在
                        String pluginFolderPath = allPluginsPath + plugin.getKey();
                        String pluginJsonPath = pluginFolderPath + "/" + plugin.getKey() + "Setting.json";

                        if (!Files.exists(Paths.get(pluginFolderPath))) {
                            System.out.println("[-] 当前插件 " + plugin.getKey() + " 缺失文件夹：" + pluginFolderPath);
                            continue;
                        }

                        if (!Files.exists(Paths.get(pluginJsonPath))) {
                            System.out.println("[-] 当前插件 " + plugin.getKey() + " 缺失文件：" + pluginJsonPath);
                            continue;
                        }

                        // 创建插件的菜单项
                        JMenu submenu = new JMenu(plugin.getKey());

                        System.out.println("[+] 当前插件 " + plugin.getKey() + " 已加载");

                        // 为插件添加"运行"、"设置"、"关于"三个选项
                        JMenuItem runItem = new JMenuItem("运行");
                        JMenuItem settingItem = new JMenuItem("设置");
                        JMenuItem aboutItem = new JMenuItem("关于");

                        // 对菜单项添加相应的事件处理器
                        runItem.addActionListener(event -> {
                            addPluginFrame(plugin.getKey()); // 弹出运行面板
                            // addPluginTab(tabbedPane, plugin.getKey()); // 新增标签
                        });
                        settingItem.addActionListener(event -> {
                            // 这里添加设置的事件处理代码
                            addPluginsSettingOpen(pluginJsonPath);
                        });
                        aboutItem.addActionListener(event -> {
                            // 这里添加关于的事件处理代码
                            addPluginAbout(pluginJsonPath);
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

    // 新增插件“运行”功能面板
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

    // 新增插件“关于”
    public static void addPluginAbout(String pluginJsonPath) {

        try {
            Gson gson = new Gson();
            Map<String, Object> settingMap = gson.fromJson(
                    new FileReader(pluginJsonPath), Map.class);

            // 获取 "关于" 相关的信息
            Map<String, String> aboutMap = (Map<String, String>) settingMap.get("About");
            String project = aboutMap.get("Project");
            String address = aboutMap.get("Address");
            String author = aboutMap.get("Author");
            String version = aboutMap.get("Version");
            String update = aboutMap.get("Update");

            JEditorPane editorPane = new JEditorPane("text/html", "");
            editorPane.setText(
                    "<html><body>" +
                            "<b>Project: " + project + "</b><br>" +
                            "Address: <a href='" + address + "'>" + address + "</a><br>" +
                            "Author: " + author + "<br>" +
                            "Version: " + version + "<br>" +
                            "Update: " + update +
                            "</body></html>"
            );
            editorPane.setEditable(false);
            editorPane.setOpaque(false);
            editorPane.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent evt) {
                    if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        try {
                            Desktop.getDesktop().browse(evt.getURL().toURI());
                        } catch (IOException | URISyntaxException ex) {
                            JOptionPane.showMessageDialog(null,
                                    "无法打开链接，错误: " + ex.getMessage(),
                                    "错误",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            // 弹出一个包含JEditorPane的消息对话框
            JOptionPane.showMessageDialog(null, new JScrollPane(editorPane),
                    "关于项目", JOptionPane.PLAIN_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // 打开该插件设置文件
    public static void addPluginsSettingOpen(String pluginJsonPath){
        File fofaHackSettingsFile = new File(pluginJsonPath);
        if (fofaHackSettingsFile.exists()) {
            // 如果文件存在，使用notepad打开它
            try {
                new ProcessBuilder("notepad", pluginJsonPath).start();
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "无法打开配置文件！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // 如果文件不存在，显示弹窗
            JOptionPane.showMessageDialog(null, "未获取到配置文件！", "错误", JOptionPane.ERROR_MESSAGE);
        }
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

    public static String localCurrentTab(JTabbedPane tabbedPane) {
        int selectedIndex = tabbedPane.getSelectedIndex();
        String tabTitle = "";
        if (selectedIndex != -1) {
            tabTitle = tabbedPane.getTitleAt(selectedIndex);
            System.out.println("当前标签：" + tabTitle);
        }
        return tabTitle;
    }

    public static void switchTab(JTabbedPane tabbedPane, String tabName) {
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
