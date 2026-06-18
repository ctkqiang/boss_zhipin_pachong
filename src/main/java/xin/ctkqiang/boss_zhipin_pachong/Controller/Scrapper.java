package xin.ctkqiang.boss_zhipin_pachong.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xin.ctkqiang.boss_zhipin_pachong.Database.Constant;
import xin.ctkqiang.boss_zhipin_pachong.Database.DatabaseHandler;
import xin.ctkqiang.boss_zhipin_pachong.Model.Job;

/**
 * BOSS直聘职位爬虫服务 (2026版)。
 *
 * <p>通过BOSS直聘 Web API 搜索职位，支持 JSON 和 HTML 双模式解析，
 * 自动重试与反爬延时。</p>
 *
 * <h3>使用示例</h3>
 * <pre>
 *   List&lt;Job&gt; jobs = scrapper.scrapeJobs("Java开发", 1, 10);
 * </pre>
 */
@Service
public class Scrapper {

    private static final Logger LOG = LoggerFactory.getLogger(Scrapper.class);
    private static final String BASE_URL = Constant.BASE_URL;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    @Autowired
    private DatabaseHandler databaseHandler;

    /** Jsoup自动管理Cookie */
    private final java.util.Map<String, String> sessionCookies = new java.util.concurrent.ConcurrentHashMap<>();
    /** 请求间隔（毫秒），避免触发频率限制 code=35 */
    private static final long REQUEST_INTERVAL_MS = 3_000;
    private volatile long lastRequestTime = 0;

    /**
     * 从BOSS直聘首页获取Cookie建立会话。
     * 使用Jsoup自动处理Set-Cookie，比手动解析更可靠。
     */
    private void ensureSession() {
        if (!sessionCookies.isEmpty()) return;

        try {
            var resp = Jsoup.connect("https://www.zhipin.com/web/geek/job")
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                            + "AppleWebKit/537.36 (KHTML, like Gecko) "
                            + "Chrome/131.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .timeout(15000)
                    .execute();

            // Jsoup parses Set-Cookie headers automatically
            sessionCookies.putAll(resp.cookies());
            LOG.info("会话Cookie获取成功: {} 个", sessionCookies.size());
        } catch (Exception e) {
            LOG.warn("获取首页Cookie失败: {}", e.getMessage());
        }
    }

    private String buildCookieHeader() {
        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<String, String> e : sessionCookies.entrySet()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append(e.getKey()).append("=").append(e.getValue());
        }
        return sb.toString();
    }

    /**
     * 拼接搜索URL。
     *
     * @param keyword  搜索关键词
     * @param page     页码
     * @param pageSize 每页条数
     * @return 完整查询URL
     */
    private String buildSearchUrl(String keyword, int page, int pageSize) {
        return BASE_URL
                + "?query=" + keyword
                + "&city=100010000"
                + "&page=" + page
                + "&pageSize=" + pageSize
                + "&experience=&degree=&industry=&scale=&stage=&position=";
    }

    /**
     * 抓取职位列表（默认第1页，每页10条）。
     *
     * @param keyword 搜索关键词
     * @return 职位列表
     * @throws IOException 网络错误或触发反爬
     */
    public List<Job> scrapeJobs(String keyword) throws IOException {
        return scrapeJobs(keyword, 1, 10);
    }

    /**
     * 抓取职位列表（带分页）。
     *
     * @param keyword  搜索关键词
     * @param page     页码（从1开始）
     * @param pageSize 每页条数
     * @return 职位列表
     * @throws IOException 网络错误或触发反爬
     */
    public List<Job> scrapeJobs(String keyword, int page, int pageSize) throws IOException {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }

        // 请求间隔，避免触发 code=35
        long now = System.currentTimeMillis();
        long sinceLast = now - lastRequestTime;
        if (sinceLast < REQUEST_INTERVAL_MS) {
            try { TimeUnit.MILLISECONDS.sleep(REQUEST_INTERVAL_MS - sinceLast); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        lastRequestTime = System.currentTimeMillis();

        String url = buildSearchUrl(keyword, page, pageSize);
        LOG.info("开始爬取: page={}, pageSize={}, keyword={}", page, pageSize, keyword);

        String jsonResponse = fetchWithRetry(url);
        List<Job> jobs = parseResponse(jsonResponse);

        if (!jobs.isEmpty()) {
            databaseHandler.saveJobs(jobs);
            LOG.info("爬取完成: 共 {} 条职位", jobs.size());
        } else {
            LOG.warn("未找到匹配的职位: keyword={}", keyword);
        }

        return jobs;
    }

    /**
     * 带重试的HTTP请求。
     *
     * @param url 请求URL
     * @return 响应体字符串
     * @throws IOException 所有重试耗尽后抛出
     */
    private String fetchWithRetry(String url) throws IOException {
        ensureSession();
        IOException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            String text = null;
            try {
                var conn = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                                + "Chrome/131.0.0.0 Safari/537.36")
                        .header("Accept", "application/json, text/plain, */*")
                        .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                        .header("Cache-Control", "no-cache")
                        .header("Referer", "https://www.zhipin.com/web/geek/job")
                        .header("sec-ch-ua",
                                "\"Chromium\";v=\"131\", \"Google Chrome\";v=\"131\"")
                        .header("sec-ch-ua-platform", "\"macOS\"")
                        .header("Sec-Fetch-Site", "same-origin")
                        .header("Sec-Fetch-Mode", "cors")
                        .header("Sec-Fetch-Dest", "empty")
                        .ignoreContentType(true)
                        .timeout(30000)
                        .cookies(sessionCookies);

                Document doc = conn.get();
                sessionCookies.putAll(conn.response().cookies());
                text = doc.text();
                LOG.debug("响应长度: {} 字符", text.length());

                // code=37 means session invalid — clear cookies and retry
                if (text.contains("\"code\":37")) {
                    LOG.info("code=37 会话失效, 清除Cookie重试 ({}/{})", attempt, MAX_RETRIES);
                    sessionCookies.clear();
                    try { TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS); }
                    catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
                    continue;
                }

                return text;

            } catch (IOException e) {
                lastException = e;
                LOG.warn("第 {}/{} 次请求失败: {}", attempt, MAX_RETRIES, e.getMessage());
            }

            if (attempt < MAX_RETRIES) {
                long delay = RETRY_DELAY_MS * attempt;
                try { TimeUnit.MILLISECONDS.sleep(delay); }
                catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
            }
        }

        throw new IOException("请求失败 (已重试 " + MAX_RETRIES + " 次)", lastException);
    }

    /**
     * 解析BOSS直聘响应（JSON优先，HTML兜底）。
     *
     * @param response 原始响应文本
     * @return 解析后的职位列表
     */
    private List<Job> parseResponse(String response) {
        List<Job> jobs = new ArrayList<>();

        if (response == null || response.isBlank()) {
            return jobs;
        }

        String trimmed = response.trim();

        if (trimmed.startsWith("{")) {
            jobs = parseJsonResponse(trimmed);
        } else if (trimmed.startsWith("<!DOCTYPE") || trimmed.startsWith("<html")) {
            jobs = parseHtmlResponse(trimmed);
        } else {
            LOG.warn("未知响应格式，前200字符: {}", trimmed.substring(0, Math.min(200, trimmed.length())));
        }

        return jobs;
    }

    /**
     * 解析JSON格式的API响应。
     *
     * <p>BOSS直聘JSON响应结构 (2026):
     * <pre>
     * {
     *   "code": 0,
     *   "zpData": {
     *     "jobList": [ { ... }, { ... } ],
     *     "html": "&lt;html&gt;...&lt;/html&gt;"
     *   }
     * }
     * </pre>
     *
     * @param json JSON字符串
     * @return 职位列表
     */
    private List<Job> parseJsonResponse(String json) {
        List<Job> jobs = new ArrayList<>();

        try {
            JSONObject root = new JSONObject(json);

            int code = root.optInt("code", -1);
            if (code == 37) {
                LOG.warn("会话失效 (code=37): {}, 重新获取Cookie", root.optString("message"));
                sessionCookies.clear();
                return jobs;
            }
            if (code == 35) {
                LOG.error("触发反爬限制 (code=35): 访问频率过高，请稍后再试");
                return jobs;
            }
            if (code != 0) {
                LOG.warn("API返回异常 code={}, message={}",
                        code, root.optString("message", "无"));
                return jobs;
            }

            JSONObject zpData = root.optJSONObject("zpData");
            if (zpData == null) {
                LOG.warn("响应中缺少 zpData 字段");
                return jobs;
            }

            // 优先解析 jobList（新版API）
            JSONArray jobList = zpData.optJSONArray("jobList");
            if (jobList != null && !jobList.isEmpty()) {
                for (int i = 0; i < jobList.length(); i++) {
                    JSONObject item = jobList.getJSONObject(i);
                    Job job = parseJobItem(item);
                    if (job != null) {
                        jobs.add(job);
                    }
                }
                LOG.info("从 jobList 解析到 {} 条职位", jobs.size());
                return jobs;
            }

            // 兜底：解析 html 字段（旧版API）
            String html = zpData.optString("html", "");
            if (!html.isEmpty()) {
                jobs = parseHtmlResponse(html);
            }

        } catch (Exception e) {
            LOG.error("JSON解析失败: {}", e.getMessage(), e);
        }

        return jobs;
    }

    /**
     * 从JSON对象中提取单条职位信息。
     *
     * @param item 职位JSON对象
     * @return Job实例，或null（解析失败时）
     */
    private Job parseJobItem(JSONObject item) {
        try {
            String title = item.optString("jobName", "");
            String company = item.optString("brandName", "");
            String salary = item.optString("salaryDesc", "");
            String location = item.optString("cityName", "")
                    + " " + item.optString("areaDistrict", "");

            String experience = item.optString("jobExperience", "");
            if (experience.isEmpty()) {
                experience = item.optString("jobExperienceName", "");
            }

            JSONArray tagsArr = item.optJSONArray("skills");
            List<String> tagList = new ArrayList<>();
            if (tagsArr != null) {
                for (int j = 0; j < tagsArr.length(); j++) {
                    tagList.add(tagsArr.optString(j, ""));
                }
            }

            String contact = item.optString("bossName", "");
            String encryptId = item.optString("encryptJobId",
                    item.optString("encryptId", ""));
            String jobUrl = encryptId.isEmpty() ? ""
                    : "https://www.zhipin.com/job_detail/" + encryptId + ".html";

            if (title.isEmpty() || company.isEmpty()) {
                return null;
            }

            return new Job(title, company, salary, location.trim(),
                    experience, tagList.toArray(new String[0]), contact, jobUrl);

        } catch (Exception e) {
            LOG.debug("解析单条职位失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析HTML格式的响应（CSS选择器兜底）。
     *
     * @param html HTML字符串
     * @return 职位列表
     */
    private List<Job> parseHtmlResponse(String html) {
        List<Job> jobs = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        Elements cards = doc.select(".job-card-box, .job-list-box .job-primary, .job-card");
        if (cards.isEmpty()) {
            cards = doc.select("[class*='job-']");
        }

        for (Element card : cards) {
            try {
                String title = extractText(card,
                        ".job-name, .job-title, [class*='job-name'], [class*='job-title']");
                String salary = extractText(card, ".red, .salary, [class*='salary']");
                String company = extractText(card,
                        ".company-text, .company-name, [class*='company']");
                String location = extractText(card,
                        ".job-area, .location, [class*='area'], [class*='location']");

                Elements tagEls = card.select(".tag-item, .skills span, [class*='tag']");
                List<String> tags = new ArrayList<>();
                for (Element t : tagEls) {
                    String tagText = t.text().trim();
                    if (!tagText.isEmpty()) {
                        tags.add(tagText);
                    }
                }

                String experience = tagEls.isEmpty() ? "" : tagEls.first().text();

                if (!title.isEmpty() && !company.isEmpty()) {
                    jobs.add(new Job(title, company, salary, location,
                            experience, tags.toArray(new String[0]), "", ""));
                    LOG.debug("HTML解析: {} @ {}", title, company);
                }
            } catch (Exception e) {
                LOG.debug("HTML卡片解析失败: {}", e.getMessage());
            }
        }

        LOG.info("从 HTML 解析到 {} 条职位", jobs.size());
        return jobs;
    }

    /**
     * 从元素中提取文本，按优先级尝试多个CSS选择器。
     *
     * @param parent    父元素
     * @param selectors 选择器列表（按优先级排列）
     * @return 提取的文本，未匹配到返回空字符串
     */
    private String extractText(Element parent, String selectors) {
        for (String sel : selectors.split(",\\s*")) {
            Elements els = parent.select(sel.trim());
            if (!els.isEmpty()) {
                return els.first().text().trim();
            }
        }
        return "";
    }
}
