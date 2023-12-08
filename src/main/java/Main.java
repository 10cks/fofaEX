import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        JFrame jFrame = new JFrame("fofaEX");

        // 设置外观风格
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        // 刷新jf容器及其内部组件的外观
        SwingUtilities.updateComponentTreeUI(jFrame);
        jFrame.setSize(1000, 500);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 确保按下关闭按钮时结束程序

        // 创建输入框
        JTextField textField0 = createTextFieldFofa("fofaEX: fofa Extension");
        textField0.setForeground(Color.BLUE);
        Font font = new Font("Mono", Font.BOLD, 14);
        textField0.setFont(font);
        textField0.setCaretPosition(textField0.getText().length());


        // 创建输入框
        JTextField textField1 = createTextField("http://fofa.info/");
        JTextField textField2 = createTextField("请输入邮箱");
        JTextField textField3 = createTextField("请输入API key");

        // 创建检查账户按钮
        JButton button = new JButton("检查账户");
        button.setFocusPainted(false); // 添加这一行来取消焦点边框的绘制
        button.addActionListener(e -> {
            // 点击按钮时显示输入的数据
            JOptionPane.showMessageDialog(null, "网址: " + textField1.getText()
                    + "\n邮箱: " + textField2.getText()
                    + "\nAPI key: " + textField3.getText());
        });


        // 创建按钮面板，不改变布局（保持BoxLayout）
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));


        // 创建主面板并使用BoxLayout布局
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // 创建面板并使用FlowLayout布局
        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        // 创建面板并使用GridLayout布局
        JPanel panel4 = new JPanel(new GridLayout(0, 5, 10, 10)); // 0表示行数不限，5表示每行最多5个组件，10, 10是组件之间的间距
        JPanel panel5 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // 创建"更新规则"按钮
        JButton updateButton = new JButton("更新规则");
        updateButton.setFocusPainted(false);
        // 新增一个LinkedHashMap，用于存储按钮的键名和键值
        Map<String, JButton> buttonsMap = new LinkedHashMap<>();
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 读取文件内容，并创建新的按钮
                try {
                    BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\root\\IdeaProjects\\fofaEX\\hello.txt"));
                    Map<String, String> newMap = new LinkedHashMap<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("\"") && line.contains("\":\"")) {
                            String[] parts = line.split(":");
                            String key = parts[0].replace("\"", "").trim();
                            String value = parts[1].replace("\"", "").replace(",", "").trim();
                            newMap.put(key, value);
                        }
                    }
                    reader.close();

                    // 移除按钮
                    Iterator<Map.Entry<String, JButton>> iterator = buttonsMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, JButton> entry = iterator.next();
                        if (!newMap.containsKey(entry.getKey())) {
                            panel4.remove(entry.getValue());
                            iterator.remove();
                        }
                    }

                    // 更新并新增按钮
                    for (Map.Entry<String, String> entry : newMap.entrySet()) {
                        JButton existingButton = buttonsMap.get(entry.getKey());
                        if (existingButton == null) {
                            // 新按钮
                            JButton newButton = new JButton(entry.getKey());
                            newButton.setActionCommand(entry.getValue());
                            newButton.setFocusPainted(false); // 添加这一行来取消焦点边框的绘制
                            newButton.setFocusable(false);  // 禁止了按钮获取焦点，因此按钮不会在被点击后显示为"激活"或"选中"的状态
                            newButton.addActionListener(actionEvent -> {
                                if (newButton.getForeground() != Color.RED) {
                                    textField0.setText(textField0.getText() + " " + newButton.getActionCommand());
                                    newButton.setForeground(Color.RED);
                                    newButton.setFont(newButton.getFont().deriveFont(Font.BOLD)); // 设置字体为粗体
                                } else {
                                    textField0.setText(textField0.getText().replace(" " + newButton.getActionCommand(), ""));
                                    newButton.setForeground(null);
                                    newButton.setFont(null);
                                }
                            });
                            panel4.add(newButton);
                            buttonsMap.put(entry.getKey(), newButton);
                        } else {
                            // This is an existing button
                            existingButton.setActionCommand(entry.getValue());
                            existingButton.setText(entry.getKey()); // Update button text
                        }
                    }

                    panel4.revalidate();
                    panel4.repaint();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        // 添加组件到面板
        panel1.add(textField1); // 网址
        panel1.add(textField2); // 邮箱
        panel1.add(textField3); // API key
        panel1.add(button);     // 检查账户
        panel2.add(textField0); // FofaEX: Fofa Grammar Extension
        panel2.add(updateButton);


// 创建一个带有指定的空白边框的新面板，其中指定了上、左、下、右的边距
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 添加面板到主面板
        mainPanel.add(panel1);
        mainPanel.add(panel2);
        mainPanel.add(panel3);
        mainPanel.add(panel4);
        mainPanel.add(panel5);


        // 把面板添加到JFrame
        jFrame.add(mainPanel, BorderLayout.NORTH);
        jFrame.add(buttonPanel, BorderLayout.WEST);

        // 设置窗口居中并显示
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);

        // 在程序运行时，使 textField0 获得焦点
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textField0.requestFocusInWindow();
            }
        });
    }

    private static JTextField createTextField(String text) {
        JTextField textField = new JTextField(text, 20);
        textField.setPreferredSize(new Dimension(200, 20));

        // 创建只有底边的边框
        Border blueBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.RED);
        Border defaultBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY);

        // 设置默认边框
        textField.setBorder(defaultBorder);

        // 添加鼠标监听器
        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // 鼠标进入时，设置边框颜色为蓝色
                textField.setBorder(blueBorder);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // 鼠标离开时，将边框颜色设回默认颜色
                textField.setBorder(defaultBorder);
            }
        });

        return textField;
    }

    private static JTextField createTextFieldFofa(String text) {
        JTextField textField = new JTextField(text);
        textField.setPreferredSize(new Dimension(380, 40));

        // 创建只有底边的边框
        Border blueBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.RED);
        Border defaultBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY);

        // 设置默认边框
        textField.setBorder(defaultBorder);

        // 添加鼠标监听器
        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // 鼠标进入时，设置边框颜色为蓝色
                textField.setBorder(blueBorder);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // 鼠标离开时，将边框颜色设回默认颜色
                textField.setBorder(defaultBorder);
            }
        });
        return textField;
    }
}