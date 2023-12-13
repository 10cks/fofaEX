import com.google.common.hash.Hashing;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IconHashCalculator extends JFrame {

    private JTextField urlField;
    private JButton calculateButton;
    private JButton copyButton;
    private JTextArea resultArea;
    private static final String[] ua = new String[]{
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Safari/605.1.15",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.41 Safari/537.36 Edg/88.0.705.22"
    };

    public IconHashCalculator() {
        createUI();
    }

    private void createUI() {
        setTitle("图标哈希计算器");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true); // 这一行将窗体设置为始终置顶

        urlField = new JTextField(20);
        calculateButton = new JButton("计算");
        copyButton = new JButton("复制到剪贴板");

        calculateButton.setFocusPainted(false); // 添加这一行来取消焦点边框的绘制
        calculateButton.setFocusable(false);  // 禁止了按钮获取焦点，因此按钮不会在被点击后显示为"激活"或"选中"的状态

        copyButton.setFocusPainted(false); // 添加这一行来取消焦点边框的绘制
        copyButton.setFocusable(false);  // 禁止了按钮获取焦点，因此按钮不会在被点击后显示为"激活"或"选中"的状态

        resultArea = new JTextArea();
        resultArea.setEditable(false);

        calculateButton.addActionListener(this::performCalculation);
        copyButton.addActionListener(this::copyToClipboard);

        JPanel panel = new JPanel();
        panel.add(new JLabel("URL:"));
        panel.add(urlField);
        panel.add(calculateButton);
        panel.add(copyButton);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
    }

    private void performCalculation(ActionEvent event) {
        // 更改按钮文本，表明正在计算，并禁用按钮
        calculateButton.setText("计算中");
        calculateButton.setEnabled(false);

        // 创建并启动后台计算任务
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // 后台线程中执行的耗时计算
                HashMap<String, String> result = getFaviconHash(urlField.getText());
                // 如果返回结果中包含错误信息，则返回错误提示
                if (result.containsKey("code") && "error".equals(result.get("code"))) {
                    return "计算哈希值出错：" + result.get("msg");
                } else {
                    // 否则返回计算结果
                    return result.get("msg");
                }
            }

            @Override
            protected void done() {
                // 计算完成后的 UI 更新必须在事件派发线程中进行
                try {
                    String result = get(); // 获取 doInBackground() 的返回结果
                    resultArea.setText(result);
                } catch (Exception e) {
                    //e.printStackTrace();
                    resultArea.setText("计算哈希值出错");
                }
                // 无论计算成功与否，恢复按钮状态
                calculateButton.setText("计算");
                calculateButton.setEnabled(true);
            }
        };
        worker.execute(); // 启动 SwingWorker
    }

    private void copyToClipboard(ActionEvent event) {
        String result = resultArea.getText();
        StringSelection selection = new StringSelection(result);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }
    private static final OkHttpClient unsafeClient = createUnsafeOkHttpClient();
    public static HashMap<String, String> getFaviconHash(String url) {
        HashMap<String, String> result = new HashMap<>();
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", ua[(new SecureRandom()).nextInt(ua.length)]) // 确保ua数组已定义且有效
                .build();

        try (Response response = unsafeClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            byte[] bytes = response.body().bytes();
            if (bytes.length == 0) {
                result.put("code", "error");
                result.put("msg", "Empty response body");
                return result;
            }

            String encoded = Base64.getMimeEncoder().encodeToString(bytes);
            String hash = getIconHash(encoded);
            result.put("msg", "icon_hash=\"" + hash + "\"");
            return result;
        } catch (IOException e) {
            result.put("code", "error");
            result.put("msg", e.getMessage());
            return result;
        }
    }

    public static String getIconHash(String f) {
        int murmu = Hashing
                .murmur3_32()
                .hashString(f.replaceAll("\r", "") + "\n", StandardCharsets.UTF_8)
                .asInt();
        return String.valueOf(murmu);
    }

    private static final OkHttpClient createUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                            // No need to implement.
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                            // No need to implement.
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}