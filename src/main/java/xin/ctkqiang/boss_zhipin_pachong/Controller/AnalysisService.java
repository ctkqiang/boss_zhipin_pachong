package xin.ctkqiang.boss_zhipin_pachong.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xin.ctkqiang.boss_zhipin_pachong.Database.DatabaseHandler;
import xin.ctkqiang.boss_zhipin_pachong.Model.Job;

/**
 * 职位数据分析服务。
 *
 * <p>对已爬取的职位数据进行薪资范围统计和平均薪资趋势分析。</p>
 */
@Service
public class AnalysisService {

    @Autowired
    private DatabaseHandler databaseHandler;

    /**
     * 分析薪资数据。
     *
     * @return 包含 salaryRanges 和 averageSalaries 的 Map
     */
    public Map<String, Object> analyzeSalaryData() {
        List<Job> jobs = databaseHandler.getAllJobs();
        Map<String, Object> result = new HashMap<>();
        result.put("salaryRanges", calculateSalaryRanges(jobs));
        result.put("averageSalaries", calculateAverageSalaries(jobs));
        result.put("totalJobs", jobs.size());
        return result;
    }

    private List<Map<String, Object>> calculateSalaryRanges(List<Job> jobs) {
        Map<String, Integer> ranges = new TreeMap<>();
        for (Job job : jobs) {
            String range = normalizeSalaryRange(job.getSalary());
            ranges.put(range, ranges.getOrDefault(range, 0) + 1);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> e : ranges.entrySet()) {
            result.add(Map.of("range", e.getKey(), "count", e.getValue()));
        }
        return result;
    }

    private List<Map<String, Object>> calculateAverageSalaries(List<Job> jobs) {
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd")
                .format(new java.util.Date());
        List<Double> values = new ArrayList<>();
        for (Job job : jobs) {
            double avg = averageFromRange(job.getSalary());
            if (avg > 0) values.add(avg);
        }
        double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        return List.of(Map.of("date", today, "average", Math.round(avg * 100.0) / 100.0));
    }

    private String normalizeSalaryRange(String salary) {
        if (salary == null || salary.isBlank()) return "未知";
        return salary.replaceAll("[kK]", "k");
    }

    private double averageFromRange(String salary) {
        if (salary == null || salary.isBlank()) return 0;
        String s = salary.toLowerCase().replace("k", "");
        String[] parts = s.split("-");
        if (parts.length == 2) {
            try {
                return (Double.parseDouble(parts[0]) + Double.parseDouble(parts[1])) / 2.0;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
