package com.example.feed_the_hunger;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.feed_the_hunger.User.User_Login;
import com.example.feed_the_hunger.Volenteer.Volenteer_Login;
// import com.example.feed_the_hunger.Admin.Admin_Login; // Commented since not used now

public class activity_choose extends AppCompatActivity {

    Button userButton, volunteerButton, adminButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        // Initialize buttons
        userButton = findViewById(R.id.userButton);
        volunteerButton = findViewById(R.id.volunteerButton);
        adminButton = findViewById(R.id.adminButton);

        // Redirect to User Login
        userButton.setOnClickListener(v -> {
            Toast.makeText(this, "User Selected", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(activity_choose.this, User_Login.class));
        });

        // Redirect to Volunteer Login
        volunteerButton.setOnClickListener(v -> {
            Toast.makeText(this, "Volunteer Selected", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(activity_choose.this, Volenteer_Login.class));
        });

        // Admin button commented out for now

        adminButton.setOnClickListener(v -> {
            // Toast.makeText(this, "Admin Selected", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(activity_choose.this, Admin_Login.class));
        });

    }
}
