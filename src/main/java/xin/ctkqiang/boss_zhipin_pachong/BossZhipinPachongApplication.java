package xin.ctkqiang.boss_zhipin_pachong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BossZhipinPachongApplication {
	private static final Logger logger = LoggerFactory.getLogger(BossZhipinPachongApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(BossZhipinPachongApplication.class, args);

		SystemInfo();
	}

	public static void SystemInfo() {
		String javaVersion = System.getProperty("java.version");
		String javaVendor = System.getProperty("java.vendor");
		String osName = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		String osArch = System.getProperty("os.arch");

		logger.info("[BOSS直聘] 系统已启动...");

		String mavenVersion;

		try {
			Class<?> mavenClass = Class.forName("org.apache.maven.model.Model");
			Package mavenPackage = mavenClass.getPackage();
			mavenVersion = mavenPackage.getImplementationVersion();
			if (mavenVersion == null) {
				mavenVersion = mavenPackage.getSpecificationVersion();
			}
			if (mavenVersion == null) {
				mavenVersion = "3.9.6";
			}
		} catch (ClassNotFoundException e) {
			mavenVersion = "3.9.6";
		}
		String springVersion = org.springframework.core.SpringVersion.getVersion();

		logger.info("========= 系统信息 =========");
		logger.info("Java 版本: {} ({})", javaVersion, javaVendor);
		logger.info("操作系统: {} {} ({})", osName, osVersion, osArch);
		logger.info("Maven 版本: {}", mavenVersion);
		logger.info("Spring 版本: {}", springVersion);
		logger.info("应用名称: BOSS直聘爬虫");
		logger.info("开发者: 钟智强");
		logger.info("===========================");
	}
}
