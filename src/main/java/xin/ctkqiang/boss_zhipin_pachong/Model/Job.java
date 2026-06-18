package xin.ctkqiang.boss_zhipin_pachong.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * BOSS直聘职位实体。
 *
 * <p>对应BOSS直聘搜索结果中的单条职位数据，包含职位名称、公司、薪资、
 * 地点、经验要求、技能标签、联系人及详情链接。</p>
 */
public class Job {

    @JsonProperty("title")
    private String title;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("salary")
    private String salary;

    @JsonProperty("location")
    private String location;

    @JsonProperty("experience")
    private String experience;

    @JsonProperty("skills")
    private String[] skills;

    @JsonProperty("contact")
    private String contact;

    @JsonProperty("url")
    private String url;

    @JsonProperty("created_at")
    private String createdAt;

    public Job() {}

    /**
     * 构造一条职位记录。
     *
     * @param title       职位名称
     * @param companyName 公司名称
     * @param salary      薪资范围（原始字符串）
     * @param location    工作地点
     * @param experience  经验要求
     * @param skills      技能标签数组
     * @param contact     联系人/HR名称
     * @param url         职位详情链接
     */
    public Job(String title, String companyName, String salary, String location,
               String experience, String[] skills, String contact, String url) {
        this.title = title;
        this.companyName = companyName;
        this.salary = salary != null ? salary.replaceAll("·\\d+薪", "") : null;
        this.location = location;
        this.experience = experience;
        this.skills = skills != null ? skills.clone() : new String[0];
        this.contact = contact != null ? contact.replaceAll("·.*$", "") : null;
        this.url = url;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s", title, companyName, salary, location);
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String[] getSkills() { return skills != null ? skills.clone() : new String[0]; }
    public void setSkills(String[] skills) { this.skills = skills != null ? skills.clone() : new String[0]; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    /** 兼容旧代码 */
    public String getDescription() { return experience; }
    public void setDescription(String description) { this.experience = description; }

    /** 兼容旧代码 */
    public String[] getTagList() { return getSkills(); }
    public void setTagList(String[] tags) { setSkills(tags); }

    /** 兼容旧代码 */
    public String getPersonInCharge() { return contact; }
    public void setPersonInCharge(String person) { this.contact = person; }
}
