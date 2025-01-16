package com.example.lebrecruiter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lebrecruiter.models.Job;

public class JobApplicationActivity extends BaseActivity {

    private TextView textViewJobTitle, textViewJobDescription, textViewJobSkills, textViewJobPayout, textViewAppliedStatus;
    private Button btnApplyForJob;
    private int jobId = -1;
    private boolean isApplied = false; // Replace with actual logic from your backend

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
        String userId = sharedPreferences.getString("userId", "-1"); // SHARED PREFS
        jobId = job.getJobId();

        // Populate UI with job data
        if (job != null) {
            textViewJobTitle.setText(job.getTitle());
            textViewJobDescription.setText(job.getDescription());
            textViewJobSkills.setText("Skills Required: " + job.getSkillsRequired());
            textViewJobPayout.setText("Payout: $" + job.getPayout());
            textViewAppliedStatus.setText(isApplied ? "Already Applied" : "Not Applied");
        }

        // Handle Apply button click
        btnApplyForJob.setOnClickListener(v -> {
            if (!isApplied) {
                applyForJob(job.getJobId());
            } else {
                Toast.makeText(this, "You have already applied for this job.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyForJob(int jobId) {
        // Add logic to apply for the job via an API call
        Toast.makeText(this, "Application submitted successfully!", Toast.LENGTH_SHORT).show();
        isApplied = true;
        textViewAppliedStatus.setText("Already Applied");
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_job_application;
    }
}
