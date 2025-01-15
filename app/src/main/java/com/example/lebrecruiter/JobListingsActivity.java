package com.example.lebrecruiter;

import android.os.Bundle;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.lebrecruiter.models.Job;
import com.example.lebrecruiter.models.JobAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JobListingsActivity extends BaseActivity {

    private GridView jobsGridView;
    private JobAdapter jobAdapter;
    private ArrayList<Job> jobsList;
    private SearchView searchView;
    private Spinner spinnerCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_listings);

        jobsGridView = findViewById(R.id.jobsGridView);
        searchView = findViewById(R.id.searchView);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        jobsList = new ArrayList<>();
        jobAdapter = new JobAdapter(this, jobsList, R.layout.job_listing_item);
        jobsGridView.setAdapter(jobAdapter);


        setupSearch();
        fetchJobs(null, null); // Initial fetch without filters
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchJobs(query, spinnerCategory.getSelectedItem().toString());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void fetchJobs(String title, String category) {
        String url = "http://10.0.2.2:8080/api/jobs?title=" + (title != null ? title : "") +
                "&category=" + (category != null ? category : "") +
                "&status=Open";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    jobsList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jobJson = response.getJSONObject(i);
                            int jobId = jobJson.getInt("jobId");
                            String jobTitle = jobJson.getString("title");
                            String jobDescription = jobJson.getString("description");
                            String status = jobJson.getString("status");
                            String skillsRequired = jobJson.getString("skillsRequired");
                            String payout = jobJson.getString("payout");

                            jobsList.add(new Job(jobId, jobTitle, jobDescription, status, skillsRequired, payout));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    jobAdapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "Failed to load jobs.", Toast.LENGTH_SHORT).show());

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_job_listings;
    }
}
