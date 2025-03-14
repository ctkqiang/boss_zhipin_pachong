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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import xin.ctkqiang.boss_zhipin_pachong.Model.Job;

/**
 * SQLite数据库处理类
 * 
 * 该类负责管理与SQLite数据库的所有交互，主要功能包括：
 * 1. 数据库连接的建立与管理
 * 2. 数据表的创建与维护
 * 3. 职位数据的持久化存储
 * 4. 数据库连接的安全关闭
 * 
 * 数据库结构：
 * 表名：jobs
 * 字段：
 * - id: 自增主键
 * - title: 职位名称
 * - company: 公司名称
 * - salary: 薪资范围
 * - location: 工作地点
 * - experience: 经验要求
 * - skills: 技能要求（逗号分隔）
 * - created_at: 记录创建时间
 * 
 * 使用示例：
 * DatabaseHandler db = new DatabaseHandler();
 * db.saveJobs(jobsList);
 * db.close();
 * 
 * 注意事项：
 * 1. 确保数据库文件有写入权限
 * 2. 大批量数据写入时注意性能
 * 3. 使用完毕后必须调用close()方法
 * 4. 异常处理已集成日志记录
 */
@Component
public class DatabaseHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseHandler.class);
    private static final String DB_URL = "jdbc:sqlite:jobs.db";
    private Connection connection;

    public DatabaseHandler() {
        initializeDatabase();
    }

    /**
     * 初始化数据库连接
     * 
     * 该方法在Spring容器启动时自动执行，完成：
     * 1. 加载SQLite JDBC驱动
     * 2. 建立数据库连接
     * 3. 创建必要的数据表
     * 
     * @throws RuntimeException 当数据库初始化失败时
     */
    @PostConstruct
    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            createJobsTable();
            LOG.info("数据库连接成功");
        } catch (SQLException | ClassNotFoundException e) {
            LOG.error("数据库连接失败: {}", e.getMessage());
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    /**
     * 创建职位信息表
     * 
     * 如果表不存在，则创建包含以下字段的表：
     * - id: 自增主键
     * - title: 职位名称（非空）
     * - company: 公司名称（非空）
     * - salary: 薪资范围
     * - location: 工作地点
     * - experience: 经验要求
     * - skills: 技能要求
     * - created_at: 记录创建时间戳
     * 
     * @throws SQLException 当表创建失败时
     */
    private void createJobsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS jobs ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "title TEXT NOT NULL,"
                + "company TEXT NOT NULL,"
                + "salary TEXT,"
                + "location TEXT,"
                + "experience TEXT,"
                + "skills TEXT,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            LOG.info("职位表创建成功");
        }
    }

    /**
     * 批量保存职位信息
     * 
     * 将爬取到的职位信息批量保存到数据库中：
     * 1. 使用预编译语句提高性能
     * 2. 自动处理字段值为null的情况
     * 3. 技能标签以逗号分隔存储
     * 
     * @param jobs 待保存的职位信息列表
     */
    public void saveJobs(List<Job> jobs) {
        String sql = "INSERT INTO jobs (title, company, salary, location, experience, skills) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Job job : jobs) {
                pstmt.setString(1, job.getTitle());
                pstmt.setString(2, job.getCompanyName());
                pstmt.setString(3, job.getSalary());
                pstmt.setString(4, job.getLocation());
                pstmt.setString(5, job.getDescription());
                pstmt.setString(6, String.join(",", job.getTagList()));
                pstmt.executeUpdate();
            }
            LOG.info("成功保存 {} 个职位信息", jobs.size());
        } catch (SQLException e) {
            LOG.error("保存职位信息失败: {}", e.getMessage());
        }
    }

    /**
     * 安全关闭数据库连接
     * 
     * 在应用程序关闭时调用，确保：
     * 1. 检查连接是否存在
     * 2. 检查连接是否已关闭
     * 3. 安全关闭连接
     * 4. 记录关闭状态
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOG.info("数据库连接已关闭");
            }
        } catch (SQLException e) {
            LOG.error("关闭数据库连接失败: {}", e.getMessage());
        }
    }

    public List<Job> getAllJobs() {
        List<Job> jobs = new ArrayList<>();
        String sql = "SELECT * FROM jobs";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String[] skills = rs.getString("skills") != null ? rs.getString("skills").split(",") : new String[0];

                Job job = new Job(
                        rs.getString("title"),
                        rs.getString("company"), // Changed from company_name to company
                        rs.getString("salary"),
                        rs.getString("location"),
                        rs.getString("experience"),
                        skills,
                        "", // contact_url is not in the schema
                        "" // company_info is not in the schema
                );
                jobs.add(job);
            }
        } catch (SQLException e) {
            LOG.error("获取职位信息失败: {}", e.getMessage());
        }

        return jobs;
    }

}
