package com.example.feed_the_hunger.Volenteer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.feed_the_hunger.MyIP;
import com.example.feed_the_hunger.R;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class Volenteer_Registration extends AppCompatActivity {

    EditText fname, em, pass, cpass, loc;
    Button reg;
    TextView log;
    ProgressDialog progressDialog;

    String baseUrl = MyIP.IP_ADDRESS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volenteer_registration);

        fname = findViewById(R.id.fname);
        em = findViewById(R.id.em);
        pass = findViewById(R.id.pass);
        cpass = findViewById(R.id.cpass);
        loc = findViewById(R.id.loc);
        reg = findViewById(R.id.reg);
        log = findViewById(R.id.log);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");
        progressDialog.setCancelable(false);

        reg.setOnClickListener(v -> {

            String fullName = fname.getText().toString().trim();
            String email = em.getText().toString().trim();
            String password = pass.getText().toString().trim();
            String confirmPassword = cpass.getText().toString().trim();
            String location = loc.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()
                    || confirmPassword.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            registerVolunteer(fullName, email, password, location);
        });

        log.setOnClickListener(v -> {
            startActivity(new Intent(this, Volenteer_Login.class));
            finish();
        });
    }

    private void registerVolunteer(String fullName, String email, String password, String location) {
        progressDialog.show();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(Volenteer_Registration.this,
                                "Failed to get FCM token",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    String fcmToken = task.getResult();
                    String url = baseUrl + "user/volunteer/register";

                    StringRequest request = new StringRequest(
                            Request.Method.POST,
                            url,
                            response -> {
                                progressDialog.dismiss();

                                Toast.makeText(Volenteer_Registration.this,
                                        "Registration Successful",
                                        Toast.LENGTH_LONG).show();

                                startActivity(new Intent(Volenteer_Registration.this, Volenteer_Login.class));
                                finish();
                            },
                            error -> {
                                progressDialog.dismiss();

                                String message;

                                if (error.networkResponse != null) {
                                    try {
                                        String body = new String(error.networkResponse.data, "utf-8");
                                        message = "Code: " + error.networkResponse.statusCode + "\n" + body;
                                    } catch (Exception e) {
                                        message = "Code: " + error.networkResponse.statusCode;
                                    }
                                } else {
                                    message = error.toString();
                                }

                                Toast.makeText(Volenteer_Registration.this, message, Toast.LENGTH_LONG).show();
                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("fullName", fullName);
                            params.put("email", email);
                            params.put("password", password);
                            params.put("location", location);
                            params.put("userType", "volunteer");
                            params.put("fcmToken", fcmToken);
                            return params;
                        }

                        @Override
                        public String getBodyContentType() {
                            return "application/x-www-form-urlencoded; charset=UTF-8";
                        }
                    };

                    RequestQueue queue = Volley.newRequestQueue(Volenteer_Registration.this);
                    queue.add(request);
                });
    }
}