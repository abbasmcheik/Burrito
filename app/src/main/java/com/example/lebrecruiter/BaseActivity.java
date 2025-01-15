package com.example.lebrecruiter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;


public abstract class BaseActivity extends AppCompatActivity {
    protected DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private String userRole;
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        // Initialize toolbar first
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        } else {
            Log.e(TAG, "Toolbar not found in layout");
            return;
        }

        initializeViews();
        setupDrawer();
        setupNavigationView();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        if (drawerLayout == null || navigationView == null) {
            throw new IllegalStateException("Layout must include drawer_layout and navigation_view");
        }
    }

    private void setupDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);  // Get toolbar reference here
        if (toolbar == null) {
            Log.e(TAG, "Toolbar not found in layout");
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open_drawer,
                R.string.close_drawer
        );

        drawerLayout.addDrawerListener(drawerToggle);  // Simplified listener
        drawerToggle.syncState();
    }

    private void setupNavigationView() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userRole = sharedPreferences.getString("role", "");

        Menu menu = navigationView.getMenu();
        updateMenuVisibility(menu);

        navigationView.setNavigationItemSelectedListener(item -> handleNavigation(item));
    }

    private void updateMenuVisibility(Menu menu) {// update drawer visibility according to userRole
        MenuItem postJobItem = menu.findItem(R.id.nav_post_job);
        MenuItem myJobsItem = menu.findItem(R.id.nav_my_jobs);
        MenuItem jobListings = menu.findItem(R.id.nav_job_listings);

        if (postJobItem != null) {
            // Show Post Job menu item only for recruiters
            postJobItem.setVisible("Recruiter".equalsIgnoreCase(userRole));
        }

        if (myJobsItem != null) {
            // Show My Jobs menu item only for recruiters
            myJobsItem.setVisible("Recruiter".equalsIgnoreCase(userRole));
        }

        if (jobListings != null) {
            // Show My Jobs menu item only for freelancers
            jobListings.setVisible("Freelancer".equalsIgnoreCase(userRole));
        }

        // Add more role-based visibility logic here if needed
    }


    protected boolean handleNavigation(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.nav_profile) {
            intent = new Intent(this, ProfileActivity.class);
        } else if (id == R.id.nav_post_job && "Recruiter".equalsIgnoreCase(userRole)) {
            intent = new Intent(this, JobPostingActivity.class);
        } else if (id == R.id.nav_my_jobs) {
            intent = new Intent(this, MyJobsActivity.class);
        } else if (id == R.id.nav_job_listings) {
            intent = new Intent(this, JobListingsActivity.class);
        } else if (id == R.id.nav_logout) {
            performLogout();
            return true;
        }

        if (intent != null) {
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void performLogout() {
        // Clear preferences
        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        Intent logoutIntent = new Intent(this, LoginActivity.class);
        logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(logoutIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    // Child classes must implement this to provide their layout
    protected abstract int getLayoutResourceId();
}