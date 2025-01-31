package com.example.lebrecruiter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.lebrecruiter.models.Application;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationsActivity extends BaseActivity {

    private LinearLayout jobsContainer;
    private String recruiterId;

    private final ActivityResultLauncher<Intent> applicationDetailsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getBooleanExtra("jobStatusUpdated", false)) {
                fetchApplications(Integer.parseInt(recruiterId));
            }
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        jobsContainer = findViewById(R.id.jobsContainer);

        // Fetch recruiterId from SharedPreferences
        recruiterId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", "");

        if (recruiterId.isEmpty()) {
            Toast.makeText(this, "Recruiter ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch applications
        fetchApplications(Integer.parseInt(recruiterId));
    }


    private void fetchApplications(int recruiterId) {
        if (recruiterId == -1) {
            recruiterId = getCurrentRecruiterId(); // Ensure we always have a valid recruiterId
        }

        String url = "http://10.0.2.2:8080/api/applications/recruiter/" + recruiterId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
            List<Application> applications = new ArrayList<>();

            // Parse applications from the response
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject appJson = response.getJSONObject(i);

                    // Application details
                    int applicationId = appJson.getInt("applicationId");
                    String status = appJson.getString("status");
                    String appliedAt = appJson.getString("appliedAt");

                    // Job details (nested object)
                    JSONObject jobJson = appJson.getJSONObject("job");
                    int jobId = jobJson.getInt("jobId");
                    String title = jobJson.getString("title");
                    String description = jobJson.getString("description");
                    String category = jobJson.getString("category");
                    String skillsRequired = jobJson.getString("skillsRequired");
                    String payout = jobJson.getString("payout");
                    String jobStatus = jobJson.getString("status");

                    // Freelancer details (nested object)
                    JSONObject freelancerJson = appJson.getJSONObject("freelancer");
                    int freelancerId = freelancerJson.getInt("userId");
                    String freelancerName = freelancerJson.getString("firstName") + " " + freelancerJson.getString("lastName");
                    String freelancerUserName = freelancerJson.getString("userName");

                    // Create Application object
                    Application application = new Application(applicationId, jobId, freelancerId, status, appliedAt);
                    application.setTitle(title);
                    application.setDescription(description);
                    application.setSkills(skillsRequired);
                    application.setCategory(category);
                    application.setPayout(payout);
                    application.setJobStatus(jobStatus);

                    // Set freelancer name for display (if needed)
                    application.setFreelancerName(freelancerName);
                    application.setFreelancerUserName(freelancerUserName); // Add username

                    applications.add(application);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // Group applications by jobId and display them
            groupAndDisplayApplications(applications);
        }, error -> {
            Toast.makeText(this, "Failed to load applications.", Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    // Utility method to get the current recruiter ID
    private int getCurrentRecruiterId() {
        String recruiterId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", "");
        return recruiterId.isEmpty() ? -1 : Integer.parseInt(recruiterId);
    }


    private void groupAndDisplayApplications(List<Application> applications) {
        // Group applications by jobId
        Map<Integer, List<Application>> groupedApplications = new HashMap<>();
        for (Application app : applications) {
            groupedApplications.computeIfAbsent(app.getJobId(), k -> new ArrayList<>()).add(app);
        }

        // Display applications grouped by jobId
        jobsContainer.removeAllViews(); // Clear existing views
        for (Map.Entry<Integer, List<Application>> entry : groupedApplications.entrySet()) {
            addJobSection(entry.getValue());
        }
    }

    private void addJobSection(List<Application> applications) {
        if (!applications.isEmpty()) {
            // Add a separator for the job section
            if (jobsContainer.getChildCount() > 0) { // Add separator only if it's not the first job
                View jobSeparator = new View(this);
                jobSeparator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 4)); // Height of the separator
                jobSeparator.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                jobsContainer.addView(jobSeparator);
            }

            Application firstApplication = applications.get(0);

            // Job Title
            TextView jobTitle = new TextView(this);
            jobTitle.setText("Job: " + firstApplication.getTitle());
            jobTitle.setTextSize(20);
            jobTitle.setPadding(16, 16, 16, 8);
            jobTitle.setTextColor(getResources().getColor(android.R.color.white));
            jobTitle.setBackgroundColor(getResources().getColor(android.R.color.black));

            // Add margin for spacing
            LinearLayout.LayoutParams jobTitleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            jobTitleParams.setMargins(0, 28, 0, 28); // Top and bottom margins
            jobTitle.setLayoutParams(jobTitleParams);

            jobsContainer.addView(jobTitle);

            // Applications
            for (Application app : applications) {
                View applicationView = getApplicationView(app);
                jobsContainer.addView(applicationView);
            }
        }
    }

    private View getApplicationView(Application application) {
        LinearLayout applicationLayout = new LinearLayout(this);
        applicationLayout.setOrientation(LinearLayout.VERTICAL);
        applicationLayout.setPadding(16, 16, 16, 16);
        applicationLayout.setBackgroundColor(getResources().getColor(android.R.color.black));

        // Add margin for spacing
        LinearLayout.LayoutParams applicationLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        applicationLayoutParams.setMargins(0, 8, 0, 8); // Top and bottom margins
        applicationLayout.setLayoutParams(applicationLayoutParams);

        // Freelancer Username
        TextView freelancerUserName = new TextView(this);
        freelancerUserName.setText("Freelancer: " + application.getFreelancerUserName());
        freelancerUserName.setTextColor(getResources().getColor(android.R.color.white));
        applicationLayout.addView(freelancerUserName);

        // Application Status
        TextView status = new TextView(this);
        status.setText("Status: " + application.getStatus());
        status.setTextColor(getResources().getColor(android.R.color.white));
        applicationLayout.addView(status);

        // Applied At (Date only)
        TextView appliedAt = new TextView(this);
        String dateOnly = application.getAppliedAt().split("T")[0]; // Extract date portion
        appliedAt.setText("Applied At: " + dateOnly);
        appliedAt.setTextColor(getResources().getColor(android.R.color.white));
        applicationLayout.addView(appliedAt);

        // Add a separator
        View applicationSeparator = new View(this);
        applicationSeparator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)); // Height of the separator
        applicationSeparator.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        applicationLayout.addView(applicationSeparator);

        // Make the application clickable
        applicationLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, ApplicationDetailsActivity.class);
            intent.putExtra("applicationId", application.getApplicationId());
            intent.putExtra("freelancerId", application.getFreelancerId());
            intent.putExtra("jobId", application.getJobId());
            intent.putExtra("status", application.getStatus());
            intent.putExtra("appliedAt", application.getAppliedAt());
            intent.putExtra("title", application.getTitle());
            intent.putExtra("description", application.getDescription());
            intent.putExtra("skills", application.getSkills());
            intent.putExtra("category", application.getCategory());
            intent.putExtra("payout", application.getPayout());
            intent.putExtra("jobStatus", application.getJobStatus());

            // Launch with activity result launcher
            applicationDetailsLauncher.launch(intent);
        });

        return applicationLayout;
    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_applications;
    }

}
