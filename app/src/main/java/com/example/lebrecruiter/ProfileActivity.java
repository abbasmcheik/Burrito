package com.example.lebrecruiter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONObject;

public class ProfileActivity extends BaseActivity {

    private TextView roleTextView, firstNameTextView, lastNameTextView, usernameTextView, emailTextView, dobTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize UI elements
        roleTextView = findViewById(R.id.textViewRole);
        firstNameTextView = findViewById(R.id.textViewFirstName);
        lastNameTextView = findViewById(R.id.textViewLastName);
        usernameTextView = findViewById(R.id.textViewUsername);
        emailTextView = findViewById(R.id.textViewEmail);
        dobTextView = findViewById(R.id.textViewDob);


        //OR GET VIA SHARED PREFERENCES
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "-1"); // SHARED PREFS
        String role = sharedPreferences.getString("role", ""); // SHARED PREFS

        // Set user role at the top
        roleTextView.setText("Role: " + role);

        // Fetch user data from the server using the userId
        fetchUserData(userId);

    }

    private void fetchUserData(String userId) {
        ApiHandler.getInstance(getApplicationContext()).getUserDetails(userId, new ApiHandler.UserDetailsCallback() {
            @Override
            public void onSuccess(JSONObject user) {
                // Populate the user info in the UI
                firstNameTextView.setText("First Name: " + user.optString("firstName", ""));
                lastNameTextView.setText("Last Name: " + user.optString("lastName", ""));
                usernameTextView.setText("Username: " + user.optString("userName", ""));
                emailTextView.setText("Email: " + user.optString("email", ""));
                dobTextView.setText("Date of Birth: " + user.optString("dob", ""));
            }

            @Override
            public void onError(Exception error) {
                // Handle errors
                dobTextView.setText("Failed to fetch user data");
            }
        });
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_profile;
    }
}
