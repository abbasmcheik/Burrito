package com.example.lebrecruiter.models;

public class Job {
    private int jobId;
    private String title;
    private String description;
    private String status;
    private String skillsRequired;
    private String payout;
    private String category; // New field

    public Job(int jobId, String title, String description, String status, String skillsRequired, String payout, String category) {
        this.jobId = jobId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.skillsRequired = skillsRequired;
        this.payout = payout;
        this.category = category; // Initialize new field
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

    public String getStatus() {
        return status;
    }

    public String getSkillsRequired() {
        return skillsRequired;
    }

    public String getPayout() {
        return payout;
    }

    public String getCategory() {
        return category; // New getter
    }
}
