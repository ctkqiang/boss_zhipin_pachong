package xin.ctkqiang.boss_zhipin_pachong.Database;

/**
 * 全局常量。
 *
 * <p>BOSS直聘 Web API 端点 (2026版)。</p>
 */
public final class Constant {

    private Constant() {}

    /** BOSS直聘搜索API端点 */
    public static final String BASE_URL =
            "https://www.zhipin.com/wapi/zpgeek/search/joblist.json";

    /** 请求超时（毫秒） */
    public static final int REQUEST_TIMEOUT_MS = 30_000;

    /** 最大重试次数 */
    public static final int MAX_RETRIES = 3;

    /** 重试间隔基数（毫秒） */
    public static final long RETRY_DELAY_MS = 2_000;
}
