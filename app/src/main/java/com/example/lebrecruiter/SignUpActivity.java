package com.example.lebrecruiter;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
        TextView dateOfBirth = findViewById(R.id.editTextDateOfBirth);
        Button submitButton = findViewById(R.id.buttonSubmitSignUp);
        Spinner spinnerSecurityQuestion = findViewById(R.id.spinnerSecurityQuestion);
        EditText securityAnswer = findViewById(R.id.editTextSecurityAnswer);

        // Track Selected Role
        final String[] selectedRole = {null};

        // Handle Role Selection
        buttonRecruiter.setOnClickListener(view -> {
            selectedRole[0] = "Recruiter";
            buttonRecruiter.setBackgroundTintList(getColorStateList(R.color.purple));
            buttonFreelancer.setBackgroundTintList(getColorStateList(R.color.black));
        });

        buttonFreelancer.setOnClickListener(view -> {
            selectedRole[0] = "Freelancer";
            buttonFreelancer.setBackgroundTintList(getColorStateList(R.color.purple));
            buttonRecruiter.setBackgroundTintList(getColorStateList(R.color.black));
        });

        dateOfBirth.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(SignUpActivity.this, (view1, year1, month1, dayOfMonth) -> {
                // Format the date as YYYY-MM-DD
                String formattedDate = String.format(Locale.US, "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                dateOfBirth.setText(formattedDate);
            }, year, month, day);

            // Optional: Set a maximum or minimum date
            calendar.set(Calendar.YEAR, year - 18); // User must be at least 18 years old
            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
            datePickerDialog.show();
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

            // Validate Email
            if (!isValidEmail(userEmail)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dob.isEmpty()) {
                Toast.makeText(this, "Please select a valid date of birth", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate age 18+
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date dobDate = sdf.parse(dob);
                Calendar dobCalendar = Calendar.getInstance();
                dobCalendar.setTime(dobDate);

                Calendar today = Calendar.getInstance();
                int age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);

                if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                    age--;
                }

                if (age < 18) {
                    Toast.makeText(this, "You must be at least 18 years old", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (ParseException e) {
                e.printStackTrace();
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

                ApiHandler.getInstance(this).createUser(userData, new ApiHandler.UserCreationCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        Toast.makeText(SignUpActivity.this, "Account created successfully as " + selectedRole[0] + "!", Toast.LENGTH_SHORT).show();
                        finish(); // Navigate back or to the login screen
                    }

                    @Override
                    public void onError(Exception error) {
                        Toast.makeText(SignUpActivity.this, "Failed to create account: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


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


    private boolean isValidEmail(String email) { // Email field format validation
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


}
