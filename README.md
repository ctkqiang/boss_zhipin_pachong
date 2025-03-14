
<div align="center">
  <img src="./assets/logo.svg" alt="2D05 Logo" width="150"/>
  <h1>BOSS 直聘爬虫</h1>
  <p>
    <img src="https://img.shields.io/badge/版本-1.0.0-blue.svg" alt="version"/>
    <img src="https://img.shields.io/badge/许可证-Mulan PSL-green.svg" alt="license"/>
    <img src="https://img.shields.io/badge/平台-MacOS%20%7C%20Linux | Windows-lightgrey.svg" alt="platform"/>
    <img src="https://img.shields.io/badge/语言-Java-orange.svg" alt="language"/>
    
  </p>
  <p>
    <a href="#功能特点">功能</a> •
    <a href="#安装方法">安装</a> •
    <a href="#使用方法">使用</a> •
    <a href="#系统架构">架构</a> •
    <a href="#免责声明">免责声明</a>
  </p>
</div>


基于 Spring Boot 的 BOSS 直聘职位信息爬虫系统，提供自动化的职位信息采集和数据处理功能。系统采用现代化的技术栈，包括 Spring Boot 框架、SQLite 数据库和 RESTful API 设计，实现了智能的反爬虫策略和高效的数据解析能力。该系统可以帮助求职者和 HR 快速获取 BOSS 直聘平台的职位信息，支持关键词搜索，并将数据以结构化的方式存储，便于后续分析和使用。


## 功能特点

- 支持关键词搜索职位信息
- 自动解析职位详情数据
- REST API接口支持
- SQLite本地数据存储
- 智能反爬虫处理

## 系统要求

| 组件        | 版本要求 |
| ----------- | -------- |
| Java        | 11+      |
| Maven       | 3.9.6+   |
| Spring Boot | 2.7.18   |
| SQLite      | 3.x      |


## 快速开始

1. 克隆项目
```bash
git clone https://github.com/ctkqiang/boss_zhipin_pachong.git
````

2. 编译运行

```bash
cd boss_zhipin_pachong
mvn spring-boot:run
```

3. 访问 API

```bash
curl "http://localhost:8080/api/jobs?keyword=Java"
```

## API 文档

### 主页面

**请求信息：**
- 方法：`GET`
- 路径：`/`
- Content-Type: `text/html`

![index](./assets/index.png)

**响应：**
返回职位搜索主页面，包含搜索表单和实时职位列表展示。

### 数据分析页面

**请求信息：**
- 方法：`GET`
- 路径：`/analysis`
- Content-Type: `text/html`

![analysis](./assets/analysis.png)

**响应：**
返回职位数据分析页面，展示：
- 薪资分布统计
- 技能需求热度
- 地区分布情况
- 经验要求分析

### 获取职位列表

**请求信息：**
- 方法：`GET`
- 路径：`/api/jobs`
- Content-Type: `application/json`

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| keyword | String | 是 | 职位搜索关键词 | "Java 开发" |

**响应格式：**
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "title": "高级Java开发工程师",
      "company": "阿里巴巴",
      "salary": "25-35K",
      "location": "杭州",
      "experience": "3-5年",
      "skills": ["Java", "Spring Boot", "MySQL"]
    }
  ]
}
```

## 系统运行示例

<details>
<summary>🚀 点击查看运行日志</summary>

```console
. \_**\_ \_ ** \_ \_
/\\ / **_'_ ** \_ _(_)_ \_\_ \_\_ _ \ \ \ \
( ( )\_** | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/ \_**)| |_)| | | | | || (_| | ) ) ) )
' |\_**\_| .**|_| |_|_| |_\__, | / / / /
=========|_|==============|_\_\_/=/_/_/_/
:: Spring Boot :: (v2.7.18)

2025-03-14 21:56:29.470 INFO 66470 --- [ restartedMain] x.c.b.BossZhipinPachongApplication : Starting BossZhipinPachongApplication using Java 20.0.1 on M2.local with PID 66470 (/Users/ctkqiang/Documents/上饶满星科技有限公司/boss_zhipin_pachong/target/classes started by ctkqiang in /Users/ctkqiang/Documents/上饶满星科技有限公司/boss_zhipin_pachong)
2025-03-14 21:56:29.471 INFO 66470 --- [ restartedMain] x.c.b.BossZhipinPachongApplication : No active profile set, falling back to 1 default profile: "default"
2025-03-14 21:56:29.642 INFO 66470 --- [ restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer : Tomcat initialized with port(s): 8080 (http)
2025-03-14 21:56:29.643 INFO 66470 --- [ restartedMain] o.apache.catalina.core.StandardService : Starting service [Tomcat]
2025-03-14 21:56:29.643 INFO 66470 --- [ restartedMain] org.apache.catalina.core.StandardEngine : Starting Servlet engine: [Apache Tomcat/9.0.83]
2025-03-14 21:56:29.657 INFO 66470 --- [ restartedMain] o.a.c.c.C.[Tomcat].[localhost].[/] : Initializing Spring embedded WebApplicationContext
2025-03-14 21:56:29.657 INFO 66470 --- [ restartedMain] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 183 ms
2025-03-14 21:56:29.675 INFO 66470 --- [ restartedMain] x.c.b.Database.DatabaseHandler : 职位表创建成功
2025-03-14 21:56:29.675 INFO 66470 --- [ restartedMain] x.c.b.Database.DatabaseHandler : 数据库连接成功
2025-03-14 21:56:29.676 INFO 66470 --- [ restartedMain] x.c.b.Database.DatabaseHandler : 职位表创建成功
2025-03-14 21:56:29.676 INFO 66470 --- [ restartedMain] x.c.b.Database.DatabaseHandler : 数据库连接成功
2025-03-14 21:56:29.715 INFO 66470 --- [ restartedMain] o.s.b.a.w.s.WelcomePageHandlerMapping : Adding welcome page template: index
2025-03-14 21:56:29.749 INFO 66470 --- [ restartedMain] o.s.b.d.a.OptionalLiveReloadServer : LiveReload server is running on port 35729
2025-03-14 21:56:29.781 INFO 66470 --- [ restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer : Tomcat started on port(s): 8080 (http) with context path ''
2025-03-14 21:56:29.786 INFO 66470 --- [ restartedMain] x.c.b.BossZhipinPachongApplication : Started BossZhipinPachongApplication in 0.374 seconds (JVM running for 119.091)
2025-03-14 21:56:29.789 INFO 66470 --- [ restartedMain] .ConditionEvaluationDeltaLoggingListener : Condition evaluation unchanged
2025-03-14 21:56:29.790 INFO 66470 --- [ restartedMain] x.c.b.BossZhipinPachongApplication : [BOSS 直聘] 系统已启动...
2025-03-14 21:56:29.792 INFO 66470 --- [ restartedMain] x.c.b.BossZhipinPachongApplication : ========= 系统信息 =========
2025-03-14 21:56:29.792 INFO 66470 --- [ restartedMain] x.c.b.BossZhipinPachongApplication : Java 版本: 20.0.1 (Oracle Corporation)
2025-03-14 21:56:29.792 INFO 66470 --- [ restartedMain] x.c.b.BossZhipinPachongApplication : 操作系统: Mac OS X 15.0.1 (aarch64)
2025-03-14 21:56:29.792 INFO 66470 --- [ restartedMain] x.c.b.BossZhipinPachongApplication : Maven 版本: 3.9.6
2025-03-14 21:56:29.792 INFO 66470 --- [ restartedMain] x.c.b.BossZhipinPachongApplication : Spring 版本: 5.3.31
2025-03-14 21:56:29.793 INFO 66470 --- [ restartedMain] x.c.b.BossZhipinPachongApplication : 应用名称: BOSS 直聘爬虫
2025-03-14 21:56:29.793 INFO 66470 --- [ restartedMain] x.c.b.BossZhipinPachongApplication : 开发者: 钟智强
2025-03-14 21:56:29.793 INFO 66470 --- [ restartedMain] x.c.b.BossZhipinPachongApplication : ===========================
2025-03-14 21:56:39.845 INFO 66470 --- [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/] : Initializing Spring DispatcherServlet 'dispatcherServlet'
2025-03-14 21:56:39.846 INFO 66470 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet : Initializing Servlet 'dispatcherServlet'
2025-03-14 21:56:39.852 INFO 66470 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet : Completed initialization in 6 ms
2025-03-14 21:56:50.042 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 成功解析职位: | | 12-15K | Xptah
2025-03-14 21:56:50.043 ERROR 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 解析职位条目时出错: Index 4 out of bounds for length 4
2025-03-14 21:56:50.043 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 成功解析职位: | | 18-35K·15 薪 | 半糖去冰科技
2025-03-14 21:56:50.043 ERROR 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 解析职位条目时出错: Index 4 out of bounds for length 4
2025-03-14 21:56:50.043 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 成功解析职位: | | 20-40K | 蔚蓝智能科技
2025-03-14 21:56:50.043 ERROR 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 解析职位条目时出错: Index 4 out of bounds for length 4
2025-03-14 21:56:50.043 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 成功解析职位: | | 30-40K·16 薪 | 京东物流
2025-03-14 21:56:50.044 ERROR 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 解析职位条目时出错: Index 4 out of bounds for length 4
2025-03-14 21:56:50.044 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 成功解析职位: | | 10-15K·13 薪 | 池骋网络
2025-03-14 21:56:50.044 ERROR 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 解析职位条目时出错: Index 4 out of bounds for length 4
2025-03-14 21:56:50.044 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 成功解析职位: | | 6-11K | 山东亚云信息技术
2025-03-14 21:56:50.044 ERROR 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 解析职位条目时出错: Index 4 out of bounds for length 4
2025-03-14 21:56:50.044 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 成功解析职位: | | 15-16K | 宇泛智能
2025-03-14 21:56:50.044 ERROR 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 解析职位条目时出错: Index 4 out of bounds for length 4
2025-03-14 21:56:50.044 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 成功解析职位: | | 18-30K | 千讯云创
2025-03-14 21:56:50.044 ERROR 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 解析职位条目时出错: Index 4 out of bounds for length 4
2025-03-14 21:56:50.044 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 成功解析职位: | | 7-12K | 绘智科教
2025-03-14 21:56:50.044 ERROR 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 解析职位条目时出错: Index 4 out of bounds for length 4
2025-03-14 21:56:50.045 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 成功解析职位: | | 15-30K | 北京经华智业教育...
2025-03-14 21:56:50.045 ERROR 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 解析职位条目时出错: Index 4 out of bounds for length 4
2025-03-14 21:56:50.045 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : 共找到 10 个职位信息
2025-03-14 21:56:50.045 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : ========== 职位列表 ==========
2025-03-14 21:56:50.045 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper :
职位:
公司: Xptah
薪资: 12-15K
地点:
经验: 1-3 年
技能: 成都, 大专, Flutter

---

2025-03-14 21:56:50.045 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper :
职位:
公司: 半糖去冰科技
薪资: 18-35K
地点:
经验: 1-3 年
技能: 北京, 本科, Android 开发经验

---

2025-03-14 21:56:50.045 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper :
职位:
公司: 蔚蓝智能科技
薪资: 20-40K
地点:
经验: 3-5 年
技能: 南京, 本科, C++, 通信相关专业, 技术方案规划

---

2025-03-14 21:56:50.045 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper :
职位:
公司: 京东物流
薪资: 30-40K
地点:
经验: 5-10 年
技能: 北京, 本科, flutter, 跨端开发

---

2025-03-14 21:56:50.045 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper :
职位:
公司: 池骋网络
薪资: 10-15K
地点:
经验: 1-3 年
技能: 广州, 大专, 功能开发, 混合开发经验, 混合开发

---

2025-03-14 21:56:50.045 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper :
职位:
公司: 山东亚云信息技术
薪资: 6-11K
地点:
经验: 1-3 年
技能: 日照, 大专, Flutter

---

2025-03-14 21:56:50.045 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper :
职位:
公司: 宇泛智能
薪资: 15-16K
地点:
经验: 3-5 年
技能: 杭州, 本科, Android 客户端产品研发, 计算机/软件工程相关专业, 通信相关专业

---

2025-03-14 21:56:50.045 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper :
职位:
公司: 千讯云创
薪资: 18-30K
地点:
经验: 年终奖金丰厚
技能: 深圳, 大专, Android 开发经验, Android 客户端产品研发

---

2025-03-14 21:56:50.045 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper :
职位:
公司: 绘智科教
薪资: 7-12K
地点:
经验: 1-3 年
技能: 广州, 大专, Java, CSS, 后端开发经验

---

2025-03-14 21:56:50.045 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper :
职位:
公司: 北京经华智业教育...
薪资: 15-30K
地点:
经验: 5-10 年
技能: 北京, 本科, Android 客户端产品研发, 架构设计/优化, Flutter

---

2025-03-14 21:56:50.045 INFO 66470 --- [nio-8080-exec-4] x.c.b.Controller.Scrapper : ============================
2025-03-14 21:56:50.062 INFO 66470 --- [nio-8080-exec-4] x.c.b.Database.DatabaseHandler : 成功保存 10 个职位信息
```
</details>


## 数据库结构

职位表(jobs):

| 字段名     | 类型      | 说明     | 约束                      |
| ---------- | --------- | -------- | ------------------------- |
| id         | INTEGER   | 自增主键 | PRIMARY KEY AUTOINCREMENT |
| title      | TEXT      | 职位名称 | NOT NULL                  |
| company    | TEXT      | 公司名称 | NOT NULL                  |
| salary     | TEXT      | 薪资范围 | -                         |
| location   | TEXT      | 工作地点 | -                         |
| experience | TEXT      | 经验要求 | -                         |
| skills     | TEXT      | 技能要求 | -                         |
| created_at | TIMESTAMP | 创建时间 | DEFAULT CURRENT_TIMESTAMP |


## 算法

![流程图](./doc/BOSS直聘爬虫系统流程.png)


## 注意事项

- 请合理控制爬取频率，避免对目标网站造成压力
- 仅用于学习和研究目的，请勿用于商业用途
- 遵守目标网站的 robots.txt 规则


## 许可证

本项目采用 **木兰宽松许可证 (Mulan PSL)** 进行许可。  
有关详细信息，请参阅 [LICENSE](LICENSE) 文件。

[![License: Mulan PSL v2](https://img.shields.io/badge/License-Mulan%20PSL%202-blue.svg)](http://license.coscl.org.cn/MulanPSL2)


## 🌟 开源项目赞助计划

### 用捐赠助力发展

感谢您使用本项目！您的支持是开源持续发展的核心动力。  
每一份捐赠都将直接用于：  
✅ 服务器与基础设施维护  
✅ 新功能开发与版本迭代  
✅ 文档优化与社区建设

点滴支持皆能汇聚成海，让我们共同打造更强大的开源工具！

---

### 🌐 全球捐赠通道

#### 国内用户

<div align="center" style="margin: 40px 0">

<div align="center">
<table>
<tr>
<td align="center" width="300">
<img src="https://github.com/ctkqiang/ctkqiang/blob/main/assets/IMG_9863.jpg?raw=true" width="200" />
<br />
<strong>🔵 支付宝</strong>
</td>
<td align="center" width="300">
<img src="https://github.com/ctkqiang/ctkqiang/blob/main/assets/IMG_9859.JPG?raw=true" width="200" />
<br />
<strong>🟢 微信支付</strong>
</td>
</tr>
</table>
</div>
</div>

#### 国际用户

<div align="center" style="margin: 40px 0">
  <a href="https://qr.alipay.com/fkx19369scgxdrkv8mxso92" target="_blank">
    <img src="https://img.shields.io/badge/Alipay-全球支付-00A1E9?style=flat-square&logo=alipay&logoColor=white&labelColor=008CD7">
  </a>
  
  <a href="https://ko-fi.com/F1F5VCZJU" target="_blank">
    <img src="https://img.shields.io/badge/Ko--fi-买杯咖啡-FF5E5B?style=flat-square&logo=ko-fi&logoColor=white">
  </a>
  
  <a href="https://www.paypal.com/paypalme/ctkqiang" target="_blank">
    <img src="https://img.shields.io/badge/PayPal-安全支付-00457C?style=flat-square&logo=paypal&logoColor=white">
  </a>
  
  <a href="https://donate.stripe.com/00gg2nefu6TK1LqeUY" target="_blank">
    <img src="https://img.shields.io/badge/Stripe-企业级支付-626CD9?style=flat-square&logo=stripe&logoColor=white">
  </a>
</div>

---

### 📌 开发者社交图谱

#### 技术交流

<div align="center" style="margin: 20px 0">
  <a href="https://github.com/ctkqiang" target="_blank">
    <img src="https://img.shields.io/badge/GitHub-开源仓库-181717?style=for-the-badge&logo=github">
  </a>
  
  <a href="https://stackoverflow.com/users/10758321/%e9%92%9f%e6%99%ba%e5%bc%ba" target="_blank">
    <img src="https://img.shields.io/badge/Stack_Overflow-技术问答-F58025?style=for-the-badge&logo=stackoverflow">
  </a>
  
  <a href="https://www.linkedin.com/in/ctkqiang/" target="_blank">
    <img src="https://img.shields.io/badge/LinkedIn-职业网络-0A66C2?style=for-the-badge&logo=linkedin">
  </a>
</div>

#### 社交互动

<div align="center" style="margin: 20px 0">
  <a href="https://www.instagram.com/ctkqiang" target="_blank">
    <img src="https://img.shields.io/badge/Instagram-生活瞬间-E4405F?style=for-the-badge&logo=instagram">
  </a>
  
  <a href="https://twitch.tv/ctkqiang" target="_blank">
    <img src="https://img.shields.io/badge/Twitch-技术直播-9146FF?style=for-the-badge&logo=twitch">
  </a>
  
  <a href="https://github.com/ctkqiang/ctkqiang/blob/main/assets/IMG_9245.JPG?raw=true" target="_blank">
    <img src="https://img.shields.io/badge/微信公众号-钟智强-07C160?style=for-the-badge&logo=wechat">
  </a>
</div>

---

🙌 感谢您成为开源社区的重要一员！  
💬 捐赠后欢迎通过社交平台与我联系，您的名字将出现在项目致谢列表！
