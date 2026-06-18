package xin.ctkqiang.boss_zhipin_pachong.Controller;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.bonigarcia.wdm.WebDriverManager;
import xin.ctkqiang.boss_zhipin_pachong.Database.Constant;
import xin.ctkqiang.boss_zhipin_pachong.Database.DatabaseHandler;
import xin.ctkqiang.boss_zhipin_pachong.Model.Job;

/**
 * BOSS直聘爬虫 — Selenium 主模式 + Jsoup 兜底。
 */
@Service
public class Scrapper {

    private static final Logger LOG = LoggerFactory.getLogger(Scrapper.class);
    private static final String BASE_URL = Constant.BASE_URL;
    private static final int MAX_RETRIES = 2;
    private static final long RETRY_DELAY_MS = 3_000;
    private static final long REQUEST_INTERVAL_MS = 5_000;

    private final Map<String, String> sessionCookies = new ConcurrentHashMap<>();
    private volatile long lastRequestTime = 0;
    private WebDriver driver;

    @Autowired
    private DatabaseHandler databaseHandler;

    private void waitInterval() {
        long now = System.currentTimeMillis();
        long gap = now - lastRequestTime;
        if (gap < REQUEST_INTERVAL_MS) {
            try { Thread.sleep(REQUEST_INTERVAL_MS - gap); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        lastRequestTime = System.currentTimeMillis();
    }

    private WebDriver getDriver() {
        if (driver == null) {
            try {
                WebDriverManager.chromedriver().setup();
                ChromeOptions opts = new ChromeOptions();
                opts.addArguments("--headless=new",
                        "--no-sandbox", "--disable-dev-shm-usage",
                        "--disable-gpu", "--window-size=1920,1080",
                        "--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                                + "AppleWebKit/537.36 Chrome/131.0.0.0 Safari/537.36",
                        "--lang=zh-CN");
                driver = new ChromeDriver(opts);
                driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
                LOG.info("Selenium ChromeDriver 已启动 (headless)");
            } catch (Throwable e) {
                LOG.warn("Selenium 不可用, 使用 Jsoup 模式: {}", e.getMessage());
                driver = null;
            }
        }
        return driver;
    }

    private void quitDriver() {
        if (driver != null) {
            try { driver.quit(); } catch (Exception ignored) {}
            driver = null;
        }
    }

    /** 用 Selenium 打开 BOSS直聘搜索页面, 模拟真实用户行为, 等 JS 渲染后解析 DOM。 */
    private List<Job> scrapeWithSelenium(String keyword, int page) {
        List<Job> jobs = new ArrayList<>();
        WebDriver d = getDriver();
        if (d == null) return jobs;

        try {
            // 先访问首页建立会话
            d.get("https://www.zhipin.com/web/geek/job");
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

            // 访问搜索页面（真实用户路径，非 API）
            String searchUrl = "https://www.zhipin.com/web/geek/job?query=" + keyword
                    + "&city=100010000&page=" + page;
            LOG.info("Selenium 搜索页: {}", searchUrl);
            d.get(searchUrl);

            WebDriverWait wait = new WebDriverWait(d, Duration.ofSeconds(20));
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".job-list-box,.job-card-box,[class*='job-card'],.search-job-result")));
            } catch (Exception e) {
                LOG.warn("等待页面渲染超时, 尝试解析当前DOM");
            }
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

            // 解析渲染后的 DOM
            List<WebElement> cards = d.findElements(By.cssSelector(
                    ".job-list-box li,.job-card-box .job-card,.search-job-result .job-card,"
                            + ".job-primary,[class*='job-card']:not([class*='container'])"));

            // 如果卡片为空，尝试更宽泛的选择器
            if (cards.isEmpty()) {
                cards = d.findElements(By.cssSelector("li[class*='job'],div[class*='job-card'],"
                        + ".result-list .job-item,[class*='JobCard']"));
            }

            LOG.info("找到 {} 个 DOM 卡片", cards.size());

            for (WebElement card : cards) {
                try {
                    String text = card.getText();
                    if (text.isEmpty() || text.length() < 10) continue;

                    String title = textOrEmpty(card,
                            ".job-name,.job-title,[class*='job-name'],[class*='job-title'],.title");
                    String salary = textOrEmpty(card,
                            ".red,.salary,[class*='salary'],.salary-desc");
                    String company = textOrEmpty(card,
                            ".company-text,.company-name,[class*='company'],.company");
                    String location = textOrEmpty(card,
                            ".job-area,.location,[class*='area'],[class*='location']");

                    List<WebElement> tags = card.findElements(By.cssSelector(
                            ".tag-item,.skills span,.tag-list span,[class*='tag']:not([class*='container'])"));
                    List<String> skillList = new ArrayList<>();
                    for (WebElement t : tags) {
                        String s = t.getText().trim();
                        if (!s.isEmpty() && s.length() < 30) skillList.add(s);
                    }
                    String exp = skillList.isEmpty() ? "" : skillList.get(0);

                    if (!title.isEmpty() && title.length() > 1 && title.length() < 100) {
                        jobs.add(new Job(title, company, salary, location,
                                exp, skillList.toArray(new String[0]), "", ""));
                        LOG.debug("Selenium: {} @ {}", title, company);
                    }
                } catch (Exception ignored) {}
            }

            LOG.info("Selenium 解析完成: {} 条有效职位", jobs.size());
        } catch (Exception e) {
            LOG.error("Selenium 爬取失败: {}", e.getMessage());
        }
        return jobs;
    }

    private String textOrEmpty(WebElement parent, String css) {
        try {
            WebElement el = parent.findElement(By.cssSelector(css));
            return el.getText().trim();
        } catch (Exception e) { return ""; }
    }

    /** Jsoup HTTP 请求（Cookie 从 Selenium 首页获取或 Jsoup 兜底） */
    private String fetchWithRetry(String url) throws IOException {
        IOException last = null;
        for (int i = 1; i <= MAX_RETRIES; i++) {
            try {
                var conn = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                                + "AppleWebKit/537.36 Chrome/131.0.0.0 Safari/537.36")
                        .header("Accept", "application/json, text/plain, */*")
                        .header("Accept-Language", "zh-CN,zh;q=0.9")
                        .header("Referer", "https://www.zhipin.com/web/geek/job")
                        .ignoreContentType(true).timeout(20000)
                        .cookies(sessionCookies);
                Document doc = conn.get();
                sessionCookies.putAll(conn.response().cookies());
                String text = doc.text();
                if (text.contains("\"code\":37")) { sessionCookies.clear(); continue; }
                if (text.contains("\"code\":35")) { LOG.warn("code=35 频率限制"); return text; }
                return text;
            } catch (IOException e) { last = e; }
            try { Thread.sleep(RETRY_DELAY_MS * i); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
        throw new IOException("请求失败 (重试" + MAX_RETRIES + "次)", last);
    }

    private String buildSearchUrl(String keyword, int page, int pageSize) {
        return BASE_URL + "?query=" + keyword + "&city=100010000&page=" + page + "&pageSize=" + pageSize;
    }

    public List<Job> scrapeJobs(String keyword) throws IOException {
        return scrapeJobs(keyword, 1, 10);
    }

    /** 主入口：Selenium 优先，Jsoup 兜底 */
    public List<Job> scrapeJobs(String keyword, int page, int pageSize) throws IOException {
        if (keyword == null || keyword.isBlank())
            throw new IllegalArgumentException("搜索关键词不能为空");

        waitInterval();
        LOG.info("开始爬取: page={}, pageSize={}, keyword={}", page, pageSize, keyword);

        List<Job> jobs;

        // 尝试 Selenium
        jobs = scrapeWithSelenium(keyword, page);
        if (!jobs.isEmpty()) {
            databaseHandler.saveJobs(jobs);
            LOG.info("Selenium 爬取完成: {} 条", jobs.size());
            return jobs;
        }

        // 兜底 Jsoup
        try {
            if (sessionCookies.isEmpty()) {
                try {
                    var resp = Jsoup.connect("https://www.zhipin.com/web/geek/job")
                            .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                                    + "AppleWebKit/537.36 Chrome/131.0.0.0 Safari/537.36")
                            .timeout(15000).execute();
                    sessionCookies.putAll(resp.cookies());
                } catch (Exception ignored) {}
            }
            String json = fetchWithRetry(buildSearchUrl(keyword, page, pageSize));
            jobs = parseJsonResponse(json);
        } catch (IOException e) {
            LOG.error("Jsoup 兜底也失败: {}", e.getMessage());
            jobs = new ArrayList<>();
        }

        if (!jobs.isEmpty()) {
            databaseHandler.saveJobs(jobs);
            LOG.info("Jsoup 爬取完成: {} 条", jobs.size());
        } else {
            LOG.warn("未获取到职位: keyword={}", keyword);
        }

        return jobs;
    }

    private List<Job> parseJsonResponse(String json) {
        List<Job> jobs = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(json);
            int code = root.optInt("code", -1);
            if (code == 37) { sessionCookies.clear(); return jobs; }
            if (code == 35) { LOG.error("code=35 频率限制"); return jobs; }
            if (code != 0) { LOG.warn("API code={} msg={}", code, root.optString("message","")); return jobs; }

            JSONObject zpData = root.optJSONObject("zpData");
            if (zpData == null) return jobs;

            JSONArray jobList = zpData.optJSONArray("jobList");
            if (jobList != null) {
                for (int i = 0; i < jobList.length(); i++) {
                    JSONObject item = jobList.getJSONObject(i);
                    Job j = parseJobItem(item);
                    if (j != null) jobs.add(j);
                }
                return jobs;
            }

            String html = zpData.optString("html", "");
            if (!html.isEmpty()) jobs = parseHtmlResponse(html);
        } catch (Exception e) { LOG.error("JSON解析失败: {}", e.getMessage()); }
        return jobs;
    }

    private Job parseJobItem(JSONObject item) {
        try {
            String title = item.optString("jobName", "");
            String company = item.optString("brandName", "");
            String salary = item.optString("salaryDesc", "");
            String location = item.optString("cityName","") + " " + item.optString("areaDistrict","");
            String exp = item.optString("jobExperience", item.optString("jobExperienceName", ""));
            JSONArray tagsArr = item.optJSONArray("skills");
            List<String> tags = new ArrayList<>();
            if (tagsArr != null) for (int j = 0; j < tagsArr.length(); j++) tags.add(tagsArr.optString(j,""));
            String contact = item.optString("bossName", "");
            String encId = item.optString("encryptJobId", item.optString("encryptId", ""));
            String jobUrl = encId.isEmpty() ? "" : "https://www.zhipin.com/job_detail/" + encId + ".html";
            if (title.isEmpty() || company.isEmpty()) return null;
            return new Job(title, company, salary, location.trim(), exp, tags.toArray(new String[0]), contact, jobUrl);
        } catch (Exception e) { return null; }
    }

    private List<Job> parseHtmlResponse(String html) {
        List<Job> jobs = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements cards = doc.select(".job-card-box,.job-list-box .job-primary,.job-card,[class*='job-']");
        for (Element card : cards) {
            try {
                String title = card.select(".job-name,.job-title,[class*='job-name'],[class*='job-title']").text();
                String salary = card.select(".red,.salary,[class*='salary']").text();
                String company = card.select(".company-text,.company-name,[class*='company']").text();
                String location = card.select(".job-area,.location,[class*='area']").text();
                Elements tagEls = card.select(".tag-item,.skills span,[class*='tag']");
                List<String> tags = new ArrayList<>();
                for (Element t : tagEls) { String s = t.text().trim(); if (!s.isEmpty()) tags.add(s); }
                String exp = tags.isEmpty() ? "" : tags.get(0);
                if (!title.isEmpty() && !company.isEmpty())
                    jobs.add(new Job(title, company, salary, location, exp, tags.toArray(new String[0]), "", ""));
            } catch (Exception ignored) {}
        }
        return jobs;
    }
}
