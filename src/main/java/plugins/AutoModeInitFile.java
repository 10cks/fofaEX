package plugins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.dongliu.commons.Sys;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static plugins.PluginDetails.getDetailsFromJson;

public class AutoModeInitFile {
    private static final String FILE_PATH = "./plugins/AutoMode.json";
    private static final String CONTENT = "{\n\t\"flow\": \"httpx(FofaEX,link) -> ip2domain(FofaEX,ip) -> domain2icp(ip2domain,result_site)\"\n}";

    public String getFlow() throws IOException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            createFile(file);
            System.out.println("[+] 自动模式配置文件 AutoMode.json 创建成功");
        }
        return readFromFile();
    }
    private void createFile(File file) throws IOException {
        file.getParentFile().mkdirs();
        try(FileWriter writer = new FileWriter(file)) {
            writer.write(CONTENT);
        }
    }
    private String readFromFile() throws IOException {
        String jsonContent = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonContent, JsonObject.class);
        return jsonObject.get("flow").getAsString();
    }

    // 根据配置文件更新AutoMode.json
    public void processJson() throws IOException {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        AutoMode automode;
        try (Reader reader = new FileReader("./plugins/AutoMode.json")) {
            automode = gson.fromJson(reader, AutoMode.class);
        }

        List<String> flows = new ArrayList<>(List.of(automode.flow.split(" -> ")));
        String inputFile = "";
        String selectColumn = "";

        for(int i = 0; i < flows.size(); i++) {
            String flow = flows.get(i);
            if (flow.contains("(")) {
                String prevSettingName = flow.substring(0, flow.indexOf("("));
                String prevSettingFileName = "./plugins/" + prevSettingName + "/" + prevSettingName + "Setting.json";
                PluginDetails pluginDetails = getDetailsFromJson(prevSettingFileName);
                assert pluginDetails != null;
                inputFile = pluginDetails.getInputFile();
                selectColumn = pluginDetails.getSelectColumn();
                flow = prevSettingName + "(" + inputFile + "," + selectColumn + ")";
                flows.set(i, flow);
            }
        }
        // 更新
        automode.flow = String.join(" -> ", flows);

        try (FileWriter writer = new FileWriter("./plugins/AutoMode.json")) {
            // 检测结果写回到文件中
            writer.write(gson.toJson(automode));
        }
    }

    public void setDetailsJson(Map<String, Object> jsonMap, String selectColumnValue) {
        Map<String, Object> runMap = (Map<String, Object>) jsonMap.get("Run");
        Map<String, Object> inputTargetMap = (Map<String, Object>) runMap.get("InputTarget");
        inputTargetMap.put("selectColumn", selectColumnValue);
    }
    public void setInputFile(Map<String, Object> settingMap, String inputFileValue) {
        Map<String, Object> runMap = (Map<String, Object>) settingMap.get("Run");
        runMap.put("InputFile", inputFileValue);
    }
    // 根据 AutoMode.json 更新配置文件
    public void updateSettingsJson() throws IOException {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        AutoMode automode;
        try (Reader reader = new FileReader("./plugins/AutoMode.json")) {
            automode = gson.fromJson(reader, AutoMode.class);
        }

        List<String> flows = new ArrayList<>(Arrays.asList(automode.flow.split(" -> ")));

        for (int i = 0; i < flows.size(); i++) {
            String flow = flows.get(i);
            if (flow.contains("(")) {
                String prevSettingName = flow.substring(0, flow.indexOf("("));
                String selectColumnArgs = flow.substring(flow.indexOf("(")+1, flow.indexOf(")"));
                String[] args = selectColumnArgs.split(",");
                String selectColumnValue = args.length > 1 ? args[1] : "";

                // 构建新的参数
                String inputFileValue = "./coredata/" + args[0] + ".json";
                String prevSettingFileName = "./plugins/" + prevSettingName + "/" + prevSettingName + "Setting.json";
                Map<String, Object> settingMap;
                try (Reader settingReader = new FileReader(prevSettingFileName)) {
                    settingMap = gson.fromJson(settingReader, Map.class);
                }

                // 更新json详细信息和InputFile
                setInputFile(settingMap, inputFileValue);
                setDetailsJson(settingMap, selectColumnValue);

                try (Writer writer = new FileWriter(prevSettingFileName)) {
                    writer.write(gson.toJson(settingMap));
                }
            }
        }
    }
}