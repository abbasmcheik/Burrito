package com.example.lebrecruiter;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lebrecruiter.models.Job;
import com.example.lebrecruiter.models.JobAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JobListingsActivity extends BaseActivity { //Activity for freelancers to filter sort and apply for jobs

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
        fetchJobs(null, ""); // Initial fetch without filters, does not use specific endpoint
        fetchCategories();
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //fetchJobs(query, spinnerCategory.getSelectedItem().toString());
                if (query.isEmpty()) {
                    fetchJobs(null, null); // Load all jobs
                } else {
                    fetchJobsByGeneralSearch(query);
                }
                searchView.clearFocus(); // clear
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    fetchJobs(null, null);
                    // Load all jobs when search is cleared
                    // we can put all the logic here but it will probably overload the server ( too many calls)
                }
                return false; // Returning false allows onQueryTextSubmit to still trigger
            }
        });
    }

    private void fetchJobs(String title, String category) { // Get all jobs
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

    private void fetchCategories() {// for spinner category
        String url = "http://10.0.2.2:8080/api/jobs/categories";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray categoriesArray = new JSONArray(response);
                        List<String> categories = new ArrayList<>();
                        for (int i = 0; i < categoriesArray.length(); i++) {
                            categories.add(categoriesArray.getString(i));
                        }
                        populateCategorySpinner(categories);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Handle error
                    Toast.makeText(this, "Error fetching categories", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        );

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    private void populateCategorySpinner(List<String> categories) {
        // Add a default entry at the start of the list
        categories.add(0, "Select a category"); // Or an empty string ""

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner categorySpinner = findViewById(R.id.spinnerCategory); // Ensure this ID matches your layout
        categorySpinner.setAdapter(adapter);

//        // Optionally, handle selection events if needed
//        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position == 0) {
//                    // Default/empty category selected
//                    Toast.makeText(getApplicationContext(), "Please select a category.", Toast.LENGTH_SHORT).show();
//                } else {
//                    String selectedCategory = categories.get(position);
//                    // Handle the selected category
//                    fetchJobs(null, selectedCategory);
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Handle no selection
//            }
//        });
    }


    private void fetchJobsByGeneralSearch(String searchTerm) {
        String url = "http://10.0.2.2:8080/api/jobs/general-search?searchTerm=" + (searchTerm != null ? searchTerm : "");

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
                error -> Toast.makeText(this, "Failed to search jobs.", Toast.LENGTH_SHORT).show());

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_job_listings;
    }
}
