![image](https://github.com/10cks/fofaEX/assets/47177550/4baead1c-b329-48d4-ab31-a5975057abcd)


![](https://badgen.net/static/license/MIT/green/?icon=github)
![](https://badgen.net/static/language/Java/green?icon=github)
![](https://badgen.net/static/category/fofa-client/blue/)
![](https://badgen.net/static/category/redteam-tool/blue/)

## 简介

FOFA EX 是一款基于 fofa api 的图形化工具，在基础上集成了 fofa 官方的四十个 api 接口，增加搜索数量调整、翻页、iconHash生成、搜索耗时统计、当前用户个人账户信息查询等功能，查询结果可实施编辑与表内搜索，可进行导出；
增加快捷语法编辑记录功能，可将收录的语法进行保存与快捷输入；当前搜索结果支持一键打开链接。

![image](https://github.com/10cks/fofaEX/assets/47177550/b1c91436-e8e7-463f-ac6d-4ea2ef737604)

## 账户设置

客户端需输入邮箱与key，第一次登录后保存账户会将配置文件生成在本地 accounts.txt 文件中：
![image](https://github.com/10cks/fofaEX/assets/47177550/89c472c1-3330-4147-89b1-ae21b35aba9e)

检查账户功能可查看当前账户信息（会员显示点数为"-1"是正常现象）：
![image](https://github.com/10cks/fofaEX/assets/47177550/1742229e-a585-491d-8f24-544eb8e15f3b)


## API 搜索
当前已提供以下 api 搜索功能（部分功能取决与当前账户权限）：
```
ip,port,protocol,country,country_name,region,city,longitude,latitude,as_number,as_organization,host,domain,os,server,icp,
title,jarm,header,banner,base_protocol,link,certs_issuer_org,certs_issuer_cn,certs_subject_org,certs_subject_cn,tls_ja3s,
tls_version,product,product_category,version,lastupdatetime,cname,icon_hash,certs_valid,cname_domain,body,icon,fid,structinfo
```
默认使用常用的7个选项，可进行勾选或取消：

![image](https://github.com/10cks/fofaEX/assets/47177550/bea065ab-2d66-4397-b79e-aab986f61535)

fofa api 官方链接：https://fofa.info/api



