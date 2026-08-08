package com.example.feed_the_hunger;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class admin_dashboard extends AppCompatActivity {

    TextView welcomeText;
    CardView cardManageVolunteer, cardViewReports, cardCreateAdmin, cardManageUsers, cardAdminLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        // Bind views
        welcomeText = findViewById(R.id.adminWelcomeText);
        cardManageVolunteer = findViewById(R.id.cardManageVolunteer);
        cardViewReports = findViewById(R.id.cardViewReports);
        cardCreateAdmin = findViewById(R.id.cardCreateAdmin);
        cardManageUsers = findViewById(R.id.cardManageUsers);
        cardAdminLogout = findViewById(R.id.cardAdminLogout);

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminMain), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // Welcome text
        welcomeText.setText("Welcome Admin");

        // Open volunteer list
        cardManageVolunteer.setOnClickListener(v -> {
            Intent intent = new Intent(admin_dashboard.this, admin_vol_main.class);
            startActivity(intent);
        });

        // Open food reports / food list
        cardViewReports.setOnClickListener(v -> {
            Intent intent = new Intent(admin_dashboard.this, admin_food_main.class);
            startActivity(intent);
        });

        // Open create admin screen
        cardCreateAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(admin_dashboard.this, admin_registration.class);
            startActivity(intent);
        });

        // Open user list
        cardManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(admin_dashboard.this, admin_user_main.class);
            startActivity(intent);
        });

        // Logout
        cardAdminLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(admin_dashboard.this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(admin_dashboard.this, admin_login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}