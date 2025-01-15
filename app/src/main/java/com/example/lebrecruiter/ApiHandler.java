package com.example.lebrecruiter;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class ApiHandler {
    private static final String BASE_URL = "http://10.0.2.2:8080/api"; // Replace <YOUR_SERVER_IP> with your server's IP
    private static ApiHandler instance;
    private RequestQueue requestQueue;
    private static Context context;

    private ApiHandler(Context ctx) {
        context = ctx;
        requestQueue = getRequestQueue();
    }

    public static synchronized ApiHandler getInstance(Context ctx) {
        if (instance == null) {
            instance = new ApiHandler(ctx);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public void login(String usernameOrEmail, String password, final LoginCallback callback) {
        String url = BASE_URL + "/users/login";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("usernameOrEmail", usernameOrEmail);
            requestBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest loginRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> callback.onSuccess(response),
                error -> callback.onError(error)
        );

        getRequestQueue().add(loginRequest);
    }

    public void getUserDetails(String userId, final UserDetailsCallback callback) {
        String url = BASE_URL + "/users/" + userId;

        JsonObjectRequest userDetailsRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> callback.onSuccess(response),
                error -> callback.onError(error)
        );

        getRequestQueue().add(userDetailsRequest);
    }

    public interface UserDetailsCallback {
        void onSuccess(JSONObject user);

        void onError(Exception error);
    }

    public interface LoginCallback {
        void onSuccess(JSONObject response);

        void onError(VolleyError error);
    }

    public void createUser(JSONObject userData, final UserCreationCallback callback) {
        String url = BASE_URL + "/users";

        JsonObjectRequest createUserRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                userData,
                response -> callback.onSuccess(response),
                error -> callback.onError(new Exception(error.getMessage()))
        );

        getRequestQueue().add(createUserRequest);
    }

    public interface UserCreationCallback {
        void onSuccess(JSONObject response);

        void onError(Exception error);
    }

    public void resetPassword(JSONObject requestBody, final ResetPasswordCallback callback) {
        String url = BASE_URL + "/users/reset-password";
        // the server is returning 200 OK instead of 201 OK so it's returning a string instead of a message, handled
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    callback.onSuccess(response); // Directly pass the plain text response
                },
                error -> {
                    String errorMessage = "Failed to reset password";
                    if (error.networkResponse != null) {
                        errorMessage += ": " + new String(error.networkResponse.data);
                    }
                    callback.onError(errorMessage);
                }
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return requestBody.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        getRequestQueue().add(request);
    }


    public interface ResetPasswordCallback {
        void onSuccess(String message);

        void onError(String error);
    }

}
