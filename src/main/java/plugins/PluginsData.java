package plugins;

import java.util.List;
import java.util.Map;

public class PluginsData {
}
class AutoMode {
    String flow;
}
class Setting {
    Run Run;
    About About;
}
class Run {
    String path;
    Map<String, String> params;
    String inputFile;
    Map<String, String> inputTarget;
    String outputFile;
    List<String> outputTarget;
}
class About {
    String project;
    String address;
    String author;
    String version;
    String update;
}