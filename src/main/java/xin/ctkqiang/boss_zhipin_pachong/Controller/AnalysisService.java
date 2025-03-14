package xin.ctkqiang.boss_zhipin_pachong.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xin.ctkqiang.boss_zhipin_pachong.Database.DatabaseHandler;
import xin.ctkqiang.boss_zhipin_pachong.Model.Job;

import java.util.*;

/**
 * 职位数据分析服务
 * 主要功能：分析职位薪资数据，生成薪资范围统计和平均薪资趋势
 */
@Service
public class AnalysisService {
    @Autowired
    private DatabaseHandler databaseHandler;

    /**
     * 分析薪资数据
     * 
     * @return 包含薪资范围统计和平均薪资的Map
     */
    public Map<String, Object> analyzeSalaryData() {
        List<Job> jobs = databaseHandler.getAllJobs();
        Map<String, Object> result = new HashMap<>();

        result.put("salaryRanges", calculateSalaryRanges(jobs));
        result.put("averageSalaries", calculateAverageSalaries(jobs));

        return result;
    }

    /**
     * 计算薪资范围分布
     * 
     * @param jobs 职位列表
     * @return 各个薪资范围及其对应的职位数量
     */
    private List<Map<String, Object>> calculateSalaryRanges(List<Job> jobs) {
        // 处理薪资范围统计
        Map<String, Integer> ranges = new TreeMap<>();

        for (Job job : jobs) {
            String range = processSalaryRange(job.getSalary());
            ranges.put(range, ranges.getOrDefault(range, 0) + 1);
        }

        // 转换为前端所需的数据格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : ranges.entrySet()) {
            Map<String, Object> rangeData = new HashMap<>();
            rangeData.put("range", entry.getKey());
            rangeData.put("count", entry.getValue());
            result.add(rangeData);
        }

        return result;
    }

    /**
     * 计算每日平均薪资
     * 
     * @param jobs 职位列表
     * @return 每日平均薪资数据
     */
    private List<Map<String, Object>> calculateAverageSalaries(List<Job> jobs) {
        // 按日期统计薪资
        Map<String, List<Double>> salariesByDate = new TreeMap<>();

        // 获取当前日期
        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd")
                .format(new java.util.Date());

        // 收集薪资数据
        for (Job job : jobs) {
            double avgSalary = calculateAverageSalary(job.getSalary());
            salariesByDate.computeIfAbsent(currentDate, k -> new ArrayList<>()).add(avgSalary);
        }

        // 计算每日平均值
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : salariesByDate.entrySet()) {
            Map<String, Object> dateData = new HashMap<>();
            dateData.put("date", entry.getKey());
            dateData.put("average", calculateAverage(entry.getValue()));
            result.add(dateData);
        }

        return result;
    }

    /**
     * 处理薪资范围字符串
     * 
     * @param salary 原始薪资字符串（如："10k-15k"）
     * @return 标准化的薪资范围字符串
     */
    private String processSalaryRange(String salary) {
        // 转换薪资范围格式，例如：10k-15k -> 10-15k
        return salary.toLowerCase().replace("k", "").replace("K", "") + "k";
    }

    /**
     * 计算薪资范围的平均值
     * 
     * @param salary 薪资范围字符串
     * @return 平均薪资值
     */
    private double calculateAverageSalary(String salary) {
        // 计算薪资范围的平均值
        String[] parts = salary.toLowerCase().replace("k", "").split("-");
        if (parts.length == 2) {
            try {
                double min = Double.parseDouble(parts[0]);
                double max = Double.parseDouble(parts[1]);
                return (min + max) / 2;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * 计算数值列表的平均值
     * 
     * @param values 数值列表
     * @return 平均值
     */
    private double calculateAverage(List<Double> values) {
        if (values.isEmpty())
            return 0;
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }
}