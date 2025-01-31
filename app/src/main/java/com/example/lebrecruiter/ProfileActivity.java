package com.example.lebrecruiter;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends BaseActivity {

    private TextView roleTextView, firstNameTextView, lastNameTextView, usernameTextView, emailTextView, dobTextView;
    private EditText firstNameEditText, lastNameEditText, dateOfBirthEditText;
    private EditText usernameEditText, emailEditText;

    private Button saveButton;
    private ImageButton editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View elements
        roleTextView = findViewById(R.id.textViewRole);
        firstNameTextView = findViewById(R.id.textViewFirstName);
        lastNameTextView = findViewById(R.id.textViewLastName);
        usernameTextView = findViewById(R.id.textViewUsername);
        emailTextView = findViewById(R.id.textViewEmail);
        dobTextView = findViewById(R.id.textViewDob);

        // Initialize Edit elements
        firstNameEditText = findViewById(R.id.editTextFirstName);
        lastNameEditText = findViewById(R.id.editTextLastName);
        dateOfBirthEditText = findViewById(R.id.editTextDateOfBirth);
        usernameEditText = findViewById(R.id.editTextUsername);
        emailEditText = findViewById(R.id.editTextEmail);

        // Initialize Labels and set them to GONE by default
        findViewById(R.id.labelFirstName).setVisibility(View.GONE);
        findViewById(R.id.labelLastName).setVisibility(View.GONE);
        findViewById(R.id.labelDob).setVisibility(View.GONE);
        findViewById(R.id.labelUsername).setVisibility(View.GONE);
        findViewById(R.id.labelEmail).setVisibility(View.GONE);

        //Buttons
        editButton = findViewById(R.id.buttonEditProfile);
        saveButton = findViewById(R.id.buttonSaveProfile);

        //GET DATA VIA SHARED PREFERENCES
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "-1"); // SHARED PREFS
        String role = sharedPreferences.getString("role", ""); // SHARED PREFS

        // Set user role at the top
        roleTextView.setText("Role: " + role);

        // Setup DatePicker for the Date of Birth field
        setupDatePicker();

        // Fetch user data from the server using the userId
        fetchUserData(userId);

        // Edit Button Logic
        editButton.setOnClickListener(v -> {
            toggleEditing(true);
            saveButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.GONE);
        });

        // Save Button Logic
        saveButton.setOnClickListener(v -> {
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String dateOfBirth = dateOfBirthEditText.getText().toString().trim();

            if (validateFields(firstName, lastName, dateOfBirth)) {
                saveProfileDetails(firstName, lastName, dateOfBirth);
            }
        });

        // Disable Editing by Default
        toggleEditing(false);

    }

    private void fetchUserData(String userId) {
        ApiHandler.getInstance(getApplicationContext()).getUserDetails(userId, new ApiHandler.UserDetailsCallback() {
            @Override
            public void onSuccess(JSONObject user) {
                firstNameTextView.setText("First Name: " + user.optString("firstName", ""));
                lastNameTextView.setText("Last Name: " + user.optString("lastName", ""));
                usernameTextView.setText("Username: " + user.optString("userName", ""));
                emailTextView.setText("Email: " + user.optString("email", ""));
                dobTextView.setText("Date of Birth: " + user.optString("dob", ""));
            }

            @Override
            public void onError(Exception error) {
                dobTextView.setText("Failed to fetch user data");
            }
        });
    }

    private void toggleEditing(boolean enable) {
        // Toggle editable fields (First Name, Last Name, Date of Birth)
        toggleField(firstNameTextView, firstNameEditText, findViewById(R.id.labelFirstName), enable);
        toggleField(lastNameTextView, lastNameEditText, findViewById(R.id.labelLastName), enable);
        toggleField(dobTextView, dateOfBirthEditText, findViewById(R.id.labelDob), enable);

        // Ensure labels for Username and Email remain HIDDEN unless in edit mode
        findViewById(R.id.labelUsername).setVisibility(enable ? View.VISIBLE : View.GONE);
        findViewById(R.id.labelEmail).setVisibility(enable ? View.VISIBLE : View.GONE);

        // Handle username field
        if (enable) {
            usernameTextView.setVisibility(View.GONE); // Hide TextView in edit mode
            usernameEditText.setVisibility(View.VISIBLE); // Show EditText
            usernameEditText.setText(usernameTextView.getText().toString().replace("Username: ", "").trim()); // Set the actual username
            usernameEditText.setEnabled(false); // Non-editable
            usernameEditText.setFocusable(false); // Ensure no focus
            usernameEditText.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        } else {
            usernameTextView.setVisibility(View.VISIBLE); // Show TextView in view mode
            usernameEditText.setVisibility(View.GONE); // Hide EditText
        }

        // Handle email field
        if (enable) {
            emailTextView.setVisibility(View.GONE); // Hide TextView in edit mode
            emailEditText.setVisibility(View.VISIBLE); // Show EditText
            emailEditText.setText(emailTextView.getText().toString().replace("Email: ", "").trim()); // Set the actual email
            emailEditText.setEnabled(false); // Non-editable
            emailEditText.setFocusable(false); // Ensure no focus
            emailEditText.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        } else {
            emailTextView.setVisibility(View.VISIBLE); // Show TextView in view mode
            emailEditText.setVisibility(View.GONE); // Hide EditText
        }
    }


    private void toggleField(TextView textView, EditText editText, TextView label, boolean editable) {
        if (editable) {
            label.setVisibility(View.VISIBLE);  // Show label in edit mode

            // Copy text from TextView to EditText, removing the prefix (content after ':')
            String currentText = textView.getText().toString();
            int prefixIndex = currentText.indexOf(":");
            if (prefixIndex != -1) {
                currentText = currentText.substring(prefixIndex + 1).trim();
            }
            editText.setText(currentText);
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
            editText.bringToFront(); // Ensure EditText is brought to the front
        } else {
            label.setVisibility(View.GONE);  // Hide label when not editing

            // Copy text from EditText to TextView, adding the prefix back
            String prefix = textView.getText().toString().split(":")[0]; // Extract prefix before ':'
            textView.setText(prefix + ": " + editText.getText().toString().trim());
            editText.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            textView.bringToFront(); // Ensure TextView is brought to the front
        }
    }

    private boolean validateFields(String firstName, String lastName, String dateOfBirth) {
        if (firstName.isEmpty() || lastName.isEmpty() || dateOfBirth.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveProfileDetails(String firstName, String lastName, String dateOfBirth) {
        String userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", "");
        if (userId.isEmpty()) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = "http://10.0.2.2:8080/api/users/" + userId;

        Map<String, String> params = new HashMap<>();
        params.put("firstName", firstName);
        params.put("lastName", lastName);
        //  params.put("userName", usernameEditText.getText().toString().trim()); //do not allow to update
        //  params.put("email", emailEditText.getText().toString().trim());       //username and email
        params.put("dob", dateOfBirth);

        JSONObject jsonBody = new JSONObject(params);

        StringRequest request = new StringRequest(Request.Method.PUT, url, response -> {
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            toggleEditing(false);
            saveButton.setVisibility(View.GONE);
            editButton.setVisibility(View.VISIBLE);
        }, error -> Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show()) {
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

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void setupDatePicker() {
        dateOfBirthEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            // If the EditText already has a date, parse it
            String currentDate = dateOfBirthEditText.getText().toString().trim();
            if (!currentDate.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    calendar.setTime(sdf.parse(currentDate));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileActivity.this, R.style.CustomDatePickerTheme, // Your custom theme
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        dateOfBirthEditText.setText(formattedDate);
                    }, year, month, day);


            // Optional: Set maximum and minimum dates
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()); // Set today as the max date
            datePickerDialog.show();
        });
    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_profile;
    }

}
