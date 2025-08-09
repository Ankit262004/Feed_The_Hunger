package com.example.feed_the_hunger.Volenteer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.feed_the_hunger.R;

import java.util.HashMap;
import java.util.Map;

public class Volenteer_Login extends AppCompatActivity {

    EditText em, pass;
    Button loginButton;
    TextView registerRedirect, forgetPass;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volenteer_login);

        // Initialize views
        em = findViewById(R.id.em);
        pass = findViewById(R.id.pass);
        loginButton = findViewById(R.id.loginButton);
        registerRedirect = findViewById(R.id.registerRedirect);
        forgetPass = findViewById(R.id.forgetpass);

        // Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");

        // Login click
        loginButton.setOnClickListener(v -> {
            String email = em.getText().toString().trim();
            String password = pass.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            } else {
                loginVolunteer(email, password);
            }
        });

        // Register redirect
        registerRedirect.setOnClickListener(v -> {
            startActivity(new Intent(Volenteer_Login.this, Volenteer_Registration.class));
        });

        // Forgot password click
        forgetPass.setOnClickListener(v ->
                Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show()
        );
    }

    private void loginVolunteer(String email, String password) {
        progressDialog.show();

        String url = "http://192.168.29.195:3000/volenteer/loginvolunteer"; // Your backend API

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();

                    if (response.equalsIgnoreCase("Login Success")) {
                        Toast.makeText(Volenteer_Login.this, "Login Successful", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(Volenteer_Login.this, activity_volenteer_dashboard.class));
                        finish();
                    } else {
                        Toast.makeText(Volenteer_Login.this, "Volunteer not found. Redirecting to registration...", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(Volenteer_Login.this, Volenteer_Registration.class));
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(Volenteer_Login.this, "Login Failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
}
