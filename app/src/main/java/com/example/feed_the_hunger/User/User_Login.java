package com.example.feed_the_hunger.User;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.feed_the_hunger.activity_choose;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

public class User_Login extends AppCompatActivity {

    private EditText em;
    private TextInputEditText pass;
    private Button loginButton;
    private TextView forgetpass, registerRedirect, backToHome;

    private RequestQueue requestQueue;

    private static final String TAG = "user_login";
    private final String LOGIN_URL = MyIP.IP_ADDRESS + "user/login";

    private String fcmToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_login);

        em = findViewById(R.id.em);
        pass = findViewById(R.id.pass);
        loginButton = findViewById(R.id.loginButton);
        forgetpass = findViewById(R.id.forgetpass);
        registerRedirect = findViewById(R.id.registerRedirect);
        backToHome = findViewById(R.id.backToHome);

        requestQueue = Volley.newRequestQueue(this);

        getFcmToken();

        loginButton.setOnClickListener(v -> validateAndLogin());

        forgetpass.setOnClickListener(v -> {
            Intent intent = new Intent(User_Login.this, User_forgotpassword.class);
            startActivity(intent);
        });

        registerRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(User_Login.this, User_Registration.class);
            startActivity(intent);
        });

        backToHome.setOnClickListener(v -> {
            Intent intent = new Intent(User_Login.this, activity_choose.class);
            startActivity(intent);
            finish();
        });
    }

    private void getFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    fcmToken = task.getResult();
                    Log.d(TAG, "FCM Token: " + fcmToken);
                });
    }

    private void validateAndLogin() {
        String email = em.getText().toString().trim().toLowerCase();
        String password = pass.getText() != null
                ? pass.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(email)) {
            em.setError("Enter email");
            em.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            pass.setError("Enter password");
            pass.requestFocus();
            return;
        }

        loginUser(email, password);
    }

    private void loginUser(String email, String password) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", email);
            jsonObject.put("password", password);

            if (!TextUtils.isEmpty(fcmToken)) {
                jsonObject.put("fcmToken", fcmToken);
            }

            Log.d(TAG, "LOGIN URL: " + LOGIN_URL);
            Log.d(TAG, "REQUEST BODY: " + jsonObject.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    LOGIN_URL,
                    jsonObject,
                    response -> {
                        try {
                            boolean success = response.optBoolean("success", false);
                            String message = response.optString("message", "Login completed");

                            if (success) {
                                JSONObject userObject = response.optJSONObject("user");
                                String token = response.optString("token", "");

                                if (userObject == null) {
                                    Toast.makeText(User_Login.this,
                                            "User data missing in response",
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }

                                String userId = userObject.optString("_id", "");
                                String fullName = userObject.optString("fullName", "");
                                String userEmail = userObject.optString("email", "");
                                String location = userObject.optString("location", "");
                                String userType = userObject.optString("userType", "user");

                                saveLoginSession(
                                        userId,
                                        fullName,
                                        userEmail,
                                        location,
                                        userType,
                                        token
                                );

                                Toast.makeText(
                                        User_Login.this,
                                        message,
                                        Toast.LENGTH_LONG
                                ).show();

                                Intent intent = new Intent(
                                        User_Login.this,
                                        activity_user_dashboard.class
                                );

                                intent.putExtra("userId", userId);
                                intent.putExtra("fullName", fullName);
                                intent.putExtra("email", userEmail);
                                intent.putExtra("location", location);
                                intent.putExtra("userType", userType);

                                startActivity(intent);
                                finish();

                            } else {
                                Toast.makeText(
                                        User_Login.this,
                                        message,
                                        Toast.LENGTH_LONG
                                ).show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(
                                    User_Login.this,
                                    "Response parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    },
                    this::handleVolleyError
            );

            requestQueue.add(request);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(
                    this,
                    "Something went wrong: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void saveLoginSession(
            String userId,
            String fullName,
            String email,
            String location,
            String userType,
            String token
    ) {
        SharedPreferences sharedPreferences =
                getSharedPreferences("UserSession", MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLoggedIn", true);
        editor.putString("userId", userId);
        editor.putString("fullName", fullName);
        editor.putString("email", email);
        editor.putString("location", location);
        editor.putString("userType", userType);
        editor.putString("token", token);

        editor.apply();
    }

    private void handleVolleyError(VolleyError error) {
        String message = "Login failed";

        try {
            if (error.networkResponse != null) {
                int statusCode = error.networkResponse.statusCode;

                if (error.networkResponse.data != null) {
                    String responseData =
                            new String(error.networkResponse.data);

                    Log.e(TAG, "Status Code: " + statusCode);
                    Log.e(TAG, "Error Response: " + responseData);

                    try {
                        JSONObject errorObject =
                                new JSONObject(responseData);

                        message = errorObject.optString(
                                "message",
                                message
                        );

                    } catch (Exception jsonException) {
                        message =
                                "Error " + statusCode + ": " + responseData;
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

        Toast.makeText(
                User_Login.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}