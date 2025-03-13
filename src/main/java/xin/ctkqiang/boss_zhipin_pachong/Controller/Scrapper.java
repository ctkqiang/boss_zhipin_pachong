package xin.ctkqiang.boss_zhipin_pachong.Controller;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import xin.ctkqiang.boss_zhipin_pachong.Database.Constant;
import xin.ctkqiang.boss_zhipin_pachong.Model.Job;

import org.json.JSONObject;
import org.json.JSONArray;

@Service
public class Scrapper {
    private static final Logger LOG = LoggerFactory.getLogger(Scrapper.class);
    private static final String Url = Constant.BASE_URL;

    private String GetQueryUrl(String Param) {
        assert (Param != null);
        return Scrapper.Url + "?query=" + Param + "&city=100010000";
    }

    public List<Job> ScrapeJob(@NonNull String Param) throws IOException {
        String JobUrl = this.GetQueryUrl(Param);
        List<Job> jobs = new ArrayList<>();

        LOG.info("爬取网址: {}", JobUrl);

        try {
            Document document = Jsoup.connect(JobUrl)
                    .userAgent(
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .header("Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
                    .header("Host", "www.zhipin.com")
                    .header("sec-ch-ua",
                            "\"Chromium\";v=\"122\", \"Google Chrome\";v=\"122\", \"Not(A:Brand\";v=\"24\"")
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "\"macOS\"")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .ignoreContentType(true)
                    .timeout(30000)
                    .maxBodySize(0)
                    .followRedirects(true)
                    .get();

            String jsonResponse = document.text();
            LOG.info("API Response: {}", jsonResponse);

            JSONObject json = new JSONObject(jsonResponse);

            if (json.getInt("code") == 0 && json.has("zpData")) {
                JSONObject zpData = json.getJSONObject("zpData");
                String htmlContent = zpData.getString("html");

                if (!htmlContent.isEmpty()) {
                    Document jobDocument = Jsoup.parse(htmlContent);
                    Elements jobCards = jobDocument.select(".item");

                    for (Element card : jobCards) {
                        String title = card.select(".title-text").text();
                        String salary = card.select(".salary").text();
                        String company = card.select(".company").text();
                        String location = card.select(".workplace").text();

                        Elements labelElements = card.select(".labels span");
                        String experience = labelElements.first() != null ? labelElements.first().text() : "";
                        String[] tags = labelElements.stream()
                                .map(Element::text)
                                .toArray(String[]::new);

                        String description = "";
                        String companyInfo = card.select(".name").text();

                        if (!title.isEmpty() && !company.isEmpty()) {
                            jobs.add(new Job(title, company, salary, location,
                                    experience, tags, description, companyInfo));
                            LOG.info("已添加职位: {}", title);
                        }
                    }
                }
            }

            LOG.info("已找到 {} 个职位信息", jobs.size());
            return jobs;
        } catch (IOException e) {
            LOG.error("网络连接失败: {}", e.getMessage());
            throw e;
        }
    }
}
