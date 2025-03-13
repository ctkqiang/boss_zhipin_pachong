package xin.ctkqiang.boss_zhipin_pachong.Controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import xin.ctkqiang.boss_zhipin_pachong.Model.Job;

/**
 * BOSS直聘职位搜索API接口
 * 
 * 提供RESTful API接口用于查询BOSS直聘职位信息：
 * 1. 支持关键词搜索职位
 * 2. 返回JSON格式的职位数据
 * 3. 自动处理错误情况
 * 
 * API端点说明：
 * GET /api/jobs?keyword={关键词}
 * 
 * 请求参数：
 * - keyword: 职位搜索关键词（必填）
 * 
 * 返回数据：
 * - 成功：职位列表数组
 * - 失败：错误信息对象
 * 
 * 使用示例：
 * GET http://localhost:8080/api/jobs?keyword=Java开发
 * 
 * 注意事项：
 * 1. 关键词不能为空
 * 2. 每次请求可能需要较长处理时间
 * 3. 遇到访问限制时会返回相应错误信息
 */
@RestController
@RequestMapping("/api")
public class API {
    @Autowired
    private Scrapper scrapper;

    @GetMapping("/jobs")
    public ResponseEntity<?> getJobs(@RequestParam String keyword) {
        try {
            List<Job> jobs = scrapper.ScrapeJob(keyword);
            return ResponseEntity.ok(jobs);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("爬取失败: " + e.getMessage()));
        }
    }
}

/**
 * 错误响应实体类
 * 
 * 用于封装API错误信息的数据结构
 * 包含：
 * - message: 错误信息描述
 */
class ErrorResponse {
    private String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
