package com.example.feed_the_hunger.Volenteer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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

public class volenteer_verify_otp extends AppCompatActivity {

    private EditText otpEditText;
    private Button verifyOtpButton;
    private RequestQueue requestQueue;

    private String email = "";

    private static final String TAG = "vol_verify_otp";
    private final String URL = MyIP.IP_ADDRESS + "user/forgot-password/verify-otp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volenteer_verify_otp);

        otpEditText = findViewById(R.id.otpEditText);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);

        requestQueue = Volley.newRequestQueue(this);

        if (getIntent() != null) {
            email = getIntent().getStringExtra("email");
            if (email == null) email = "";
        }

        verifyOtpButton.setOnClickListener(v -> verifyOtp());
    }

    private void verifyOtp() {
        String otp = otpEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email missing. Please try again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (TextUtils.isEmpty(otp)) {
            otpEditText.setError("Enter OTP");
            otpEditText.requestFocus();
            return;
        }

        try {
            verifyOtpButton.setEnabled(false);
            verifyOtpButton.setText("Verifying...");

            JSONObject obj = new JSONObject();
            obj.put("email", email);
            obj.put("otp", otp);

            Log.d(TAG, "VERIFY OTP URL: " + URL);
            Log.d(TAG, "REQUEST BODY: " + obj.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    URL,
                    obj,
                    response -> {
                        verifyOtpButton.setEnabled(true);
                        verifyOtpButton.setText("Verify OTP");

                        String message = response.optString("message", "OTP verified successfully");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                        Intent i = new Intent(this, volenteer_reset_password.class);
                        i.putExtra("email", email);
                        startActivity(i);
                        finish();
                    },
                    error -> {
                        verifyOtpButton.setEnabled(true);
                        verifyOtpButton.setText("Verify OTP");
                        handleError(error);
                    }
            );

            requestQueue.add(request);

        } catch (Exception e) {
            verifyOtpButton.setEnabled(true);
            verifyOtpButton.setText("Verify OTP");
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleError(VolleyError error) {
        String message = "OTP failed";

        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                String responseData = new String(error.networkResponse.data);
                Log.e(TAG, "ERROR RESPONSE: " + responseData);

                JSONObject errorObject = new JSONObject(responseData);
                message = errorObject.optString("message", message);
            } else {
                message = "Network error: " + error.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}