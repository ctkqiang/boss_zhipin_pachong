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
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import xin.ctkqiang.boss_zhipin_pachong.Database.Constant;
import xin.ctkqiang.boss_zhipin_pachong.Model.Job;

import org.json.JSONObject;

@Service
public class Scrapper {
    private static final Logger LOG = LoggerFactory.getLogger(Scrapper.class);
    private static final String Url = Constant.BASE_URL;

    // 生成查询 URL
    private String GetQueryUrl(String Param) {
        assert (Param != null);
        return Scrapper.Url + "?query=" + Param + "&city=100010000&page=1&pageSize=10";
    }

    public List<Job> ScrapeJob(@NonNull String Param) throws IOException {
        String JobUrl = this.GetQueryUrl(Param);
        List<Job> jobs = new ArrayList<>();

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
                throw new IOException("BOSS直聘访问限制");
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
                                title, // 职位标题 from lines[0]
                                actualCompanyName, // 公司名称 from tagList[0]
                                salary, // 薪资
                                location, // 位置
                                experience, // 经验要求
                                skills.toArray(new String[0]),
                                "",
                                ""));

                        LOG.info("成功解析职位: {} | {} | {} | {}", title, actualCompanyName, salary,
                                location);
                    } catch (Exception e) {
                        LOG.error("解析职位条目时出错: {}", e.getMessage());
                    }
                }
            }

            if (jobs.isEmpty()) {
                LOG.info("总共没有个职位信息", jobs.size());
            } else {
                LOG.info("总共找到 {} 个职位信息", jobs.size());
            }
            return jobs;
        } catch (IOException e) {
            LOG.error("网络请求失败: {}", e.getMessage());
            throw e;
        }
    }
}