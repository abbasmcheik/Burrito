package com.example.lebrecruiter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmailOrUsername;
    private EditText editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmailOrUsername = findViewById(R.id.editTextEmailOrUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonSubmitLogin = findViewById(R.id.buttonSubmitLogin);

        buttonSubmitLogin.setOnClickListener(view -> {
            String emailOrUsername = editTextEmailOrUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (emailOrUsername.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                authenticateUser(emailOrUsername, password);
            }
        });
    }

    private void authenticateUser(String emailOrUsername, String password) {
        // Here you can send a network request to authenticate the user
        // For now, we simulate a successful login
        if (emailOrUsername.equals("test") && password.equals("1234")) {
            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
            // Navigate to another activity if required
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
        }
    }
}
