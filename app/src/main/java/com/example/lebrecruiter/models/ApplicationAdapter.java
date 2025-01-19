package com.example.lebrecruiter.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lebrecruiter.R;

import java.util.List;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private final List<Application> applications;

    public ApplicationAdapter(List<Application> applications) {
        this.applications = applications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single application item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Application application = applications.get(position);

        // Display job details
        holder.jobTitle.setText(application.getTitle());
        holder.jobDescription.setText(application.getDescription());
        holder.jobSkills.setText("Skills: " + application.getSkills());
        holder.jobCategory.setText("Category: " + application.getCategory());
        holder.jobPayout.setText("Payout: $" + application.getPayout());
        holder.jobStatus.setText("Job Status: " + application.getJobStatus());

        // Display application-specific details
        holder.applicationStatus.setText("Application Status: " + application.getStatus());
        holder.appliedAt.setText("Applied At: " + application.getAppliedAt());

        // Change application status text color based on the status value
        String status = application.getStatus();
        if ("Rejected".equalsIgnoreCase(status)) {
            holder.applicationStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        } else if ("Pending".equalsIgnoreCase(status)) {
            holder.applicationStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_light));
        } else { // Default to green for "Accepted" or other statuses
            holder.applicationStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
        }
    }


    @Override
    public int getItemCount() {
        return applications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitle, jobDescription, jobSkills, jobCategory, jobPayout, jobStatus, applicationStatus, appliedAt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            jobTitle = itemView.findViewById(R.id.textViewJobTitle);
            jobDescription = itemView.findViewById(R.id.textViewJobDescription);
            jobSkills = itemView.findViewById(R.id.textViewJobSkills);
            jobCategory = itemView.findViewById(R.id.textViewJobCategory);
            jobPayout = itemView.findViewById(R.id.textViewJobPayout);
            jobStatus = itemView.findViewById(R.id.textViewJobStatus);
            applicationStatus = itemView.findViewById(R.id.textViewApplicationStatus);
            appliedAt = itemView.findViewById(R.id.textViewAppliedAt);
        }
    }

}
