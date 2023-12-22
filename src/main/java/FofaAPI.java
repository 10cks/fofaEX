import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class FofaAPI {
    public static List<String> getFieldsResult(String fofaDomain, String fields, String qbase64, String email, String key, String paramName, int size) throws Exception {

        String fieldsUrl = "/api/v1/search/all?fields=";
        String base64Url = "&qbase64=";

        String apiUrl = fofaDomain + fieldsUrl + fields + base64Url + Base64.getEncoder().encodeToString(qbase64.getBytes()) + "&email=" + email + "&key=" + key + "&size=" + size;
        URL url = new URL(apiUrl);

        System.out.println(fields + " " + url);

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

    public static JSONObject getAllJsonResult(String fofaDomain, String email, String key, String qbase64, String fieldsInput, int size, int page ,boolean full) throws Exception {

        String base64Url = "qbase64=";
        String fieldsUrl = "/api/v1/search/all?";

        if (!(fieldsInput == null)) {
            String encodedFieldsInput = URLEncoder.encode("host,ip,port" + fieldsInput, StandardCharsets.UTF_8);
            fieldsUrl = "/api/v1/search/all?fields=" + encodedFieldsInput + "&";
        }

        String apiUrl = fofaDomain + fieldsUrl + base64Url + Base64.getEncoder().encodeToString(qbase64.getBytes()) + "&email=" + email + "&key=" + key + "&size=" + size + "&page=" + page + "&full=" + full;
        URL url = new URL(apiUrl);

        System.out.println("Query: " + url);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        if (connection.getResponseCode() != 200) {
            throw new RuntimeException("Failed: HTTP error code : " + connection.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String output;
        StringBuilder responseBuilder = new StringBuilder();
        while ((output = br.readLine()) != null) {
            responseBuilder.append(output);
        }

        connection.disconnect();
        // 将response字符串转换为JSON对象
        final String response = responseBuilder.toString();

        return new JSONObject(response);
    }

    public static Object getValueFromJson(JSONObject json, String key) {

        // 首先检查json对象是否为null
        if (json == null) {
            return null; // 直接返回null，不进行后续操作
        }

        Object value = json.opt(key); // 使用opt来避免JSONException

        if (value instanceof JSONArray) {
            // 如果值是JSONArray，转换为List<List<String>>
            JSONArray jsonArray = (JSONArray) value;
            List<List<String>> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray subArray = jsonArray.optJSONArray(i);
                if (subArray == null) {
                    continue; // 如果不是JSONArray，则跳过
                }
                List<String> subList = new ArrayList<>();
                for (int j = 0; j < subArray.length(); j++) {
                    subList.add(subArray.optString(j)); // 使用optString来避免JSONException
                }
                list.add(subList);
            }
            return list;
        } else if (value instanceof Integer) {
            // 如果值是Integer，直接返回
            return (Integer) value;
        } else if (value instanceof Double) {
            // 如果值是Double，直接返回
            return (Double) value;
        } else if (value instanceof Boolean) {
            // 如果值是Boolean，直接返回
            return (Boolean) value;
        } else if (value instanceof String) {
            // 如果值是String，直接返回
            return (String) value;
        } else {
            // 如果键不存在或值为null，返回null
            return null;
        }
    }

    public static List<String> getColumn(List<List<String>> matrix, int index) {

        List<String> getColumn = new ArrayList<>();
        for (List<String> row : matrix) {
            if (!row.isEmpty()) { // 确保至少有一个元素
                getColumn.add(row.get(index)); // 添加第一列的元素
            }
        }
        return getColumn;
    }
}
