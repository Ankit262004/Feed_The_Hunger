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

public class volenteer_Forgetpassword extends AppCompatActivity {

    private EditText emailEditText;
    private Button sendOtpButton;
    private TextView backToLoginText;
    private RequestQueue requestQueue;

    private static final String TAG = "vol_forget";
    private final String URL = MyIP.IP_ADDRESS + "user/forgot-password/send-otp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volenteer_forgetpassword);

        emailEditText = findViewById(R.id.emailEditText);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        backToLoginText = findViewById(R.id.backToLoginText);

        requestQueue = Volley.newRequestQueue(this);

        sendOtpButton.setOnClickListener(v -> sendOtp());

        backToLoginText.setOnClickListener(v -> {
            startActivity(new Intent(this, Volenteer_Login.class));
            finish();
        });
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

            JSONObject obj = new JSONObject();
            obj.put("email", email);

            Log.d(TAG, "SEND OTP URL: " + URL);
            Log.d(TAG, "REQUEST BODY: " + obj.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    URL,
                    obj,
                    response -> {
                        sendOtpButton.setEnabled(true);
                        sendOtpButton.setText("Send OTP");

                        String message = response.optString("message", "OTP sent successfully");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                        Intent i = new Intent(this, volenteer_verify_otp.class);
                        i.putExtra("email", email);
                        startActivity(i);
                    },
                    error -> {
                        sendOtpButton.setEnabled(true);
                        sendOtpButton.setText("Send OTP");
                        handleError(error);
                    }
            );

            requestQueue.add(request);

        } catch (Exception e) {
            sendOtpButton.setEnabled(true);
            sendOtpButton.setText("Send OTP");
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleError(VolleyError error) {
        String message = "Error sending OTP";

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