package xin.ctkqiang.boss_zhipin_pachong.Controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import xin.ctkqiang.boss_zhipin_pachong.Model.Job;

/**
 * BOSS直聘职位搜索 REST API。
 *
 * <h3>端点</h3>
 * <ul>
 *   <li>GET /api/jobs?keyword={关键词}&page={页码}&size={每页条数}</li>
 *   <li>GET /api/health — 健康检查</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
public class API {

    @Autowired
    private Scrapper scrapper;

    /**
     * 搜索职位。
     *
     * @param keyword  搜索关键词（必填）
     * @param page     页码，默认1
     * @param pageSize 每页条数，默认10，最大50
     * @return JSON格式的职位列表或错误信息
     */
    @GetMapping("/jobs")
    public ResponseEntity<?> getJobs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        if (keyword.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "keyword 参数不能为空"));
        }
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 50) pageSize = 50;

        try {
            List<Job> jobs = scrapper.scrapeJobs(keyword, page, pageSize);
            return ResponseEntity.ok(Map.of(
                    "code", 0,
                    "keyword", keyword,
                    "page", page,
                    "page_size", pageSize,
                    "total", jobs.size(),
                    "data", jobs
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "code", -1,
                            "error", "爬取失败: " + e.getMessage()
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "code", -2,
                            "error", e.getMessage()
                    ));
        }
    }

    /**
     * 健康检查。
     *
     * @return 服务状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "BOSS Zhipin Crawler 2026",
                "version", "2.0.0"
        ));
    }
}
