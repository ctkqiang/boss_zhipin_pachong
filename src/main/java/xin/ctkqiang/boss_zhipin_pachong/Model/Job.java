package xin.ctkqiang.boss_zhipin_pachong.Model;

public class Job {
    private String Title;
    private String Salary;
    private String CompanyName;
    private String Location;
    private String Description;
    private String[] TagList;
    private String PersonInCharge;
    private String url;

    public Job(String Title, String CompanyName, String Salary, String Location, String Description, String[] TagList,
            String PersonInCharge, String url) {
        this.Title = Title;
        this.CompanyName = CompanyName;
        this.Salary = Salary.replaceAll("·\\d+薪", ""); // Remove "·14薪" etc.
        this.Location = Location;
        this.Description = Description;
        this.TagList = TagList;
        this.PersonInCharge = PersonInCharge.replaceAll("·.*$", ""); // Clean up title suffix
        this.url = url;
    }

    // Add toString method for better logging
    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %s",
                Title, CompanyName, Salary, Location, PersonInCharge);
    }

    public void setTitle(String title) {
        Title = title;
    }

    public void setSalary(String salary) {
        Salary = salary;
    }

    public void setCompanyName(String companyName) {
        CompanyName = companyName;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public void setTagList(String[] tagList) {
        TagList = tagList;
    }

    public void setPersonInCharge(String personInCharge) {
        PersonInCharge = personInCharge;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return Title;
    }

    public String getSalary() {
        return Salary;
    }

    public String getCompanyName() {
        return CompanyName;
    }

    public String getLocation() {
        return Location;
    }

    public String getDescription() {
        return Description;
    }

    public String[] getTagList() {
        return TagList;
    }

    public String getPersonInCharge() {
        return PersonInCharge;
    }

    public String getUrl() {
        return url;
    }

}
