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

public class admin_verify_otp extends AppCompatActivity {

    private TextView tvOtpInfo;
    private EditText otpEditText;
    private Button verifyOtpButton;
    private RequestQueue requestQueue;

    private String email = "";

    private static final String TAG = "admin_verify_otp";
    private final String VERIFY_OTP_URL = MyIP.IP_ADDRESS + "user/admin/verify-otp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_verify_otp);

        tvOtpInfo = findViewById(R.id.tvOtpInfo);
        otpEditText = findViewById(R.id.otpEditText);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);

        requestQueue = Volley.newRequestQueue(this);

        if (getIntent() != null) {
            email = getIntent().getStringExtra("email");
            if (email == null) email = "";
        }

        if (!TextUtils.isEmpty(email)) {
            tvOtpInfo.setText("Enter the OTP sent to " + email);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainOtp), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        verifyOtpButton.setOnClickListener(v -> {
            String otp = otpEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Email missing. Try again.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            if (TextUtils.isEmpty(otp)) {
                otpEditText.setError("Enter OTP");
                otpEditText.requestFocus();
                return;
            }

            if (otp.length() != 6) {
                otpEditText.setError("Enter complete 6-digit OTP");
                otpEditText.requestFocus();
                return;
            }

            verifyOtp(email, otp);
        });
    }

    private void verifyOtp(String email, String otp) {
        try {
            verifyOtpButton.setEnabled(false);
            verifyOtpButton.setText("Verifying...");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", email);
            jsonObject.put("otp", otp);

            Log.d(TAG, "VERIFY OTP URL: " + VERIFY_OTP_URL);
            Log.d(TAG, "REQUEST BODY: " + jsonObject.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    VERIFY_OTP_URL,
                    jsonObject,
                    response -> {
                        verifyOtpButton.setEnabled(true);
                        verifyOtpButton.setText("Verify OTP");

                        String message = response.optString("message", "OTP verified successfully");
                        Toast.makeText(admin_verify_otp.this, message, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(admin_verify_otp.this, admin_reset_password.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    },
                    error -> {
                        verifyOtpButton.setEnabled(true);
                        verifyOtpButton.setText("Verify OTP");
                        handleVolleyError(error);
                    }
            );

            requestQueue.add(request);

        } catch (Exception e) {
            verifyOtpButton.setEnabled(true);
            verifyOtpButton.setText("Verify OTP");
            Toast.makeText(this, "Something went wrong: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleVolleyError(VolleyError error) {
        String message = "Invalid OTP";

        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                String responseData = new String(error.networkResponse.data);
                Log.e(TAG, "ERROR RESPONSE: " + responseData);

                JSONObject errorObject = new JSONObject(responseData);
                message = errorObject.optString("message", message);
            } else {
                message = "Network Error: " + error.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}