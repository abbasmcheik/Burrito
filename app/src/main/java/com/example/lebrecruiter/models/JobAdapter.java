package com.example.lebrecruiter.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.lebrecruiter.R;

import java.util.ArrayList;
import java.util.List;

public class JobAdapter extends android.widget.BaseAdapter {

    private Context context;
    private ArrayList<Job> jobs;
    private int layoutResourceId;

    public JobAdapter(Context context, ArrayList<Job> jobs, int layoutResourceId) {
        this.context = context;
        this.jobs = jobs;
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public int getCount() {
        return jobs.size();
    }

    @Override
    public Object getItem(int position) {
        return jobs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(layoutResourceId, parent, false);
        }

        TextView titleTextView = convertView.findViewById(R.id.textViewJobTitle);
        TextView descriptionTextView = convertView.findViewById(R.id.textViewJobDescription);
        TextView statusTextView = convertView.findViewById(R.id.textViewJobStatus);

        Job job = jobs.get(position);

        titleTextView.setText(job.getTitle());
        descriptionTextView.setText(job.getDescription());
        statusTextView.setText(job.getStatus());

        if (layoutResourceId == R.layout.job_listing_item) {
            // Additional fields for job listings
            TextView skillsTextView = convertView.findViewById(R.id.textViewJobSkills);
            TextView payoutTextView = convertView.findViewById(R.id.textViewJobPayout);
            TextView categoryTextView = convertView.findViewById(R.id.textViewJobCategory); // Add a field for category

            skillsTextView.setText("Skills: " + job.getSkillsRequired());
            payoutTextView.setText("Payout: $" + job.getPayout());
            categoryTextView.setText("Category: " + job.getCategory()); // Display category
        }

        return convertView;
    }

    public void updateJobs(List<Job> newJobs) {
        this.jobs.clear();  // Clear the existing jobs
        this.jobs.addAll(newJobs);  // Add the new jobs
        notifyDataSetChanged();  // Notify the adapter that the data has changed
    }

}
