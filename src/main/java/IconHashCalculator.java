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

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;

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

        JPanel firstRow = new JPanel();
        calculateButton = new JButton("计算");
        copyButton = new JButton("复制");

        calculateButton.setFocusPainted(false); // 添加这一行来取消焦点边框的绘制
        calculateButton.setFocusable(false);  // 禁止了按钮获取焦点，因此按钮不会在被点击后显示为"激活"或"选中"的状态

        copyButton.setFocusPainted(false); // 添加这一行来取消焦点边框的绘制
        copyButton.setFocusable(false);  // 禁止了按钮获取焦点，因此按钮不会在被点击后显示为"激活"或"选中"的状态

        resultArea = new JTextArea();
        resultArea.setLineWrap(true); // 设置自动换行
        resultArea.setWrapStyleWord(true); // 设置换行不分割单词
        resultArea.setEditable(false);

        calculateButton.addActionListener(this::performCalculation);
        copyButton.addActionListener(this::copyToClipboard);

        // 创建第一行的面板
        firstRow.add(new JLabel("URL:"));
        firstRow.add(urlField);
        firstRow.add(calculateButton);
        firstRow.add(copyButton);

        // 创建第二行的面板
        JPanel secondRow = new JPanel();
        secondRow.add(new JLabel("可直接使用: http://xxx.xxx.xxx/"));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        // 将行面板添加到主面板
        panel.add(firstRow);
        panel.add(secondRow);

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
        String faviconUrl;

        if (!url.endsWith("/favicon.ico")) {
            faviconUrl = url + "/favicon.ico";
        } else {
            faviconUrl = url;
        }
        // 第一步：提取根目录下图标

        try {
            Request faviconRequest = new Request.Builder()
                    .url(faviconUrl)
                    .header("User-Agent", ua[(new SecureRandom()).nextInt(ua.length)])
                    .build();

            try (Response faviconResponse = unsafeClient.newCall(faviconRequest).execute()) {
                if (faviconResponse.isSuccessful()) {
                    byte[] bytes = faviconResponse.body().bytes();
                    String encoded = Base64.getMimeEncoder().encodeToString(bytes);
                    String hash = getIconHash(encoded);
                    result.put("msg", "icon_hash=\"" + hash + "\"");
                    return result;
                }
            }

            // 第二步：如果根目录下不存在图标，访问网站，获取html页面，获取head中的 link标签的ico 路径
            Request pageRequest = new Request.Builder()
                    .url(url)
                    .header("User-Agent", ua[(new SecureRandom()).nextInt(ua.length)])
                    .build();

            try (Response pageResponse = unsafeClient.newCall(pageRequest).execute()) {
                if (!pageResponse.isSuccessful()) throw new IOException("Unexpected code " + pageResponse);

                Document doc = Jsoup.parse(pageResponse.body().string());
                Elements icons = doc.head().select("link[href~=.+\\.(ico|png)]");
                if (!icons.isEmpty()) {
                    String iconHref = icons.attr("href");
                    // Resolve the absolute URL if necessary
                    iconHref = resolveUrl(url, iconHref);
                    // Fetch the favicon using the URL from the <link> tag
                    Request iconRequest = new Request.Builder()
                            .url(iconHref)
                            .header("User-Agent", ua[(new SecureRandom()).nextInt(ua.length)])
                            .build();
                    try (Response iconResponse = unsafeClient.newCall(iconRequest).execute()) {
                        if (!iconResponse.isSuccessful()) throw new IOException("Unexpected code " + iconResponse);
                        byte[] iconBytes = iconResponse.body().bytes();
                        String encodedIcon = Base64.getMimeEncoder().encodeToString(iconBytes);
                        String hash = getIconHash(encodedIcon);
                        result.put("msg", "icon_hash=\"" + hash + "\"");
                        return result;
                    }
                } else {
                    throw new IOException("No favicon found");
                }
            }
        } catch (IOException e) {
            result.put("code", "error");
            result.put("msg", e.getMessage());
            return result;
        }
    }

    //    这个 resolveUrl 函数用于将相对 URL 转换为绝对 URL。在处理 HTML 文档中的链接时特别有用，
    //    因为 HTML 元素中的 href 属性可能包含绝对路径、相对路径或协议相对路径（以 // 开头）。
    //    这个函数确保无论原始 href 是何种形式，都能得到 favicon 的绝对 URL
    private static String resolveUrl(String baseUrl, String relativeUrl) {
        if (relativeUrl.startsWith("http")) {
            return relativeUrl;
        } else if (relativeUrl.startsWith("//")) {
            return "http:" + relativeUrl;
        } else if (relativeUrl.startsWith("/")) {
            return baseUrl + relativeUrl;
        } else {
            return baseUrl + "/" + relativeUrl;
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
            final TrustManager[] trustAllCerts = new TrustManager[]{
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
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}