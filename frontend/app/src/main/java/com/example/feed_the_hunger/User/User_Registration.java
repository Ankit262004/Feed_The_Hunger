package com.example.feed_the_hunger.User;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.feed_the_hunger.R;

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

        // Bind views
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

        // Populate userType spinner
        ArrayAdapter<CharSequence> userTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.usertype, android.R.layout.simple_spinner_item);
        userTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(userTypeAdapter);

        // Register button action
        reg.setOnClickListener(v -> {
            String fullName = fname.getText().toString().trim();
            String email = em.getText().toString().trim();
            String password = pass.getText().toString().trim();
            String confirmPassword = cpass.getText().toString().trim();
            String location = loc.getText().toString().trim();
            String userType = spin.getSelectedItem().toString();

            // Basic validation
            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Send data to server
            registerUser(fullName, email, password, location, userType);
        });

        // Redirect to login page when clicking "Already have an account"
        log.setOnClickListener(v -> {
            startActivity(new Intent(User_Registration.this, User_Login.class));
            finish();
        });
    }

    private void registerUser(String fullName, String email, String password, String location, String userType) {
        progressDialog.show();

        String url = "http://192.168.29.195:3000/user/registeruser"; // replace with your actual endpoint

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(User_Registration.this, "Registration Successful", Toast.LENGTH_LONG).show();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(User_Registration.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("fullName", fullName);
                params.put("email", email);
                params.put("password", password);
                params.put("location", location);
                params.put("userType", userType);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
}
