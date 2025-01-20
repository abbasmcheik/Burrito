package com.example.lebrecruiter.models;

import java.io.Serializable;

public class Application implements Serializable {
    private int applicationId;
    private int jobId; // Job ID
    private int freelancerId;
    private String status;
    private String appliedAt;
    private String freelancerName;
    private String freelancerUserName; // New field for username


    // Fields for job details
    private String title = "(Fetching...)";
    private String description = "(Fetching...)";
    private String skills = "(Fetching...)";
    private String category = "(Fetching...)";
    private String payout = "(Fetching...)";
    private String jobStatus = "(Fetching...)";

    public Application(int applicationId, int jobId, int freelancerId, String status, String appliedAt) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.freelancerId = freelancerId;
        this.status = status;
        this.appliedAt = appliedAt;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public int getJobId() {
        return jobId;
    }

    public int getFreelancerId() {
        return freelancerId;
    }

    public String getStatus() {
        return status;
    }

    public String getAppliedAt() {
        return appliedAt;
    }

    // Job detail accessors
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPayout() {
        return payout;
    }

    public void setPayout(String payout) {
        this.payout = payout;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getFreelancerName() {
        return freelancerName;
    }

    public void setFreelancerName(String freelancerName) {
        this.freelancerName = freelancerName;
    }

    public String getFreelancerUserName() {
        return freelancerUserName;
    }

    public void setFreelancerUserName(String freelancerUserName) {
        this.freelancerUserName = freelancerUserName;
    }
}
