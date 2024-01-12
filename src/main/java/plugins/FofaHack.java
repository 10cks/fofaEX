package plugins;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map;

import com.google.gson.*;

import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import tableInit.SelectedCellBorderHighlighter;

import static tableInit.GetjTableHeader.adjustColumnWidths;
import static tableInit.GetjTableHeader.getjTableHeader;

public class FofaHack {
    public static boolean exportButtonAdded;
    private Process process;
    public static JPanel panel = new JPanel(); // 主面板
    public static JPanel exportPanel = new JPanel(); // 主面板
    public static JTable table = new JTable(); // 表格
    public static JLabel rowCountLabel = new JLabel();
    private PrintWriter writer;
    private static JFrame frame; // 使frame成为类的成员变量，以便可以在任意地方访问

    public void fofaHack() {
        // 创建基础的窗口框架
        frame = new JFrame("Fofa Hack");
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null); // 居中
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 插件关闭主程序不关闭
        frame.setLayout(new BorderLayout());

        // 输入面板
        JPanel panel = new JPanel();
        JTextField commandField = new JTextField(50);
        JTextField endcountField = new JTextField(10);
        JButton executeButton = new JButton("搜索");
        JButton chooseFileButton = new JButton("选择程序");

        chooseFileButton.setFocusPainted(false);
        chooseFileButton.setFocusable(false);
        executeButton.setFocusPainted(false); // 添加这一行来取消焦点边框的绘制
        executeButton.setFocusable(false);  // 禁止了按钮获取焦点，因此按钮不会在被点击后显示为"激活"或"选中"的状态

        JTextArea resultArea = new JTextArea(10, 50);

        // 设置断行不断字
        resultArea.setWrapStyleWord(false);

        panel.add(new JLabel("查询语法："));
        panel.add(commandField);
        panel.add(new JLabel("查询数量："));
        panel.add(endcountField);
        panel.add(executeButton);
        //panel.add(chooseFileButton);

        // 结果滚动面板
        JScrollPane scrollPane = new JScrollPane(resultArea);
        resultArea.setEditable(false);

        // 添加面板和滚动面板到窗口框架
        frame.add(panel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // 执行按钮点击事件
        executeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 清空文本区域
                resultArea.setText("");
                String command = commandField.getText().trim();
                String endcount = endcountField.getText().trim();
                executeCommand(command, endcount, resultArea);
            }
        });

        // 选择文件按钮事件
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(".\\plugins")); // 设置默认目录

                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    commandField.setText("\"" + selectedFile.getAbsolutePath() + "\""); // 将选定的文件路径放入命令字段
                }
            }
        });

        // 显示窗口
        frame.setVisible(true);
    }

    private void executeCommand(String searchStr, String endcount, JTextArea resultArea) {
        try {

            // 从配置文件中读取设置
            Properties properties = new Properties();
            properties.load(new FileInputStream("./plugins/fofahack/FofaHackSetting.txt"));

            String path = properties.getProperty("Path").replace("\"", ""); // 移除引号
            String level = properties.getProperty("level").trim();
            String outputname = properties.getProperty("outputname").trim();

            // 构建 plugins/data 的文件路径对象
            File dataDirectory = new File("./plugins/fofahack/data");

            // 检查该路径表示的目录是否存在
            if (!dataDirectory.exists()) {
                // 不存在，则尝试创建该目录
                boolean wasSuccessful = dataDirectory.mkdirs(); // 使用 mkdirs 而不是 mkdir 以创建任何必需的父目录

                if (wasSuccessful) {
                    System.out.println("[+] FofaHack: The 'data' directory was created successfully.");
                } else {
                    System.out.println("Failed to create the 'data' directory.");
                }
            }

            // 构建完整的命令
            String command = String.format("%s -k %s -on %s -e %s -l %s -o json", path, searchStr, outputname, endcount, level);

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
                        try {
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


                            }else{
                                JOptionPane.showMessageDialog(null, "读取数据失败", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
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

    public static boolean checkMakeFile() throws IOException {

        // 从配置文件中读取设置
        Properties properties = new Properties();
        properties.load(new FileInputStream("./plugins/fofahack/FofaHackSetting.txt"));

        String finalname = properties.getProperty("finalname").trim();
        finalname = finalname.replace("\"", "");
        // 在这里检查 final 文件是否存在
        System.out.println(finalname);
        File file = new File(finalname);
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
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            Vector<String> columnNames = null;
            Vector<Vector<Object>> dataVector = new Vector<>();

            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                Map<String, Object> map = new LinkedHashMap<>();
                Vector<Object> row = new Vector<>();

                if (columnNames == null) {
                    columnNames = new Vector<>();
                    for (String key : jsonObject.keySet()) {
                        columnNames.add(key);
                    }
                }

                for (String columnName : columnNames) {
                    if (jsonObject.get(columnName) != null) {
                        map.put(columnName, jsonObject.get(columnName).getAsString());
                        row.add(jsonObject.get(columnName).getAsString());
                    } else {
                        map.put(columnName, null);
                        row.add(null);
                    }
                }
                dataVector.add(row);
            }

            DefaultTableModel model = new DefaultTableModel(dataVector, columnNames);
            table.setModel(model);

            // 创建一个TableRowSorter并将其设置为表格：排序功能
            TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

        } catch (IOException | JsonSyntaxException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Execute Failed!", "JSON Error", JOptionPane.ERROR_MESSAGE);
        }

        // frame.dispose(); // 关闭插件窗口
        // 更新panel和rowCountLabel
        panel.revalidate();
        panel.repaint();
        rowCountLabel.setText("Total Rows: " + table.getRowCount() + " ");
    }

    public static void exportTableToExcel(JTable table) {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // 创建一个包含整个表的工作表
        XSSFSheet completeSheet = workbook.createSheet("Complete Table");

        // 创建表头
        XSSFRow headerRow = completeSheet.createRow(0);
        for (int i = 0; i < table.getColumnCount(); i++) {
            headerRow.createCell(i).setCellValue(table.getColumnName(i));
        }

        // 写入整个表的数据
        for (int i = 0; i < table.getRowCount(); i++) {
            XSSFRow dataRow = completeSheet.createRow(i + 1);
            for (int j = 0; j < table.getColumnCount(); j++) {
                Object value = table.getValueAt(i, j);
                String text = (value == null) ? "" : value.toString();
                dataRow.createCell(j).setCellValue(text);
            }
        }

        // 为每一列创建单独的工作表并写入非空数据
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
            XSSFSheet sheet = workbook.createSheet(sanitizeSheetName(table.getColumnName(columnIndex)));

            // 创建表头
            XSSFRow columnHeaderRow = sheet.createRow(0);
            columnHeaderRow.createCell(0).setCellValue(table.getColumnName(columnIndex));

            // 写入该列的非空数据
            int dataRowIndex = 1;
            for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
                Object value = table.getValueAt(rowIndex, columnIndex);
                if (value != null) {
                    XSSFRow dataRow = sheet.createRow(dataRowIndex++);
                    dataRow.createCell(0).setCellValue(value.toString());
                }
            }
        }

        // 将工作簿保存到文件
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = dateFormat.format(new Date());

            String directoryName = "exportdata";
            File directory = new File(directoryName);

            if (!directory.exists()) {
                directory.mkdirs(); // Use mkdirs() instead of mkdir() to create any necessary parent directories
            }

            String fileName = directoryName + "/TableData_" + timestamp + ".xlsx";
            FileOutputStream output = new FileOutputStream(fileName);
            workbook.write(output);
            output.close(); // Close the file output stream
            workbook.close(); // Close the workbook
            JOptionPane.showMessageDialog(null, "Export successful!\nFile saved at: " + new File(fileName).getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper method to sanitize sheet names as per Excel sheet naming rules
    private static String sanitizeSheetName(String name) {
        return name.replaceAll("[\\\\*\\[\\]?/]", "");
    }


    public static void main() {
        // 创建类的实例并调用方法
        FofaHack plugin = new FofaHack();
        plugin.fofaHack();
    }
}