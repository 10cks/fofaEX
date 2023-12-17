package plugins;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Vector;

import tableInit.SelectedCellBorderHighlighter;

import static java.awt.BorderLayout.CENTER;
import static tableInit.GetjTableHeader.adjustColumnWidths;
import static tableInit.GetjTableHeader.getjTableHeader;

public class FofaPlugin {

    private Process process;
    private PrintWriter writer;

    public void fofaPlugin() {
        // 创建基础的窗口框架
        JFrame frame = new JFrame("Fofa Plugin");
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null); // 居中
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 插件关闭主程序不关闭
        frame.setLayout(new BorderLayout());

        // 输入面板
        JPanel panel = new JPanel();
        JTextField commandField = new JTextField(50);
        JButton executeButton = new JButton("Execute");
        JTextArea resultArea = new JTextArea(10, 50);

        // 设置自动换行
        //resultArea.setLineWrap(true);
        // 设置断行不断字
        resultArea.setWrapStyleWord(false);

        panel.add(new JLabel("Command:"));
        panel.add(commandField);
        panel.add(executeButton);

        // 结果滚动面板
        JScrollPane scrollPane = new JScrollPane(resultArea);
        resultArea.setEditable(false);

        // 添加面板和滚动面板到窗口框架
        frame.add(panel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // 按钮点击事件
        executeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 清空文本区域
                resultArea.setText("");
                String command = commandField.getText().trim();
                executeCommand(command, resultArea);
            }
        });

        // 显示窗口
        frame.setVisible(true);
    }

    private void executeCommand(String command, JTextArea resultArea) {
        try {
            // 使用 ProcessBuilder 替代 Runtime.exec
            ProcessBuilder builder = new ProcessBuilder(command.split("\\s+"));
            builder.redirectErrorStream(true); // 合并标准错误和标准输出
            process = builder.start();
            // writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8), true);
            Charset systemCharset = Charset.defaultCharset(); // 获取系统默认字符集
            writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), systemCharset), true);
            // 处理输出流
            Thread outputThread = new Thread(() -> {
//                try (BufferedReader reader = new BufferedReader(
//                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), systemCharset))) {
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
                        process.waitFor();
                    } catch (InterruptedException ex) {
                        SwingUtilities.invokeLater(() -> {
                            resultArea.append("Process was interrupted: " + ex.getMessage() + "\n");
                        });
                    }
                    SwingUtilities.invokeLater(() -> {
                        resultArea.append("\n程序运行结束\n");
                    });
                }
            });

            outputThread.start();

        } catch (Exception ex) {
            resultArea.setText("Error executing command: " + ex.getMessage());
        }
    }

    public static void loadFileIntoTable(File file,JPanel panel, JTable table) {

        // 重新设置表格头，以便新的渲染器生效
        JTableHeader header = getjTableHeader(table);
        table.setTableHeader(header);

        adjustColumnWidths(table); // 自动调整列宽
        JScrollPane scrollPane = new JScrollPane(table);

        table.setRowHeight(24); // 设置表格的行高
        table.setFillsViewportHeight(true);

        panel.removeAll();
        panel.add(scrollPane, CENTER);
        panel.revalidate();
        panel.repaint();

        // 设置表格的默认渲染器
        table.setDefaultRenderer(Object.class, new SelectedCellBorderHighlighter());

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            List<Map<String, Object>> data = new ArrayList<>();
            Vector<String> columnNames = new Vector<>();
            boolean columnsDefined = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    JsonObject jsonObject = JsonParser.parseString(line).getAsJsonObject();
                    Map<String, Object> map = new LinkedHashMap<>();

                    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue().getAsString();
                        map.put(key, value);

                        if (!columnsDefined) {
                            columnNames.add(key);
                        }
                    }
                    data.add(map);
                    columnsDefined = true;
                }
            }
            reader.close();

            Vector<Vector<Object>> dataVector = new Vector<>();
            for (Map<String, Object> datum : data) {
                Vector<Object> row = new Vector<>();
                for (String columnName : columnNames) {
                    row.add(datum.get(columnName));
                }
                dataVector.add(row);
            }

            DefaultTableModel model = new DefaultTableModel(dataVector, columnNames);
            table.setModel(model);

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "File reading error", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main() {
        // 创建类的实例并调用方法
        FofaPlugin plugin = new FofaPlugin();
        plugin.fofaPlugin();
    }
}