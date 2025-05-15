数字货币合约期权交易所解决方案
- Telegram: https://t.me/newkf21
  本源码仅限于交流学习，凡涉及到法律问题与本人无关，核心代码没有展示出来。

=====================================
---
### 提问和建议
- 使用Issuse，我们会及时跟进解答。
- Telegram: https://t.me/newkf21

# 捐赠：
#### 您的捐赠是我们开源最大的动力
- TRC20/USDT：TLRRCLjzKaNT4S3dBeYWCGU9PoUCvvYWZC

#### Coinc是一个数字货币交易系统，她使用目前最流行的Java框架和相关技术开发而成。

## 警告(自行脑补FBI warning 画面)

1. 运营一家交易所是非常不容易的.

   Coinc 框架可以使你很容易的建立一套数字货币交易系统，她是轻量级的交易所系统但功能全部完善。

2. 系统安全知识.

   Coinc 框架不能保护你的数字资产安全，也不能保证你的系统运行安全。在部署过程中，需要注意网络安全的设置，如果你不在行的话，可以找一个专业的运维人员。

3. 法律风险

- 法律风险第一条：不要触犯中华人民共和国的法律条例。
- 技术无罪，请在法律范围内使用Coinc。
- 如果你想使用Coinc作为商业应用，最好请个律师，确保你的商业应用在法律允许的范围内。一切用于商业化项目所带来的法律和经济问题，Coinc团队概不负责。

4. 你需要知道的基本知识

- 法律知识（安全第一条，法律最重要）
- Java知识（主要是spring）
- linux知识（CentOS、Ubuntu等等）
- 安全知识

### 主要技术

- 后端：Spring、SpringMVC、SpringBoot
- 数据库：Mysql
- 其他：redis
- 前端：Vue

### 测试环境
- 测试环境仅供学习参考！！！请各位学习的小伙伴合理运用。

- 测试环境地址 :
-PC官网 https://pc.coincheckusa.com 账户：abc123 密码：123456
-APP页面下载 https://www.coincheckusa.com/app 账户：abc123 密码：123456
-管理台 
### 开源许可协议
    Apache
---

##  模块介绍

1. echo2-admin

- 管理台，为基础模块，必须部署
- 依赖服务：无

2. echo2-api

- 提供用户相关的接口（如登录、注册、资产列表）,该模块为基础为基础模块，必须部署
- 依赖服务：mysql,redis,短信接口

3. 其他模块不要关心，主打的就是轻量级又满足需求

##  重点业务介绍

    后端框架的核心模块为 echo2-admin,echo2-api。
## 环境搭建
- Centos 6.8
- MySQL 5.5.16
- Redis-x64-3.2.100
- nginx-1.16.0
- JRE 8u241
- JDK 1.8
- Vue


## 服务部署准备

1. 项目用了Lombok插件，无论用什么IDE工具，请务必先安装Lombok插件
2. jdk版本1.8以上
3. 初始化sql在sql文件夹

## 修改服务配置文件
简单配置

```
各个模块/src/main/resources/dev/application-dev.yml
```

mysql数据库:

```
spring.datasource.**
```

reids

```
redis.**
```

### 服务启动
1. maven构建打包服务 

2. 将两个核心模块target文件夹下的XX.jar上传到自己的服务器

3. 先启动哪个都行

4.前端:部署pc官网 部署app页面 部署管理台页面。前端全部使用vue

## 商业版演示图
```
PC官网
```
#### ![Topology-Image](/Snipaste_2025-05-11_16-37-18.png)
#### ![Topology-Image](/Snipaste_2025-05-11_16-38-00.png)
```
管理台
```
#### ![Topology-Image](/Snipaste_2025-05-11_16-38-22.png)
```
APP下载页面
```
#### ![Topology-Image](/Snipaste_2025-05-11_16-39-08.png)

```
APP页面展示
```
#### ![Topology-Image](/photo_2025-05-12_00-01-36.jpg)
#### ![Topology-Image](/photo_2025-05-12_00-39-29.jpg)
