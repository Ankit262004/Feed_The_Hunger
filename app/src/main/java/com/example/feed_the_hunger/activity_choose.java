package com.example.feed_the_hunger;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.feed_the_hunger.User.User_Login;
import com.example.feed_the_hunger.Volenteer.Volenteer_Login;
import com.example.feed_the_hunger.admin_login;

public class activity_choose extends AppCompatActivity {

    Button userButton, volunteerButton, adminButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        userButton = findViewById(R.id.userButton);
        volunteerButton = findViewById(R.id.volunteerButton);
        adminButton = findViewById(R.id.adminButton);

        userButton.setOnClickListener(v -> {
            Toast.makeText(this, "User Selected", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(activity_choose.this, User_Login.class));
        });

        volunteerButton.setOnClickListener(v -> {
            Toast.makeText(this, "Volunteer Selected", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(activity_choose.this, Volenteer_Login.class));
        });

        adminButton.setOnClickListener(v -> {
            Toast.makeText(this, "Admin Selected", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(activity_choose.this, admin_login.class));
        });
    }
}