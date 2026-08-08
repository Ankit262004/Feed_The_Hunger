package com.example.feed_the_hunger.Volenteer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.feed_the_hunger.R;
import com.example.feed_the_hunger.vol_req;

public class activity_volenteer_dashboard extends AppCompatActivity {

    CardView cardProfile, cardRequests, cardDeliveries, cardLogout;
    TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volenteer_dashboard);

        cardProfile = findViewById(R.id.cardProfile);
        cardRequests = findViewById(R.id.cardRequests);
        cardDeliveries = findViewById(R.id.cardDeliveries);
        cardLogout = findViewById(R.id.cardLogout);
        welcomeText = findViewById(R.id.welcomeText);

        SharedPreferences prefs = getSharedPreferences("VolunteerSession", MODE_PRIVATE);
        String fullName = prefs.getString("fullName", "");

        if (fullName != null && !fullName.trim().isEmpty()) {
            welcomeText.setText("Welcome, " + fullName);
        } else {
            welcomeText.setText("Welcome Volunteer");
        }

        cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(activity_volenteer_dashboard.this, volenteer_profile.class);
            startActivity(intent);
        });

        cardRequests.setOnClickListener(v -> {
            Intent intent = new Intent(activity_volenteer_dashboard.this, vol_req.class);
            startActivity(intent);
        });

        cardDeliveries.setOnClickListener(v -> {
            Intent intent = new Intent(activity_volenteer_dashboard.this, volunteer_deliveries_main.class);
            startActivity(intent);
        });

        cardLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(activity_volenteer_dashboard.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        SharedPreferences sessionPrefs = getSharedPreferences("VolunteerSession", MODE_PRIVATE);
                        sessionPrefs.edit().clear().apply();

                        Toast.makeText(activity_volenteer_dashboard.this, "Logout Successful", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(activity_volenteer_dashboard.this, Volenteer_Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }
}