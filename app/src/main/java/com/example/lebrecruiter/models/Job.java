package com.example.lebrecruiter.models;

public class Job {
    private int jobId;
    private String title;
    private String description;
    private String status;

    public Job(int jobId, String title, String description, String status) {
        this.jobId = jobId;
        this.title = title;
        this.description = description;
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

    public String getStatus() {
        return status;
    }
}
