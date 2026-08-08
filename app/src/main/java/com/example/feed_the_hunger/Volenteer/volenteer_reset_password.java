package com.example.feed_the_hunger.Volenteer;

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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.feed_the_hunger.MyIP;
import com.example.feed_the_hunger.R;

import org.json.JSONObject;

public class volenteer_reset_password extends AppCompatActivity {

    private EditText newPass, confirmPass;
    private Button resetBtn;
    private TextView backToLoginText;
    private RequestQueue requestQueue;

    private String email = "";

    private static final String TAG = "vol_reset";
    private final String URL = MyIP.IP_ADDRESS + "user/forgot-password/reset-password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volenteer_reset_password);

        // Bind views
        newPass = findViewById(R.id.newPasswordEditText);
        confirmPass = findViewById(R.id.confirmPasswordEditText);
        resetBtn = findViewById(R.id.resetPasswordButton);
        backToLoginText = findViewById(R.id.backToLoginText);

        requestQueue = Volley.newRequestQueue(this);

        // Get email from previous screen
        if (getIntent() != null) {
            email = getIntent().getStringExtra("email");
            if (email == null) email = "";
        }

        // Reset button click
        resetBtn.setOnClickListener(v -> resetPassword());

        // Back to login click
        backToLoginText.setOnClickListener(v -> {
            Intent i = new Intent(this, Volenteer_Login.class);
            startActivity(i);
            finish();
        });
    }

    private void resetPassword() {
        String p1 = newPass.getText().toString().trim();
        String p2 = confirmPass.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email missing. Try again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (TextUtils.isEmpty(p1)) {
            newPass.setError("Enter new password");
            newPass.requestFocus();
            return;
        }

        if (p1.length() < 6) {
            newPass.setError("Minimum 6 characters");
            newPass.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(p2)) {
            confirmPass.setError("Confirm password");
            confirmPass.requestFocus();
            return;
        }

        if (!p1.equals(p2)) {
            confirmPass.setError("Passwords do not match");
            confirmPass.requestFocus();
            return;
        }

        try {
            resetBtn.setEnabled(false);
            resetBtn.setText("Updating...");

            JSONObject obj = new JSONObject();
            obj.put("email", email);
            obj.put("newPassword", p1);
            obj.put("confirmPassword", p2);

            Log.d(TAG, "RESET URL: " + URL);
            Log.d(TAG, "BODY: " + obj.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    URL,
                    obj,
                    response -> {
                        resetBtn.setEnabled(true);
                        resetBtn.setText("Update Password");

                        String msg = response.optString("message", "Password updated");
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

                        Intent i = new Intent(this, Volenteer_Login.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    },
                    error -> {
                        resetBtn.setEnabled(true);
                        resetBtn.setText("Update Password");
                        handleError(error);
                    }
            );

            requestQueue.add(request);

        } catch (Exception e) {
            resetBtn.setEnabled(true);
            resetBtn.setText("Update Password");
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleError(VolleyError error) {
        String message = "Reset failed";

        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                String res = new String(error.networkResponse.data);
                JSONObject obj = new JSONObject(res);
                message = obj.optString("message", message);
            } else {
                message = "Network error: " + error.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}