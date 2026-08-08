package com.example.feed_the_hunger.User;

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
import com.example.feed_the_hunger.MyIP;
import com.example.feed_the_hunger.R;

import org.json.JSONObject;

public class user_reset_password extends AppCompatActivity {

    private EditText newPasswordEditText, confirmPasswordEditText;
    private Button resetPasswordButton;
    private TextView backText;
    private RequestQueue requestQueue;

    private String email = "";
    private boolean isResettingPassword = false;

    private static final String TAG = "user_reset_password";
    private final String RESET_PASSWORD_URL = MyIP.IP_ADDRESS + "user/forgot-password/reset-password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_reset_password);

        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        backText = findViewById(R.id.backToLoginText);

        requestQueue = Volley.newRequestQueue(this);

        if (getIntent() != null) {
            email = getIntent().getStringExtra("email");
            if (email == null) email = "";
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainReset), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        resetPasswordButton.setOnClickListener(v -> {
            if (isResettingPassword) return;
            resetPassword();
        });

        backText.setOnClickListener(v -> finish());
    }

    private void resetPassword() {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email missing. Please try again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            newPasswordEditText.setError("Enter new password");
            newPasswordEditText.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            newPasswordEditText.setError("Password must be at least 6 characters");
            newPasswordEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Confirm password");
            confirmPasswordEditText.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }

        try {
            isResettingPassword = true;
            resetPasswordButton.setEnabled(false);
            resetPasswordButton.setText("Resetting...");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", email);
            jsonObject.put("newPassword", newPassword);
            jsonObject.put("confirmPassword", confirmPassword);

            Log.d(TAG, "RESET PASSWORD URL: " + RESET_PASSWORD_URL);
            Log.d(TAG, "REQUEST BODY: " + jsonObject.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    RESET_PASSWORD_URL,
                    jsonObject,
                    response -> {
                        isResettingPassword = false;
                        resetPasswordButton.setEnabled(true);
                        resetPasswordButton.setText("Reset Password");

                        String message = response.optString("message", "Password reset successful");
                        Toast.makeText(user_reset_password.this, message, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(user_reset_password.this, User_Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    },
                    error -> {
                        isResettingPassword = false;
                        resetPasswordButton.setEnabled(true);
                        resetPasswordButton.setText("Reset Password");
                        handleVolleyError(error);
                    }
            );

            requestQueue.add(request);

        } catch (Exception e) {
            isResettingPassword = false;
            resetPasswordButton.setEnabled(true);
            resetPasswordButton.setText("Reset Password");

            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleVolleyError(VolleyError error) {
        String message = "Password reset failed";

        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                String responseData = new String(error.networkResponse.data);
                Log.e(TAG, "Error Response: " + responseData);

                JSONObject errorObject = new JSONObject(responseData);
                message = errorObject.optString("message", message);
            } else {
                message = "Network error: " + error.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(user_reset_password.this, message, Toast.LENGTH_LONG).show();
    }
}