package plugins;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.awt.BorderLayout.CENTER;
import static tableInit.GetjTableHeader.adjustColumnWidths;
import static tableInit.GetjTableHeader.getjTableHeader;

public class CommonTemplate {

    private static String pluginName = "";
    private static String allPluginsPath = "./plugins/";

    private static Process runningProcess = null;
    static AtomicBoolean wasManuallyStopped = new AtomicBoolean(false);

    public static JLabel addBanner(String banner) {
        JLabel labelIcon = new JLabel(banner);
        labelIcon.setForeground(new Color(48, 49, 52));
        Font iconFont = new Font("Times New Roman", Font.BOLD, 60);
        labelIcon.setFont(iconFont);
        return labelIcon;
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

    // 保存 table 核心代码
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
            String filename = "coredata/" + tabName + ".json";
            try (FileWriter file = new FileWriter(filename)) {
                file.write(jsonArray.toString(4)); // 缩进为4个空格
                System.out.println("[+] Successfully saved JSON data to " + filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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

    // 动态创建子菜单，核心代码
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
                            addPluginFrame(pluginJsonPath, plugin.getKey(), tabbedPane); // 弹出运行面板
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
    public static void addPluginTab(JTabbedPane tabbedPane, String pluginName, String pluginJsonPath) {
        // 检查标签是否已经存在
        if (tabbedPane.indexOfTab(pluginName) != -1) {
            int existingTabIndex = tabbedPane.indexOfTab(pluginName);
            tabbedPane.removeTabAt(existingTabIndex);
        }
        //ActionListener在选择“关闭”时关闭标签页
        ActionListener closeListener = event -> tabbedPane.removeTabAt(tabbedPane.indexOfTab(pluginName));

        JPanel panel1 = new JPanel(); //创建面板
        JPanel panel2 = new JPanel(); //创建面板
        panel1.add(addBanner(pluginName), BorderLayout.NORTH);
        // 创建表格
        JTable table = createTableFromJson(pluginJsonPath);

        // 重新设置表格头，以便新的渲染器生效
        JTableHeader header = getjTableHeader(table);
        table.setTableHeader(header);
        adjustColumnWidths(table); // 自动调整列宽
        JScrollPane scrollPane = new JScrollPane(table);
        table.setRowHeight(24); // 设置表格的行高
        table.setFillsViewportHeight(true);
        adjustColumnWidths(table);
        panel2.add(scrollPane, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        mainPanel.add(panel1);
        mainPanel.add(panel2);

        mainPanel.removeAll();
        mainPanel.add(scrollPane, CENTER);

        mainPanel.revalidate();
        mainPanel.repaint();


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
    public static JTable createTableFromJson(String configFilePath) {
        // 创建Gson实例
        Gson gson = new Gson();
        String outputFilePath;
        String[] columnNames;

        // 读取并解析配置文件
        try {
            JsonReader configReader = new JsonReader(new FileReader(configFilePath));
            JsonObject configObject = gson.fromJson(configReader, JsonObject.class);
            JsonObject runObject = configObject.getAsJsonObject("Run");

            // 解析OutputFile路径以及OutputTarget列名称
            outputFilePath = runObject.get("OutputFile").getAsString();
            JsonArray outputTarget = runObject.getAsJsonArray("OutputTarget");
            columnNames = new String[outputTarget.size()];
            for (int i = 0; i < outputTarget.size(); i++) {
                columnNames[i] = outputTarget.get(i).getAsString();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        // 从OutputFile文件路径读取内容，并创建表格模型
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        try {
            Reader outputReader = new FileReader(outputFilePath);
            JsonStreamParser parser = new JsonStreamParser(outputReader);

            while (parser.hasNext()) {
                JsonObject object = parser.next().getAsJsonObject();
                Vector<String> row = new Vector<String>();
                for (int i = 0; i < columnNames.length; i++) {
                    if (object.has(columnNames[i])) {
                        JsonElement element = object.get(columnNames[i]);
                        row.add(element.isJsonNull() ? "" : element.getAsString());
                    } else {
                        row.add(""); // 对应的列值为空
                    }
                }
                model.addRow(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 使用模型创建表格
        JTable table = new JTable(model);
        return table;
    }

    // 新增插件“运行”功能面板
    public static void addPluginFrame(String pluginJsonPath, String frameName, JTabbedPane tabbedPane) {
        // 建立新的窗口Frame
        JFrame newFrame = new JFrame(frameName);
        newFrame.setSize(800, 600);  // 改变窗口大小，使其能容纳更多文本
        newFrame.setLocationRelativeTo(null); // 窗口位置居中
        newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 关闭窗口时只关闭当前窗口，不影响其他窗口

        // 创建使用BorderLayout的面板
        JPanel panel = new JPanel(new BorderLayout());

        // 创建结果显示区域，并添加到面板的Center区域
        JTextArea resultArea = new JTextArea();
        resultArea.setLineWrap(true);  // 设置行包装
        resultArea.setWrapStyleWord(true);  // 设置单词包装
        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        // 创建新的面板用于放置按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 使用FlowLayout并且指定按钮靠左对齐
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // 设置边距为10px
        // 创建执行按钮，并添加到新面板
        JButton execButton = new JButton("执行");
        execButton.setFocusPainted(false); // 取消焦点边框的绘制
        execButton.setFocusable(false);
        buttonPanel.add(execButton);

        // 创建“停止”按钮
        JButton stopButton = new JButton("停止");
        stopButton.setFocusPainted(false);
        stopButton.setFocusable(false);
        buttonPanel.add(stopButton);

        // 把新面板添加到BorderLayout的North区域
        panel.add(buttonPanel, BorderLayout.NORTH);

        // 把面板添加到窗口中
        newFrame.add(panel);
        // 显示窗口
        newFrame.setVisible(true);

        // 运行按钮添加事件
        execButton.addActionListener(e -> {
            // 这里添加按钮动作，实际操作需按需修改
            String command = constructCommandFromJson(pluginJsonPath);
            // 清屏
            resultArea.setText("");
            if (command != null) {
                try {
                    // 解析设置文件，运行InputTarget部分
                    parseJsonAndWriteFile(pluginJsonPath);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                executeCommand(command, resultArea, tabbedPane, frameName, pluginJsonPath); // 执行命令并显示结果

            } else {
                JOptionPane.showMessageDialog(null, "无法构造命令", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        // 停止按钮
        stopButton.addActionListener(e -> {
            if (runningProcess != null) {
                wasManuallyStopped.set(true);  // 设置被手动停止的标志
                runningProcess.destroy();
                runningProcess = null;
            }
        });

        // 把面板添加到窗口中
        newFrame.add(panel);
        // 显示窗口
        newFrame.setVisible(true);
    }

    private static String constructCommandFromJson(String pluginJsonPath) {
        Gson gson = new Gson();
        try {
            Map<String, Object> jsonMap = gson.fromJson(new FileReader(pluginJsonPath), Map.class);
            Map<String, Object> runMap = (Map<String, Object>) jsonMap.get("Run");
            String path = (String) runMap.get("Path");
            Map<String, String> paramsMap = (Map<String, String>) runMap.get("Params");

            StringBuilder commandBuilder = new StringBuilder(path);
            for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                commandBuilder.append(" ").append(entry.getKey()).append(" ").append(entry.getValue());
            }
            return commandBuilder.toString();

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
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
    public static void addPluginsSettingOpen(String pluginJsonPath) {
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

    // 文件流操作：去空保存，完成 InputFile InputTarget 部分
    private static void parseJsonAndWriteFile(String jsonFilePath) throws IOException {
        Gson gson = new Gson();

        Map<String, Object> jsonMap;
        try (Reader reader = new FileReader(jsonFilePath)) {
            jsonMap = gson.fromJson(reader, Map.class);
        }
        if (jsonMap == null) {
            return;
        }
        Map<String, Object> runMap = (Map<String, Object>) jsonMap.get("Run");
        Map<String, String> paramsMap = (Map<String, String>) runMap.get("Params");

        String selectParam = ((Map<String, String>) runMap.get("InputTarget")).get("selectParam");
        String inputFile = (String) runMap.get("InputFile");
        String selectColumn = ((Map<String, String>) runMap.get("InputTarget")).get("selectColumn");

        List<LinkedHashMap<String, Object>> records;
        try (Reader reader = new FileReader(inputFile)) {
            records = gson.fromJson(reader, List.class);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(paramsMap.get(selectParam)))) {
            for (Map<String, Object> record : records) {
                String url = (String) record.get(selectColumn);
                if (url != null && !url.isEmpty()) {
                    writer.write(url);
                    writer.newLine();
                }
            }
        }
    }

    static void executeCommand(String command, JTextArea resultArea, JTabbedPane tabbedPane, String tabName, String pluginJsonPath) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command.split("\\s+"));
            builder.redirectErrorStream(true);
            runningProcess = builder.start();
            runningProcess.getOutputStream().close();

            Thread outputThread = new Thread(() -> {
                Process currentProcess = runningProcess; // 创建Process的局部副本
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(currentProcess.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String lineCopy = line;
                        SwingUtilities.invokeLater(() -> {
                            resultArea.append(lineCopy + "\n");
                        });
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> {
                        resultArea.append("Error reading output: " + e.getMessage() + "\n");
                    });
                } finally {
                    try {
                        currentProcess.waitFor(); // 使用局部副本而不是runningProcess
                    } catch (InterruptedException ex) {
                        SwingUtilities.invokeLater(() -> {
                            resultArea.append("Process was interrupted: " + ex.getMessage() + "\n");
                        });
                    }
                    SwingUtilities.invokeLater(() -> {
                        if (wasManuallyStopped.get()) {
                            resultArea.append("\n程序被手动停止\n");
                        } else {
                            resultArea.append("\n程序运行结束\n");
                            addPluginTab(tabbedPane, tabName, pluginJsonPath); // 新增标签
                            switchTab(tabbedPane, tabName);
                        }
                        wasManuallyStopped.set(false); // 重置状态
                    });
                }
            });

            outputThread.start();

        } catch (Exception ex) {
            resultArea.setText("Error executing command: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Execution failed", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
    }
}
