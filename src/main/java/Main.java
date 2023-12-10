import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.BorderFactory;
import javax.swing.event.*;

import java.awt.Color;
import java.util.List;

import com.r4v3zn.fofa.core.client.FofaClient;
import com.r4v3zn.fofa.core.client.FofaConstants;
import com.r4v3zn.fofa.core.client.FofaSearch;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.table.*;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

import static java.awt.BorderLayout.*;

import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


class SearchResults {
    List<String> host;
    List<String> ip;
    List<String> port;
    List<String> protocol;
    List<String> title;
    List<String> domain;
    List<String> link;
    List<String> icp;
    List<String> city;
    // ... 可能还有其他字段 ...
}

public class Main {

    // 创建输入框
    private static JTextField fofaUrl = createTextField("https://fofa.info");
    private static JTextField fofaEmail = createTextField("请输入邮箱");
    private static JTextField fofaKey = createTextField("请输入API key");

    // 设置 field 规则
    private static boolean ipMark = true;
    private static boolean portMark = true;
    private static boolean protocolMark = true;
    private static boolean titleMark = true;
    private static boolean domainMark = true;
    private static boolean linkMark = true;
    private static boolean icpMark = false;
    private static boolean cityMark = false;

    /* 下面未完成 */
    private static boolean countryMark = false;
    private static boolean countryNameMark = false;
    private static boolean regionMark = false;
    private static boolean longitudeMark = false;
    private static boolean latitudeMark = false;
    private static boolean asNumberMark = false;
    private static boolean asOrganizationMark = false;
    private static boolean hostMark = false;
    private static boolean osMark = false;
    private static boolean serverMark = false;
    private static boolean jarmMark = false;
    private static boolean headerMark = false;
    private static boolean bannerMark = false;
    private static boolean baseProtocolMark = false;
    private static boolean certsIssuerOrgMark = false;
    private static boolean certsIssuerCnMark = false;
    private static boolean certsSubjectOrgMark = false;
    private static boolean certsSubjectCnMark = false;
    private static boolean tlsJa3sMark = false;
    private static boolean tlsVersionMark = false;
    private static boolean productMark = false;
    private static boolean productCategoryMark = false;
    private static boolean versionMark = false;
    private static boolean lastupdatetimeMark = false;
    private static boolean cnameMark = false;
    private static boolean iconHashMark = false;
    private static boolean certsValidMark = false;
    private static boolean cnameDomainMark = false;
    private static boolean bodyMark = false;
    private static boolean iconMark = false;
    private static boolean fidMark = false;
    private static boolean structinfoMark = false;

    /* 上面未完成 */

    private static boolean scrollPaneMark = true;
    private static boolean exportButtonAdded = false;
    // 在类的成员变量中创建弹出菜单
    private static JPopupMenu popupMenu = new JPopupMenu();
    private static JMenuItem itemSelectColumn = new JMenuItem("选择当前整列");
    private static JMenuItem itemDeselectColumn = new JMenuItem("取消选择整列");

    static {
        // 添加菜单项到弹出菜单
        popupMenu.add(itemSelectColumn);
        popupMenu.add(itemDeselectColumn);

        // 为右键菜单项添加事件监听器
        itemSelectColumn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 全选当前列
                if (table != null) {
                    int col = table.getSelectedColumn();
                    if (col >= 0) {
                        table.setColumnSelectionAllowed(true);
                        table.setRowSelectionAllowed(false);
                        table.clearSelection();
                        table.addColumnSelectionInterval(col, col);
                    }
                }
            }
        });

        // 为取消选择列的菜单项添加事件监听器
        itemDeselectColumn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 取消选择当前列
                if (table != null) {
                    table.clearSelection();
                    // 恢复默认的行选择模式
                    table.setRowSelectionAllowed(true);
                    table.setColumnSelectionAllowed(false);
                }
            }
        });
    }

    // 创建全局数据表
    private static JTable table;

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, FileNotFoundException {
        JFrame jFrame = new JFrame("fofaEX");

        // 设置外观风格
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

        // 创建菜单栏
        JMenuBar menuBar = new JMenuBar();

        // 创建"账户设置"菜单项
        JMenu settingsMenu = new JMenu("账户设置");

        // 在此菜单项下可以添加更多的子菜单项，以下只是一个示例
        JMenuItem changePasswordMenuItem = new JMenuItem("FOFA API");
        settingsMenu.add(changePasswordMenuItem);

        menuBar.add(settingsMenu);

        // 更改"账户设置"菜单项的事件监听
        changePasswordMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 创建新的JFrame
                JFrame settingsFrame = new JFrame("Settings");

                // 创建新的面板并添加组件
                JPanel settingsPanel = new JPanel(new GridLayout(4, 2, 5, 5)); // 使用4行2列的GridLayout
                settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 设置边距

                JButton checkButton = new JButton("检查账户");
                checkButton.setFocusPainted(false); // 添加这一行来取消焦点边框的绘制
                checkButton.setFocusable(false);
                checkButton.addActionListener(e1 -> {
                    // 点击按钮时显示输入的数据
                    String email = fofaEmail.getText();
                    String key = fofaKey.getText();
                    String fofaUrl_str = fofaUrl.getText();

                    // https://fofa.info/api/v1/info/my?email=
                    String authUrl = fofaUrl_str + "/api/v1/info/my?email=" + email + "&key=" + key;

                    HttpClient httpClient = HttpClientBuilder.create().build();
                    HttpGet request = new HttpGet(authUrl);

                    try {
                        HttpResponse response = httpClient.execute(request);
                        HttpEntity entity = response.getEntity();
                        String responseBody = EntityUtils.toString(entity);

                        // 解析JSON数据
                        JSONObject json = new JSONObject(responseBody);

                        if (!json.getBoolean("error")) {
                            // 账户验证有效
                            StringBuilder output = new StringBuilder();
                            output.append("账户验证有效\n");
                            output.append("邮箱地址: ").append(json.getString("email")).append("\n");
                            output.append("用户名: ").append(json.getString("username")).append("\n");

                            if (json.getBoolean("isvip")) {
                                output.append("身份权限：FOFA会员\n");
                            } else {
                                output.append("身份权限：普通用户\n");
                            }
                            ;
                            output.append("F点数量: ").append(json.getInt("fofa_point")).append("\n");
                            output.append("API月度剩余查询次数: ").append(json.getInt("remain_api_query")).append("\n");
                            output.append("API月度剩余返回数量: ").append(json.getInt("remain_api_data")).append("\n");
                            JOptionPane.showMessageDialog(null, output.toString());
                        } else {
                            // 账户验证无效
                            JOptionPane.showMessageDialog(null, "账户验证无效！", "提示", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (IOException | JSONException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "发生错误，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });

                // 添加组件到设置面板
                settingsPanel.add(new JLabel("FOFA URL:"));
                settingsPanel.add(fofaUrl);
                settingsPanel.add(new JLabel("Email:"));
                settingsPanel.add(fofaEmail);
                settingsPanel.add(new JLabel("API Key:"));
                settingsPanel.add(fofaKey);
                settingsPanel.add(checkButton);

                // 添加设置面板到设置窗口，并显示设置窗口
                settingsFrame.add(settingsPanel);
                settingsFrame.pack();
                settingsFrame.setLocationRelativeTo(null); // 使窗口居中显示
                settingsFrame.setResizable(false);
                settingsFrame.setVisible(true);
            }
        });


        // 创建"关于"菜单项
        JMenu aboutMenu = new JMenu("关于");
        JMenuItem aboutMenuItem = new JMenuItem("关于项目");
        aboutMenu.add(aboutMenuItem);
        menuBar.add(aboutMenu);

        // 为"关于项目"菜单项添加动作监听器
        aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JEditorPane editorPane = new JEditorPane("text/html", "");
                editorPane.setText(
                        "<html><body>" +
                                "<b>fofa EX:</b><br>" +
                                "Project: <a href='https://github.com/10cks/fofaEX'>https://github.com/10cks/fofaEX</a><br>" +
                                "Author: BWNER<br>" +
                                "version: 1.0<br>" +
                                "Update: 2023.12.11" +
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
            }
        });

        // 在JFrame中添加菜单栏
        jFrame.setJMenuBar(menuBar);

        // 刷新jf容器及其内部组件的外观
        SwingUtilities.updateComponentTreeUI(jFrame);
        jFrame.setSize(1000, 800);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 确保按下关闭按钮时结束程序

        // 创建 fofa 输入框
        JTextField textField0 = createTextFieldFofa("fofaEX: FOFA Extension");

        // 创建数据表
        table = new JTable();

        textField0.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // 当输入框内的文字是提示文字时，先清空输入框再允许输入
                if (textField0.getText().equals("fofaEX: FOFA Extension")) {
                    textField0.setText("");
                }
            }
        });

        // 设置背景色为 (4, 12, 31)
        textField0.setBackground(new Color(48, 49, 52));
        // 设置光标
        textField0.setCaret(new CustomCaret(Color.WHITE));

        // 设置字体
        Font font = new Font("Mono", Font.BOLD, 14);
        textField0.setFont(font);

        // fofaEX: FOFA Extension 事件
        textField0.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // 当输入框得到焦点时，如果当前是提示文字，则清空输入框并将文字颜色设置为白色
                if (textField0.getText().equals("fofaEX: FOFA Extension")) {
                    textField0.setText("");
                    textField0.setForeground(Color.WHITE);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // 当输入框失去焦点时，如果输入框为空，则显示提示文字，并将文字颜色设置为灰色
                if (textField0.getText().isEmpty()) {
                    textField0.setText("fofaEX: FOFA Extension");
                    textField0.setForeground(Color.GRAY);
                }
            }

        });

        textField0.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (textField0.getText().equals("fofaEX: FOFA Extension")) {
                    textField0.setText("");
                    textField0.setForeground(Color.WHITE);
                }
            }
        });


        textField0.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                textField0.setForeground(Color.WHITE);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (textField0.getText().isEmpty()) {
                    textField0.setForeground(Color.GRAY);
                } else {
                    textField0.setForeground(Color.WHITE);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // 平滑字体，无需处理
            }
        });

        // 将光标放在末尾
        // textField0.setCaretPosition(textField0.getText().length());

        // 编辑撤销

        // 创建UndoManager和添加UndoableEditListener。
        final UndoManager undoManager = new UndoManager();
        Document doc = textField0.getDocument();
        doc.addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {
                undoManager.addEdit(e.getEdit());
            }
        });

        // 添加KeyListener到textField。
        textField0.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_Z) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)) {
                    if (undoManager.canUndo()) {
                        undoManager.undo();
                    }
                } else {
                    if ((e.getKeyCode() == KeyEvent.VK_Z)) {
                        e.getModifiersEx();
                    }
                }
            }
        });


//        String asciiIcon =
//                "   __            __             _____  __  __\n" +
//                "  / _|   ___    / _|   __ _    | ____| \\ \\/ /\n" +
//                " | |_   / _ \\  | |_   / _` |   |  _|    \\  / \n" +
//                " |  _| | (_) | |  _| | (_| |   | |___   /  \\ \n" +
//                " |_|    \\___/  |_|    \\__,_|   |_____| /_/\\_\\";
//
//        JLabel labelIcon = new JLabel("<html><pre>" + asciiIcon + "</pre></html>");
        JLabel labelIcon = new JLabel(" FOFA EX");
        labelIcon.setForeground(new Color(48, 49, 52)); // 设置文本颜色为红色
        Font iconFont = new Font("Times New Roman", Font.BOLD, 60);
        labelIcon.setFont(iconFont);


        // 创建按钮面板，不改变布局（保持BoxLayout）
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        // 创建主面板并使用BoxLayout布局
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // 创建一个子面板，用来在搜索框边上新增按钮
        JPanel subPanel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // 创建面板并使用FlowLayout布局
        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4)); // hgap: 组件间的水平间距 vgap: 件间的垂直间距
        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        // 创建面板并使用GridLayout布局
        JPanel panel4 = new JPanel(new GridLayout(0, 5, 10, 10)); // 0表示行数不限，5表示每行最多5个组件，10, 10是组件之间的间距
        JPanel panel5 = new JPanel(new BorderLayout());

        panel5.setBorder(BorderFactory.createEmptyBorder(20, 5, 10, 5));

        // panel6 用来放导出表格的按键
        JPanel panel6 = new JPanel();

        // JPanel panel7 = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        JPanel panel7 = new JPanel(new GridLayout(0, 10, 0, 0));

        JPanel panel8 = new JPanel();

        // 创建"更新规则"按钮
        JButton updateButton = new JButton("♻");
        updateButton.setFocusPainted(false);
        updateButton.setFocusable(false);
        // 新增一个LinkedHashMap，用于存储按钮的键名和键值
        Map<String, JButton> buttonsMap = new LinkedHashMap<>();
        BufferedReader rulesReader = new BufferedReader(new FileReader("rules.txt"));
        BufferedReader accountsReader = new BufferedReader(new FileReader("accounts.txt"));
        settingInit(rulesReader, accountsReader, panel4, textField0, fofaEmail, fofaKey, buttonsMap);
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 读取文件内容，并创建新的按钮
                try {
                    BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\root\\IdeaProjects\\fofaEX\\rules.txt"));
                    Map<String, String> newMap = new LinkedHashMap<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();

                        // 跳过井号注释
                        if (line.startsWith("#")) {
                            continue;
                        }

                        if (line.startsWith("\"") && line.contains("{") && line.contains("}")) {
                            String[] parts = line.split(":", 2);

                            String key = parts[0].substring(1, parts[0].length() - 1).trim();

                            String value = parts[1].substring(1, parts[1].length() - 2).trim();
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
                            newButton.setToolTipText(entry.getValue()); // 设置按钮的 ToolTip 为键值，悬浮显示
                            newButton.setFocusPainted(false); // 添加这一行来取消焦点边框的绘制
                            newButton.setFocusable(false);  // 禁止了按钮获取焦点，因此按钮不会在被点击后显示为"激活"或"选中"的状态
                            newButton.addActionListener(actionEvent -> {
                                if (newButton.getForeground() != Color.RED) {
                                    // 如果文本为提示文字，则清空文本
                                    if (textField0.getText().contains("fofaEX: FOFA Extension")) {
                                        textField0.setText("");
                                    }
                                    textField0.setText(textField0.getText() + " " + newButton.getActionCommand());
                                    newButton.setForeground(Color.RED);
                                    newButton.setFont(newButton.getFont().deriveFont(Font.BOLD)); // 设置字体为粗体
                                } else {
                                    textField0.setText(textField0.getText().replace(" " + newButton.getActionCommand(), ""));
                                    newButton.setForeground(null);
                                    newButton.setFont(null);
                                    // 如果为空则设置 prompt
                                    if (textField0.getText().isEmpty()) {
                                        textField0.setText("fofaEX: FOFA Extension");
                                        textField0.setForeground(Color.GRAY);
                                        // 将光标放在开头
                                        textField0.setCaretPosition(0);

                                    }
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

        // 搜索按钮
        // 将textField0添加到新的SubPanel
        subPanel1.add(textField0);
        searchButton("搜索", subPanel1, textField0, fofaEmail, fofaKey, fofaUrl, panel5, panel6, labelIcon, panel2, panel7);

        // 添加组件到面板
        //panel1.add(fofaUrl); // 网址
        //panel1.add(fofaEmail); // 邮箱
        //panel1.add(fofaKey); // API key
        //panel1.add(checkButton);  // 检查账户

        panel1.add(labelIcon);
        panel2.add(subPanel1); // 搜索框 + 搜索按钮

        // 添加逻辑运算组件
        createLogicAddButton("=", "=", panel3, textField0);
        createLogicAddButton("==", "==", panel3, textField0);
        createLogicAddButton("&&", "&&", panel3, textField0);
        createLogicAddButton("||", "||", panel3, textField0);
        createLogicAddButton("!=", "!=", panel3, textField0);
        createLogicAddButton("*=", "*=", panel3, textField0);


        // 新增折叠按钮到panel3
        JButton foldButton = new JButton("▼");
        foldButton.setFocusPainted(false); //添加这一行来取消焦点边框的绘制
        foldButton.setFocusable(false);

        // 添加点击事件
        foldButton.addActionListener(new ActionListener() {
            boolean isFolded = false;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!isFolded) {
                    // 折叠 panel4
                    panel4.setVisible(false);
                    foldButton.setText("◀");
                    scrollPaneMark = false;
                } else {
                    // 展开 panel4
                    panel4.setVisible(true);
                    foldButton.setText("▼");
                    scrollPaneMark = true;
                }
                isFolded = !isFolded;

                // 重新验证和重绘包含 panel4 和 panel5 的主面板
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });

        panel3.add(updateButton); // 更新规则
        panel3.add(foldButton);


        // 创建复选框
        addRuleBox(panel7, "ip", newValue -> ipMark = newValue, ipMark);
        addRuleBox(panel7, "port", newValue -> portMark = newValue, portMark);
        addRuleBox(panel7, "protocol", newValue -> protocolMark = newValue, protocolMark);
        addRuleBox(panel7, "title", newValue -> titleMark = newValue, titleMark);
        addRuleBox(panel7, "domain", newValue -> domainMark = newValue, domainMark);
        addRuleBox(panel7, "link", newValue -> linkMark = newValue, linkMark);
        addRuleBox(panel7, "icp", newValue -> icpMark = newValue, icpMark);
        addRuleBox(panel7, "city", newValue -> cityMark = newValue, cityMark);

        /* 下面代码未完成 */

        addRuleBox(panel7, "country", newValue -> countryMark = newValue, countryMark);
        addRuleBox(panel7, "countryName", newValue -> countryNameMark = newValue, countryNameMark);
        addRuleBox(panel7, "region", newValue -> regionMark = newValue, regionMark);
        addRuleBox(panel7, "longitude", newValue -> longitudeMark = newValue, longitudeMark);
        addRuleBox(panel7, "latitude", newValue -> latitudeMark = newValue, latitudeMark);
        addRuleBox(panel7, "asNumber", newValue -> asNumberMark = newValue, asNumberMark);
        addRuleBox(panel7, "asOrganization", newValue -> asOrganizationMark = newValue, asOrganizationMark);
        addRuleBox(panel7, "host", newValue -> hostMark = newValue, hostMark);
        addRuleBox(panel7, "os", newValue -> osMark = newValue, osMark);
        addRuleBox(panel7, "server", newValue -> serverMark = newValue, serverMark);
        addRuleBox(panel7, "jarm", newValue -> jarmMark = newValue, jarmMark);
        addRuleBox(panel7, "header", newValue -> headerMark = newValue, headerMark);
        addRuleBox(panel7, "banner", newValue -> bannerMark = newValue, bannerMark);
        addRuleBox(panel7, "baseProtocol", newValue -> baseProtocolMark = newValue, baseProtocolMark);
        addRuleBox(panel7, "certsIssuerOrg", newValue -> certsIssuerOrgMark = newValue, certsIssuerOrgMark);
        addRuleBox(panel7, "certsIssuerCn", newValue -> certsIssuerCnMark = newValue, certsIssuerCnMark);
        addRuleBox(panel7, "certsSubjectOrg", newValue -> certsSubjectOrgMark = newValue, certsSubjectOrgMark);
        addRuleBox(panel7, "certsSubjectCn", newValue -> certsSubjectCnMark = newValue, certsSubjectCnMark);
        addRuleBox(panel7, "tlsJa3s", newValue -> tlsJa3sMark = newValue, tlsJa3sMark);
        addRuleBox(panel7, "tlsVersion", newValue -> tlsVersionMark = newValue, tlsVersionMark);
        addRuleBox(panel7, "product", newValue -> productMark = newValue, productMark);
        addRuleBox(panel7, "productCategory", newValue -> productCategoryMark = newValue, productCategoryMark);
        addRuleBox(panel7, "version", newValue -> versionMark = newValue, versionMark);
        addRuleBox(panel7, "lastupdatetime", newValue -> lastupdatetimeMark = newValue, lastupdatetimeMark);
        addRuleBox(panel7, "cname", newValue -> cnameMark = newValue, cnameMark);
        addRuleBox(panel7, "iconHash", newValue -> iconHashMark = newValue, iconHashMark);
        addRuleBox(panel7, "certsValid", newValue -> certsValidMark = newValue, certsValidMark);
        addRuleBox(panel7, "cnameDomain", newValue -> cnameDomainMark = newValue, cnameDomainMark);
        addRuleBox(panel7, "body", newValue -> bodyMark = newValue, bodyMark);
        addRuleBox(panel7, "icon", newValue -> iconMark = newValue, iconMark);
        addRuleBox(panel7, "fid", newValue -> fidMark = newValue, fidMark);
        addRuleBox(panel7, "structinfo", newValue -> structinfoMark = newValue, structinfoMark);


        /* 上面代码未完成 */


        // 设置全局边框：创建一个带有指定的空白边框的新面板，其中指定了上、左、下、右的边距
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 添加面板到主面板
        mainPanel.add(panel1);
        mainPanel.add(panel2);
        mainPanel.add(panel7);
        mainPanel.add(panel3);
        mainPanel.add(panel4);
        mainPanel.add(panel5);
        mainPanel.add(panel6);

        // 把面板添加到JFrame
        jFrame.add(mainPanel, NORTH);
        jFrame.add(buttonPanel, WEST);
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
        RoundJTextField textField = new RoundJTextField(0);
        textField.setText(text);
        textField.setPreferredSize(new Dimension(800, 50));

        // 设置文本与边框的间距
        textField.setMargin(new Insets(0, 10, 0, 5));

        // 创建只有底边的边框
        Border blueBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.RED);
        Border defaultBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY);

        // 设置默认边框
        // textField.setBorder(defaultBorder);

        return textField;
    }

    private static void createLogicAddButton(String buttonText, String appendText, JPanel panel, JTextField textField) {
        // 创建按钮
        JButton button = new JButton(buttonText);
        button.setFocusPainted(false); // 不显示按钮焦点外边框
        button.setFocusable(false); // 禁止按钮获取焦点

        // 添加点击事件
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (textField.getText().contains("fofaEX: FOFA Extension")) {
                    textField.setText("");
                }
                // 追加指定文本到文本框中
                textField.setText(textField.getText() + " " + appendText);
            }
        });
        // 将按钮添加到指定面板中
        panel.add(button);
    }

    private static void searchButton(String buttonText, JPanel panel, JTextField textField, JTextField emailField, JTextField keyField, JTextField urlField, JPanel resultPanel, JPanel exportPanel, JLabel changeIcon, JPanel disablePanel2, JPanel disablePanel7) {

        JButton button = new JButton(buttonText);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setPreferredSize(new Dimension(60, 50));
        button.addActionListener(new ActionListener() {
            public List<String> processSearchResult(String query, String searchType) throws Exception {

                String domain = urlField.getText().trim();
                String email = emailField.getText().trim();
                String key = keyField.getText().trim();

                FofaConstants.BASE_URL = domain;
                FofaClient fofaClient = new FofaClient(email, key);
                FofaSearch fofaSearch = new FofaSearch(fofaClient);

                String rawData = fofaSearch.all(query, searchType).getResults().toString();
                String[] trimmedData = rawData.substring(1, rawData.length() - 1).split(", ");
                return Arrays.asList(trimmedData);
            }

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                String domain = urlField.getText().trim();
                String email = emailField.getText().trim();
                String key = keyField.getText().trim();
                String grammar = textField.getText().trim();

                String orginIconStr = changeIcon.getText();

//                String searchAsciiIcon = "                ____                                 _       _                             \n" +
//                        "               / ___|    ___    __ _   _ __    ___  | |__   (_)  _ __     __ _             \n" +
//                        "               \\___ \\   / _ \\  / _` | | '__|  / __| | '_ \\  | | | '_ \\   / _` |            \n" +
//                        "                ___) | |  __/ | (_| | | |    | (__  | | | | | | | | | | | (_| |  \n" +
//                        "               |____/   \\___|  \\__,_| |_|     \\___| |_| |_| |_| |_| |_|  \\__, | \n" ;
//
//
//                changeIcon.setText("<html><pre>" + searchAsciiIcon + "</pre></html>");
                changeIcon.setText(" FOFA EX");
                changeIcon.setForeground(new Color(89, 154, 248)); // 设置文本颜色为红色
                Font font = new Font("Times New Roman", Font.BOLD, 60);
                changeIcon.setFont(font);

                setComponentsEnabled(disablePanel2, false);
                setComponentsEnabled(disablePanel7, false);


                // 创建 SwingWorker 来处理搜索任务
                SwingWorker<SearchResults, Void> worker = new SwingWorker<SearchResults, Void>() {

                    private void fontSet(JLabel changeIcon, String originIconStr) {
                        changeIcon.setText(originIconStr);
                        changeIcon.setForeground(new Color(48, 49, 52));
                    }

                    @Override
                    protected SearchResults doInBackground() throws Exception {

                        SearchResults results = new SearchResults();

                        try {
                            // 调用 SDK
                            FofaConstants.BASE_URL = domain;
                            FofaClient fofaClient = new FofaClient(email, key);
                            FofaSearch fofaSearch = new FofaSearch(fofaClient);

                            String query = grammar;
                            if (query.equals("fofaEX: FOFA Extension")) {
                                query = ""; // 将字符串设置为空
                            }

                            String allData = fofaSearch.all(query).getResults().toString();

                            String[] hostAllData = allData.substring(1, allData.length() - 1).split(", ");
                            List<String> hostShow = Arrays.asList(hostAllData);

                            List<String> ipShow = null;
                            List<String> portShow = null;
                            List<String> protocolShow = null;
                            List<String> titleShow = null;
                            List<String> domainShow = null;
                            List<String> linkShow = null;
                            List<String> icpShow = null;
                            List<String> cityShow = null;

                            if (ipMark) {
                                ipShow = processSearchResult(query, "ip");
                            }

                            if (portMark) {
                                portShow = processSearchResult(query, "port");
                            }

                            if (protocolMark) {
                                protocolShow = processSearchResult(query, "protocol");
                            }

                            if (titleMark) {
                                List<String> encodedTitles = processSearchResult(query, "title");
                                titleShow = decodeHtmlEntities(encodedTitles);
                            }

                            if (domainMark) {
                                domainShow = processSearchResult(query, "domain");
                            }

                            // List<String> linkShow = processSearchResult(query, "link");
                            if (linkMark) {
                                linkShow = getApiResult(domain, "link", query, email, key, "results");
                            }

                            if (icpMark) {
                                icpShow = processSearchResult(query, "icp");
                            }

                            if (cityMark) {
                                cityShow = processSearchResult(query, "city");
                            }

                            results.host = hostShow;
                            results.ip = ipShow;
                            results.port = portShow;
                            results.protocol = protocolShow;
                            results.title = titleShow;
                            results.domain = domainShow;
                            results.link = linkShow;
                            results.icp = icpShow;
                            results.city = cityShow;

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
                                        fontSet(changeIcon, orginIconStr);
                                        return;
                                    }
                                    // 检查 table 是否有模型和数据
                                    if (table.getModel() == null || table.getModel().getRowCount() <= 0) {
                                        JOptionPane.showMessageDialog(null, "当前无数据");
                                        fontSet(changeIcon, orginIconStr);
                                        return;
                                    }
                                    exportTableToExcel(table);
                                }
                            });

                        } catch (JSONException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "发生错误，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
                            fontSet(changeIcon, orginIconStr);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(null, e.getMessage(), "执行失败", JOptionPane.ERROR_MESSAGE);
                            fontSet(changeIcon, orginIconStr);
                            throw new RuntimeException(e);
                        }
                        fontSet(changeIcon, orginIconStr);

                        return results;
                    }

                    @Override
                    protected void done() {
                        try {
                            SearchResults searchResults = get();
                            boolean hasData = searchResults != null && !searchResults.host.isEmpty();

                            setComponentsEnabled(disablePanel2, true);
                            setComponentsEnabled(disablePanel7, true);

                            showResultsInTable(
                                    searchResults.host,
                                    searchResults.ip,
                                    searchResults.port,
                                    searchResults.protocol,
                                    searchResults.title,
                                    searchResults.domain,
                                    searchResults.link,
                                    searchResults.icp,
                                    searchResults.city,
                                    resultPanel
                            );

                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
                // 启动 SwingWorker
                worker.execute();
            }
        });
        panel.add(button);
    }

    private static void showResultsInTable(List<String> host, List<String> tableIpShow, List<String> tablePortShow, List<String> protocolShow, List<String> titleShow, List<String> domainShow, List<String> linkShow, List<String> icpShow, List<String> cityShow, JPanel panel) {
        // String[] columnNames = {"host","ip","port", "protocol", "title", "domain","link","icp","city"};
        List<String> columnNamesList = new ArrayList<String>(List.of("host"));

        if (cityMark) {
            columnNamesList.add(1, "city");  // 插入“ip" 列到正确的位置
        }

        if (icpMark) {
            columnNamesList.add(1, "icp");  // 插入“ip" 列到正确的位置
        }

        if (linkMark) {
            columnNamesList.add(1, "link");  // 插入“ip" 列到正确的位置
        }

        if (domainMark) {
            columnNamesList.add(1, "domain");  // 插入“ip" 列到正确的位置
        }

        if (titleMark) {
            columnNamesList.add(1, "title");  // 插入“ip" 列到正确的位置
        }

        if (protocolMark) {
            columnNamesList.add(1, "protocol");  // 插入“ip" 列到正确的位置
        }

        if (portMark) {
            columnNamesList.add(1, "port");  // 插入“ip" 列到正确的位置
        }

        if (ipMark) {
            columnNamesList.add(1, "ip");  // 插入“ip" 列到正确的位置
        }

        String[] columnNames = columnNamesList.toArray(new String[0]);
        Object[][] data = new Object[host.size()][columnNames.length];

        for (int i = 0; i < host.size(); i++) {
            data[i][0] = host.get(i);

            int columnIndex = 1;

            if (ipMark && tableIpShow.size() > i) {
                data[i][columnIndex++] = tableIpShow.get(i);
            }

            if (portMark && tablePortShow.size() > i) {
                data[i][columnIndex++] = tablePortShow.get(i);
            }
            if (protocolMark && protocolShow.size() > i) {
                data[i][columnIndex++] = protocolShow.get(i);
            }
            if (titleMark && titleShow.size() > i) {
                data[i][columnIndex++] = titleShow.get(i);
            }
            if (domainMark && domainShow.size() > i) {
                data[i][columnIndex++] = domainShow.get(i);
            }
            if (linkMark && linkShow.size() > i) {
                data[i][columnIndex++] = linkShow.get(i);
            }
            if (icpMark && icpShow.size() > i) {
                data[i][columnIndex++] = icpShow.get(i);
            }
            if (cityMark && cityShow.size() > i) {
                data[i][columnIndex++] = cityShow.get(i);
            }
        }
        DefaultTableModel model = new DefaultTableModel(data, columnNames);

        table.setModel(model);
        JTableHeader header = getjTableHeader();


        // 重新设置表格头，以便新的渲染器生效
        table.setTableHeader(header);

        adjustColumnWidths(table); // 自动调整列宽
        JScrollPane scrollPane = new JScrollPane(table);


        if (scrollPaneMark) {
            scrollPane.setPreferredSize(new Dimension(800, 450)); // 设置滚动窗格的首选大小
        } else {
            scrollPane.setPreferredSize(new Dimension(800, 600)); // 设置滚动窗格的首选大小
        }

        table.setRowHeight(24); // 设置表格的行高
        table.setFillsViewportHeight(true);

        panel.removeAll();
        panel.add(scrollPane, CENTER);
        panel.revalidate();
        panel.repaint();

        // 下面的代码将确保 table 被正确地初始化和更新
        if (table == null) {
            table = new JTable(model);
        } else {
            table.setModel(model);
        }

        // 添加右键鼠标事件
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (row >= 0 && col >= 0) {
                        // 右键显示弹出菜单
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

    }

    private static JTableHeader getjTableHeader() {
        JTableHeader header = table.getTableHeader();
        // 自定义列标题的渲染器
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                // 设置渲染器返回的组件类型为JLabel
                JLabel headerLabel = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                // 设置背景色为灰色
                headerLabel.setBackground(Color.GRAY);
                // 设置文字颜色为白色
                headerLabel.setForeground(Color.WHITE);
                // 设置文字居中
                headerLabel.setHorizontalAlignment(JLabel.CENTER);
                // 设置标题加粗和大小
                headerLabel.setFont(new Font(headerLabel.getFont().getFamily(), Font.BOLD, 15));
                // 设置边框，其中颜色设置为白色
                headerLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.WHITE));
                // 设置数据文字大小
                table.setFont(new Font("Serif", Font.PLAIN, 15)); // 设置数据单元格的字体和大小

                return headerLabel;
            }
        });
        return header;
    }

    public static void adjustColumnWidths(JTable table) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 15; // Min width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 1, width);
            }
            if (width > 300)
                width = 300;
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    // 账户设置
    static public void settingInit(BufferedReader rules, BufferedReader accounts, JPanel initPanel, JTextField initTextField, JTextField fofaEmail, JTextField fofaKey, Map<String, JButton> initButtonsMap) {


        try {
            String fofaEmailLine = accounts.readLine();
            String fofaKeyLine = accounts.readLine();

            // 检查是否有内容需要解析和赋值
            if (fofaEmailLine != null && fofaKeyLine != null) {
                fofaEmail.setText(fofaEmailLine.split(":")[1]);
                fofaKey.setText(fofaKeyLine.split(":")[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 读取文件内容，并创建新的按钮
        try {
            Map<String, String> newMap = new LinkedHashMap<>();
            String line;
            while ((line = rules.readLine()) != null) {
                line = line.trim();

                // 跳过井号注释
                if (line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("\"") && line.contains("{") && line.contains("}")) {
                    String[] parts = line.split(":", 2);

                    String key = parts[0].substring(1, parts[0].length() - 1).trim();

                    String value = parts[1].substring(1, parts[1].length() - 2).trim();
                    newMap.put(key, value);
                }
            }
            rules.close();

            // 移除按钮
            Iterator<Map.Entry<String, JButton>> iterator = initButtonsMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, JButton> entry = iterator.next();
                if (!newMap.containsKey(entry.getKey())) {
                    initPanel.remove(entry.getValue());
                    iterator.remove();
                }
            }

            // 更新并新增按钮
            for (Map.Entry<String, String> entry : newMap.entrySet()) {
                JButton existingButton = initButtonsMap.get(entry.getKey());
                if (existingButton == null) {
                    // 新按钮
                    JButton newButton = new JButton(entry.getKey());
                    newButton.setActionCommand(entry.getValue());
                    newButton.setToolTipText(entry.getValue()); // 设置按钮的 ToolTip 为键值，悬浮显示
                    newButton.setFocusPainted(false); // 添加这一行来取消焦点边框的绘制
                    newButton.setFocusable(false);  // 禁止了按钮获取焦点，因此按钮不会在被点击后显示为"激活"或"选中"的状态
                    newButton.addActionListener(actionEvent -> {
                        if (newButton.getForeground() != Color.RED) {
                            // 如果文本为提示文字，则清空文本
                            if (initTextField.getText().contains("fofaEX: FOFA Extension")) {
                                initTextField.setText("");
                            }
                            initTextField.setText(initTextField.getText() + " " + newButton.getActionCommand());
                            newButton.setForeground(Color.RED);
                            newButton.setFont(newButton.getFont().deriveFont(Font.BOLD)); // 设置字体为粗体
                        } else {
                            initTextField.setText(initTextField.getText().replace(" " + newButton.getActionCommand(), ""));
                            newButton.setForeground(null);
                            newButton.setFont(null);
                            // 如果为空则设置 prompt
                            if (initTextField.getText().isEmpty()) {
                                initTextField.setText("fofaEX: FOFA Extension");
                                initTextField.setForeground(Color.GRAY);
                                // 将光标放在开头
                                initTextField.setCaretPosition(0);

                            }
                        }
                    });
                    initPanel.add(newButton);
                    initButtonsMap.put(entry.getKey(), newButton);
                } else {
                    // This is an existing button
                    existingButton.setActionCommand(entry.getValue());
                    existingButton.setText(entry.getKey()); // Update button text
                }
            }
            initPanel.revalidate();
            initPanel.repaint();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private static void exportTableToExcel(JTable table) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Table Data");

        // 创建表头
        XSSFRow headerRow = sheet.createRow(0);
        for (int i = 0; i < table.getColumnCount(); i++) {
            headerRow.createCell(i).setCellValue(table.getColumnName(i));
        }

        // 写入数据行
        for (int i = 0; i < table.getRowCount(); i++) {
            XSSFRow dataRow = sheet.createRow(i + 1);
            for (int j = 0; j < table.getColumnCount(); j++) {
                Object value = table.getValueAt(i, j);
                String text = (value == null) ? "" : value.toString(); // 检查是否为null
                dataRow.createCell(j).setCellValue(text);
            }
        }

        // 将工作簿保存到文件
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = dateFormat.format(new Date());

            String directoryName = "exportData";
            File directory = new File(directoryName);

            if (!directory.exists()) {
                directory.mkdir();
            }

            String fileName = directoryName + "/TableData_" + timestamp + ".xlsx";
            FileOutputStream output = new FileOutputStream(fileName);
            workbook.write(output);
            workbook.close();
            output.close();
            JOptionPane.showMessageDialog(null, "Export successful!\n File saved at: " + new File(fileName).getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 单独给link用的，可以封装成 SDK
    public static List<String> getApiResult(String fofaDomain, String fields, String qbase64, String email, String key, String paramName) throws Exception {
        String apiUrl = fofaDomain + "/api/v1/search/all?fields=" + fields + "&qbase64=" + Base64.getEncoder().encodeToString(qbase64.getBytes()) + "&email=" + email + "&key=" + key;
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        if (connection.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String output;
        StringBuilder responseBuilder = new StringBuilder();
        while ((output = br.readLine()) != null) {
            responseBuilder.append(output);
        }

        connection.disconnect();
        String response = responseBuilder.toString();
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray jsonArray = jsonResponse.getJSONArray(paramName);
        List<String> results = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            results.add(jsonArray.getString(i));
        }

        return results;
    }

    public static void addRuleBox(JPanel panel, String checkBoxName, RuleMarkChangeCallback callback, Boolean selectMark) {
        // 创建复选框
        JCheckBox newBox = new JCheckBox(checkBoxName);
        newBox.setFocusPainted(false);
        newBox.setSelected(callback != null && callback instanceof RuleMarkChangeCallback);
        newBox.setSelected(selectMark);  // 直接使用 ipMark 的当前值

        // 添加 ItemListener
        newBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                // 使用回调接口来通知外部变量的更改
                if (callback != null) {
                    callback.onRuleMarkChange(e.getStateChange() == ItemEvent.SELECTED);
                }
            }
        });

        // 添加到面板
        panel.add(newBox);
    }

    // addRuleBox 的回调函数
    public interface RuleMarkChangeCallback {
        void onRuleMarkChange(boolean newValue);
    }

    // 处理 title 实体编码问题
    public static List<String> decodeHtmlEntities(List<String> encodedTitles) {
        return encodedTitles.stream()
                .map(StringEscapeUtils::unescapeHtml4)
                .collect(Collectors.toList());
    }

    private static void setComponentsEnabled(Container container, boolean enabled) {
        for (Component component : container.getComponents()) {
            component.setEnabled(enabled);
            if (component instanceof Container) {
                setComponentsEnabled((Container) component, enabled);
            }
        }
    }
}