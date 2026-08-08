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

public class User_forgotpassword extends AppCompatActivity {

    private EditText emailEditText;
    private Button sendOtpButton;
    private TextView backToLoginText;
    private RequestQueue requestQueue;

    private static final String TAG = "user_forgetpassword";
    private final String SEND_OTP_URL = MyIP.IP_ADDRESS + "user/forgot-password/send-otp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_forgotpassword);

        emailEditText = findViewById(R.id.emailEditText);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        backToLoginText = findViewById(R.id.backToLoginText);

        requestQueue = Volley.newRequestQueue(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainForget), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sendOtpButton.setOnClickListener(v -> sendOtp());

        backToLoginText.setOnClickListener(v -> finish());
    }

    private void sendOtp() {
        String email = emailEditText.getText().toString().trim().toLowerCase();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Enter email");
            emailEditText.requestFocus();
            return;
        }

        try {
            sendOtpButton.setEnabled(false);
            sendOtpButton.setText("Sending...");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", email);

            Log.d(TAG, "SEND OTP URL: " + SEND_OTP_URL);
            Log.d(TAG, "REQUEST BODY: " + jsonObject.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    SEND_OTP_URL,
                    jsonObject,
                    response -> {
                        sendOtpButton.setEnabled(true);
                        sendOtpButton.setText("Send OTP");

                        String message = response.optString("message", "OTP sent successfully");
                        Toast.makeText(User_forgotpassword.this, message, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(User_forgotpassword.this, user_verify_otp.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    },
                    error -> {
                        sendOtpButton.setEnabled(true);
                        sendOtpButton.setText("Send OTP");
                        handleVolleyError(error);
                    }
            );

            requestQueue.add(request);

        } catch (Exception e) {
            sendOtpButton.setEnabled(true);
            sendOtpButton.setText("Send OTP");
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleVolleyError(VolleyError error) {
        String message = "Failed to send OTP";

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

        Toast.makeText(User_forgotpassword.this, message, Toast.LENGTH_LONG).show();
    }
}