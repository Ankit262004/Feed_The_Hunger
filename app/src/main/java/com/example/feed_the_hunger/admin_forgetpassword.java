package com.example.feed_the_hunger;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
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

public class admin_forgetpassword extends AppCompatActivity {

    private EditText forgotEmailEditText;
    private Button submitForgotButton;
    private RequestQueue requestQueue;

    private final String SEND_OTP_URL = MyIP.IP_ADDRESS + "user/admin/send-otp";
    private static final String TAG = "admin_forgetpassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_forgetpassword);

        forgotEmailEditText = findViewById(R.id.forgotEmailEditText);
        submitForgotButton = findViewById(R.id.submitForgotButton);
        requestQueue = Volley.newRequestQueue(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        submitForgotButton.setOnClickListener(v -> {
            String email = forgotEmailEditText.getText().toString().trim().toLowerCase();

            if (TextUtils.isEmpty(email)) {
                forgotEmailEditText.setError("Please enter your email");
                forgotEmailEditText.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                forgotEmailEditText.setError("Enter a valid email");
                forgotEmailEditText.requestFocus();
                return;
            }

            sendOtpToEmail(email);
        });
    }

    private void sendOtpToEmail(String email) {
        try {
            submitForgotButton.setEnabled(false);
            submitForgotButton.setText("Sending...");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", email);

            Log.d(TAG, "SEND OTP URL: " + SEND_OTP_URL);
            Log.d(TAG, "Request Body: " + jsonObject.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    SEND_OTP_URL,
                    jsonObject,
                    response -> {
                        submitForgotButton.setEnabled(true);
                        submitForgotButton.setText("Send OTP");

                        String message = response.optString("message", "OTP sent successfully");
                        Toast.makeText(admin_forgetpassword.this, message, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(admin_forgetpassword.this, admin_verify_otp.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    },
                    error -> {
                        submitForgotButton.setEnabled(true);
                        submitForgotButton.setText("Send OTP");
                        handleVolleyError(error);
                    }
            );

            requestQueue.add(request);

        } catch (Exception e) {
            submitForgotButton.setEnabled(true);
            submitForgotButton.setText("Send OTP");

            e.printStackTrace();
            Toast.makeText(this, "Something went wrong: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleVolleyError(VolleyError error) {
        String message = "Failed to send OTP";

        try {
            if (error.networkResponse != null) {
                int statusCode = error.networkResponse.statusCode;
                String responseData = "";

                if (error.networkResponse.data != null) {
                    responseData = new String(error.networkResponse.data);
                }

                Log.e(TAG, "Status Code: " + statusCode);
                Log.e(TAG, "Error Data: " + responseData);

                try {
                    JSONObject errorObject = new JSONObject(responseData);
                    message = errorObject.optString("message", message);
                } catch (Exception e) {
                    message = "Error " + statusCode + ": " + responseData;
                }

            } else {
                message = "Network Error: " + error.toString();
                Log.e(TAG, "Volley Error: " + error.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(admin_forgetpassword.this, message, Toast.LENGTH_LONG).show();
    }
}