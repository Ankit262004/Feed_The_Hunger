package com.example.feed_the_hunger.Volenteer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.feed_the_hunger.MyIP;
import com.example.feed_the_hunger.R;

import org.json.JSONObject;

public class volenteer_profile extends AppCompatActivity {

    private TextView tvName, tvEmail, tvLocation, tvIdentity, tvUserId, tvAccountStatus;
    private TextView tvTotalAccepted, tvTotalRejected, tvTotalHandled;
    private ProgressBar progressBar;

    private static final String TAG = "volenteer_profile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volenteer_profile);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        if (!initViews()) {
            finish();
            return;
        }

        loadProfile();
    }

    private boolean initViews() {
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvLocation = findViewById(R.id.tvLocation);
        tvIdentity = findViewById(R.id.tvIdentity);
        tvUserId = findViewById(R.id.tvUserId);
        tvAccountStatus = findViewById(R.id.tvAccountStatus);

        tvTotalAccepted = findViewById(R.id.tvTotalAccepted);
        tvTotalRejected = findViewById(R.id.tvTotalRejected);
        tvTotalHandled = findViewById(R.id.tvTotalHandled);

        progressBar = findViewById(R.id.progressBar);

        if (tvName == null || tvEmail == null || tvLocation == null || tvIdentity == null
                || tvUserId == null || tvAccountStatus == null
                || tvTotalAccepted == null || tvTotalRejected == null || tvTotalHandled == null
                || progressBar == null) {

            Toast.makeText(this, "Volunteer Profile XML ID mismatch", Toast.LENGTH_LONG).show();
            Log.e(TAG, "One or more volunteer profile views are null");
            return false;
        }

        return true;
    }

    private void loadProfile() {
        SharedPreferences prefs = getSharedPreferences("VolunteerSession", MODE_PRIVATE);

        String userId = prefs.getString("userId", "");
        String fullName = prefs.getString("fullName", "");
        String email = prefs.getString("email", "");
        String location = prefs.getString("location", "");
        String userType = prefs.getString("userType", "");
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        Log.d(TAG, "isLoggedIn=" + isLoggedIn + ", userId=" + userId);

        if (!isLoggedIn || userId.isEmpty()) {
            tvName.setText(fullName.isEmpty() ? "---" : fullName);
            tvEmail.setText(email.isEmpty() ? "---" : email);
            tvLocation.setText(location.isEmpty() ? "---" : location);
            tvIdentity.setText(userType.isEmpty() ? "volunteer" : userType);
            tvUserId.setText("---");

            tvTotalAccepted.setText("0");
            tvTotalRejected.setText("0");
            tvTotalHandled.setText("0");

            tvAccountStatus.setText("Not Logged In");

            Toast.makeText(this, "Volunteer not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        tvName.setText(fullName.isEmpty() ? "---" : fullName);
        tvEmail.setText(email.isEmpty() ? "---" : email);
        tvLocation.setText(location.isEmpty() ? "---" : location);
        tvIdentity.setText(userType.isEmpty() ? "volunteer" : userType);
        tvUserId.setText(userId);

        tvTotalAccepted.setText("0");
        tvTotalRejected.setText("0");
        tvTotalHandled.setText("0");

        tvAccountStatus.setText("Loading...");
        progressBar.setVisibility(View.VISIBLE);

        String url = MyIP.IP_ADDRESS + "user/profile/" + userId;
        Log.d(TAG, "VOLUNTEER PROFILE URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "VOLUNTEER PROFILE RESPONSE: " + response.toString());

                    try {
                        boolean success = response.optBoolean("success", false);

                        if (!success) {
                            tvAccountStatus.setText("Load Failed");
                            Toast.makeText(
                                    volenteer_profile.this,
                                    response.optString("message", "Failed to load profile"),
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        JSONObject user = response.getJSONObject("user");

                        String serverName = user.optString("fullName", fullName);
                        String serverEmail = user.optString("email", email);
                        String serverLocation = user.optString("location", location);
                        String serverType = user.optString("userType", userType);
                        String serverUserId = user.optString("_id", userId);

                        int totalAccepted = response.optInt("totalAccepted", 0);
                        int totalRejected = response.optInt("totalRejected", 0);
                        int totalHandled = response.optInt("totalHandled", 0);

                        tvName.setText(serverName.isEmpty() ? "---" : serverName);
                        tvEmail.setText(serverEmail.isEmpty() ? "---" : serverEmail);
                        tvLocation.setText(serverLocation.isEmpty() ? "---" : serverLocation);
                        tvIdentity.setText(serverType.isEmpty() ? "volunteer" : serverType);
                        tvUserId.setText(serverUserId.isEmpty() ? "---" : serverUserId);

                        tvTotalAccepted.setText(String.valueOf(totalAccepted));
                        tvTotalRejected.setText(String.valueOf(totalRejected));
                        tvTotalHandled.setText(String.valueOf(totalHandled));

                        tvAccountStatus.setText("Active");

                    } catch (Exception e) {
                        Log.e(TAG, "Volunteer profile parse error", e);
                        tvAccountStatus.setText("Parse Error");
                        Toast.makeText(
                                volenteer_profile.this,
                                "Parse error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);

                    String message = "Error loading profile";
                    tvAccountStatus.setText("Error");

                    try {
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String responseData = new String(error.networkResponse.data);
                            Log.e(TAG, "VOLUNTEER PROFILE ERROR RESPONSE: " + responseData);

                            try {
                                JSONObject errorObject = new JSONObject(responseData);
                                message = errorObject.optString("message", message);
                            } catch (Exception e) {
                                message = "Code: " + error.networkResponse.statusCode + " | " + responseData;
                            }
                        } else if (error.networkResponse != null) {
                            message = "Code: " + error.networkResponse.statusCode;
                        } else if (error.getMessage() != null) {
                            message = error.getMessage();
                        } else {
                            message = "Unknown profile error";
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Volunteer volley parse error", e);
                    }

                    Toast.makeText(volenteer_profile.this, message, Toast.LENGTH_LONG).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}