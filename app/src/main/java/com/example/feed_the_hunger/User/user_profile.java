package com.example.feed_the_hunger.User;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.feed_the_hunger.R;

public class user_profile extends AppCompatActivity {

    private TextView tvName, tvEmail, tvLocation, tvIdentity, tvTotalOrders, tvUserId, tvAccountStatus;
    private ProgressBar progressBar;
    private static final String TAG = "user_profile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_user_profile);

            if (findViewById(R.id.main) != null) {
                ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
            }

            tvName = findViewById(R.id.tvName);
            tvEmail = findViewById(R.id.tvEmail);
            tvLocation = findViewById(R.id.tvLocation);
            tvIdentity = findViewById(R.id.tvIdentity);
            tvTotalOrders = findViewById(R.id.tvTotalOrders);
            tvUserId = findViewById(R.id.tvUserId);
            tvAccountStatus = findViewById(R.id.tvAccountStatus);
            progressBar = findViewById(R.id.progressBar);

            if (tvName == null || tvEmail == null || tvLocation == null || tvIdentity == null
                    || tvTotalOrders == null || tvUserId == null || tvAccountStatus == null || progressBar == null) {
                Toast.makeText(this, "Profile XML mismatch", Toast.LENGTH_LONG).show();
                Log.e(TAG, "One or more views are null");
                finish();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);

            String userId = prefs.getString("userId", "");
            String fullName = prefs.getString("fullName", "");
            String email = prefs.getString("email", "");
            String location = prefs.getString("location", "");
            String userType = prefs.getString("userType", "");
            boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

            Log.d(TAG, "Profile opened successfully");
            Log.d(TAG, "isLoggedIn = " + isLoggedIn);
            Log.d(TAG, "userId = " + userId);

            tvName.setText(fullName.isEmpty() ? "---" : fullName);
            tvEmail.setText(email.isEmpty() ? "---" : email);
            tvLocation.setText(location.isEmpty() ? "---" : location);
            tvIdentity.setText(userType.isEmpty() ? "---" : userType);
            tvUserId.setText(userId.isEmpty() ? "---" : userId);
            tvTotalOrders.setText("0");
            tvAccountStatus.setText(isLoggedIn ? "Active" : "Not Logged In");

            progressBar.setVisibility(android.view.View.GONE);

            Toast.makeText(this, "Profile screen opened", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "CRASH IN PROFILE", e);
            Toast.makeText(this, "Profile crash: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
}