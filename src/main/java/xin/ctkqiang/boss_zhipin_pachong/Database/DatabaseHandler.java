package xin.ctkqiang.boss_zhipin_pachong.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import xin.ctkqiang.boss_zhipin_pachong.Model.Job;

/**
 * SQLite数据库操作组件。
 *
 * <p>管理 jobs 表的DDL和CRUD操作。表结构包含职位名称、公司、薪资、地点、
 * 经验要求、技能标签、联系人、职位链接及创建时间戳。</p>
 */
@Component
public class DatabaseHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseHandler.class);
    private static final String DB_URL = "jdbc:sqlite:jobs.db";
    private Connection connection;

    @PostConstruct
    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            createJobsTable();
            LOG.info("SQLite 数据库连接成功: {}", DB_URL);
        } catch (SQLException | ClassNotFoundException e) {
            LOG.error("数据库初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    private void createJobsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS jobs ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "title TEXT NOT NULL,"
                + "company TEXT NOT NULL,"
                + "salary TEXT,"
                + "location TEXT,"
                + "experience TEXT,"
                + "skills TEXT,"
                + "contact TEXT,"
                + "url TEXT,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }

        // Add columns that may not exist in older schema versions.
        for (String col : new String[]{"contact", "url"}) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("ALTER TABLE jobs ADD COLUMN " + col + " TEXT");
            } catch (SQLException ignored) {
            }
        }

        LOG.info("jobs 表就绪");
    }

    /**
     * 批量保存职位记录。
     *
     * @param jobs 待保存的职位列表
     */
    public void saveJobs(List<Job> jobs) {
        String sql = "INSERT INTO jobs (title, company, salary, location, experience, skills, contact, url) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            int count = 0;
            for (Job job : jobs) {
                pstmt.setString(1, job.getTitle());
                pstmt.setString(2, job.getCompanyName());
                pstmt.setString(3, job.getSalary());
                pstmt.setString(4, job.getLocation());
                pstmt.setString(5, job.getExperience());
                pstmt.setString(6, String.join(",", job.getSkills()));
                pstmt.setString(7, job.getContact());
                pstmt.setString(8, job.getUrl());
                pstmt.executeUpdate();
                count++;
            }
            LOG.info("成功保存 {} 条职位记录", count);
        } catch (SQLException e) {
            LOG.error("保存职位失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 查询全部职位。
     *
     * @return 职位列表
     */
    public List<Job> getAllJobs() {
        List<Job> jobs = new ArrayList<>();
        String sql = "SELECT * FROM jobs ORDER BY created_at DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String skillsStr = rs.getString("skills");
                String[] skills = (skillsStr != null && !skillsStr.isEmpty())
                        ? skillsStr.split(",") : new String[0];

                Job job = new Job(
                        rs.getString("title"),
                        rs.getString("company"),
                        rs.getString("salary"),
                        rs.getString("location"),
                        rs.getString("experience"),
                        skills,
                        rs.getString("contact"),
                        rs.getString("url")
                );
                job.setCreatedAt(rs.getString("created_at"));
                jobs.add(job);
            }
        } catch (SQLException e) {
            LOG.error("查询职位失败: {}", e.getMessage(), e);
        }

        return jobs;
    }

    /**
     * 按关键词模糊搜索。
     *
     * @param keyword 搜索关键词
     * @return 匹配的职位列表
     */
    public List<Job> searchJobs(String keyword) {
        List<Job> jobs = new ArrayList<>();
        String sql = "SELECT * FROM jobs WHERE title LIKE ? OR company LIKE ? "
                + "ORDER BY created_at DESC LIMIT 100";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String[] skills = rs.getString("skills") != null
                            ? rs.getString("skills").split(",") : new String[0];
                    Job job = new Job(
                            rs.getString("title"),
                            rs.getString("company"),
                            rs.getString("salary"),
                            rs.getString("location"),
                            rs.getString("experience"),
                            skills,
                            rs.getString("contact"),
                            rs.getString("url")
                    );
                    jobs.add(job);
                }
            }
        } catch (SQLException e) {
            LOG.error("搜索职位失败: {}", e.getMessage(), e);
        }

        return jobs;
    }

    @PreDestroy
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOG.info("数据库连接已关闭");
            }
        } catch (SQLException e) {
            LOG.error("关闭数据库连接失败: {}", e.getMessage(), e);
        }
    }
}
