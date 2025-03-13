package xin.ctkqiang.boss_zhipin_pachong.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import xin.ctkqiang.boss_zhipin_pachong.Model.Job;

@RestController
@RequestMapping("/api")
public class API {
    private final Scrapper scrapper;
    private static final int MAX_REQUESTS = 10;
    private static final int WINDOW_MINUTES = 1;
    private final Map<String, Integer> requestCounts = new HashMap<>();
    private final Map<String, Long> lastResetTime = new HashMap<>();
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
    public ResponseEntity<?> GetJob(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();

        if (isRateLimited(clientIp)) {
            LOG.warn("BOSS: {} 请求过于频繁", clientIp);

            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("请求过于频繁，请稍后再试");
        }

        try {
            List<Job> jobs = this.scrapper.ScrapeJob("Java");
            return ResponseEntity.ok(jobs);
        } catch (IOException e) {
            LOG.error("爬取职位信息失败: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("服务器内部错误");
        }
    }

    private boolean isRateLimited(String clientIp) {
        long currentTime = System.currentTimeMillis();
        long windowStart = lastResetTime.getOrDefault(clientIp, 0L);

        if (currentTime - windowStart > WINDOW_MINUTES * 60 * 1000) {
            requestCounts.put(clientIp, 1);
            lastResetTime.put(clientIp, currentTime);
            return false;
        }

        int count = requestCounts.getOrDefault(clientIp, 0) + 1;
        requestCounts.put(clientIp, count);

        return count > MAX_REQUESTS;
    }
}
