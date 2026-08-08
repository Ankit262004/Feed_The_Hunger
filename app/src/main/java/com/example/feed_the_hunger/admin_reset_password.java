package com.example.feed_the_hunger;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class admin_reset_password extends AppCompatActivity {

    EditText newPasswordEditText, confirmPasswordEditText;
    Button resetPasswordButton;
    TextView backToLoginText;
    RequestQueue requestQueue;

    String RESET_PASSWORD_URL = MyIP.IP_ADDRESS + "user/admin/reset-password";
    String email = "";

    private static final String TAG = "admin_reset_password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_reset_password);

        // Bind views from XML
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        backToLoginText = findViewById(R.id.backToLoginText);

        requestQueue = Volley.newRequestQueue(this);

        // Get email from previous screen
        if (getIntent() != null && getIntent().hasExtra("email")) {
            email = getIntent().getStringExtra("email");
            if (email == null) {
                email = "";
            }
        }

        // Window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainReset), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Update password button click
        resetPasswordButton.setOnClickListener(v -> validateAndUpdatePassword());

        // Back to login click
        backToLoginText.setOnClickListener(v -> {
            Intent intent = new Intent(admin_reset_password.this, admin_login.class);
            startActivity(intent);
            finish();
        });
    }

    private void validateAndUpdatePassword() {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword)) {
            newPasswordEditText.setError("Enter new password");
            newPasswordEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Enter confirm password");
            confirmPasswordEditText.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            newPasswordEditText.setError("Password must be at least 6 characters");
            newPasswordEditText.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Email missing. Restart forgot password flow.", Toast.LENGTH_LONG).show();
            return;
        }

        updatePassword(email, newPassword, confirmPassword);
    }

    private void updatePassword(String email, String newPassword, String confirmPassword) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", email);
            jsonObject.put("newPassword", newPassword);
            jsonObject.put("confirmPassword", confirmPassword);

            Log.d(TAG, "RESET URL: " + RESET_PASSWORD_URL);
            Log.d(TAG, "RESET BODY: " + jsonObject.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    RESET_PASSWORD_URL,
                    jsonObject,
                    response -> {
                        String message = response.optString("message", "Password updated successfully");
                        Toast.makeText(admin_reset_password.this, message, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(admin_reset_password.this, admin_login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    },
                    this::handleVolleyError
            );

            requestQueue.add(request);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Something went wrong: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleVolleyError(VolleyError error) {
        String message = "Password update failed";

        try {
            if (error.networkResponse != null) {
                int statusCode = error.networkResponse.statusCode;
                String responseData = "";

                if (error.networkResponse.data != null) {
                    responseData = new String(error.networkResponse.data);
                    Log.e(TAG, "Status Code: " + statusCode);
                    Log.e(TAG, "Error Response: " + responseData);

                    try {
                        JSONObject errorObject = new JSONObject(responseData);
                        message = errorObject.optString("message", "Password update failed");
                    } catch (Exception jsonException) {
                        message = "Error " + statusCode + ": " + responseData;
                    }
                } else {
                    message = "Error " + statusCode;
                }
            } else {
                message = "Network error: " + error.toString();
                Log.e(TAG, "Volley Error: " + error.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(admin_reset_password.this, message, Toast.LENGTH_LONG).show();
    }
}