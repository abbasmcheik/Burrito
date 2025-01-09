package com.example.lebrecruiter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity {

    private TextView roleTextView, firstNameTextView, lastNameTextView, usernameTextView, emailTextView, dobTextView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI elements
        roleTextView = findViewById(R.id.textViewRole);
        firstNameTextView = findViewById(R.id.textViewFirstName);
        lastNameTextView = findViewById(R.id.textViewLastName);
        usernameTextView = findViewById(R.id.textViewUsername);
        emailTextView = findViewById(R.id.textViewEmail);
        dobTextView = findViewById(R.id.textViewDob);

        // Get user data from the Intent
        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");
        String role = intent.getStringExtra("role");

        // Set user role at the top
        roleTextView.setText("Role: " + role);

        // Fetch user data from the server using the userId
        fetchUserData(userId);
        //OR GET VIA SHARED PREFERENCES
        //SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        //                    String userRole = sharedPreferences.getString("role", ""); // SHARED PREFS

        // Set up Drawer Layout
        String userRole = getIntent().getStringExtra("role");
        setupDrawer(userRole);
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

    private void setupDrawer(String userRole) {
        // Find the DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configure the ActionBarDrawerToggle
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open_drawer,
                R.string.close_drawer
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Set up NavigationView and handle item clicks
        NavigationView navigationView = findViewById(R.id.navigation_view);

        if (!"Recruiter".equalsIgnoreCase(userRole)) {
            navigationView.getMenu().findItem(R.id.nav_post_job).setVisible(false);
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_profile) {
                    drawerLayout.closeDrawers();
                } else if (id == R.id.nav_post_job) {

                    if ("Recruiter".equalsIgnoreCase(userRole)) {
                        startActivity(new Intent(ProfileActivity.this, JobPostingActivity.class)
                                .putExtra("role", userRole));

                    }
                    drawerLayout.closeDrawers();

                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    drawerLayout.closeDrawers();
                } else if (id == R.id.nav_logout) {
                    Intent logoutIntent = new Intent(ProfileActivity.this, LoginActivity.class);
                    logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(logoutIntent);
                    finish();
                }
                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle toggle button behavior
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
