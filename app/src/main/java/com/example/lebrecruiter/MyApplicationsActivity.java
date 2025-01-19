package com.example.lebrecruiter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lebrecruiter.models.Application;
import com.example.lebrecruiter.models.ApplicationAdapter;
import com.example.lebrecruiter.models.Job;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyApplicationsActivity extends BaseActivity {

    private RecyclerView recyclerViewApplications;
    private ApplicationAdapter adapter;
    private List<Application> applications;
    private RequestQueue requestQueue;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize RecyclerView
        recyclerViewApplications = findViewById(R.id.recyclerViewApplications);
        recyclerViewApplications.setLayoutManager(new LinearLayoutManager(this));

        // Initialize data structures
        applications = new ArrayList<>();
        adapter = new ApplicationAdapter(applications);
        recyclerViewApplications.setAdapter(adapter);

        // Get user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "-1");

        // Initialize Volley
        requestQueue = Volley.newRequestQueue(this);

        // Fetch applications
        fetchApplications();
    }

    private void fetchApplications() {
        String url = "http://10.0.2.2:8080/api/applications/freelancer/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        applications.clear(); // Clear the list before populating
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject applicationJson = response.getJSONObject(i);

                            // Parse basic application details
                            int applicationId = applicationJson.getInt("applicationId");
                            String status = applicationJson.getString("status");
                            String appliedAt = applicationJson.getString("appliedAt");
                            int freelancerId = applicationJson.getJSONObject("freelancer").getInt("userId");

                            // Create a placeholder Job object
                            JSONObject jobJson = applicationJson.getJSONObject("job");
                            Job placeholderJob = new Job(
                                    jobJson.getInt("jobId"),
                                    "(Fetching...)", // Placeholder title
                                    "(Fetching...)", // Placeholder description
                                    "(Fetching...)", // Placeholder category
                                    "(Fetching...)", // Placeholder skills
                                    "(Fetching...)", // Placeholder payout
                                    "(Fetching...)"  // Placeholder job status
                            );

                            // Create an Application object with the placeholder Job
                            Application application = new Application(applicationId, placeholderJob.getJobId(), freelancerId, status, appliedAt);
                            applications.add(application);

                            // Fetch full job details
                            fetchJobDetails(application);
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to parse applications.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch applications.", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                });

        requestQueue.add(request);
    }



    private void fetchJobDetails(Application application) {
        String url = "http://10.0.2.2:8080/api/jobs/" + application.getJobId();

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jobJson = new JSONObject(response);

                        // Update application with job details
                        application.setTitle(jobJson.getString("title"));
                        application.setDescription(jobJson.getString("description"));
                        application.setSkills(jobJson.getString("skillsRequired"));
                        application.setCategory(jobJson.getString("category"));
                        application.setPayout(jobJson.getString("payout"));
                        application.setJobStatus(jobJson.getString("status"));

                        // Notify adapter of data changes
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to parse job details.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch job details.", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                });

        requestQueue.add(request);
    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_my_applications;
    }
}
