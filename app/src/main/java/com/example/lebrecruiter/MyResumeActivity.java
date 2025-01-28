package com.example.lebrecruiter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lebrecruiter.utils.VolleyMultipartRequest;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MyResumeActivity extends BaseActivity {

    private ImageButton editButton;
    private TextView experienceTextView, skillsTextView, aboutMeTextView;
    private EditText experienceEditText, skillsEditText, aboutMeEditText;
    private Button saveButton;
    private RequestQueue requestQueue;
    private String userId;
    private static final int PICK_FILE_REQUEST = 1;
    private Button uploadResumeButton;
    private String uploadUrl = "http://10.0.2.2:8080/api/resumes/{freelancerId}/upload"; // Replace {freelancerId} dynamically


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Views
        experienceTextView = findViewById(R.id.textViewExperience);
        skillsTextView = findViewById(R.id.textViewSkills);
        aboutMeTextView = findViewById(R.id.textViewAboutMe);

        experienceEditText = findViewById(R.id.editTextExperience);
        skillsEditText = findViewById(R.id.editTextSkills);
        aboutMeEditText = findViewById(R.id.editTextAboutMe);

        editButton = findViewById(R.id.buttonEditResume);
        saveButton = findViewById(R.id.buttonSaveResume);
        uploadResumeButton = findViewById(R.id.buttonUploadResume);

        // Disable upload button by default
        uploadResumeButton.setEnabled(false);
        uploadResumeButton.setOnClickListener(v -> openFileChooser());

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Get User ID from Shared Preferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "-1");

        // Load resume data and check existence
        getResume(userId);

        // Edit Button Logic
        editButton.setOnClickListener(v -> toggleEditing(true));

        // Save Button Logic
        saveButton.setOnClickListener(v -> {
            String experience = experienceEditText.getText().toString().trim();
            String skills = skillsEditText.getText().toString().trim();
            String aboutMe = aboutMeEditText.getText().toString().trim();

            if (validateFields(experience, skills, aboutMe)) {
                saveResume(userId, experience, skills, aboutMe);
            }
        });

        // Disable Editing by Default
        toggleEditing(false);
    }

    private void getResume(String userId) {
        String url = "http://10.0.2.2:8080/api/resumes/" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                // Parse response
                JSONObject resume = new JSONObject(response);
                experienceTextView.setText("Years of Experience: " + resume.optString("yearsOfExperience", ""));
                skillsTextView.setText("Skills: " + resume.optString("skills", ""));
                aboutMeTextView.setText("About Me: " + resume.optString("aboutMe", ""));

                // Enable the upload button as resume exists
                uploadResumeButton.setEnabled(true);
            } catch (Exception e) {
                Toast.makeText(MyResumeActivity.this, "Failed to parse resume data.", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                // Resume does not exist, show a message
                Toast.makeText(MyResumeActivity.this, "Please set up your resume before uploading a CV.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MyResumeActivity.this, "Failed to fetch resume data.", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(request);
    }


    private void saveResume(String userId, String experience, String skills, String aboutMe) {
        String getUrl = "http://10.0.2.2:8080/api/resumes/" + userId;

        // Check if resume exists using GET API
        StringRequest getRequest = new StringRequest(Request.Method.GET, getUrl, response -> {
            // Resume exists, call update
            updateResume(userId, experience, skills, aboutMe);
        }, error -> {
            if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                // Resume does not exist, call create
                createResume(userId, experience, skills, aboutMe);
            } else {
                // Handle other errors
                Toast.makeText(this, "Failed to fetch resume. Try again.", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(getRequest);
    }

    private void createResume(String userId, String experience, String skills, String aboutMe) {
        String createUrl = "http://10.0.2.2:8080/api/resumes/" + userId + "/create";

        // Prepare the request payload
        Map<String, String> params = new HashMap<>();
        params.put("yearsOfExperience", experience);
        params.put("skills", skills);
        params.put("aboutMe", aboutMe);

        JSONObject jsonBody = new JSONObject(params);

        StringRequest postRequest = new StringRequest(Request.Method.POST, createUrl, response -> {
            Toast.makeText(this, "Resume created successfully!", Toast.LENGTH_SHORT).show();
            toggleEditing(false); // Disable editing after success
            // Enable the upload button after a successful update
            uploadResumeButton.setEnabled(true);
        }, error -> Toast.makeText(this, "Failed to create resume.", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            public byte[] getBody() {
                return jsonBody.toString().getBytes();
            }
        };

        requestQueue.add(postRequest);
    }

    private void updateResume(String userId, String experience, String skills, String aboutMe) {
        String updateUrl = "http://10.0.2.2:8080/api/resumes/" + userId;

        // Prepare the request payload
        Map<String, String> params = new HashMap<>();
        params.put("yearsOfExperience", experience);
        params.put("skills", skills);
        params.put("aboutMe", aboutMe);

        JSONObject jsonBody = new JSONObject(params);

        StringRequest putRequest = new StringRequest(Request.Method.PUT, updateUrl, response -> {
            Toast.makeText(this, "Resume updated successfully!", Toast.LENGTH_SHORT).show();
            toggleEditing(false); // Disable editing after success
            // Enable the upload button after a successful update
            uploadResumeButton.setEnabled(true);
        }, error -> Toast.makeText(this, "Failed to update resume.", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            public byte[] getBody() {
                return jsonBody.toString().getBytes();
            }
        };

        requestQueue.add(putRequest);
    }

    private void toggleEditing(boolean enable) {
        if (enable) {
            // Enable editing: copy TextView content to EditText
            toggleField(experienceTextView, experienceEditText, true);
            toggleField(skillsTextView, skillsEditText, true);
            toggleField(aboutMeTextView, aboutMeEditText, true);

            // Show EditText fields and Save button, hide TextView fields, Edit button, and Upload button
            editButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.VISIBLE);
            uploadResumeButton.setVisibility(View.GONE); // Hide upload button during editing
        } else {
            // Disable editing: copy EditText content back to TextView
            toggleField(experienceTextView, experienceEditText, false);
            toggleField(skillsTextView, skillsEditText, false);
            toggleField(aboutMeTextView, aboutMeEditText, false);

            // Show TextView fields and Edit button, hide EditText fields, Save button, and show Upload button
            editButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.GONE);
            uploadResumeButton.setVisibility(View.VISIBLE); // Show upload button when not editing
        }
    }

    private void toggleField(TextView textView, EditText editText, boolean editable) {
        if (editable) {
            // Extract text without the prefix and populate EditText
            String currentText = textView.getText().toString();
            int prefixIndex = currentText.indexOf(":");
            if (prefixIndex != -1) {
                currentText = currentText.substring(prefixIndex + 1).trim();
            }
            editText.setText(currentText);

            // Hide TextView and show EditText
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
        } else {
            // Format text with prefix and copy back to TextView
            String prefix = textView.getText().toString().split(":")[0]; // Extract the label
            String updatedText = editText.getText().toString().trim();
            textView.setText(prefix + ": " + updatedText);

            // Hide EditText and show TextView
            editText.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }
    }

    private boolean validateFields(String experience, String skills, String aboutMe) {
        if (experience.isEmpty() || skills.isEmpty() || aboutMe.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void uploadResume(byte[] fileBytes) {
        String url = uploadUrl.replace("{freelancerId}", userId);

        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, url, response -> {
            Toast.makeText(MyResumeActivity.this, "Resume uploaded successfully!", Toast.LENGTH_SHORT).show();
        }, error -> {
            Toast.makeText(MyResumeActivity.this, "Failed to upload resume: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        }) {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("file", new DataPart("resume.pdf", fileBytes, "application/pdf")); // Key must match server's expectation
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }


    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf"); // Restrict to PDF files
        startActivityForResult(Intent.createChooser(intent, "Select Resume"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                // Get file input stream
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                byte[] fileBytes = readBytes(inputStream);

                // Upload file
                uploadResume(fileBytes);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to select file", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_my_resume;
    }
}
