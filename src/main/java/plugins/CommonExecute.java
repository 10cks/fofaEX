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

    public static JTable table = new JTable(); // 表格

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
        FofaHack plugin = new FofaHack();
        plugin.fofaHack();
    }
}