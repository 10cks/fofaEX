![image](https://github.com/10cks/fofaEX/assets/47177550/4baead1c-b329-48d4-ab31-a5975057abcd)

![](https://badgen.net/static/language/Java/orange?icon=github)
![](https://badgen.net//github/license/10cks/fofaEX)
![](https://badgen.net/github/releases/10cks/fofaEX/github/releases/)
![](https://badgen.net/github/release/10cks/fofaEX/stable)

## 简介

FOFA EX 是一款基于fofa api实现的红队综合利用工具，可基于模板进行插件加载，目前集成了httpX可进行fofa搜索结果一键探活。

集成了 fofa 官方的四十个 api 接口，增加搜索数量调整、翻页、iconHash生成、搜索耗时统计、当前用户个人账户信息查询等功能，查询结果可实施编辑与表内搜索，可进行导出； 增加快捷语法编辑记录功能，可将收录的语法进行保存与快捷输入；右键支持当前搜索结果一键打开链接等功能。

[更新日志](https://github.com/10cks/fofaEX/blob/master/docs/update.md) [问题修复](https://github.com/10cks/fofaEX/blob/master/docs/issues.md) [第三方插件](https://github.com/10cks/fofaEX/blob/master/docs/plugins.md)

![image](https://github.com/10cks/fofaEX/assets/47177550/b1c91436-e8e7-463f-ac6d-4ea2ef737604)

## 优势

1. 更多的默认数据查询：默认查询全部数据（fofa_viewer 仅默认查询近一年数据）

![image](https://github.com/10cks/fofaEX/assets/47177550/787106c1-7238-4531-8ab4-77de1d58f1d6)

2. 快捷保存查询语法，便于HW或SRC挖掘
3. 全部 API 接口的支持，界面可选择接口显示范围
4. 查询结果在线编辑导出，后续会为右键添加更多新功能
5. 可自动化调用第三方插件，目前持续开发中：当前展示为 httpX 一键探活 fofa 搜索结果，可通过设置plugins/httpxSetting.json来设置导出选项：

![image](https://github.com/10cks/fofaEX/assets/47177550/52cdea65-ea84-4235-96d1-228d6de46d7e)

运行 httpX 会自动弹出单独的运行结果面板：

![image](https://github.com/10cks/fofaEX/assets/47177550/07491450-3c1c-4e8c-b19a-04c99c8cf8c6)

## 运行

该程序使用 Java11 编写，V2.2已使用插件模式可进行一键探活：

[最新发布版本点击下载](https://github.com/10cks/fofaEX/releases/download/2.2/fofaEX_v2_2.zip) [V2.2]

[最新测试版本点击下载](https://github.com/10cks/fofaEX/releases/download/2.1/fofaEX_v2_1_pre.zip) [V2.1]




请使用编码启动：
```
java "-Dfile.encoding=UTF-8" -jar .\fofaEX.jar
```

## 登录模式：账户设置

客户端需输入邮箱与key，第一次登录后保存账户会将配置文件生成在本地 accounts.txt 文件中（当前fofa输入key就可以调用API，无需设置邮箱）：

![image](https://github.com/10cks/fofaEX/assets/47177550/89c472c1-3330-4147-89b1-ae21b35aba9e)

检查账户功能可查看当前账户信息（会员显示点数为"-1"是正常现象）：

![image](https://github.com/10cks/fofaEX/assets/47177550/1742229e-a585-491d-8f24-544eb8e15f3b)

## API 搜索功能

当前已提供以下 api 搜索功能（部分功能取决与当前账户权限）：
```
ip,port,protocol,country,country_name,region,city,longitude,latitude,as_number,as_organization,host,domain,os,server,icp,
title,jarm,header,banner,base_protocol,link,certs_issuer_org,certs_issuer_cn,certs_subject_org,certs_subject_cn,tls_ja3s,
tls_version,product,product_category,version,lastupdatetime,cname,icon_hash,certs_valid,cname_domain,body,icon,fid,structinfo
```
默认使用常用的7个选项，可进行勾选或取消：

![image](https://github.com/10cks/fofaEX/assets/47177550/bea065ab-2d66-4397-b79e-aab986f61535)

fofa api 官方链接：https://fofa.info/api

## 快捷输入功能

按钮单击为快速输入，输入后显示为红色高亮；再次点击则撤回输入，颜色恢复。
用户可实时新增、编辑、删除按钮，按钮配置文件保存在当前目录 rules.txt 文件中。

![image](https://github.com/10cks/fofaEX/assets/47177550/979ba680-98a4-403d-84be-af0f096b829c)

## iconHash 计算

可通过直接输入：
https://baidu.com/ 或者 https://baidu.com/favicon.ico 来计算图标哈希值：

![image](https://github.com/10cks/fofaEX/assets/47177550/601744d2-2fef-4930-8ec5-969bcbb50835)

## 表格操作

当前表格中，右键集成了部分功能，其他功能将后续更新。

![image](https://github.com/10cks/fofaEX/assets/47177550/d0e3c7dd-b733-4bb3-8dd3-889b5e8af4f9)

## 翻页功能及搜索计时

右下角统计当前表格数据与全部数据占比，显示当前页面数及本次搜索耗时。

![image](https://github.com/10cks/fofaEX/assets/47177550/65705cba-a8e1-494b-9444-b6a68b5bcb89)

## 导出功能

导出excel表会以“全部数据sheet+各列去空sheet”的形式放在一个表中，方便第三方工具直接使用数据：

![image](https://github.com/10cks/fofaEX/assets/47177550/1d0d4513-0168-4154-9dce-e28905826f4e)

![image](https://github.com/10cks/fofaEX/assets/47177550/7e8ab7b6-dafe-4244-9dd6-a762963d2bd4)



## 插件模式

当前集成了 httpX 插件（windows平台），目录结构为：

```
.
├── fofaEX.jar
├── plugins
│   ├── AllPlugins.json
│   └── httpx
│       ├── httpx.exe
│       └── httpxSetting.json
├── rules.txt
└── run.bat
```

AllPlugins.json 设置插件开关，false 关闭插件，true 打开插件：

```
{
    "dirsearch":false,
    "httpx":true
}
```

httpxSetting.json 设置 httpX 的运行配置：（配置文件名需要为：插件名 + Setting.json，插件名需要与 AllPlugins.json 中的一致）

```
{
    "Run":{
        "Path":"./plugins/httpx/httpx.exe",
        "Params":{
            "-duc":"",
            "-l":"./plugins/httpx/input.txt",
            "-status-code":"",
            "-o":"./plugins/httpx/HttpX.json",
            "-nc":"",
            "-j":""
        },
        "InputFile":"./coredata/FofaEX.json",
        "InputTarget":{
            "selectParam":"-l",
            "selectColumn":"link"
        },
        "OutputFile":"./plugins/httpx/HttpX.json",
        "OutputTarget":["url","port","title","status_code"]
    },
    "About":{
        "Project": "httpX",
        "Address": "https://github.com/projectdiscovery/httpx",
        "Author": "ProjectDiscovery",
        "Version": "v1.3.7",
        "Update": "2023.11.13"
    }
}
```

1. "Path" 指定程序路径
2. "Params" 指定程序运行默认参数
3. "InputFile" 指定 fofaEX 使用 API 查到的数据
4. "InputTarget" 指定 httpX 产生文件的参数与fofa数据中需要进行扫描的列（此处为link）
5. "OutputFile" 指定 httpX 运行后产生文件保存的位置
6. "OutputTarget" 指定 fofaEX 在插件页面所展示的列内容

PS：coredata 目录为核心交换文件目录，此功能后续会进行使用，目前请忽略。

## 关于项目

目前项目还在开发中，有很多 idea 还在逐步实现。后续打算该平台集成第三方工具来进行一键化操作，也欢迎各位师傅提出想法与建议。

## FQA

> 是否支持 java8？

当前版本不支持，后续会增加java8版本支持

> 是否支持鹰图等多平台API？

作为红队工具，这个后续是一定会有的，会将更多的 API 功能以插件形式集成进 fofa EX 中。

> 关于免账号登录模式

适用于fofaEX的插件目前仅供内部使用。

## 致谢

###  

在开发中有很多师傅帮了我答疑解惑，加速了项目的开发，在此非常感谢下面的各位师傅的帮助：

[Mechoy](https://github.com/Mechoy) [XinCaoZ](https://github.com/XinCaoZ) [ha1yu](https://github.com/ha1yu) [wavesky](https://github.com/wave-to) [gh0stkey](https://github.com/gh0stkey/HaE)

### FOFA 共创者计划

FofaEX 已加入 [FOFA 共创者计划](https://fofa.info/development)，感谢 FOFA 提供的账号支持。

![](https://user-images.githubusercontent.com/40891670/209631625-f73811b0-a26a-4a42-8158-e5061464481d.png)

### JetBrains OS licenses
fofaEX had been being developed with IDEA IDE under the free JetBrains Open Source license(s) granted by JetBrains s.r.o., hence I would like to express my thanks here.

## 参考链接

https://github.com/fofapro/fofa_view

https://github.com/wgpsec/fofa_viewer


