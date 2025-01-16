package com.example.lebrecruiter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
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
    private List<Job> allJobsList = new ArrayList<>();
    //
    private Button btnAdvancedSearch;
    private LinearLayout advancedSearchSection;
    private Spinner spinnerSortByPayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_job_listings); //Set in BaseActivity

        jobsGridView = findViewById(R.id.jobsGridView);
        searchView = findViewById(R.id.searchView);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        // Advanced search views
        btnAdvancedSearch = findViewById(R.id.btnAdvancedSearch);
        advancedSearchSection = findViewById(R.id.advancedSearchSection);
        spinnerSortByPayout = findViewById(R.id.spinnerSortByPayout);

        jobsList = new ArrayList<>();
        jobAdapter = new JobAdapter(this, jobsList, R.layout.job_listing_item);
        jobsGridView.setAdapter(jobAdapter);

        setupSearch();
        fetchJobs(null, ""); // Initial fetch without filters, does not use specific endpoint
        fetchCategories();

        setupAdvancedSearch();

        jobsGridView.setOnItemClickListener((parent, view, position, id) -> {
            Job selectedJob = (Job) jobAdapter.getItem(position);
            Intent intent = new Intent(JobListingsActivity.this, JobApplicationActivity.class);
            intent.putExtra("job", selectedJob);
            startActivity(intent);
        });

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
        String url = "http://10.0.2.2:8080/api/jobs?title=" + (title != null ? title : "") + "&category=" + (category != null ? category : "") + "&status=Open";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
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
                    String jobCategory = jobJson.getString("category");

                    Job job = new Job(jobId, jobTitle, jobDescription, status, skillsRequired, payout, jobCategory);
                    jobsList.add(job); // Add to currently displayed jobs
                    allJobsList.add(job); // Add to full dataset for filtering
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            jobAdapter.notifyDataSetChanged();
        }, error -> Toast.makeText(this, "Failed to load jobs.", Toast.LENGTH_SHORT).show());

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void fetchCategories() {// for spinner category
        String url = "http://10.0.2.2:8080/api/jobs/categories";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
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
        }, error -> {
            // Handle error
            Toast.makeText(this, "Error fetching categories", Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        });

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

        // Setup the spinner listener
        setupCategorySpinner();
    }

    private void setupCategorySpinner() {
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = spinnerCategory.getSelectedItem().toString();
                if (!selectedCategory.equals("Select a category")) {
                    // Filter the current jobsList based on the selected category
                    filterJobsByCategory(selectedCategory);
                } else {
                    // Show all jobs if "Select a category" is chosen
                    jobAdapter.updateJobs(new ArrayList<>(allJobsList)); // Reset to full dataset
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
    }

    private void filterJobsByCategory(String category) {
        List<Job> filteredJobs = new ArrayList<>();
        for (Job job : allJobsList) { // Filter using the full dataset
            if (category.equals("Select a category") || job.getCategory().equals(category)) {
                filteredJobs.add(job);
            }
        }

        // Update the adapter with filtered jobs
        jobAdapter.updateJobs(filteredJobs);
    }


    private void fetchJobsByGeneralSearch(String searchTerm) {
        String url = "http://10.0.2.2:8080/api/jobs/general-search?searchTerm=" + (searchTerm != null ? searchTerm : "");

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
            allJobsList.clear();
            jobsList.clear();
            String selectedCategory = spinnerCategory.getSelectedItem().toString();

            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject jobJson = response.getJSONObject(i);
                    int jobId = jobJson.getInt("jobId");
                    String jobTitle = jobJson.getString("title");
                    String jobDescription = jobJson.getString("description");
                    String status = jobJson.getString("status");
                    String skillsRequired = jobJson.getString("skillsRequired");
                    String payout = jobJson.getString("payout");
                    String category = jobJson.getString("category");

                    Job job = new Job(jobId, jobTitle, jobDescription, status, skillsRequired, payout, category);
                    allJobsList.add(job);

                    // Add to jobsList if category matches or no category selected
                    if (selectedCategory.equals("Select a category") || selectedCategory.equals(category)) {
                        jobsList.add(job);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            jobAdapter.notifyDataSetChanged();
        }, error -> Toast.makeText(this, "Failed to search jobs.", Toast.LENGTH_SHORT).show());

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void setupAdvancedSearch() {
        // Toggle the visibility of the advanced search section
        btnAdvancedSearch.setOnClickListener(v -> {
            if (advancedSearchSection.getVisibility() == View.GONE) {
                advancedSearchSection.setVisibility(View.VISIBLE);
            } else {
                advancedSearchSection.setVisibility(View.GONE);
            }
        });

        // Populate the spinner for sorting options
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Ascending", "Descending"});
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortByPayout.setAdapter(sortAdapter);

        // Handle sorting logic
        spinnerSortByPayout.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sortOrder = spinnerSortByPayout.getSelectedItem().toString();
                sortJobsByPayout(sortOrder.equals("Ascending"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
    }

    private void sortJobsByPayout(boolean ascending) {
        jobsList.sort((job1, job2) -> {
            try {
                double payout1 = Double.parseDouble(job1.getPayout().replace("$", "").trim());
                double payout2 = Double.parseDouble(job2.getPayout().replace("$", "").trim());
                return ascending ? Double.compare(payout1, payout2) : Double.compare(payout2, payout1);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return 0; // Treat invalid payout as equal
            }
        });
        jobAdapter.notifyDataSetChanged();
    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_job_listings;
    }
}
