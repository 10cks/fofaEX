package plugins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import static plugins.CommonTemplate.initAutoModeFile;

public class ModifyModeJsonDialog extends JFrame{

    class JsonData {
        String flow;
    }

    public static void main(String[] args) throws IOException {
        new ModifyModeJsonDialog();
    }

    public ModifyModeJsonDialog() throws IOException {
        swingUI();
    }

    public void swingUI() throws IOException {
        initAutoModeFile.getFlow();
        String flow = readJson();

        JFrame frame = new JFrame("ModifyModeJsonDialog");
        JPanel panel = new JPanel(new BorderLayout());

        JTextField textField = new JTextField(flow);
        textField.setPreferredSize(new Dimension(500, 50));  // 200为宽度，50为高度，你可以根据需要更改

        JButton updateButton = new JButton("更新");
        updateButton.setFocusPainted(false); // 不显示按钮焦点外边框
        updateButton.setFocusable(false); // 禁止按钮获取焦点
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                writeJson(textField.getText());     // 写入 AutoMode.json
                try {
                    initAutoModeFile.processJson(); // 根据配置文件更新AutoMode.json
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                textField.setText(readJson());      // 再次读取 AutoMode 刷新弹窗界面
                JOptionPane.showMessageDialog(frame, "更新成功"); // 添加保存成功的提示
            }
        });


        JButton saveButton = new JButton("保存");

        saveButton.setFocusPainted(false); // 不显示按钮焦点外边框
        saveButton.setFocusable(false); // 禁止按钮获取焦点
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                writeJson(textField.getText());     // 写入 AutoMode.json
                try {
                    initAutoModeFile.updateSettingsJson(); // 根据AutoMode.json 更新配置文件
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                textField.setText(readJson());      // 刷新弹窗界面
                JOptionPane.showMessageDialog(frame, "保存成功"); // 添加保存成功的提示
            }
        });

        panel.add(textField, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new BorderLayout());

        buttonPanel.add(updateButton,BorderLayout.NORTH);
        buttonPanel.add(saveButton,BorderLayout.SOUTH);

        panel.add(buttonPanel,BorderLayout.EAST);

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frame.setSize(400, 200);  // 设置窗口的默认大小，你根据需要更改
        frame.setLocationRelativeTo(null);  // 将窗口定位在屏幕中央

        frame.pack();
        frame.setVisible(true);
    }

    // 读取Json 文件
    public String readJson() {
        try {
            Gson gson = new Gson();
            BufferedReader br = new BufferedReader(new FileReader("./plugins/AutoMode.json"));
            JsonData data = gson.fromJson(br, JsonData.class);
            return data.flow;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 保存数据到Json 文件
    public void writeJson(String newFlow) {
        try {
            JsonData data = new JsonData();
            data.flow = newFlow;
            //添加disableHtmlEscaping方法
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            FileWriter writer = new FileWriter("./plugins/AutoMode.json");
            writer.write(gson.toJson(data));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}