package com.example.lebrecruiter.models;

import java.io.Serializable;

public class Application implements Serializable {
    private int applicationId;
    private int jobId;
    private int freelancerId;
    private String status;

    public Application(int applicationId, int jobId, int freelancerId, String status) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.freelancerId = freelancerId;
        this.status = status;
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
}
