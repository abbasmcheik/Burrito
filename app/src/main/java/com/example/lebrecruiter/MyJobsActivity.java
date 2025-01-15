package com.example.lebrecruiter;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.lebrecruiter.models.Job;
import com.example.lebrecruiter.models.JobAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyJobsActivity extends BaseActivity {

    private GridView jobsGridView;
    private ArrayList<Job> jobsList;
    private JobAdapter jobAdapter;

    private static final int JOB_DETAILS_REQUEST_CODE = 100; // Request code for JobDetailsActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get user role from SharedPreferences
        String userRole = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("role", "");

        // Restrict access if the user is not a recruiter
        if (!"recruiter".equalsIgnoreCase(userRole)) {
            Toast.makeText(this, "Access Denied. This page is only for recruiters.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
            return;
        }

        // Get userId from SharedPreferences
        String userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", "");

        // Restrict access if userId is not valid
        if (userId.isEmpty()) {
            Toast.makeText(this, "Access Denied. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        jobsGridView = findViewById(R.id.jobsGridView);
        jobsList = new ArrayList<>();
        jobAdapter = new JobAdapter(this, jobsList, R.layout.job_item);
        jobsGridView.setAdapter(jobAdapter);

        // Fetch jobs
        fetchJobs(userId);

        // Handle item clicks
        jobsGridView.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            Job selectedJob = jobsList.get(position);

            Intent intent = new Intent(MyJobsActivity.this, JobDetailsActivity.class);
            intent.putExtra("jobId", selectedJob.getJobId());
            intent.putExtra("title", selectedJob.getTitle());
            intent.putExtra("description", selectedJob.getDescription());
            intent.putExtra("category", "Sample Category"); // Replace with real category if available
            intent.putExtra("skillsRequired", selectedJob.getSkillsRequired()); // Pass actual skills
            intent.putExtra("payout", selectedJob.getPayout()); // Pass actual payout
            intent.putExtra("status", selectedJob.getStatus());

            startActivityForResult(intent, JOB_DETAILS_REQUEST_CODE);
        });
    }

    private void fetchJobs(String userId) {
        String url = "http://10.0.2.2:8080/api/jobs/recruiter/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
            jobsList.clear();
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject jobJson = response.getJSONObject(i);
                    String title = jobJson.getString("title");
                    String description = jobJson.getString("description");
                    String status = jobJson.getString("status");
                    String skillsRequired = jobJson.getString("skillsRequired"); // Fetch skills
                    String payout = jobJson.getString("payout"); // Fetch payout
                    int jobId = jobJson.getInt("jobId");

                    jobsList.add(new Job(jobId, title, description, status, skillsRequired, payout));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            jobAdapter.notifyDataSetChanged();
        }, error -> {
            Toast.makeText(this, "Failed to load jobs.", Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == JOB_DETAILS_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            int deletedJobId = data.getIntExtra("deletedJobId", -1);

            // Remove the deleted job from the list
            if (deletedJobId != -1) {
                for (int i = 0; i < jobsList.size(); i++) {
                    if (jobsList.get(i).getJobId() == deletedJobId) {
                        jobsList.remove(i);
                        break;
                    }
                }

                // Notify the adapter about the changes
                jobAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_my_jobs;
    }
}
