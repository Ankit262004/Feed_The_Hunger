package com.example.feed_the_hunger.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.feed_the_hunger.R;
import com.example.feed_the_hunger.Volenteer.activity_food;
import com.example.feed_the_hunger.food_main_;

public class activity_user_dashboard extends AppCompatActivity {

    private CardView cardProfile, cardFoods, cardOrders, cardLogout;
    private static final String TAG = "user_dashboard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cardProfile = findViewById(R.id.cardProfile);
        cardFoods = findViewById(R.id.cardFoods);
        cardOrders = findViewById(R.id.cardOrders);
        cardLogout = findViewById(R.id.cardLogout);

        setWelcomeName();

        cardProfile.setOnClickListener(v -> {
            animateCard(cardProfile);

            Log.d(TAG, "Profile clicked");
            try {
                Intent intent = new Intent(activity_user_dashboard.this, user_profile.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error opening profile", e);
                Toast.makeText(activity_user_dashboard.this,
                        "Profile screen error: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        cardFoods.setOnClickListener(v -> {
            animateCard(cardFoods);

            Intent intent = new Intent(activity_user_dashboard.this, activity_food.class);
            startActivity(intent);
        });

        cardOrders.setOnClickListener(v -> {
            animateCard(cardOrders);

            Intent intent = new Intent(activity_user_dashboard.this, food_main_.class);
            startActivity(intent);
        });

        cardLogout.setOnClickListener(v -> {
            animateCard(cardLogout);
            showLogoutDialog();
        });
    }

    private void setWelcomeName() {
        android.widget.TextView welcomeText = findViewById(R.id.welcomeText);

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String fullName = prefs.getString("fullName", "");

        if (fullName != null && !fullName.trim().isEmpty()) {
            welcomeText.setText("Welcome, " + fullName);
        } else {
            welcomeText.setText("Welcome User");
        }
    }

    private void animateCard(CardView cardView) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 0.94f,
                1.0f, 0.94f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );

        scaleAnimation.setDuration(90);
        scaleAnimation.setRepeatCount(1);
        scaleAnimation.setRepeatMode(Animation.REVERSE);

        cardView.startAnimation(scaleAnimation);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    Toast.makeText(this, "Logout Successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(activity_user_dashboard.this, User_Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}