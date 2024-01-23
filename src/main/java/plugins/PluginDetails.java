package plugins;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluginDetails {
    private String inputFile;
    private String selectColumn;

    // 这里可以加入其他你需要的属性

    // getter和setter方法
    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = extractFileName(inputFile);
    }

    public String getSelectColumn() {
        return selectColumn;
    }

    public void setSelectColumn(String selectColumn) {
        this.selectColumn = selectColumn;
    }

    static PluginDetails getDetailsFromJson(String pluginJsonPath) {
        Gson gson = new Gson();
        PluginDetails pluginDetails = new PluginDetails();
        try (FileReader reader = new FileReader(pluginJsonPath)) {
            Map<String, Object> jsonMap = gson.fromJson(reader, Map.class);
            Map<String, Object> runMap = (Map<String, Object>) jsonMap.get("Run");

            String inputFile = (String) runMap.get("InputFile");
            pluginDetails.setInputFile(inputFile);

            Map<String, Object> inputTargetMap = (Map<String, Object>) runMap.get("InputTarget");
            String selectColumn = (String) inputTargetMap.get("selectColumn");
            pluginDetails.setSelectColumn(selectColumn);

            return pluginDetails;

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private String extractFileName(String filePath) {
        Pattern pattern = Pattern.compile(".*/(.*)(?=\\.json)");
        Matcher matcher = pattern.matcher(filePath);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}