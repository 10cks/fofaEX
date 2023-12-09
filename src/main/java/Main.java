import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.BorderFactory;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.Color;
import java.util.List;

import com.r4v3zn.fofa.core.DO.SearchData;
import com.r4v3zn.fofa.core.client.FofaClient;
import com.r4v3zn.fofa.core.client.FofaConstants;
import com.r4v3zn.fofa.core.client.FofaSearch;
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

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.*;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

import static java.awt.BorderLayout.*;

public class Main {


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

    // 创建数据表
    private static JTable table; // 添加这一行
    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, FileNotFoundException {
        JFrame jFrame = new JFrame("fofaEX");

        // 设置外观风格
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        // 刷新jf容器及其内部组件的外观
        SwingUtilities.updateComponentTreeUI(jFrame);
        jFrame.setSize(1000, 500);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 确保按下关闭按钮时结束程序

        // 创建 fofa 输入框
        JTextField textField0 = createTextFieldFofa("fofaEX: FOFA Extension");

        // 创建数据表
        table = new JTable();

        textField0.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
                // 当输入框内的文字是提示文字时，先清空输入框再允许输入
                if (textField0.getText().equals("fofaEX: FOFA Extension")){
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
                if (textField0.getText().equals("fofaEX: FOFA Extension")){
                    textField0.setText("");
                    textField0.setForeground(Color.WHITE);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // 当输入框失去焦点时，如果输入框为空，则显示提示文字，并将文字颜色设置为灰色
                if (textField0.getText().isEmpty()){
                    textField0.setText("fofaEX: FOFA Extension");
                    textField0.setForeground(Color.GRAY);
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
                if (textField0.getText().isEmpty()){
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
                } else if ((e.getKeyCode() == KeyEvent.VK_Z) && ((e.getModifiersEx() & (KeyEvent.CTRL_DOWN_MASK & KeyEvent.SHIFT_DOWN_MASK)) != 0)) {
                    if (undoManager.canRedo()) {
                        undoManager.redo();
                    }
                }
            }
        });

        // 创建输入框
        JTextField fofaUrl = createTextField("https://fofa.info");
        JTextField fofaEmail = createTextField("请输入邮箱");
        JTextField fofaKey = createTextField("请输入API key");

        // 创建检查账户按钮
        JButton checkButton = new JButton("检查账户");
        checkButton.setFocusPainted(false); // 添加这一行来取消焦点边框的绘制
        checkButton.setFocusable(false);
        checkButton.addActionListener(e -> {
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

                    if(json.getBoolean("isvip")){
                        output.append("身份权限：FOFA会员\n");
                    }else{
                        output.append("身份权限：普通用户\n");
                    };
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


        // 创建按钮面板，不改变布局（保持BoxLayout）
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        // 创建主面板并使用BoxLayout布局
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // 创建一个子面板，用来在搜索框边上新增按钮
        JPanel subPanel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // 创建面板并使用FlowLayout布局
        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        // 创建面板并使用GridLayout布局
        JPanel panel4 = new JPanel(new GridLayout(0, 5, 10, 10)); // 0表示行数不限，5表示每行最多5个组件，10, 10是组件之间的间距
        JPanel panel5 = new JPanel(new BorderLayout());

        panel5.setBorder(BorderFactory.createEmptyBorder(20, 5, 10, 5));

        JPanel panel6 = new JPanel();
        JButton exportButton = new JButton("Export to Excel");
        panel6.add(exportButton);

        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 在这里检查 table 是否被初始化
                if (table == null) {
                    JOptionPane.showMessageDialog(null, "Table is not initialized yet.");
                    return;
                }
                // 检查 table 是否有模型和数据
                if (table.getModel() == null || table.getModel().getRowCount() <= 0) {
                    JOptionPane.showMessageDialog(null, "Table is empty.");
                    return;
                }
                exportTableToExcel(table);
            }
        });

        // 创建"更新规则"按钮
        JButton updateButton = new JButton("更新规则");
        updateButton.setFocusPainted(false);
        updateButton.setFocusable(false);
        // 新增一个LinkedHashMap，用于存储按钮的键名和键值
        Map<String, JButton> buttonsMap = new LinkedHashMap<>();
        BufferedReader rulesReader = new BufferedReader(new FileReader("rules.txt"));
        BufferedReader accountsReader = new BufferedReader(new FileReader("accounts.txt"));
        settingInit(rulesReader,accountsReader,panel4,textField0,fofaEmail,fofaKey,buttonsMap);
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

                            String key = parts[0].substring(1, parts[0].length()-1).trim();

                            String value = parts[1].substring(1, parts[1].length()-2).trim();
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
        searchButton("搜索", subPanel1, textField0, fofaEmail, fofaKey,fofaUrl,panel5);

        // 添加组件到面板
        panel1.add(fofaUrl); // 网址
        panel1.add(fofaEmail); // 邮箱
        panel1.add(fofaKey); // API key
        panel1.add(checkButton);  // 检查账户
        panel1.add(updateButton); // 更新规则
        // panel2.add(textField0); // FofaEX: Fofa Grammar Extension
        panel2.add(subPanel1); // 搜索框 + 搜索按钮

        // 添加逻辑运算组件
        createLogicAddButton("=", "=", panel3, textField0);
        createLogicAddButton("==", "==", panel3, textField0);
        createLogicAddButton("&&", "&&", panel3, textField0);
        createLogicAddButton("||", "||", panel3, textField0);
        createLogicAddButton("!=", "!=", panel3, textField0);
        createLogicAddButton("*=", "*=", panel3, textField0);


        // 新增折叠按钮

        JButton foldButton = new JButton("▼");
        foldButton.setFocusPainted(false); //添加这一行来取消焦点边框的绘制
        foldButton.setFocusable(false);

        // 添加点击事件
        foldButton.addActionListener(new ActionListener() {
            boolean isFolded = false;
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!isFolded) {
                    // 折叠panel4为一行
                    panel4.setLayout(new GridLayout(1, 0));
                    foldButton.setText("▲");
                } else {
                    // 恢复panel4原本的样子
                    panel4.setLayout(new GridLayout(0, 5, 10, 10)); //假设原本是每行最多5个
                    foldButton.setText("▼");
                }
                isFolded = !isFolded;
                panel4.revalidate();
                panel4.repaint();
            }
        });
        panel3.add(foldButton);



        // 创建一个带有指定的空白边框的新面板，其中指定了上、左、下、右的边距
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 添加面板到主面板
        mainPanel.add(panel1);
        mainPanel.add(panel2);
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

    private static void searchButton(String buttonText, JPanel panel, JTextField textField, JTextField emailField, JTextField keyField,JTextField urlField,JPanel resultPanel) {
        JButton button = new JButton(buttonText);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setPreferredSize(new Dimension(60, 50));
        button.addActionListener(new ActionListener() {


            public List<String> processSearchResult(String query, String searchType) throws Exception {

                String domain = urlField.getText().trim();
                String email = emailField.getText().trim();
                String key = keyField.getText().trim();
                String grammar = textField.getText().trim();

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

                try {
                    // 调用 SDK
                    FofaConstants.BASE_URL = domain;
                    FofaClient fofaClient = new FofaClient(email, key);
                    FofaSearch fofaSearch = new FofaSearch(fofaClient);

                    String query = grammar;

                    //System.out.println(allData); // 查询
                    //System.out.println(fofaSearch.all(query)); // 查询
                    //System.out.println(fofaSearch.all(query,"ip")); // 查询

                    /*
                    processSearchResult 函数流程:
                    fofaSearch.all(query,"ip") -> tableIp -> tableIpTrim -> tableIpShow
                    */

                    String allData = fofaSearch.all(query).getResults().toString();

                    String[] hostAllData = allData.substring(1, allData.length() - 1).split(", ");
                    List<String> host = Arrays.asList(hostAllData);

                    List<String> tableIpShow  = processSearchResult(query, "ip");
                    List<String> tablePortShow  = processSearchResult(query, "port");
                    List<String> protocolShow = processSearchResult(query, "protocol");
                    List<String> titleShow = processSearchResult(query, "title");
                    List<String> domainShow = processSearchResult(query, "domain");
                    List<String> linkShow = processSearchResult(query, "icp");
                    List<String> icpShow = processSearchResult(query, "icp");
                    List<String> country_nameShow = processSearchResult(query, "country_name");

                    // 使用搜索结果更新表格
                    showResultsInTable(host, tableIpShow, tablePortShow, protocolShow, titleShow, domainShow,linkShow,icpShow,country_nameShow, resultPanel);

                } catch (JSONException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "发生错误，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "执行失败", JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException(e);
                }
            }
        });

        panel.add(button);
    }

    private static void showResultsInTable(List<String> host, List<String>tableIpShow , List<String>tablePortShow , List<String>protocolShow , List<String>titleShow ,List<String>domainShow,List<String>linkShow,List<String>icpShow,List<String>country_nameShow,JPanel panel) {
        String[] columnNames = {"host","ip","port", "protocol", "title", "domain", "link","icp","country_name"};
        Object[][] data = new Object[host.size()][9];
        for (int i = 0; i < host.size(); i++) {
            data[i][0] = host.get(i);
            if (tableIpShow.size() > i) {
                data[i][1] = tableIpShow.get(i);
            }
            if (tablePortShow.size() > i) {
                data[i][2] = tablePortShow.get(i);
            }
            if (protocolShow.size() > i) {
                data[i][3] = protocolShow.get(i);
            }
            if (titleShow.size() > i) {
                data[i][4] = titleShow.get(i);
            }
            if (domainShow.size() > i) {
                data[i][5] = domainShow.get(i);
            }
            if (icpShow.size() > i) {
                data[i][7] = icpShow.get(i);
            }
            if (country_nameShow.size() > i) {
                data[i][8] = country_nameShow.get(i);
            }
        }
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        table.setModel(model);
        JTableHeader header = table.getTableHeader();

// 自定义Header的渲染器
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

// 重新设置表格头，以便新的渲染器生效
        table.setTableHeader(header);

        adjustColumnWidths(table); // 自动调整列宽
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 450)); // 设置滚动窗格的首选大小
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

        // 在初始化 JTable 之后，添加以下代码
// 在初始化 JTable 之后，添加以下代码
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (row >= 0 && col >= 0) {
                        // 在右键点击前不要自动改变选择，除非需要
                        // table.setRowSelectionInterval(row, row);
                        // table.setColumnSelectionInterval(col, col);

                        // 显示弹出菜单
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

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
            if(width > 300)
                width=300;
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    static public void settingInit(BufferedReader rules,BufferedReader accounts,JPanel initPanel,JTextField initTextField,JTextField fofaEmail,JTextField fofaKey,Map<String, JButton> initButtonsMap ){


        try {
            String fofaEmailLine = accounts.readLine();
            String fofaKeyLine = accounts.readLine();

            // 检查是否有内容需要解析和赋值
            if (fofaEmailLine != null && fofaKeyLine != null)
            {
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

                        String key = parts[0].substring(1, parts[0].length()-1).trim();

                        String value = parts[1].substring(1, parts[1].length()-2).trim();
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
                dataRow.createCell(j).setCellValue(table.getValueAt(i, j).toString());
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
}