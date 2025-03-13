package xin.ctkqiang.boss_zhipin_pachong.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import xin.ctkqiang.boss_zhipin_pachong.Model.Job;

@RestController
@RequestMapping("/api")
public class API {
    private final Scrapper scrapper;
    private static final Logger LOG = LoggerFactory.getLogger(API.class);

    @Autowired
    public API(Scrapper scrapper) {
        this.scrapper = scrapper;
    }

    @GetMapping("/hello")
    public String Hello() {
        return "Hello World";
    }

    @GetMapping("/job")
    public List<Job> GetJob() {
        try {
            return this.scrapper.ScrapeJob("Java");
        } catch (IOException | InterruptedException e) {
            LOG.error("爬取职位信息失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
