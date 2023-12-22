package plugins;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Vector;

import com.google.gson.JsonSyntaxException;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import tableInit.SelectedCellBorderHighlighter;

import static java.awt.BorderLayout.CENTER;
import static tableInit.GetjTableHeader.adjustColumnWidths;
import static tableInit.GetjTableHeader.getjTableHeader;

public class CommonExecute {
    public static boolean exportButtonAdded;
    private Process process;
    public static JPanel panel = new JPanel(); // 主面板
    public static JPanel exportPanel = new JPanel(); // 主面板
    public static JTable table = new JTable(); // 表格
    public static JLabel rowCountLabel = new JLabel();
    private PrintWriter writer;
    private static JFrame frame; // 使frame成为类的成员变量，以便可以在任意地方访问

    public void fofaPlugin() {
        // 创建基础的窗口框架
        frame = new JFrame("Fofa Hack");
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null); // 居中
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 插件关闭主程序不关闭
        frame.setLayout(new BorderLayout());

        // 输入面板
        JPanel panel = new JPanel();
        JTextField commandField = new JTextField(50);
        JButton executeButton = new JButton("执行");
        executeButton.setFocusPainted(false); // 添加这一行来取消焦点边框的绘制
        executeButton.setFocusable(false);  // 禁止了按钮获取焦点，因此按钮不会在被点击后显示为"激活"或"选中"的状态

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
            ProcessBuilder builder = new ProcessBuilder(command.split("\\s+"));
            builder.redirectErrorStream(true); // 合并标准错误和标准输出
            process = builder.start();
            writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8), true);
            // 处理输出流
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
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
                        if (checkMakeFile()) {
                            System.out.println("[*] FofaHack running success.");
                            // 导出表格
                            JButton exportButton = new JButton("Export to Excel");
                            exportButton.setFocusPainted(false); // 添加这一行来取消焦点边框的绘制
                            exportButton.setFocusable(false);  // 禁止了按钮获取焦点，因此按钮不会在被点击后显示为"激活"或"选中"的状态


                            if (!exportButtonAdded) {
                                exportPanel.add(exportButton);
                                exportButtonAdded = true;
                            }
                            exportButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    // 在这里检查 table 是否被初始化
                                    if (table == null) {
                                        JOptionPane.showMessageDialog(null, "表格没有被初始化");
                                        return;
                                    }
                                    // 检查 table 是否有模型和数据
                                    if (table.getModel() == null || table.getModel().getRowCount() <= 0) {
                                        JOptionPane.showMessageDialog(null, "当前无数据");
                                        return;
                                    }
                                    exportTableToExcel(table);
                                }
                            });


                        }
                        ;
                    });
                }
            });

            outputThread.start();

        } catch (Exception ex) {
            resultArea.setText("Error executing command: " + ex.getMessage());
            // 在这里弹出“执行失败”的警告窗口
            JOptionPane.showMessageDialog(null, "Execution failed", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static boolean checkMakeFile() {
        // 在这里检查文件是否存在
        File file = new File("test.txt");
        if (file.exists()) {
            loadFileIntoTable(file);

            return true;
        }
        return false;
    }

    public static void loadFileIntoTable(File file) {

        // 重新设置表格头，以便新的渲染器生效
        JTableHeader header = getjTableHeader(table);
        table.setTableHeader(header);

        adjustColumnWidths(table); // 自动调整列宽
        JScrollPane scrollPane = new JScrollPane(table);

        table.setRowHeight(24); // 设置表格的行高
        table.setFillsViewportHeight(true);

        panel.removeAll();
        panel.add(scrollPane, BorderLayout.CENTER);

        // 设置表格的默认渲染器
        table.setDefaultRenderer(Object.class, new SelectedCellBorderHighlighter());

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            List<Map<String, Object>> data = new ArrayList<>();
            Vector<String> columnNames = new Vector<>();
            boolean columnsDefined = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    try {
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
                    }catch (JsonSyntaxException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Execute Failed!", "JSON Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

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
        frame.dispose(); // 关闭插件窗口
        // 更新panel和rowCountLabel
        panel.revalidate();
        panel.repaint();
        rowCountLabel.setText("Total Rows: " + table.getRowCount() + " ");
    }

    public static void exportTableToExcel(JTable table) {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // 第一个工作表保存全部数据
        XSSFSheet allDataSheet = workbook.createSheet("All Data Table");

        // 创建表头
        XSSFRow headerRow = allDataSheet.createRow(0);
        for (int i = 0; i < table.getColumnCount(); i++) {
            headerRow.createCell(i).setCellValue(table.getColumnName(i));
        }

        // 写入数据行到 "All Data Table"
        for (int i = 0; i < table.getRowCount(); i++) {
            XSSFRow dataRow = allDataSheet.createRow(i + 1);
            for (int j = 0; j < table.getColumnCount(); j++) {
                Object value = table.getValueAt(i, j);
                String text = (value == null) ? "" : value.toString(); // 检查是否为null
                dataRow.createCell(j).setCellValue(text);
            }
        }

        // 为每一列创建一个新的Sheet
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
            XSSFSheet sheet = workbook.createSheet(table.getColumnName(columnIndex));
            int rowDataIndex = 0;

            // 写入列头
            XSSFRow row = sheet.createRow(rowDataIndex++);
            row.createCell(0).setCellValue(table.getColumnName(columnIndex));

            // 遍历该列的每一行数据
            for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
                Object value = table.getValueAt(rowIndex, columnIndex);
                if (value != null) {
                    String cellValue = value.toString().trim(); // 去除前后空白字符
                    // 如果处理后的值不为空，写入到单元格
                    if (!cellValue.isEmpty()) {
                        row = sheet.createRow(rowDataIndex++);
                        row.createCell(0).setCellValue(cellValue);
                    }
                }
            }
        }

        // 保存工作簿到文件系统
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = dateFormat.format(new Date());

            String directoryName = "exportdata";
            File directory = new File(directoryName);

            if (!directory.exists()) {
                directory.mkdir();
            }

            String fileName = directoryName + "/TableData_" + timestamp + ".xlsx";
            FileOutputStream output = new FileOutputStream(fileName);
            workbook.write(output);
            workbook.close();
            output.close();
            JOptionPane.showMessageDialog(null, "Export successful!\nFile saved at: " + new File(fileName).getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void main() {
        // 创建类的实例并调用方法
        FofaPlugin plugin = new FofaPlugin();
        plugin.fofaPlugin();
    }
}