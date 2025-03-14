package xin.ctkqiang.boss_zhipin_pachong.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import xin.ctkqiang.boss_zhipin_pachong.Database.Constant;
import xin.ctkqiang.boss_zhipin_pachong.Database.DatabaseHandler;
import xin.ctkqiang.boss_zhipin_pachong.Model.Job;

import org.json.JSONObject;

/**
 * BOSS直聘职位爬虫服务类
 * 
 * 该类主要负责从BOSS直聘网站抓取职位信息，包括以下核心功能：
 * 1. 自动构造搜索URL并发送HTTP请求
 * 2. 智能解析返回的HTML和JSON数据
 * 3. 提取职位详细信息，包括：
 * - 职位名称
 * - 公司名称
 * - 工作地点
 * - 薪资范围
 * - 经验要求
 * - 技能要求
 * 4. 自动检测并处理反爬虫限制
 * 5. 将抓取的数据保存到SQLite数据库
 * 
 * 使用示例：
 * 
 * <pre>
 * Scrapper scrapper = new Scrapper();
 * List<Job> jobs = scrapper.ScrapeJob("Java开发");
 * </pre>
 * 
 * 技术特点：
 * - 使用JSoup进行HTML解析
 * - 支持JSON格式数据处理
 * - 采用Spring框架进行依赖注入
 * - 实现数据持久化存储
 * 
 * 注意事项：
 * 1. 访问频率限制：建议每次请求间隔不少于1秒
 * 2. 反爬虫处理：遇到code=35时会抛出IOException
 * 3. 数据完整性：确保所有必要字段都已正确提取
 * 4. 内存管理：大量数据时注意内存使用
 */
@Service
public class Scrapper {
    @Autowired
    private DatabaseHandler databaseHandler;
    private static final Logger LOG = LoggerFactory.getLogger(Scrapper.class);
    private static final String Url = Constant.BASE_URL;

    /**
     * 生成查询URL
     * 
     * 根据搜索关键词构造完整的BOSS直聘搜索URL，包含以下参数：
     * - query: 搜索关键词
     * - city: 城市代码（默认100010000）
     * - page: 页码（默认1）
     * - pageSize: 每页数量（默认10）
     * 
     * @param Param 搜索关键词（不能为null）
     * @return 完整的查询URL字符串
     * @throws AssertionError 当Param为null时抛出
     */
    private String GetQueryUrl(String Param) {
        assert (Param != null);
        LOG.debug("职位关键词： " + Param);
        return Scrapper.Url + "?query=" + Param + "&city=100010000&page=1&pageSize=10";
    }

    /**
     * 抓取职位信息
     * 
     * 执行完整的职位信息抓取流程：
     * 1. 构造并发送HTTP请求
     * 2. 智能识别响应格式（HTML/JSON）
     * 3. 解析职位数据
     * 4. 提取关键信息
     * 5. 保存到数据库
     * 
     * 响应处理逻辑：
     * - HTML响应：使用CSS选择器提取数据
     * - JSON响应：解析zpData中的职位信息
     * 
     * 异常处理：
     * - 访问限制：抛出IOException
     * - 解析错误：记录日志并继续处理
     * - 网络错误：抛出IOException
     * 
     * @param Param 搜索关键词
     * @return 职位信息列表
     * @throws IOException          当网络请求失败或触发反爬虫机制时
     * @throws NullPointerException 当参数为null时
     */
    public List<Job> ScrapeJob(@NonNull String Param) throws IOException {
        String JobUrl = this.GetQueryUrl(Param);
        List<Job> jobs = new ArrayList<>();

        if (Param == null) {
            throw new NullPointerException("搜索关键词不能为空");
        }

        try {
            Document document = Jsoup.connect(JobUrl)
                    .userAgent(
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Cache-Control", "no-cache")
                    .header("Host", "www.zhipin.com")
                    .header("Referer", "https://www.zhipin.com/web/geek/job")
                    .header("sec-ch-ua",
                            "\"Chromium\";v=\"122\", \"Google Chrome\";v=\"122\", \"Not(A:Brand\";v=\"24\"")
                    .header("sec-ch-ua-platform", "\"macOS\"")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Dest", "empty")
                    .ignoreContentType(true)
                    .timeout(300000)
                    .get();

            String jsonResponse = document.text();

            LOG.debug(jsonResponse);

            if (jsonResponse.trim().startsWith("<!DOCTYPE html>") || jsonResponse.trim().startsWith("<html>")) {
                Elements jobCards = document.select(".job-list-box .job-primary");

                for (Element card : jobCards) {
                    String title = card.select(".job-name").text();
                    String salary = card.select(".red").text();
                    String company = card.select(".company-text").text();
                    String location = card.select(".job-area").text();

                    Elements tagElements = card.select(".tags .tag-item");
                    String[] tags = tagElements.stream()
                            .map(Element::text)
                            .toArray(String[]::new);

                    String experience = tagElements.first() != null ? tagElements.first().text() : "";

                    if (!title.isEmpty() && !company.isEmpty()) {
                        jobs.add(new Job(title, company, salary, location, experience, tags, "", company));
                        LOG.info("找到职位: {} 于 {}", title, company);
                    }
                }
                return jobs;
            }

            JSONObject json = new JSONObject(jsonResponse);

            if (json.getInt("code") == 35) {
                LOG.error("BOSS直聘提醒：当前访问量较大，请稍后再试。");
                throw new IOException("BOSS直聘提醒：当前访问量较大，请稍后再试。");
            }

            if (json.has("code") && json.getInt("code") == 0 && json.has("zpData")) {
                JSONObject zpData = json.getJSONObject("zpData");
                String html = zpData.getString("html");

                String[] jobEntries = html.split("\n \n \n \n");

                for (String entry : jobEntries) {
                    try {
                        if (entry.trim().isEmpty())
                            continue;

                        String[] lines = entry.split("\n");
                        if (lines.length < 4)
                            continue;

                        String title = lines[0].trim();
                        String salary = lines[4].trim();
                        String location = lines[1].trim();
                        String experience = "";
                        List<String> skills = new ArrayList<>();
                        String actualCompanyName = "";
                        String contactUrl = ""; // 新增联系URL字段

                        // 从HTML中提取联系URL
                        if (entry.contains("data-url=\"")) {
                            int startIndex = entry.indexOf("data-url=\"") + 10;
                            int endIndex = entry.indexOf("\"", startIndex);
                            contactUrl = "https://www.zhipin.com" + entry.substring(startIndex, endIndex);
                        }

                        for (int i = 6; i < lines.length; i++) {
                            String line = lines[i].trim();
                            if (line.contains("立即沟通"))
                                break;
                            if (i == 6) {
                                actualCompanyName = line; // 第一个标签是公司名称
                            } else if (i == 7) {
                                location = line; // 第二个标签是地点
                            } else if (line.contains("年")) {
                                experience = line;
                            } else if (!line.isEmpty()) {
                                skills.add(line);
                            }
                        }

                        jobs.add(new Job(
                                title, // 职位标题
                                location, // 公司名称 (lines[1])
                                salary, // 薪资
                                actualCompanyName, // 位置 (从标签获取)
                                experience, // 经验要求
                                skills.toArray(new String[0]),
                                "",
                                contactUrl // 添加联系URL
                        ));

                        LOG.info("成功解析职位: {} | {} | {} | {}", title, actualCompanyName, salary,
                                location);
                    } catch (Exception e) {
                        LOG.error("解析职位条目时出错: {}", e.getMessage());
                    }
                }
            }

            if (jobs.isEmpty()) {
                LOG.info("没有找到职位信息");
            } else {
                LOG.info("共找到 {} 个职位信息", jobs.size());
                LOG.info("========== 职位列表 ==========");

                for (Job job : jobs) {
                    LOG.info("\n职位: {}\n公司: {}\n薪资: {}\n地点: {}\n经验: {}\n技能: {}\n------------------------",
                            job.getTitle(),
                            job.getCompanyName(),
                            job.getSalary(),
                            job.getLocation(),
                            job.getDescription(),
                            String.join(", ", job.getTagList()));
                }

                LOG.info("============================");
                databaseHandler.saveJobs(jobs);
            }

            return jobs;
        } catch (IOException e) {
            LOG.error("网络请求失败: {}", e.getMessage());
            throw e;
        }
    }
}