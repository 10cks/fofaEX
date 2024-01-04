package plugins;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
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

import static tableInit.GetjTableHeader.adjustColumnWidths;
import static tableInit.GetjTableHeader.getjTableHeader;

public class CommonExecute {
    public static boolean exportButtonAdded;
    private static Process process;
    public static JPanel panel = new JPanel(); // 主面板
    public static JPanel exportPanel = new JPanel(); // 主面板
    public static JTable table = new JTable(); // 表格
    public static JLabel rowCountLabel = new JLabel();
    private static PrintWriter writer;
    private static JFrame frame; // 使frame成为类的成员变量，以便可以在任意地方访问
    private static Process runningProcess = null;
    static int executeCommand(String command, JTextArea resultArea) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command.split("\\s+"));
            builder.redirectErrorStream(true);
            runningProcess = builder.start();
            runningProcess.getOutputStream().close();

            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(runningProcess.getInputStream(), StandardCharsets.UTF_8))) {
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
                        runningProcess.waitFor();
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
            JOptionPane.showMessageDialog(null, "Execution failed", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return 0;
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
            final File directory = new File(directoryName);

            if (!directory.exists()) {
                directory.mkdir();
            }

            String fileName = directoryName + "/TableData_" + timestamp + ".xlsx";
            FileOutputStream output = new FileOutputStream(fileName);

            workbook.write(output);
            workbook.close();
            output.close();

            final JDialog dialog = new JDialog();
            dialog.setTitle("Export Successful");
            dialog.setLayout(new BorderLayout());

            JLabel label = new JLabel("File saved at: " +
                    new File(fileName).getAbsolutePath());
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());

            JButton openDirectoryButton = new JButton("打开目录");
            openDirectoryButton.setFocusPainted(false);
            openDirectoryButton.setFocusable(false);
            openDirectoryButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().open(directory);
                        dialog.dispose();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            JButton okButton = new JButton("确定");
            okButton.setFocusPainted(false);
            okButton.setFocusable(false);
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                }
            });

            panel.add(openDirectoryButton);
            panel.add(okButton);

            dialog.add(label, BorderLayout.CENTER);
            dialog.add(panel, BorderLayout.SOUTH);
            dialog.pack();
            dialog.setLocationRelativeTo(null);  // Center dialog
            dialog.setVisible(true);
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