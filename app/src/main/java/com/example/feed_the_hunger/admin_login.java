package com.example.feed_the_hunger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class admin_login extends AppCompatActivity {

    EditText adminEmail, adminPassword;
    Button adminLoginButton;
    TextView forgetpass, createAdminRedirect, backToHome;
    ProgressDialog progressDialog;

    String URL = MyIP.IP_ADDRESS + "user/admin/login";
    private static final String TAG = "ADMIN_LOGIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        adminEmail = findViewById(R.id.adminEmail);
        adminPassword = findViewById(R.id.adminPassword);
        adminLoginButton = findViewById(R.id.adminLoginButton);
        forgetpass = findViewById(R.id.forgetpass);
        createAdminRedirect = findViewById(R.id.createAdminRedirect);
        backToHome = findViewById(R.id.backToHome);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Admin login...");
        progressDialog.setCancelable(false);

        adminLoginButton.setOnClickListener(v -> loginAdmin());

        createAdminRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(admin_login.this, admin_registration.class);
            startActivity(intent);
        });

        forgetpass.setOnClickListener(v -> {
            Intent intent = new Intent(admin_login.this, admin_forgetpassword.class);
            startActivity(intent);
        });

        backToHome.setOnClickListener(v -> {
            Intent intent = new Intent(admin_login.this, activity_choose.class);
            startActivity(intent);
            finish();
        });
    }

    private void loginAdmin() {
        String email = adminEmail.getText().toString().trim().toLowerCase();
        String password = adminPassword.getText().toString().trim();

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
            adminPassword.setError("Enter admin password");
            adminPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            adminPassword.setError("Password must be at least 6 characters");
            adminPassword.requestFocus();
            return;
        }

        Log.d(TAG, "Login URL = " + URL);
        Log.d(TAG, "Email = " + email);

        progressDialog.show();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL,
                response -> {
                    progressDialog.dismiss();
                    Log.d(TAG, "Response = " + response);

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.optBoolean("success", false);
                        String message = jsonObject.optString("message", "Login completed");

                        Toast.makeText(admin_login.this, message, Toast.LENGTH_SHORT).show();

                        if (success) {
                            JSONObject adminObject = jsonObject.optJSONObject("admin");
                            String token = jsonObject.optString("token", "");

                            String adminId = "";
                            String fullName = "";
                            String adminEmailValue = email;
                            String location = "";
                            String userType = "admin";

                            if (adminObject != null) {
                                adminId = adminObject.optString("_id", "");
                                fullName = adminObject.optString("fullName", "");
                                adminEmailValue = adminObject.optString("email", email);
                                location = adminObject.optString("location", "");
                                userType = adminObject.optString("userType", "admin");
                            }

                            SharedPreferences prefs = getSharedPreferences("AdminSession", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putString("adminId", adminId);
                            editor.putString("fullName", fullName);
                            editor.putString("email", adminEmailValue);
                            editor.putString("location", location);
                            editor.putString("userType", userType);
                            editor.putString("token", token);
                            editor.apply();

                            Intent intent = new Intent(admin_login.this, admin_dashboard.class);
                            startActivity(intent);
                            finish();
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Parse Error = " + e.getMessage());
                        Toast.makeText(admin_login.this, "JSON Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    handleVolleyError(error);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
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

    private void handleVolleyError(VolleyError error) {
        String message = "Admin login failed";

        try {
            if (error.networkResponse != null) {
                int statusCode = error.networkResponse.statusCode;

                if (error.networkResponse.data != null) {
                    String responseBody = new String(error.networkResponse.data);
                    Log.e(TAG, "Status Code = " + statusCode);
                    Log.e(TAG, "Error Body = " + responseBody);

                    try {
                        JSONObject errorObject = new JSONObject(responseBody);
                        message = errorObject.optString("message", message);
                    } catch (Exception e) {
                        message = "Error " + statusCode + ": " + responseBody;
                    }
                } else {
                    message = "Error " + statusCode;
                }
            } else {
                message = "Network Error: " + error.toString();
                Log.e(TAG, "Volley Error = " + error.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}