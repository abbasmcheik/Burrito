package com.example.lebrecruiter.models;

import java.util.List;

public class JobWithApplications {
    private String title;
    private List<Application> applications;

    public JobWithApplications(String title, List<Application> applications) {
        this.title = title;
        this.applications = applications;
    }

    public String getTitle() {
        return title;
    }

    public List<Application> getApplications() {
        return applications;
    }
}
