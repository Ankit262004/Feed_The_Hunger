package com.example.feed_the_hunger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class admin_registration extends AppCompatActivity {

    EditText adminFullName, adminEmail, adminPassword, adminConfirmPassword, adminLocation;
    Button adminRegisterButton;
    TextView loginRedirect, backToHome;
    ProgressDialog progressDialog;

    String URL = MyIP.IP_ADDRESS + "user/admin/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_registration);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        adminFullName = findViewById(R.id.adminFullName);
        adminEmail = findViewById(R.id.adminEmail);
        adminPassword = findViewById(R.id.adminPassword);
        adminConfirmPassword = findViewById(R.id.adminConfirmPassword);
        adminLocation = findViewById(R.id.adminLocation);
        adminRegisterButton = findViewById(R.id.adminRegisterButton);
        loginRedirect = findViewById(R.id.loginRedirect);
        backToHome = findViewById(R.id.backToHome);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating admin...");
        progressDialog.setCancelable(false);

        adminRegisterButton.setOnClickListener(v -> registerAdmin());

        loginRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(admin_registration.this, admin_login.class);
            startActivity(intent);
            finish();
        });

        backToHome.setOnClickListener(v -> {
            Intent intent = new Intent(admin_registration.this, activity_choose.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerAdmin() {

        String fullName = adminFullName.getText().toString().trim();
        String email = adminEmail.getText().toString().trim().toLowerCase();
        String password = adminPassword.getText().toString().trim();
        String confirmPassword = adminConfirmPassword.getText().toString().trim();
        String location = adminLocation.getText().toString().trim();

        if (fullName.isEmpty()) {
            adminFullName.setError("Enter full name");
            adminFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            adminEmail.setError("Enter admin email");
            adminEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            adminEmail.setError("Enter valid email");
            adminEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            adminPassword.setError("Enter password");
            adminPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            adminPassword.setError("Password must be at least 6 characters");
            adminPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            adminConfirmPassword.setError("Confirm your password");
            adminConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            adminConfirmPassword.setError("Passwords do not match");
            adminConfirmPassword.requestFocus();
            return;
        }

        if (location.isEmpty()) {
            adminLocation.setError("Enter location");
            adminLocation.requestFocus();
            return;
        }

        progressDialog.show();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL,
                response -> {
                    progressDialog.dismiss();

                    try {
                        JSONObject json = new JSONObject(response);
                        boolean success = json.optBoolean("success", false);
                        String message = json.optString("message", "Registration completed");

                        Toast.makeText(admin_registration.this, message, Toast.LENGTH_SHORT).show();

                        if (success) {
                            Intent intent = new Intent(admin_registration.this, admin_login.class);
                            startActivity(intent);
                            finish();
                        }

                    } catch (JSONException e) {
                        Toast.makeText(admin_registration.this, "JSON Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();

                    String message = "Registration failed";

                    try {
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String responseData = new String(error.networkResponse.data);
                            JSONObject errorObj = new JSONObject(responseData);
                            message = errorObj.optString("message", message);
                        } else {
                            message = error.toString();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(admin_registration.this, message, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("fullName", fullName);
                params.put("email", email);
                params.put("password", password);
                params.put("location", location);
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}