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

		logger.info("[BOSS直聘] 系统已启动...");
	}
}
