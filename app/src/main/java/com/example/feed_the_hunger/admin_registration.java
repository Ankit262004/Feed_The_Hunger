package com.example.feed_the_hunger;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class admin_registration extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 100;
    private static final int MAP_PICKER_REQUEST = 200;

    private FusedLocationProviderClient fusedLocationClient;

    private double latitude = 0.0;
    private double longitude = 0.0;

    EditText adminFullName, adminEmail, adminPassword,
            adminConfirmPassword, adminLocation;

    MaterialButton adminRegisterButton;
    MaterialButton btnCurrentLocation, btnSelectMap;

    TextView loginRedirect, backToHome;

    ProgressDialog progressDialog;

    String URL = MyIP.IP_ADDRESS + "user/admin/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_admin_registration);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(android.R.id.content),
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

        // =========================
        // FIND VIEWS
        // =========================

        adminFullName =
                findViewById(R.id.adminFullName);

        adminEmail =
                findViewById(R.id.adminEmail);

        adminPassword =
                findViewById(R.id.adminPassword);

        adminConfirmPassword =
                findViewById(R.id.adminConfirmPassword);

        adminLocation =
                findViewById(R.id.adminLocation);

        adminRegisterButton =
                findViewById(R.id.adminRegisterButton);

        btnCurrentLocation =
                findViewById(R.id.btnCurrentLocation);

        btnSelectMap =
                findViewById(R.id.btnSelectMap);

        loginRedirect =
                findViewById(R.id.loginRedirect);

        backToHome =
                findViewById(R.id.backToHome);

        // =========================
        // PROGRESS DIALOG
        // =========================

        progressDialog =
                new ProgressDialog(this);

        progressDialog.setMessage(
                "Creating admin..."
        );

        progressDialog.setCancelable(false);

        // =========================
        // LOCATION CLIENT
        // =========================

        fusedLocationClient =
                LocationServices
                        .getFusedLocationProviderClient(this);

        // =========================
        // REGISTER BUTTON
        // =========================

        adminRegisterButton.setOnClickListener(
                v -> registerAdmin()
        );

        // =========================
        // CURRENT LOCATION
        // =========================

        btnCurrentLocation.setOnClickListener(
                v -> getCurrentLocation()
        );

        // =========================
        // SELECT ON MAP
        // =========================

        btnSelectMap.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            admin_registration.this,
                            MapPickerActivity.class
                    );

            startActivityForResult(
                    intent,
                    MAP_PICKER_REQUEST
            );
        });

        // =========================
        // LOGIN REDIRECT
        // =========================

        loginRedirect.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            admin_registration.this,
                            admin_login.class
                    );

            startActivity(intent);
            finish();
        });

        // =========================
        // BACK TO HOME
        // =========================

        backToHome.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            admin_registration.this,
                            activity_choose.class
                    );

            startActivity(intent);
            finish();
        });
    }

    // =========================================================
    // CURRENT LOCATION
    // =========================================================

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_CODE
            );

            return;
        }

        fusedLocationClient
                .getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location != null) {

                        latitude =
                                location.getLatitude();

                        longitude =
                                location.getLongitude();

                        getAddressFromCoordinates(
                                latitude,
                                longitude
                        );

                    } else {

                        Toast.makeText(
                                this,
                                "Location not available",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to get current location",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    // =========================================================
    // RECEIVE RESULT FROM MAP PICKER
    // =========================================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == MAP_PICKER_REQUEST &&
                resultCode == RESULT_OK &&
                data != null) {

            latitude =
                    data.getDoubleExtra(
                            "latitude",
                            0.0
                    );

            longitude =
                    data.getDoubleExtra(
                            "longitude",
                            0.0
                    );

            // Convert selected coordinates
            // into readable address
            getAddressFromCoordinates(
                    latitude,
                    longitude
            );
        }
    }

    // =========================================================
    // GET ADDRESS FROM LATITUDE/LONGITUDE
    // =========================================================

    private void getAddressFromCoordinates(
            double lat,
            double lon
    ) {

        Geocoder geocoder =
                new Geocoder(
                        this,
                        Locale.getDefault()
                );

        try {

            List<Address> addresses =
                    geocoder.getFromLocation(
                            lat,
                            lon,
                            1
                    );

            if (addresses != null &&
                    !addresses.isEmpty()) {

                Address address =
                        addresses.get(0);

                String addressText =
                        address.getAddressLine(0);

                if (addressText != null &&
                        !addressText.isEmpty()) {

                    adminLocation.setText(
                            addressText
                    );

                    Toast.makeText(
                            this,
                            "Location selected",
                            Toast.LENGTH_SHORT
                    ).show();

                } else {

                    // If address isn't available,
                    // show coordinates instead.

                    String coordinates =
                            String.format(
                                    Locale.getDefault(),
                                    "%.6f, %.6f",
                                    lat,
                                    lon
                            );

                    adminLocation.setText(
                            coordinates
                    );

                    Toast.makeText(
                            this,
                            "Location selected",
                            Toast.LENGTH_SHORT
                    ).show();
                }

            } else {

                String coordinates =
                        String.format(
                                Locale.getDefault(),
                                "%.6f, %.6f",
                                lat,
                                lon
                        );

                adminLocation.setText(
                        coordinates
                );

                Toast.makeText(
                        this,
                        "Location selected using coordinates",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } catch (IOException e) {

            e.printStackTrace();

            String coordinates =
                    String.format(
                            Locale.getDefault(),
                            "%.6f, %.6f",
                            lat,
                            lon
                    );

            adminLocation.setText(
                    coordinates
            );

            Toast.makeText(
                    this,
                    "Address unavailable, coordinates selected",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =========================================================
    // REGISTER ADMIN
    // =========================================================

    private void registerAdmin() {

        String fullName =
                adminFullName
                        .getText()
                        .toString()
                        .trim();

        String email =
                adminEmail
                        .getText()
                        .toString()
                        .trim()
                        .toLowerCase();

        String password =
                adminPassword
                        .getText()
                        .toString()
                        .trim();

        String confirmPassword =
                adminConfirmPassword
                        .getText()
                        .toString()
                        .trim();

        String location =
                adminLocation
                        .getText()
                        .toString()
                        .trim();

        // =========================
        // VALIDATION
        // =========================

        if (fullName.isEmpty()) {

            adminFullName.setError(
                    "Enter full name"
            );

            adminFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {

            adminEmail.setError(
                    "Enter admin email"
            );

            adminEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            adminEmail.setError(
                    "Enter valid email"
            );

            adminEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {

            adminPassword.setError(
                    "Enter password"
            );

            adminPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {

            adminPassword.setError(
                    "Password must be at least 6 characters"
            );

            adminPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {

            adminConfirmPassword.setError(
                    "Confirm your password"
            );

            adminConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {

            adminConfirmPassword.setError(
                    "Passwords do not match"
            );

            adminConfirmPassword.requestFocus();
            return;
        }

        if (location.isEmpty()) {

            adminLocation.setError(
                    "Enter location"
            );

            adminLocation.requestFocus();
            return;
        }

        // =========================
        // SHOW PROGRESS
        // =========================

        progressDialog.show();

        // =========================
        // VOLLEY REQUEST
        // =========================

        StringRequest request =
                new StringRequest(
                        Request.Method.POST,
                        URL,

                        response -> {

                            progressDialog.dismiss();

                            try {

                                JSONObject json =
                                        new JSONObject(
                                                response
                                        );

                                boolean success =
                                        json.optBoolean(
                                                "success",
                                                false
                                        );

                                String message =
                                        json.optString(
                                                "message",
                                                "Registration completed"
                                        );

                                Toast.makeText(
                                        admin_registration.this,
                                        message,
                                        Toast.LENGTH_SHORT
                                ).show();

                                if (success) {

                                    Intent intent =
                                            new Intent(
                                                    admin_registration.this,
                                                    admin_login.class
                                            );

                                    startActivity(intent);
                                    finish();
                                }

                            } catch (JSONException e) {

                                Toast.makeText(
                                        admin_registration.this,
                                        "JSON Error: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        },

                        error -> {

                            progressDialog.dismiss();

                            String message =
                                    "Registration failed";

                            try {

                                if (error.networkResponse != null &&
                                        error.networkResponse.data != null) {

                                    String responseData =
                                            new String(
                                                    error.networkResponse.data
                                            );

                                    JSONObject errorObj =
                                            new JSONObject(
                                                    responseData
                                            );

                                    message =
                                            errorObj.optString(
                                                    "message",
                                                    message
                                            );

                                } else {

                                    message =
                                            error.toString();
                                }

                            } catch (Exception e) {

                                e.printStackTrace();
                            }

                            Toast.makeText(
                                    admin_registration.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                ) {

                    @Override
                    protected Map<String, String>
                    getParams() {

                        Map<String, String> params =
                                new HashMap<>();

                        params.put(
                                "fullName",
                                fullName
                        );

                        params.put(
                                "email",
                                email
                        );

                        params.put(
                                "password",
                                password
                        );

                        params.put(
                                "location",
                                location
                        );

                        // Coordinates can be sent later
                        // if your backend is updated.

                        // params.put(
                        //         "latitude",
                        //         String.valueOf(latitude)
                        // );

                        // params.put(
                        //         "longitude",
                        //         String.valueOf(longitude)
                        // );

                        return params;
                    }

                    @Override
                    public String getBodyContentType() {

                        return "application/x-www-form-urlencoded; charset=UTF-8";
                    }
                };

        RequestQueue queue =
                Volley.newRequestQueue(this);

        queue.add(request);
    }

    // =========================================================
    // PERMISSION RESULT
    // =========================================================

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == LOCATION_PERMISSION_CODE) {

            if (grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {

                getCurrentLocation();

            } else {

                Toast.makeText(
                        this,
                        "Location permission denied",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
}