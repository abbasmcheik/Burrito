package com.example.lebrecruiter;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);// back button

        // Initialize Toggle Buttons and Input Fields
        Button buttonRecruiter = findViewById(R.id.buttonRecruiter);
        Button buttonFreelancer = findViewById(R.id.buttonFreelancer);
        EditText firstName = findViewById(R.id.editTextFirstName);
        EditText lastName = findViewById(R.id.editTextLastName);
        EditText userName = findViewById(R.id.editTextUserName);
        EditText email = findViewById(R.id.editTextEmail);
        EditText password = findViewById(R.id.editTextPassword);
        EditText dateOfBirth = findViewById(R.id.editTextDateOfBirth);
        Button submitButton = findViewById(R.id.buttonSubmitSignUp);
        Spinner spinnerSecurityQuestion = findViewById(R.id.spinnerSecurityQuestion);
        EditText securityAnswer = findViewById(R.id.editTextSecurityAnswer);

        // Track Selected Role
        final String[] selectedRole = {null};

        // Handle Role Selection
        buttonRecruiter.setOnClickListener(view -> {
            selectedRole[0] = "Recruiter";
            buttonRecruiter.setBackgroundTintList(getColorStateList(R.color.black));
            buttonFreelancer.setBackgroundTintList(getColorStateList(R.color.black));
        });

        buttonFreelancer.setOnClickListener(view -> {
            selectedRole[0] = "Freelancer";
            buttonFreelancer.setBackgroundTintList(getColorStateList(R.color.black));
            buttonRecruiter.setBackgroundTintList(getColorStateList(R.color.black));
        });

        // Handle Sign-Up Submission
        submitButton.setOnClickListener(view -> {
            String fName = firstName.getText().toString().trim();
            String lName = lastName.getText().toString().trim();
            String uName = userName.getText().toString().trim();
            String userEmail = email.getText().toString().trim();
            String userPassword = password.getText().toString().trim();
            String dob = dateOfBirth.getText().toString().trim();
            String selectedQuestion = spinnerSecurityQuestion.getSelectedItem().toString();
            String secAnswer = securityAnswer.getText().toString().trim();

            // Validate Inputs
            if (selectedRole[0] == null) {
                Toast.makeText(this, "Please select a role (Recruiter or Freelancer)", Toast.LENGTH_SHORT).show();
                return;
            }

            if (fName.isEmpty() || lName.isEmpty() || uName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty() || dob.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedQuestion.isEmpty() || secAnswer.isEmpty()) {
                Toast.makeText(this, "Security question and answer cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject userData = new JSONObject();
            try {
                userData.put("firstName", fName);
                userData.put("lastName", lName);
                userData.put("userName", uName);
                userData.put("email", userEmail);
                userData.put("password", userPassword);
                userData.put("dob", dob);
                userData.put("role", selectedRole[0]);
                userData.put("securityQuestion", selectedQuestion);
                userData.put("securityAnswer", secAnswer);

                // Call API to register user (you can implement it in ApiHandler)
                // ApiHandler.getInstance(context).createUser(userData, callback);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Account created successfully as " + selectedRole[0] + "!", Toast.LENGTH_SHORT).show();

            // Finish the activity or navigate elsewhere
            finish();
        });
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the "Up" button press
            onBackPressed(); // Go back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
