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

    private final Context context;
    private final ArrayList<Job> jobs;
    private final int layoutResourceId;

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
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(layoutResourceId, parent, false);
            holder = new ViewHolder(convertView);

            // Add consistent margins
            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) layoutParams).setMargins(16, 16, 16, 25); // Adjust as needed
            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Reset dynamic properties
        convertView.setVisibility(View.VISIBLE); // Ensure visibility is not overridden
        convertView.setAlpha(1.0f); // Reset transparency if animations are used

        // Set data
        Job job = jobs.get(position);
        holder.titleTextView.setText(job.getTitle());
        holder.descriptionTextView.setText(job.getDescription());
        holder.statusTextView.setText(job.getStatus());

        // Dynamically set the status color
        switch (job.getStatus()) {
            case "Open":
                holder.statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "Closed":
                holder.statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                break;
            default:
                holder.statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_orange_light));
                break;
        }

        if (layoutResourceId == R.layout.job_listing_item) {
            holder.skillsTextView.setText("Skills: " + job.getSkillsRequired());
            holder.payoutTextView.setText("Payout: $" + job.getPayout());
            holder.categoryTextView.setText("Category: " + job.getCategory());
        }

        return convertView;
    }


    public void updateJobs(List<Job> newJobs) {
        this.jobs.clear(); // Clear the existing jobs
        this.jobs.addAll(newJobs); // Add the new jobs
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    // ViewHolder class to cache views
    private static class ViewHolder {
        final TextView titleTextView;
        final TextView descriptionTextView;
        final TextView statusTextView;
        final TextView skillsTextView;
        final TextView payoutTextView;
        final TextView categoryTextView;

        ViewHolder(View view) {
            titleTextView = view.findViewById(R.id.textViewJobTitle);
            descriptionTextView = view.findViewById(R.id.textViewJobDescription);
            statusTextView = view.findViewById(R.id.textViewJobStatus);
            skillsTextView = view.findViewById(R.id.textViewJobSkills);
            payoutTextView = view.findViewById(R.id.textViewJobPayout);
            categoryTextView = view.findViewById(R.id.textViewJobCategory);
        }
    }
}
