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
import com.example.feed_the_hunger.R;

import java.util.HashMap;
import java.util.Map;

public class Volenteer_Registration extends AppCompatActivity {

    EditText fname, em, pass, cpass, loc;
    Button reg;
    TextView log;
    ProgressDialog progressDialog;

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

        reg.setOnClickListener(v -> {
            String fullName = fname.getText().toString().trim();
            String email = em.getText().toString().trim();
            String password = pass.getText().toString().trim();
            String confirmPassword = cpass.getText().toString().trim();
            String location = loc.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            registerVolunteer(fullName, email, password, location);
        });

        log.setOnClickListener(v -> {
            Intent intent = new Intent(Volenteer_Registration.this, Volenteer_Login.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerVolunteer(String fullName, String email, String password, String location) {
        progressDialog.show();

        String url = "http://192.168.29.195:3000/volenteer/registervolunteer";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(Volenteer_Registration.this, "Registration Successful", Toast.LENGTH_LONG).show();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(Volenteer_Registration.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("fullName", fullName);
                params.put("email", email);
                params.put("password", password);
                params.put("location", location);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
}
