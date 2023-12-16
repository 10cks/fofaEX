package plugins;

import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Map;

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

    public static void loadFileIntoTable(File file, JTable table) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            List<Map<String, String>> data = new ArrayList<>();

            // Read the file line by line and parse each line as a JSON object
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    JSONObject jsonObject = new JSONObject(line);
                    Map<String, String> map = new LinkedHashMap<>();
                    jsonObject.keys().forEachRemaining(key -> {
                        map.put(key, jsonObject.get(key).toString());
                    });
                    data.add(map);
                }
            }
            reader.close();

            // Assuming all json objects have the same keys, get column names from the first object
            // Ensure column names are in the order they were in JSON
            Vector<String> columnNames = new Vector<>();
            if (!data.isEmpty()) {
                columnNames.addAll(data.get(0).keySet());
            }

            // Prepare data for the table model
            Vector<Vector<String>> dataVector = new Vector<>();
            for (Map<String, String> datum : data) {
                Vector<String> row = new Vector<>();
                for (String columnName : columnNames) {
                    row.add(datum.get(columnName));
                }
                dataVector.add(row);
            }

            // Set the model for the table
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