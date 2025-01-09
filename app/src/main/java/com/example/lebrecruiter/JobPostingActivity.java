package com.example.lebrecruiter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JobPostingActivity extends AppCompatActivity {

    private EditText jobTitleEditText, jobDescriptionEditText, skillsEditText, categoryEditText, payoutEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_posting);

        // Get the user role and recruiter ID from Intent
        Intent intent = getIntent();
        String userRole = intent.getStringExtra("role");
        // Retrieve user data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int recruiterId = sharedPreferences.getInt("userId", -1);

        // Restrict access to Recruiters only
        if (!"Recruiter".equalsIgnoreCase(userRole)) {
            Toast.makeText(this, "Access Denied: Only Recruiters can post jobs.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize input fields
        jobTitleEditText = findViewById(R.id.editTextJobTitle);
        jobDescriptionEditText = findViewById(R.id.editTextJobDescription);
        skillsEditText = findViewById(R.id.editTextSkills);
        categoryEditText = findViewById(R.id.editTextCategory);
        payoutEditText = findViewById(R.id.editTextPayout);

        // Submit button
        Button submitButton = findViewById(R.id.buttonSubmitJob);

        submitButton.setOnClickListener(v -> {
            // Validate input fields
            if (!validateInputs()) {
                return;
            }

            // Gather input data
            String title = jobTitleEditText.getText().toString().trim();
            String description = jobDescriptionEditText.getText().toString().trim();
            String skillsRequired = skillsEditText.getText().toString().trim();
            String category = categoryEditText.getText().toString().trim();
            double payout = Double.parseDouble(payoutEditText.getText().toString().trim());

            // Prepare JSON body
            JSONObject jobData = new JSONObject();
            try {
                jobData.put("title", title);
                jobData.put("description", description);
                jobData.put("category", category);
                jobData.put("skillsRequired", skillsRequired);
                jobData.put("payout", payout);
                jobData.put("status", "Open");

                // Add recruiter information
                JSONObject recruiter = new JSONObject();
                recruiter.put("userId", recruiterId);
                jobData.put("recruiter", recruiter);

                // Send POST request
                postJob(jobData);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to create job data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs() {
        if (jobTitleEditText.getText().toString().trim().isEmpty()) {
            jobTitleEditText.setError("Job Title is required");
            return false;
        }
        if (jobDescriptionEditText.getText().toString().trim().isEmpty()) {
            jobDescriptionEditText.setError("Job Description is required");
            return false;
        }
        if (skillsEditText.getText().toString().trim().isEmpty()) {
            skillsEditText.setError("Required Skills are mandatory");
            return false;
        }
        if (categoryEditText.getText().toString().trim().isEmpty()) {
            categoryEditText.setError("Category is required");
            return false;
        }
        if (payoutEditText.getText().toString().trim().isEmpty()) {
            payoutEditText.setError("Payout is required");
            return false;
        }
        return true;
    }

    private void postJob(JSONObject jobData) {
        String url = "http://10.0.2.2:8080/api/jobs";

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, jobData,
                response -> Toast.makeText(this, "Job Posted Successfully", Toast.LENGTH_SHORT).show(),
                error -> {
                    Toast.makeText(this, "Failed to Post Job", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Add request to the queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(postRequest);
    }
}
