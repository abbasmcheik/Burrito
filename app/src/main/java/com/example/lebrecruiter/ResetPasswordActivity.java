package com.example.lebrecruiter;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class ResetPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        EditText username = findViewById(R.id.editTextUsername);
        Spinner spinnerSecurityQuestion = findViewById(R.id.spinnerSecurityQuestion);
        EditText securityAnswer = findViewById(R.id.editTextSecurityAnswer);
        EditText newPassword = findViewById(R.id.editTextNewPassword);
        Button resetButton = findViewById(R.id.buttonResetPassword);

        resetButton.setOnClickListener(v -> {
            String user = username.getText().toString().trim();
            String selectedQuestion = spinnerSecurityQuestion.getSelectedItem().toString();
            String answer = securityAnswer.getText().toString().trim();
            String password = newPassword.getText().toString().trim();

            if (user.isEmpty() || answer.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject requestBody = new JSONObject();
            try {
                requestBody.put("username", user);
                requestBody.put("securityAnswer", answer);
                requestBody.put("newPassword", password);
                requestBody.put("securityQuestion", selectedQuestion);

                // Call API to reset password
                ApiHandler.getInstance(this).resetPassword(requestBody, new ApiHandler.ResetPasswordCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(ResetPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ResetPasswordActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }
}
