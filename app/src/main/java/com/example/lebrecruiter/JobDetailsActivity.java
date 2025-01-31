package com.example.lebrecruiter;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lebrecruiter.utils.VolleyMultipartRequest;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class JobDetailsActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST_CODE = 1;
    private int jobId; // Job ID for API calls
    private TextView statusTextView;
    private TextView titleTextView, descriptionTextView, categoryTextView, skillsTextView, payoutTextView;
    private EditText titleEditText, descriptionEditText, categoryEditText, skillsEditText, payoutEditText;
    private ImageButton editButton;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_details);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Job Details");
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        // Retrieve job data passed from MyJobsActivity
        jobId = getIntent().getIntExtra("jobId", -1); // Ensure jobId is passed
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String category = getIntent().getStringExtra("category");
        String skillsRequired = getIntent().getStringExtra("skillsRequired");
        String payout = getIntent().getStringExtra("payout");
        String status = getIntent().getStringExtra("status");

        // Check if jobId is valid
        if (jobId == -1) {
            Toast.makeText(this, "Error: Job ID not provided", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if jobId is invalid
            return;
        }

        // Initialize UI components
        titleTextView = findViewById(R.id.textViewJobTitle);
        descriptionTextView = findViewById(R.id.textViewJobDescription);
        categoryTextView = findViewById(R.id.textViewCategory);
        skillsTextView = findViewById(R.id.textViewSkillsRequired);
        payoutTextView = findViewById(R.id.textViewPayout);

        titleEditText = findViewById(R.id.editTextJobTitle);
        descriptionEditText = findViewById(R.id.editTextJobDescription);
        categoryEditText = findViewById(R.id.editTextCategory);
        skillsEditText = findViewById(R.id.editTextSkillsRequired);
        payoutEditText = findViewById(R.id.editTextPayout);

        statusTextView = findViewById(R.id.textViewStatus);
        editButton = findViewById(R.id.buttonEdit);
        saveButton = findViewById(R.id.buttonSave);

        // Populate fields
        titleTextView.setText("Title: " + title);
        descriptionTextView.setText("Description: " + description);
        categoryTextView.setText("Category: " + category);
        skillsTextView.setText("Skills Required: " + skillsRequired);
        payoutTextView.setText("Payout: " + payout);
        statusTextView.setText("Status: " + status);

        titleTextView.setVisibility(View.VISIBLE);
        descriptionTextView.setVisibility(View.VISIBLE);
        categoryTextView.setVisibility(View.VISIBLE);
        skillsTextView.setVisibility(View.VISIBLE);
        payoutTextView.setVisibility(View.VISIBLE);

        TextView statusTextView = findViewById(R.id.textViewStatus);
        statusTextView.setText(status);
        // Set the text color based on the status value
        if ("Open".equalsIgnoreCase(status)) {
            statusTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
        } else if ("Closed".equalsIgnoreCase(status)) {
            statusTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
        } else {
            statusTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_light));
        }

        // Show Edit Button Only for Open Jobs
        ImageButton editButton = findViewById(R.id.buttonEdit);

        if ("Open".equalsIgnoreCase(status)) {
            editButton.setVisibility(View.VISIBLE);
        } else {
            editButton.setVisibility(View.GONE);
        }


        // Disable editing by default
        enableEditing(false);

        // Setup Buttons
        Button uploadButton = findViewById(R.id.buttonUploadFile);
        Button downloadButton = findViewById(R.id.buttonDownloadFile);

        uploadButton.setOnClickListener(v -> selectFile());
        downloadButton.setOnClickListener(v -> downloadFile());

        // Delete Button Logic
        Button deleteButton = findViewById(R.id.buttonDeleteJob);

        // Only show the delete button for Open or Closed jobs
        if ("Open".equalsIgnoreCase(status) || "Closed".equalsIgnoreCase(status)) {
            deleteButton.setVisibility(View.VISIBLE);

            // Add click listener for delete button
            deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
        }

        // Handle Edit Button
        editButton.setOnClickListener(v -> {
            enableEditing(true);
            saveButton.setVisibility(View.VISIBLE); // Show Save Button
            editButton.setVisibility(View.GONE);    // Hide Edit Button
            deleteButton.setVisibility(View.GONE); // Hide Delete Button
        });

        // Handle Save Button
        saveButton.setOnClickListener(v -> {
            if (validatePayout()) {
                saveJobDetails(titleEditText.getText().toString().trim(), descriptionEditText.getText().toString().trim(), categoryEditText.getText().toString().trim(), skillsEditText.getText().toString().trim(), payoutEditText.getText().toString().trim());
                deleteButton.setVisibility(View.VISIBLE); // Show Delete Button after saving
            } else {
                Toast.makeText(this, "Payout must be a valid number.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Allow all file types
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                uploadFile(fileUri);
            }
        }
    }

    private void uploadFile(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, bytesRead);
            }
            byte[] fileData = byteBuffer.toByteArray();

            String url = "http://10.0.2.2:8080/api/jobs/" + jobId + "/uploadWorkFile";

            Log.d("UPLOAD", "Uploading file: " + getFileName(fileUri) + ", Size: " + fileData.length + " bytes");

            // Try Uploading with Retry Logic
            sendFileRequest(url, fileData, fileUri, 0); // Initial attempt

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show();
        }
    }

    // New Method: Handles Retrying if Upload Fails
    private void sendFileRequest(String url, byte[] fileData, Uri fileUri, int attempt) {
        if (attempt >= 3) { // Max retries = 3
            Toast.makeText(this, "Failed to upload file after multiple attempts.", Toast.LENGTH_LONG).show();
            return;
        }

        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, url,
                response -> Toast.makeText(this, "File uploaded successfully!", Toast.LENGTH_SHORT).show(),
                error -> {
                    Log.e("UPLOAD_ERROR", "Upload failed, retrying... Attempt " + (attempt + 1), error);
                    sendFileRequest(url, fileData, fileUri, attempt + 1); // Retry Upload
                }) {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("file", new DataPart(getFileName(fileUri), fileData, "application/pdf"));
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }



    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) { // Ensure the column exists
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void downloadFile() {
        String url = "http://10.0.2.2:8080/api/jobs/" + jobId + "/downloadWorkFile";

        Request<NetworkResponse> request = new Request<NetworkResponse>(Request.Method.GET, url, error -> {
            Toast.makeText(this, "No work files for this job", Toast.LENGTH_SHORT).show();
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

                    String fileName = "workfile_" + jobId + ".pdf";
                    File outputFile = new File(downloadsFolder, fileName);
                    FileOutputStream fos = new FileOutputStream(outputFile);
                    fos.write(response.data);
                    fos.close();

                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "File downloaded to: " + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show());
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

    private void showDeleteConfirmationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Delete Job").setMessage("Are you sure you want to delete this job?").setPositiveButton("Yes", (dialogInterface, which) -> deleteJob()).setNegativeButton("No", (dialogInterface, which) -> dialogInterface.dismiss()).create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        });

        dialog.show();
    }

    private void deleteJob() {
        String url = "http://10.0.2.2:8080/api/jobs/" + jobId;

        StringRequest request = new StringRequest(Request.Method.DELETE, url, response -> {
            // Notify the user and pass result back
            Toast.makeText(this, "Job deleted successfully!", Toast.LENGTH_SHORT).show();

            // Pass the jobId back to MyJobsActivity to remove it from the list
            Intent resultIntent = new Intent();
            resultIntent.putExtra("deletedJobId", jobId);
            setResult(RESULT_OK, resultIntent);

            // Finish the activity
            finish();
        }, error -> {
            // Handle errors
            Toast.makeText(this, "Failed to delete job", Toast.LENGTH_SHORT).show();
            Log.e("DeleteJobError", "Error: ", error);
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void enableEditing(boolean enable) {
        toggleField(titleTextView, titleEditText, enable);
        toggleField(descriptionTextView, descriptionEditText, enable);
        toggleField(categoryTextView, categoryEditText, enable);
        toggleField(skillsTextView, skillsEditText, enable);
        toggleField(payoutTextView, payoutEditText, enable);
    }

    private void toggleField(TextView textView, EditText editText, boolean editable) {
        if (editable) {
            // When editing, copy text from TextView to EditText
            String currentText = textView.getText().toString();
            int prefixIndex = currentText.indexOf(":");
            if (prefixIndex != -1) {
                currentText = currentText.substring(prefixIndex + 1).trim(); // Remove prefix
            }
            editText.setText(currentText);
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
        } else {
            // When displaying, copy text from EditText to TextView only if EditText has a value
            if (!editText.getText().toString().isEmpty()) {
                String prefix = textView.getText().toString().split(":")[0]; // Extract prefix before ':'
                textView.setText(prefix + ": " + editText.getText().toString().trim());
            }
            editText.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }
    }

    private boolean validatePayout() {
        try {
            Double.parseDouble(payoutEditText.getText().toString().trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void saveJobDetails(String title, String description, String category, String skills, String payout) {
        String url = "http://10.0.2.2:8080/api/jobs/" + jobId;

        // Create the request body as a JSON object
        Map<String, String> params = new HashMap<>();
        params.put("title", title);
        params.put("description", description);
        params.put("category", category);
        params.put("skillsRequired", skills);
        params.put("payout", payout);

        JSONObject jsonBody = new JSONObject(params);

        // Create the PUT request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonBody, response -> {
            Toast.makeText(this, "Job details updated successfully!", Toast.LENGTH_SHORT).show();

            // Disable editing after saving
            enableEditing(false);
            saveButton.setVisibility(View.GONE); // Hide Save Button
            editButton.setVisibility(View.VISIBLE); // Show Edit Button
        }, error -> {
            Toast.makeText(this, "Failed to update job details.", Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json"); // Set Content-Type to JSON
                return headers;
            }
        };

        // Add the request to the queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

}
