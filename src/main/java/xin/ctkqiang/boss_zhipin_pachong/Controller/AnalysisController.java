package xin.ctkqiang.boss_zhipin_pachong.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    @GetMapping("/salary")
    public Map<String, Object> getSalaryAnalysis() {
        return analysisService.analyzeSalaryData();
    }
}