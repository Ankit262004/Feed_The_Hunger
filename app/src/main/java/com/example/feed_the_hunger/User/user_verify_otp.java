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

public class user_verify_otp extends AppCompatActivity {

    private EditText otpEditText;
    private Button verifyOtpButton;
    private TextView backText;
    private RequestQueue requestQueue;

    private String email = "";

    private static final String TAG = "user_verify_otp";
    private final String VERIFY_OTP_URL = MyIP.IP_ADDRESS + "user/forgot-password/verify-otp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_verify_otp);

        otpEditText = findViewById(R.id.otpEditText);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        backText = findViewById(R.id.backText);

        requestQueue = Volley.newRequestQueue(this);

        if (getIntent() != null) {
            email = getIntent().getStringExtra("email");
            if (email == null) email = "";
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainVerifyOtp), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        verifyOtpButton.setOnClickListener(v -> verifyOtp());
        backText.setOnClickListener(v -> finish());
    }

    private void verifyOtp() {
        String otp = otpEditText.getText().toString().trim();

        if (TextUtils.isEmpty(otp)) {
            otpEditText.setError("Enter OTP");
            otpEditText.requestFocus();
            return;
        }

        try {
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
                        String message = response.optString("message", "OTP verified successfully");
                        Toast.makeText(user_verify_otp.this, message, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(user_verify_otp.this, user_reset_password.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    },
                    this::handleVolleyError
            );

            requestQueue.add(request);

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleVolleyError(VolleyError error) {
        String message = "OTP verification failed";

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

        Toast.makeText(user_verify_otp.this, message, Toast.LENGTH_LONG).show();
    }
}