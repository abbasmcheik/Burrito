package com.example.lebrecruiter.models;

import java.io.Serializable;

public class Job implements Serializable {
    private int jobId;
    private String title;
    private String description;
    private String category;
    private String skillsRequired;
    private String payout; // Changed from double to String
    private String status;

    public Job(int jobId, String title, String description, String category, String skillsRequired, String payout, String status) {
        this.jobId = jobId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.skillsRequired = skillsRequired;
        this.payout = payout;
        this.status = status;
    }

    public int getJobId() {
        return jobId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getSkillsRequired() {
        return skillsRequired;
    }

    public String getPayout() {
        return payout;
    }

    public String getStatus() {
        return status;
    }
}
