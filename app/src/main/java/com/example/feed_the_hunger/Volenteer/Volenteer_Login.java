package com.example.feed_the_hunger.Volenteer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.feed_the_hunger.MyIP;
import com.example.feed_the_hunger.R;
import com.example.feed_the_hunger.activity_choose;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Volenteer_Login extends AppCompatActivity {

    EditText em, pass;
    Button loginButton;
    TextView registerRedirect, forgetPass, backToHome;
    ProgressDialog progressDialog;

    private static final String TAG = "Volenteer_Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volenteer_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginCard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        em = findViewById(R.id.em);
        pass = findViewById(R.id.pass);
        loginButton = findViewById(R.id.loginButton);
        registerRedirect = findViewById(R.id.registerRedirect);
        forgetPass = findViewById(R.id.forgetpass);
        backToHome = findViewById(R.id.backToHome);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        loginButton.setOnClickListener(v -> {
            String email = em.getText().toString().trim().toLowerCase();
            String password = pass.getText().toString().trim();

            if (email.isEmpty()) {
                em.setError("Enter email");
                em.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                pass.setError("Enter password");
                pass.requestFocus();
                return;
            }

            loginVolunteer(email, password);
        });

        registerRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(Volenteer_Login.this, Volenteer_Registration.class);
            startActivity(intent);
        });

        forgetPass.setOnClickListener(v -> {
            Log.d(TAG, "Forgot password clicked");
            Intent intent = new Intent(Volenteer_Login.this, volenteer_Forgetpassword.class);
            startActivity(intent);
        });

        backToHome.setOnClickListener(v -> {
            Intent intent = new Intent(Volenteer_Login.this, activity_choose.class);
            startActivity(intent);
            finish();
        });
    }

    private void loginVolunteer(String email, String password) {
        progressDialog.show();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(Volenteer_Login.this, "Failed to get FCM token", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String fcmToken = task.getResult();
                    String url = MyIP.IP_ADDRESS + "user/login";

                    StringRequest request = new StringRequest(
                            Request.Method.POST,
                            url,
                            response -> {
                                progressDialog.dismiss();

                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    boolean success = jsonObject.getBoolean("success");

                                    if (success) {
                                        String message = jsonObject.optString("message", "Login successful");

                                        JSONObject userObject = jsonObject.optJSONObject("user");
                                        if (userObject != null) {
                                            String userId = userObject.optString("_id", "");
                                            String fullName = userObject.optString("fullName", "");
                                            String userEmail = userObject.optString("email", "");
                                            String location = userObject.optString("location", "");
                                            String userType = userObject.optString("userType", "volunteer");

                                            SharedPreferences sharedPreferences = getSharedPreferences("VolunteerSession", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();

                                            editor.putString("userId", userId);
                                            editor.putString("fullName", fullName);
                                            editor.putString("email", userEmail);
                                            editor.putString("location", location);
                                            editor.putString("userType", userType);
                                            editor.putString("fcmToken", fcmToken);
                                            editor.putBoolean("isLoggedIn", true);
                                            editor.apply();

                                            Log.d(TAG, "Volunteer session saved: userId=" + userId
                                                    + ", fullName=" + fullName
                                                    + ", isLoggedIn=" + sharedPreferences.getBoolean("isLoggedIn", false));
                                        }

                                        Toast.makeText(Volenteer_Login.this, message, Toast.LENGTH_LONG).show();

                                        Intent intent = new Intent(Volenteer_Login.this, activity_volenteer_dashboard.class);
                                        startActivity(intent);
                                        finish();

                                    } else {
                                        String message = jsonObject.optString("message", "Login failed");
                                        Toast.makeText(Volenteer_Login.this, message, Toast.LENGTH_LONG).show();
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(Volenteer_Login.this, "Parsing Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            },
                            error -> {
                                progressDialog.dismiss();
                                Toast.makeText(Volenteer_Login.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("email", email);
                            params.put("password", password);
                            params.put("fcmToken", fcmToken);
                            return params;
                        }
                    };

                    RequestQueue queue = Volley.newRequestQueue(Volenteer_Login.this);
                    queue.add(request);
                });
    }
}