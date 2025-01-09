package com.example.lebrecruiter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmailOrUsername;
    private EditText editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmailOrUsername = findViewById(R.id.editTextEmailOrUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button loginButton = findViewById(R.id.buttonSubmitLogin);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameOrEmail = editTextEmailOrUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (!usernameOrEmail.isEmpty() && !password.isEmpty()) {
                    ApiHandler.getInstance(getApplicationContext()).login(usernameOrEmail, password, new ApiHandler.LoginCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            try {
                                // Extract user details from the response
                                String message = response.optString("message", "No message provided");
                                String role = response.optString("role", "Unknown role");
                                String userId = response.optString("userId", "Unknown user ID");

                                // Show a success message
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

                                //SHARED PREFERENCES get
                                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("role", role);
                                editor.putInt("userId", Integer.parseInt(userId));
                                editor.apply();

                                // Redirect to ProfileActivity and pass user data
                                Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                                intent.putExtra("userId", userId);
                                intent.putExtra("role", role);
                                startActivity(intent);

                                // Optional: Finish LoginActivity so the user can't go back to it
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(LoginActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(VolleyError error) {
                            Toast.makeText(LoginActivity.this, "Login failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
