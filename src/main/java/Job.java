import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Job {
    private List<String> description, qualification;
    @JsonProperty("title")
    private String jobTitle;
    private String location;
    @JsonProperty("posted by")
    private String poster;

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public List<String> getQualification() {
        return qualification;
    }

    public void setQualification(List<String> qualification) {
        this.qualification = qualification;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Job() {
    }

    public Job(List<String> res, List<String> quals, String jobTitle, String location, String poster) {
        this.jobTitle = jobTitle;
        this.location = location;
        this.qualification = quals;
        this.description = res;
        this.poster = poster;
    }

    @Override
    public String toString() {
        return "Job{" +
                "responsibilities=" + description +
                ", qualifications=" + qualification +
                ", jobTitle='" + jobTitle + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
