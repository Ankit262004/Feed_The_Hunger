package com.example.feed_the_hunger.User;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.example.feed_the_hunger.TokenManager;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class User_Registration extends AppCompatActivity {

    EditText fname, em, pass, cpass, loc;
    Spinner spin;
    Button reg;
    TextView log;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_registration);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fname = findViewById(R.id.fname);
        em = findViewById(R.id.em);
        pass = findViewById(R.id.pass);
        cpass = findViewById(R.id.cpass);
        loc = findViewById(R.id.loc);
        spin = findViewById(R.id.spin);
        reg = findViewById(R.id.reg);
        log = findViewById(R.id.log);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");
        progressDialog.setCancelable(false);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.usertype,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);

        reg.setOnClickListener(v -> {
            String fullName = fname.getText().toString().trim();
            String email = em.getText().toString().trim().toLowerCase();
            String password = pass.getText().toString().trim();
            String confirmPassword = cpass.getText().toString().trim();
            String location = loc.getText().toString().trim();
            String userType = spin.getSelectedItem().toString().trim().toLowerCase();

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()
                    || confirmPassword.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!userType.equals("donor") && !userType.equals("receiver")) {
                Toast.makeText(this, "Please select donor or receiver", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(fullName, email, password, location, userType);
        });

        log.setOnClickListener(v -> {
            startActivity(new Intent(this, User_Login.class));
            finish();
        });
    }

    private void registerUser(String fullName, String email, String password, String location, String userType) {
        progressDialog.show();

        String url = MyIP.IP_ADDRESS + "user/registeruser";
        Log.d("REGISTER_URL", url);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    Log.d("REGISTER_SUCCESS", response);

                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        boolean success = jsonObject.optBoolean("success", false);
                        String message = jsonObject.optString("message", "Registration completed");

                        if (!success) {
                            progressDialog.dismiss();
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            return;
                        }

                        JSONObject userObj = jsonObject.getJSONObject("user");

                        // FIX: backend returns _id, not id
                        String userId = userObj.getString("_id");

                        SharedPreferences prefs = getSharedPreferences("USER_PREF", MODE_PRIVATE);
                        prefs.edit().putString("userId", userId).apply();

                        Log.d("USER_ID_SAVED", userId);

                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(task -> {
                                    progressDialog.dismiss();

                                    if (!task.isSuccessful()) {
                                        Log.e("FCM_TOKEN", "Failed", task.getException());

                                        Toast.makeText(this,
                                                message + " (token not generated yet)",
                                                Toast.LENGTH_LONG).show();

                                        startActivity(new Intent(this, User_Login.class));
                                        finish();
                                        return;
                                    }

                                    String token = task.getResult();
                                    Log.d("FCM_TOKEN", token);

                                    TokenManager.sendTokenToServer(
                                            getApplicationContext(),
                                            userId,
                                            token
                                    );

                                    Toast.makeText(this,
                                            message,
                                            Toast.LENGTH_LONG).show();

                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        startActivity(new Intent(this, User_Login.class));
                                        finish();
                                    }, 1000);
                                });

                    } catch (Exception e) {
                        progressDialog.dismiss();
                        Log.e("REGISTER_PARSE_ERROR", "Parse error", e);
                        Toast.makeText(this, "Response parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();

                    String message = "Registration failed";

                    try {
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String responseData = new String(error.networkResponse.data);
                            Log.e("REGISTER_ERROR", responseData);

                            try {
                                JSONObject errorObj = new JSONObject(responseData);
                                message = errorObj.optString("message", message);
                            } catch (Exception jsonError) {
                                message = responseData;
                            }
                        } else if (error.getCause() != null) {
                            message = error.getCause().toString();
                        } else {
                            message = error.toString();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("fullName", fullName);
                params.put("email", email);
                params.put("password", password);
                params.put("location", location);
                params.put("userType", userType);

                Log.d("REGISTER_PARAMS", params.toString());
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}