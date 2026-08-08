package com.example.feed_the_hunger.User;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.feed_the_hunger.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class User_Login extends AppCompatActivity {

    EditText em, pass;
    Button loginButton;
    TextView registerRedirect, forgetPass;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_login);

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

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");

        loginButton.setOnClickListener(v -> {
            String email = em.getText().toString().trim();
            String password = pass.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        registerRedirect.setOnClickListener(v -> {
            startActivity(new Intent(User_Login.this, User_Registration.class));
            finish();
        });

        forgetPass.setOnClickListener(v -> {
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show();
            // Implement password reset logic if needed
        });
    }

    private void loginUser(String email, String password) {
        progressDialog.show();

        String url = "http://192.168.29.195:3000/user/login";  // ✅ Use lowercase "user"

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        String message = jsonObject.getString("message");

                        if (success) {
                            Toast.makeText(User_Login.this, message, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(User_Login.this, activity_user_dashboard.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(User_Login.this, message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(User_Login.this, "Error parsing server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(User_Login.this, "Login Failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
}
