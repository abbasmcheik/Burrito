package com.example.lebrecruiter;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class ApiHandler {

    private static RequestQueue requestQueue;

    public static void fetchJobs(Context context, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }

        String url = "http://10.0.2.2:8080/api/jobs"; // Update the endpoint to match your server API

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, listener, errorListener);
        requestQueue.add(stringRequest);
    }
}
