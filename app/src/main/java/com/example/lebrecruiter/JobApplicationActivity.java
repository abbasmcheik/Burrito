package com.example.lebrecruiter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lebrecruiter.models.Job;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JobApplicationActivity extends BaseActivity {

    private TextView textViewJobTitle, textViewJobDescription, textViewJobSkills, textViewJobPayout, textViewAppliedStatus;
    private Button btnApplyForJob;
    private ImageButton btnBookmark; // Bookmark Button
    private int jobId = -1;
    private int saveId = -1; // Store saveId for unsaving
    private boolean isApplied = false;
    private boolean isSaved = false; // Track bookmark state
    private String selectedJobStatus = "Closed";
    int freelancerId;

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
        btnBookmark = findViewById(R.id.btnBookmark); // Initialize Bookmark Button

        // Get job data from intent
        Job job = (Job) getIntent().getSerializableExtra("job");

        // Get userId and jobId
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        freelancerId = Integer.parseInt(sharedPreferences.getString("userId", "-1")); // SHARED PREFS
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

        // Check if the job is saved
        checkSavedStatus(freelancerId, jobId, null);

        // Handle Bookmark Button Click
        btnBookmark.setOnClickListener(v -> {
            if (isSaved) {
                unsaveJob(freelancerId, jobId);
            } else {
                saveJob(freelancerId, jobId);
            }
        });

        // Check application status
        if (freelancerId != -1 && job != null) {
            checkApplicationStatus(freelancerId, jobId);
        }

        if (isApplied || !selectedJobStatus.trim().equals("Open") || selectedJobStatus.trim().equals("In_Progress")) {
            btnApplyForJob.setVisibility(View.GONE);
        }

        // Handle Apply button click
        btnApplyForJob.setOnClickListener(v -> {
            if (!isApplied) {
                checkResumeAndApply(freelancerId);
            } else {
                Toast.makeText(this, "You have already applied for this job.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkSavedStatus(int freelancerId, int jobId, Runnable onStatusChecked) {
        String url = "http://10.0.2.2:8080/api/saved-jobs/freelancer/" + freelancerId;

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONArray savedJobs = new JSONArray(response);
                isSaved = false;
                saveId = -1; // Reset saveId to avoid stale data

                for (int i = 0; i < savedJobs.length(); i++) {
                    JSONObject savedJob = savedJobs.getJSONObject(i);
                    JSONObject job = savedJob.getJSONObject("job");

                    if (job.getInt("jobId") == jobId) {
                        isSaved = true;
                        saveId = savedJob.getInt("saveId"); // Store the updated saveId
                        break;
                    }
                }

                updateBookmarkIcon();
                // Execute the callback after updating saveId
                if (onStatusChecked != null) {
                    onStatusChecked.run();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to parse saved jobs.", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Toast.makeText(this, "Failed to check saved job status.", Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void saveJob(int freelancerId, int jobId) {
        String url = "http://10.0.2.2:8080/api/saved-jobs/" + jobId;

        JSONObject payload = new JSONObject();
        try {
            payload.put("userId", freelancerId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload, response -> {
            isSaved = true;
            updateBookmarkIcon();
            Toast.makeText(this, "Job bookmarked successfully!", Toast.LENGTH_SHORT).show();
        }, error -> {
            Toast.makeText(this, "Failed to bookmark the job.", Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void unsaveJob(int freelancerId, int jobId) {
        checkSavedStatus(freelancerId, jobId, () -> {
            if (saveId == -1) {
                Toast.makeText(this, "Failed to fetch bookmark details. Try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "http://10.0.2.2:8080/api/saved-jobs/" + saveId;

            StringRequest request = new StringRequest(Request.Method.DELETE, url, response -> {
                isSaved = false;
                updateBookmarkIcon();
                Toast.makeText(this, "Job unbookmarked successfully!", Toast.LENGTH_SHORT).show();
            }, error -> {
                Toast.makeText(this, "Failed to unbookmark the job.", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            });

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);
        });
    }

    private void updateBookmarkIcon() {
        if (isSaved) {
            btnBookmark.setImageResource(R.drawable.ic_bookmark_filled);
        } else {
            btnBookmark.setImageResource(R.drawable.ic_bookmark_border);
        }
    }

    private void checkResumeAndApply(int freelancerId) {
        String url = "http://10.0.2.2:8080/api/resumes/" + freelancerId;

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            // Resume exists, proceed with application
            applyForJob(jobId, freelancerId);
        }, error -> {
            // Resume not found
            Toast.makeText(this, "You must set up your resume before applying for a job.", Toast.LENGTH_LONG).show();
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void applyForJob(int jobId, int freelancerId) {
        String url = "http://10.0.2.2:8080/api/applications/" + jobId + "?freelancerId=" + freelancerId;

        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            Toast.makeText(this, "Application submitted successfully!", Toast.LENGTH_SHORT).show();
            isApplied = true;
            textViewAppliedStatus.setText("Already Applied");
            btnApplyForJob.setVisibility(View.GONE); // Hide the button
        }, error -> {
            Toast.makeText(this, "Failed to apply for the job.", Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void checkApplicationStatus(int freelancerId, int jobId) {
        String url = "http://10.0.2.2:8080/api/applications/status?jobId=" + jobId + "&freelancerId=" + freelancerId;

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            // Update UI based on application status
            if (response.equalsIgnoreCase("Not Applied")) {
                isApplied = false;
                textViewAppliedStatus.setText("Not Applied");
                if (selectedJobStatus.trim().equals("Open"))
                    btnApplyForJob.setVisibility(View.VISIBLE); // Show the button
            } else {
                isApplied = true;
                textViewAppliedStatus.setText("Status: " + response);
                btnApplyForJob.setVisibility(View.GONE); // Hide the button
            }
        }, error -> {
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
