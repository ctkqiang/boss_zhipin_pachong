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

@Service
public class Scrapper {
    private static final Logger LOG = LoggerFactory.getLogger(Scrapper.class);
    private static final String Url = Constant.BASE_URL;

    private String GetQueryUrl(String Param) {
        assert (Param != null);
        return Scrapper.Url + "?query=" + Param + "&city=100010000";
    }

    public List<Job> ScrapeJob(@NonNull String Param) throws IOException, InterruptedException {
        final String JobUrl = this.GetQueryUrl(Param);
        List<Job> jobs = new ArrayList<>();

        LOG.info("爬取网址: {}", JobUrl);

        try {
            Document document = Jsoup.connect(JobUrl)
                    .userAgent(
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Cache-Control", "no-cache")
                    .header("Connection", "keep-alive")
                    .header("sec-ch-ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\"")
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "\"macOS\"")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .referrer("https://www.zhipin.com")
                    .timeout(60000)
                    .maxBodySize(0)
                    .followRedirects(true)
                    .get();

            // Wait for JavaScript content to load
            Thread.sleep(2000);

            // 移除登录弹窗
            Elements loginDialog = document.select("div.boss-login-dialog");
            if (!loginDialog.isEmpty()) {
                loginDialog.remove();
                LOG.info("已移除登录弹窗");
            }

            LOG.info("成功连接到目标网址");
            Elements jobCards = document.select("div.job-list ul li"); // 更新选择器

            for (Element card : jobCards) {

                String title = card.select("div.job-title").text();
                String company = card.select("div.company-text").text();
                String salary = card.select("span.red").text();
                String location = card.select("span.job-area").text();
                String experience = card.select("div.job-limit").text();
                String[] tags = card.select("div.tags").text().split("\\s+");
                String description = card.select("div.info-desc").text();
                String companyInfo = card.select("div.company-info").text();

                Job job = new Job(
                        title,
                        company,
                        salary,
                        location,
                        experience,
                        tags,
                        description,
                        companyInfo);

                jobs.add(job);

            }

            LOG.info("已找到 {} 个职位信息", jobs.size());
            return jobs;
        } catch (IOException e) {
            LOG.error("网络连接失败: {}", e.getMessage());
            throw e;
        }
    }
}
