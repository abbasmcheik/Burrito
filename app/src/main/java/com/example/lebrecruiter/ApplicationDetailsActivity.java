package com.example.lebrecruiter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ApplicationDetailsActivity extends AppCompatActivity {

    private TextView textViewFreelancerName, textViewFreelancerEmail, textViewFreelancerSkills, textViewFreelancerBio, textViewFreelancerExperience;
    private Button btnDownloadResume, btnAcceptFreelancer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_details);

        // Initialize UI elements
        textViewFreelancerName = findViewById(R.id.textViewFreelancerName);
        textViewFreelancerEmail = findViewById(R.id.textViewFreelancerEmail);
        textViewFreelancerSkills = findViewById(R.id.textViewFreelancerSkills);
        textViewFreelancerBio = findViewById(R.id.textViewFreelancerBio);
        textViewFreelancerExperience = findViewById(R.id.textViewFreelancerExperience); // Added for experience
        btnDownloadResume = findViewById(R.id.btnDownloadResume);
        btnAcceptFreelancer = findViewById(R.id.btnAcceptFreelancer);

        // Get freelancerId from intent
        Intent intent = getIntent();
        int freelancerId = intent.getIntExtra("freelancerId", -1);
        int jobId = intent.getIntExtra("jobId", -1);

        // Fetch freelancer details
        if (freelancerId != -1) {
            fetchFreelancerDetails(freelancerId);
        } else {
            Toast.makeText(this, "Invalid Freelancer ID", Toast.LENGTH_SHORT).show();
        }

        // Handle "Download Resume" button
        btnDownloadResume.setOnClickListener(v -> downloadResume(freelancerId));

        // Handle "Accept Freelancer" button
        btnAcceptFreelancer.setOnClickListener(v -> {
            int applicationId = getIntent().getIntExtra("applicationId", -1);
            String applicationStatus = getIntent().getStringExtra("status");
            String jobStatus = getIntent().getStringExtra("jobStatus");

            handleAcceptFreelancer(applicationId, applicationStatus, jobStatus, jobId);
        });
    }

    private void fetchFreelancerDetails(int freelancerId) {
        String url = "http://10.0.2.2:8080/api/resumes/" + freelancerId;

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject resumeJson = new JSONObject(response);

                // Parse resume details
                String skills = resumeJson.getString("skills");
                String aboutMe = resumeJson.getString("aboutMe");
                int yearsOfExperience = resumeJson.getInt("yearsOfExperience");

                // Parse freelancer details (nested object)
                JSONObject freelancerJson = resumeJson.getJSONObject("freelancer");
                String name = freelancerJson.getString("firstName") + " " + freelancerJson.getString("lastName");
                String email = freelancerJson.getString("email");

                // Populate UI with freelancer details
                textViewFreelancerName.setText("Name: " + name);
                textViewFreelancerEmail.setText("Email: " + email);
                textViewFreelancerSkills.setText("Skills: " + skills);
                textViewFreelancerBio.setText("Bio: " + aboutMe);
                textViewFreelancerExperience.setText("Experience: " + yearsOfExperience + " years"); // Populate experience

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to parse freelancer details.", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Toast.makeText(this, "Failed to fetch freelancer details.", Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void downloadResume(int freelancerId) {
        String url = "http://10.0.2.2:8080/api/resumes/" + freelancerId + "/download";

        Request<NetworkResponse> request = new Request<NetworkResponse>(Request.Method.GET, url, error -> {
            Toast.makeText(this, "Resume file not available for download.", Toast.LENGTH_SHORT).show();
            Log.e("DownloadError", "Error: ", error);
        }) {
            @Override
            protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
                try {
                    // Check if response data is empty
                    if (response.data == null || response.data.length == 0) {
                        return Response.error(new ParseError(new Exception("No file available for download.")));
                    }

                    // Save the binary file
                    File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!downloadsFolder.exists()) {
                        downloadsFolder.mkdirs();
                    }

                    String fileName = "resume_" + freelancerId + ".pdf";
                    File outputFile = new File(downloadsFolder, fileName);
                    FileOutputStream fos = new FileOutputStream(outputFile);
                    fos.write(response.data);
                    fos.close();

                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Resume downloaded to: " + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show());
                } catch (Exception e) {
                    e.printStackTrace();
                    return Response.error(new ParseError(e));
                }
                return Response.success(response, null);
            }

            @Override
            protected void deliverResponse(NetworkResponse response) {
                // No additional delivery needed
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/pdf");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void handleAcceptFreelancer(int applicationId, String applicationStatus, String jobStatus, int jobId) {
        // Validation checks
        if (!jobStatus.equalsIgnoreCase("Open")) {
            Toast.makeText(this, "Job is no longer open for applications.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (applicationStatus.equalsIgnoreCase("Accepted")) {
            Toast.makeText(this, "This application has already been accepted.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (applicationStatus.equalsIgnoreCase("Rejected")) {
            Toast.makeText(this, "This application has been rejected and cannot be accepted.", Toast.LENGTH_SHORT).show();
            return;
        }

        // API Request to Accept the Application
        String acceptApplicationUrl = "http://10.0.2.2:8080/api/applications/" + applicationId + "/status";

        JSONObject statusUpdate = new JSONObject();
        try {
            statusUpdate.put("status", "Accepted");
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to prepare the status update.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest acceptRequest = new JsonObjectRequest(Request.Method.PUT, acceptApplicationUrl, statusUpdate, response -> {
            // Application accepted, now update the job status
            updateJobStatus(jobId);
        }, error -> {
            Toast.makeText(this, "Failed to accept freelancer. Please try again.", Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(acceptRequest);
    }

    private void updateJobStatus(int jobId) {
        String updateJobStatusUrl = "http://10.0.2.2:8080/api/jobs/" + jobId + "/status";

        JSONObject statusUpdate = new JSONObject();
        try {
            statusUpdate.put("status", "In_Progress");
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to prepare the job status update.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jobStatusRequest = new JsonObjectRequest(Request.Method.PUT, updateJobStatusUrl, statusUpdate, response -> {
            Toast.makeText(this, "Job status updated to 'In Progress'.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity after successful status update
        }, error -> {
            Toast.makeText(this, "Failed to update job status. Please try again.", Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jobStatusRequest);
    }

}
