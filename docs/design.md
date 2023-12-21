# 设计规划

计划可使用第三方工具：

1. fofa-hack -> fofaExPluginLog/fofa-hack.json
2. httpX -> fofaExPluginLog/httpX.json
3. dirsearch -> fofaExPluginLog/dirsearch.json

第三方工具（输入类） -> toolName.json -> fofaEX

fofaEX  -> toolName.json -> 第三方工具（被输入类）

注册插件模板：
1. 插件名 httpX

生成：
1. 插件模式新增
   1. 运行
   2. 设置
   3. 关于
2. JTabbedPane 新增
3. 右键新增


程序运行 -> ./plugins/AllPlugins.txt -> httpx/httpxSetting.txt -> 界面生成

./plugins/AllPlugins.txt 内容：
```
httpx/httpxSetting.txt
...
```

httpxSetting.txt 配置文件内容：

```
PluginName:httpX

Path: // 程序执行的路径

Command: //两种模式，GUI 传递模式与配置文件默认模式
   // GUI 传递模式
   "参数":"名称" // 根据名称生成 JLabel 和 JTextField，输入的值最后会拼接为 “参数 值”
   // 配置文件默认模式
   "参数":"值"
   
Setting: // 打开此配置文件，此处设置为该文件路径

About:
   1. Project:
   2. Author:
   3. Version: 
   
```

