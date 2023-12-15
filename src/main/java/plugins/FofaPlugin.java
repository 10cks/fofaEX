package plugins;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

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
        resultArea.setLineWrap(true);
        // 设置断行不断字
        resultArea.setWrapStyleWord(true);

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
            // 启动进程
            process = Runtime.getRuntime().exec(command);
            writer = new PrintWriter(process.getOutputStream());

            // 创建线程来处理输出流，确保使用 UTF-8 编码
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.ISO_8859_1))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String lineCopy = line;
                        SwingUtilities.invokeLater(() -> { // 使用 SwingUtilities.invokeLater 来安全更新 UI
                            resultArea.append(lineCopy + "\n");
                        });
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> {
                        resultArea.append("Error reading output: " + e.getMessage() + "\n");
                    });
                } finally {
                    try {
                        process.waitFor(); // 等待进程结束
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

            // 启动处理输出的线程
            outputThread.start();
        } catch (Exception ex) {
            resultArea.setText("Error executing command: " + ex.getMessage());
        }
    }

    public static void main() {
        // 创建类的实例并调用方法
        FofaPlugin plugin = new FofaPlugin();
        plugin.fofaPlugin();
    }
}