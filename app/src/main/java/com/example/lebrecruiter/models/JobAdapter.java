package com.example.lebrecruiter.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.lebrecruiter.R;

import java.util.ArrayList;

public class JobAdapter extends android.widget.BaseAdapter { // to display jobs in gridview

    private Context context;
    private ArrayList<Job> jobs;

    public JobAdapter(Context context, ArrayList<Job> jobs) {
        this.context = context;
        this.jobs = jobs;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.job_item, parent, false);
        }

        TextView titleTextView = convertView.findViewById(R.id.textViewJobTitle);
        TextView descriptionTextView = convertView.findViewById(R.id.textViewJobDescription);
        TextView statusTextView = convertView.findViewById(R.id.textViewJobStatus);

        Job job = jobs.get(position);
        titleTextView.setText(job.getTitle());
        descriptionTextView.setText(job.getDescription());
        statusTextView.setText(job.getStatus());

        return convertView;
    }
}
