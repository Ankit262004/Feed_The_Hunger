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

import com.android.volley.DefaultRetryPolicy;
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

    private static final String TAG = "UserForgotPassword";

    private final String SEND_OTP_URL =
            MyIP.IP_ADDRESS + "user/forgot-password/send-otp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_forgotpassword);

        emailEditText = findViewById(R.id.emailEditText);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        backToLoginText = findViewById(R.id.backToLoginText);

        requestQueue = Volley.newRequestQueue(this);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.mainForget),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        sendOtpButton.setOnClickListener(v -> sendOtp());

        backToLoginText.setOnClickListener(v -> finish());
    }

    private void sendOtp() {

        String email =
                emailEditText.getText()
                        .toString()
                        .trim()
                        .toLowerCase();

        // -----------------------------------------
        // VALIDATE EMAIL
        // -----------------------------------------

        if (TextUtils.isEmpty(email)) {

            emailEditText.setError("Enter email");
            emailEditText.requestFocus();

            return;
        }

        try {

            // -----------------------------------------
            // BUTTON STATE
            // -----------------------------------------

            sendOtpButton.setEnabled(false);
            sendOtpButton.setText("Sending...");

            // -----------------------------------------
            // REQUEST BODY
            // -----------------------------------------

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("email", email);

            Log.d(TAG, "=================================");
            Log.d(TAG, "SEND OTP REQUEST");
            Log.d(TAG, "URL: " + SEND_OTP_URL);
            Log.d(TAG, "EMAIL: " + email);
            Log.d(TAG, "BODY: " + jsonObject);
            Log.d(TAG, "=================================");

            // -----------------------------------------
            // VOLLEY REQUEST
            // -----------------------------------------

            JsonObjectRequest request =
                    new JsonObjectRequest(
                            Request.Method.POST,
                            SEND_OTP_URL,
                            jsonObject,

                            // ---------------------------------
                            // SUCCESS
                            // ---------------------------------

                            response -> {

                                Log.d(
                                        TAG,
                                        "OTP RESPONSE: " + response
                                );

                                sendOtpButton.setEnabled(true);
                                sendOtpButton.setText("Send OTP");

                                String message =
                                        response.optString(
                                                "message",
                                                "OTP sent successfully"
                                        );

                                Toast.makeText(
                                        User_forgotpassword.this,
                                        message,
                                        Toast.LENGTH_LONG
                                ).show();

                                // ---------------------------------
                                // OPEN VERIFY OTP SCREEN
                                // ---------------------------------

                                Intent intent =
                                        new Intent(
                                                User_forgotpassword.this,
                                                user_verify_otp.class
                                        );

                                intent.putExtra(
                                        "email",
                                        email
                                );

                                startActivity(intent);
                            },

                            // ---------------------------------
                            // ERROR
                            // ---------------------------------

                            error -> {

                                Log.e(
                                        TAG,
                                        "OTP REQUEST ERROR",
                                        error
                                );

                                sendOtpButton.setEnabled(true);
                                sendOtpButton.setText("Send OTP");

                                handleVolleyError(error);
                            }
                    );

            // -----------------------------------------
            // IMPORTANT:
            // INCREASE VOLLEY TIMEOUT
            // -----------------------------------------

            request.setRetryPolicy(
                    new DefaultRetryPolicy(

                            // 15 seconds timeout
                            15000,

                            // No automatic retry
                            0,

                            // Backoff multiplier
                            1.0f
                    )
            );

            request.setShouldCache(false);

            requestQueue.add(request);

            Log.d(TAG, "OTP request added to Volley queue");

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Exception while sending OTP",
                    e
            );

            sendOtpButton.setEnabled(true);
            sendOtpButton.setText("Send OTP");

            Toast.makeText(
                    this,
                    "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =====================================================
    // VOLLEY ERROR HANDLER
    // =====================================================

    private void handleVolleyError(VolleyError error) {

        String message = "Failed to send OTP";

        Log.e(
                TAG,
                "Volley error: " + error
        );

        // -----------------------------------------
        // SERVER RESPONSE ERROR
        // -----------------------------------------

        if (error.networkResponse != null) {

            int statusCode =
                    error.networkResponse.statusCode;

            Log.e(
                    TAG,
                    "HTTP STATUS: " + statusCode
            );

            if (error.networkResponse.data != null) {

                try {

                    String responseData =
                            new String(
                                    error.networkResponse.data
                            );

                    Log.e(
                            TAG,
                            "SERVER ERROR RESPONSE: "
                                    + responseData
                    );

                    JSONObject errorObject =
                            new JSONObject(responseData);

                    message =
                            errorObject.optString(
                                    "message",
                                    message
                            );

                } catch (Exception e) {

                    Log.e(
                            TAG,
                            "Could not parse server error",
                            e
                    );
                }
            }
        }

        // -----------------------------------------
        // TIMEOUT
        // -----------------------------------------

        else if (error instanceof com.android.volley.TimeoutError) {

            message =
                    "Request timed out. Please try again.";
        }

        // -----------------------------------------
        // NO CONNECTION
        // -----------------------------------------

        else if (error instanceof com.android.volley.NoConnectionError) {

            message =
                    "Unable to connect to server.";
        }

        // -----------------------------------------
        // GENERAL NETWORK ERROR
        // -----------------------------------------

        else if (error instanceof com.android.volley.NetworkError) {

            message =
                    "Network error. Please check your connection.";
        }

        Toast.makeText(
                User_forgotpassword.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}