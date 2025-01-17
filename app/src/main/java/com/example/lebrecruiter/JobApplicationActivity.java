package com.example.lebrecruiter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lebrecruiter.models.Job;

public class JobApplicationActivity extends BaseActivity {

    private TextView textViewJobTitle, textViewJobDescription, textViewJobSkills, textViewJobPayout, textViewAppliedStatus;
    private Button btnApplyForJob;
    private int jobId = -1;
    private boolean isApplied = false; // Replace with actual logic from your backend
    private String selectedJobStatus = "Closed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize UI elements
        textViewJobTitle = findViewById(R.id.textViewJobTitle);
        textViewJobDescription = findViewById(R.id.textViewJobDescription);
        textViewJobSkills = findViewById(R.id.textViewJobSkills);
        textViewJobPayout = findViewById(R.id.textViewJobPayout);
        textViewAppliedStatus = findViewById(R.id.textViewAppliedStatus);
        btnApplyForJob = findViewById(R.id.btnApplyForJob);

        // Get job data from intent
        Job job = (Job) getIntent().getSerializableExtra("job");

        //Get userId and jobId
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int freelancerId = Integer.parseInt(sharedPreferences.getString("userId", "-1")); // SHARED PREFS
        jobId = job.getJobId();
        selectedJobStatus = job.getStatus();
        // Populate UI with job data
        if (job != null) {
            textViewJobTitle.setText(job.getTitle());
            textViewJobDescription.setText(job.getDescription());
            textViewJobSkills.setText("Skills Required: " + job.getSkillsRequired());
            textViewJobPayout.setText("Payout: $" + job.getPayout());
            textViewAppliedStatus.setText(isApplied ? "Already Applied" : "Not Applied");
        }

        // Check application status
        if (freelancerId != -1 && job != null) {
            checkApplicationStatus(freelancerId, jobId);
        }

        if (isApplied || !selectedJobStatus.trim().equals("Open")) {
            btnApplyForJob.setVisibility(View.GONE);
        }

        // Handle Apply button click
        btnApplyForJob.setOnClickListener(v -> {
            if (!isApplied) {
                applyForJob(jobId, freelancerId);
            } else {
                Toast.makeText(this, "You have already applied for this job.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyForJob(int jobId, int freelancerId) {
        String url = "http://10.0.2.2:8080/api/applications/" + jobId + "?freelancerId=" + freelancerId;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Application submitted successfully!", Toast.LENGTH_SHORT).show();
                    isApplied = true;
                    textViewAppliedStatus.setText("Already Applied");
                    btnApplyForJob.setVisibility(View.GONE); // Hide the button
                },
                error -> {
                    Toast.makeText(this, "Failed to apply for the job.", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }


    private void checkApplicationStatus(int freelancerId, int jobId) {
        String url = "http://10.0.2.2:8080/api/applications/status?jobId=" + jobId + "&freelancerId=" + freelancerId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    // Update UI based on application status
                    if (response.equalsIgnoreCase("Not Applied")) {
                        isApplied = false;
                        textViewAppliedStatus.setText("Not Applied");
                        if (!selectedJobStatus.trim().equals("Closed"))
                            btnApplyForJob.setVisibility(View.VISIBLE); // Show the button
                    } else {
                        isApplied = true;
                        textViewAppliedStatus.setText("Status: " + response);
                        btnApplyForJob.setVisibility(View.GONE); // Hide the button
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to check application status.", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_job_application;
    }
}
