package plugins;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.dongliu.commons.Sys;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AutoModeInitFile {

    private static final String FILE_PATH = "./plugins/AutoMode.json";
    private static final String CONTENT = "{\n\t\"flow\": \"httpx -> ip2domain -> domain2icp\"\n}";

    public String getFlow() throws IOException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            createFile(file);
            System.out.println("[+] 自动模式配置文件创建成功");
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
}